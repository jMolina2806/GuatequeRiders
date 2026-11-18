package com.guatequeriders.api.circuits.service;

import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.circuits.model.CircuitActivityStatus;
import com.guatequeriders.api.circuits.repository.CircuitActivityRepository;
import com.guatequeriders.api.common.exception.ConflictException;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import com.guatequeriders.api.notifications.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CircuitActivitiesService {

    private final CircuitActivityRepository circuitActivityRepository;
    private final NotificationService notificationService;

    public CircuitActivitiesService(CircuitActivityRepository circuitActivityRepository,
                                    NotificationService notificationService) {
        this.circuitActivityRepository = circuitActivityRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<CircuitActivity> findAll() {
        return circuitActivityRepository.findAllByOrderByDateAscIdAsc();
    }

    @Transactional(readOnly = true)
    public CircuitActivity findById(Long id) {
        return circuitActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circuit activity", id));
    }

    @Transactional
    public CircuitActivity create(CircuitActivity activity) {
        validateActivity(activity);
        activity.setStatus(CircuitActivityStatus.DRAFT);
        activity.setCreatedAt(LocalDateTime.now());
        return circuitActivityRepository.save(activity);
    }

    @Transactional
    public CircuitActivity update(Long id, CircuitActivity updated) {
        CircuitActivity existing = findById(id);
        CircuitActivityStatus previousStatus = existing.getStatus();
        if (isFinal(existing.getStatus())) {
            throw new ConflictException("Completed or cancelled circuit activities cannot be edited");
        }

        validateActivity(updated);
        existing.setTitle(updated.getTitle());
        existing.setCircuitName(updated.getCircuitName());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setDate(updated.getDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setPrice(updated.getPrice());
        existing.setDiscount(updated.getDiscount());
        existing.setBenefit(updated.getBenefit());
        existing.setRegistrationUrl(updated.getRegistrationUrl());
        existing.setInformationUrl(updated.getInformationUrl());
        existing.setImageUrl(updated.getImageUrl());
        if (previousStatus == CircuitActivityStatus.PUBLISHED) {
            notificationService.notifyCircuitUpdated(existing);
        }
        return existing;
    }

    @Transactional
    public CircuitActivity publish(Long id) {
        CircuitActivity activity = findById(id);
        if (activity.getStatus() != CircuitActivityStatus.DRAFT) {
            throw new ConflictException("Only DRAFT circuit activities can be published");
        }
        validateActivity(activity);
        if (activity.getDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Cannot publish a circuit activity with a past date");
        }
        activity.setStatus(CircuitActivityStatus.PUBLISHED);
        notificationService.notifyCircuitPublished(activity);
        return activity;
    }

    @Transactional
    public CircuitActivity complete(Long id) {
        CircuitActivity activity = findById(id);
        if (activity.getStatus() != CircuitActivityStatus.PUBLISHED) {
            throw new ConflictException("Only PUBLISHED circuit activities can be completed");
        }
        activity.setStatus(CircuitActivityStatus.COMPLETED);
        return activity;
    }

    @Transactional
    public CircuitActivity cancel(Long id) {
        CircuitActivity activity = findById(id);
        if (activity.getStatus() != CircuitActivityStatus.DRAFT
                && activity.getStatus() != CircuitActivityStatus.PUBLISHED) {
            throw new ConflictException("Only DRAFT or PUBLISHED circuit activities can be cancelled");
        }
        boolean wasPublished = activity.getStatus() == CircuitActivityStatus.PUBLISHED;
        activity.setStatus(CircuitActivityStatus.CANCELLED);
        if (wasPublished) {
            notificationService.notifyCircuitCancelled(activity);
        }
        return activity;
    }

    @Transactional
    public int completePastCircuitActivities() {
        List<CircuitActivity> pastPublished = circuitActivityRepository
                .findAllByStatusAndDateBefore(CircuitActivityStatus.PUBLISHED, LocalDate.now());
        for (CircuitActivity activity : pastPublished) {
            activity.setStatus(CircuitActivityStatus.COMPLETED);
        }
        return pastPublished.size();
    }

    @Transactional
    public void delete(Long id) {
        if (!circuitActivityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Circuit activity", id);
        }
        circuitActivityRepository.deleteById(id);
    }

    private void validateActivity(CircuitActivity activity) {
        requireText(activity.getTitle(), "Title is required");
        requireText(activity.getCircuitName(), "Circuit name is required");
        requireText(activity.getDescription(), "Description is required");
        requireText(activity.getLocation(), "Location is required");
        if (activity.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (activity.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (activity.getEndTime() != null && !activity.getEndTime().isAfter(activity.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (activity.getPrice() != null && activity.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }
        validateHttpsUrl(activity.getRegistrationUrl(), true, "Registration URL");
        validateHttpsUrl(activity.getInformationUrl(), false, "Information URL");
        validateHttpsUrl(activity.getImageUrl(), false, "Image URL");
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateHttpsUrl(String value, boolean required, String fieldName) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return;
        }

        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException(fieldName + " must be a valid HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(fieldName + " must be a valid HTTPS URL");
        }
    }

    private boolean isFinal(CircuitActivityStatus status) {
        return status == CircuitActivityStatus.COMPLETED || status == CircuitActivityStatus.CANCELLED;
    }
}
