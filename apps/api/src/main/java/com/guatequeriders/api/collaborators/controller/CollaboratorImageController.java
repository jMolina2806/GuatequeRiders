package com.guatequeriders.api.collaborators.controller;

import com.guatequeriders.api.collaborators.service.CollaboratorImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/uploads/collaborators")
public class CollaboratorImageController {

    private final CollaboratorImageStorageService imageStorage;

    public CollaboratorImageController(CollaboratorImageStorageService imageStorage) {
        this.imageStorage = imageStorage;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        CollaboratorImageStorageService.ServedImage image = imageStorage.load(filename);
        return ResponseEntity.ok()
                .contentType(image.mediaType())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(image.resource());
    }
}
