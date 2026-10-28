package com.guatequeriders.api.routes.photos.service;

import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.registrations.repository.RouteRegistrationRepository;
import com.guatequeriders.api.routes.model.Route;
import com.guatequeriders.api.routes.model.RouteStatus;
import com.guatequeriders.api.routes.photos.dto.RoutePhotoResponse;
import com.guatequeriders.api.routes.photos.model.RoutePhoto;
import com.guatequeriders.api.routes.photos.repository.RoutePhotoRepository;
import com.guatequeriders.api.routes.service.RoutesService;
import com.guatequeriders.api.users.model.Role;
import com.guatequeriders.api.users.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoutePhotosService {

    private static final int MAX_PHOTOS_PER_USER = 20;

    private final RoutePhotoRepository photoRepository;
    private final RouteRegistrationRepository registrationRepository;
    private final RoutesService routesService;
    private final RoutePhotoStorageService storageService;

    public RoutePhotosService(RoutePhotoRepository photoRepository,
                              RouteRegistrationRepository registrationRepository,
                              RoutesService routesService,
                              RoutePhotoStorageService storageService) {
        this.photoRepository = photoRepository;
        this.registrationRepository = registrationRepository;
        this.routesService = routesService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<RoutePhotoResponse> findAll(Long routeId, boolean admin) {
        ensureVisibleRoute(routeId, admin);
        return photoRepository.findAllByRouteIdOrderByCreatedAtAscIdAsc(routeId).stream()
                .map(RoutePhotoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoutePhotoStorageService.ServedPhoto load(Long routeId, Long photoId, boolean admin) {
        ensureVisibleRoute(routeId, admin);
        RoutePhoto photo = findPhoto(routeId, photoId);
        return storageService.load(photo);
    }

    @Transactional
    public RoutePhotoResponse upload(Long routeId, MultipartFile file, User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        Route route = routesService.findById(routeId);
        boolean admin = user.getRole() == Role.ADMIN;
        if (!admin) {
            if (route.getStatus() != RouteStatus.IN_PROGRESS && route.getStatus() != RouteStatus.COMPLETED) {
                throw new AccessDeniedException("Photos can only be uploaded to active or completed routes");
            }
            if (!registrationRepository.existsByRouteIdAndUserId(routeId, user.getId())) {
                throw new AccessDeniedException("You must be registered to upload route photos");
            }
        }
        if (photoRepository.countByRouteIdAndUserId(routeId, user.getId()) >= MAX_PHOTOS_PER_USER) {
            throw new ConflictException("A maximum of 20 photos per user and route is allowed");
        }

        RoutePhotoStorageService.StoredPhoto stored = storageService.store(routeId, file);
        registerRollbackCleanup(routeId, stored.storedFilename());

        RoutePhoto photo = new RoutePhoto();
        photo.setRoute(route);
        photo.setUser(user);
        photo.setOriginalName(stored.originalName());
        photo.setStoredFilename(stored.storedFilename());
        photo.setContentType(stored.mediaType().toString());
        photo.setSizeBytes(stored.sizeBytes());
        photo.setCreatedAt(LocalDateTime.now());
        try {
            return RoutePhotoResponse.from(photoRepository.save(photo));
        } catch (RuntimeException exception) {
            storageService.delete(routeId, stored.storedFilename());
            throw exception;
        }
    }

    @Transactional
    public void delete(Long routeId, Long photoId, User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        boolean admin = user.getRole() == Role.ADMIN;
        ensureVisibleRoute(routeId, admin);
        RoutePhoto photo = findPhoto(routeId, photoId);
        if (!admin && !photo.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the photo owner or an admin can delete this photo");
        }

        photoRepository.delete(photo);
        registerCommitCleanup(routeId, photo.getStoredFilename());
    }

    public void cleanupRouteStorage(Long routeId) {
        storageService.deleteRouteDirectory(routeId);
    }

    private Route ensureVisibleRoute(Long routeId, boolean admin) {
        Route route = routesService.findById(routeId);
        if (route.getStatus() == RouteStatus.DRAFT && !admin) {
            throw new ResourceNotFoundException("Route", routeId);
        }
        return route;
    }

    private RoutePhoto findPhoto(Long routeId, Long photoId) {
        return photoRepository.findByIdAndRouteId(photoId, routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route photo", photoId));
    }

    private void registerRollbackCleanup(Long routeId, String storedFilename) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    storageService.delete(routeId, storedFilename);
                }
            }
        });
    }

    private void registerCommitCleanup(Long routeId, String storedFilename) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            storageService.delete(routeId, storedFilename);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    storageService.delete(routeId, storedFilename);
                }
            }
        });
    }
}
