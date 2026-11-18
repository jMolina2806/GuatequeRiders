package com.guatequeriders.api.collaborators.service;

import com.guatequeriders.api.collaborators.dto.CreateCollaboratorRequest;
import com.guatequeriders.api.collaborators.dto.UpdateCollaboratorRequest;
import com.guatequeriders.api.collaborators.model.Collaborator;
import com.guatequeriders.api.collaborators.repository.CollaboratorRepository;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollaboratorsService {

    private static final Logger log = LoggerFactory.getLogger(CollaboratorsService.class);

    private final CollaboratorRepository collaboratorRepository;
    private final CollaboratorImageStorageService imageStorage;

    public CollaboratorsService(CollaboratorRepository collaboratorRepository,
                                CollaboratorImageStorageService imageStorage) {
        this.collaboratorRepository = collaboratorRepository;
        this.imageStorage = imageStorage;
    }

    @Transactional(readOnly = true)
    public List<Collaborator> findAll(boolean admin) {
        return admin
                ? collaboratorRepository.findAllByOrderByFeaturedDescActiveDescCreatedAtDescIdAsc()
                : collaboratorRepository.findAllByActiveTrueOrderByFeaturedDescCreatedAtDescIdAsc();
    }

    @Transactional(readOnly = true)
    public Collaborator findById(Long id, boolean admin) {
        Collaborator collaborator = findById(id);
        if (!admin && !collaborator.isActive()) {
            throw new ResourceNotFoundException("Collaborator", id);
        }
        return collaborator;
    }

    @Transactional
    public Collaborator create(CreateCollaboratorRequest request) {
        Collaborator collaborator = new Collaborator();
        apply(collaborator, request, true);
        return collaboratorRepository.save(collaborator);
    }

    @Transactional
    public Collaborator update(Long id, UpdateCollaboratorRequest request) {
        Collaborator collaborator = findById(id);
        String previousImage = collaborator.getImageUrl();
        apply(collaborator, request, false);
        collaborator = collaboratorRepository.save(collaborator);
        if (previousImage != null && !previousImage.equals(collaborator.getImageUrl())) {
            scheduleAfterCommit(() -> imageStorage.delete(previousImage), null);
        }
        return collaborator;
    }

    @Transactional
    public Collaborator updateActive(Long id, boolean active) {
        Collaborator collaborator = findById(id);
        collaborator.setActive(active);
        return collaboratorRepository.save(collaborator);
    }

    @Transactional
    public Collaborator uploadImage(Long id, MultipartFile file) {
        Collaborator collaborator = findById(id);
        CollaboratorImageStorageService.StoredImage storedImage = imageStorage.store(file);
        String previousImage = collaborator.getImageUrl();
        collaborator.setImageUrl(storedImage.relativePath());
        try {
            collaboratorRepository.save(collaborator);
            scheduleAfterCommit(
                    () -> imageStorage.delete(previousImage),
                    () -> imageStorage.delete(storedImage.relativePath()));
            return collaborator;
        } catch (RuntimeException exception) {
            imageStorage.delete(storedImage.relativePath());
            throw exception;
        }
    }

    @Transactional
    public Collaborator deleteImage(Long id) {
        Collaborator collaborator = findById(id);
        String previousImage = collaborator.getImageUrl();
        collaborator.setImageUrl(null);
        collaboratorRepository.save(collaborator);
        scheduleAfterCommit(() -> imageStorage.delete(previousImage), null);
        return collaborator;
    }

    @Transactional
    public void delete(Long id) {
        Collaborator collaborator = findById(id);
        String previousImage = collaborator.getImageUrl();
        collaboratorRepository.delete(collaborator);
        scheduleAfterCommit(() -> imageStorage.delete(previousImage), null);
    }

    public String publicImageUrl(Collaborator collaborator) {
        return imageStorage.publicUrl(collaborator.getImageUrl());
    }

    private Collaborator findById(Long id) {
        return collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator", id));
    }

    private void apply(Collaborator collaborator, CreateCollaboratorRequest request, boolean creating) {
        validateDiscount(request.getDiscountPercentage());
        validateWebsite(request.getWebsiteUrl());

        collaborator.setName(request.getName().trim());
        collaborator.setType(request.getType());
        collaborator.setShortDescription(request.getShortDescription().trim());
        collaborator.setDiscountPercentage(request.getDiscountPercentage());
        collaborator.setBenefit(normalizeOptional(request.getBenefit()));
        collaborator.setWebsiteUrl(normalizeOptional(request.getWebsiteUrl()));

        if (request.getFeatured() != null || creating) {
            collaborator.setFeatured(Boolean.TRUE.equals(request.getFeatured()));
        }
        if (request.getActive() != null || creating) {
            collaborator.setActive(creating ? request.getActive() == null || request.getActive() : request.getActive());
        }
    }

    private void validateDiscount(Integer discountPercentage) {
        if (discountPercentage != null && (discountPercentage < 0 || discountPercentage > 100)) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
    }

    private void validateWebsite(String websiteUrl) {
        String normalized = normalizeOptional(websiteUrl);
        if (normalized == null) return;
        try {
            URI uri = URI.create(normalized);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException("Website URL must be a valid HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Website URL must be a valid HTTPS URL");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private void scheduleAfterCommit(Runnable afterCommit, Runnable afterRollback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(afterCommit);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runSafely(afterCommit);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED && afterRollback != null) {
                    runSafely(afterRollback);
                }
            }
        });
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            log.warn("Could not complete collaborator image cleanup", exception);
        }
    }
}
