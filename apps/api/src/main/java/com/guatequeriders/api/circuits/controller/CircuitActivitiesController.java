package com.guatequeriders.api.circuits.controller;

import com.guatequeriders.api.circuits.dto.CircuitActivityResponse;
import com.guatequeriders.api.circuits.dto.CreateCircuitActivityRequest;
import com.guatequeriders.api.circuits.dto.UpdateCircuitActivityRequest;
import com.guatequeriders.api.circuits.model.CircuitActivity;
import com.guatequeriders.api.circuits.model.CircuitActivityStatus;
import com.guatequeriders.api.circuits.service.CircuitActivitiesService;
import com.guatequeriders.api.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/circuits")
public class CircuitActivitiesController {

    private final CircuitActivitiesService circuitActivitiesService;

    public CircuitActivitiesController(CircuitActivitiesService circuitActivitiesService) {
        this.circuitActivitiesService = circuitActivitiesService;
    }

    @GetMapping
    public ResponseEntity<List<CircuitActivityResponse>> getAll() {
        boolean admin = isAdmin();
        List<CircuitActivityResponse> response = circuitActivitiesService.findAll().stream()
                .filter(activity -> admin || activity.getStatus() == CircuitActivityStatus.PUBLISHED)
                .map(CircuitActivityResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CircuitActivityResponse> getById(@PathVariable Long id) {
        CircuitActivity activity = circuitActivitiesService.findById(id);
        if (!isAdmin() && activity.getStatus() != CircuitActivityStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Circuit activity", id);
        }
        return ResponseEntity.ok(CircuitActivityResponse.from(activity));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitActivityResponse> create(
            @Valid @RequestBody CreateCircuitActivityRequest request) {
        CircuitActivity created = circuitActivitiesService.create(toActivity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CircuitActivityResponse.from(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitActivityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCircuitActivityRequest request) {
        CircuitActivity updated = circuitActivitiesService.update(id, toActivity(request));
        return ResponseEntity.ok(CircuitActivityResponse.from(updated));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitActivityResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(CircuitActivityResponse.from(circuitActivitiesService.publish(id)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitActivityResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(CircuitActivityResponse.from(circuitActivitiesService.complete(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CircuitActivityResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(CircuitActivityResponse.from(circuitActivitiesService.cancel(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        circuitActivitiesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CircuitActivity toActivity(CreateCircuitActivityRequest request) {
        CircuitActivity activity = new CircuitActivity();
        activity.setTitle(request.getTitle());
        activity.setCircuitName(request.getCircuitName());
        activity.setDescription(request.getDescription());
        activity.setLocation(request.getLocation());
        activity.setDate(request.getDate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setPrice(request.getPrice());
        activity.setDiscount(request.getDiscount());
        activity.setBenefit(request.getBenefit());
        activity.setRegistrationUrl(request.getRegistrationUrl());
        activity.setInformationUrl(request.getInformationUrl());
        activity.setImageUrl(request.getImageUrl());
        return activity;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
