package com.guatequeriders.api.collaborators.controller;

import com.guatequeriders.api.collaborators.dto.CollaboratorResponse;
import com.guatequeriders.api.collaborators.dto.CreateCollaboratorRequest;
import com.guatequeriders.api.collaborators.dto.UpdateCollaboratorActiveRequest;
import com.guatequeriders.api.collaborators.dto.UpdateCollaboratorRequest;
import com.guatequeriders.api.collaborators.model.Collaborator;
import com.guatequeriders.api.collaborators.service.CollaboratorImageStorageService;
import com.guatequeriders.api.collaborators.service.CollaboratorsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/collaborators")
public class CollaboratorsController {

    private final CollaboratorsService collaboratorsService;

    public CollaboratorsController(CollaboratorsService collaboratorsService) {
        this.collaboratorsService = collaboratorsService;
    }

    @GetMapping
    public ResponseEntity<List<CollaboratorResponse>> getAll() {
        boolean admin = isAdmin();
        return ResponseEntity.ok(collaboratorsService.findAll(admin).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollaboratorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(collaboratorsService.findById(id, isAdmin())));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorResponse> create(
            @Valid @RequestBody CreateCollaboratorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(collaboratorsService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollaboratorRequest request) {
        return ResponseEntity.ok(toResponse(collaboratorsService.update(id, request)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorResponse> updateActive(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollaboratorActiveRequest request) {
        return ResponseEntity.ok(toResponse(collaboratorsService.updateActive(id, request.getActive())));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorResponse> uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(toResponse(collaboratorsService.uploadImage(id, file)));
    }

    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorResponse> deleteImage(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(collaboratorsService.deleteImage(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collaboratorsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CollaboratorResponse toResponse(Collaborator collaborator) {
        return CollaboratorResponse.from(collaborator, collaboratorsService.publicImageUrl(collaborator));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
