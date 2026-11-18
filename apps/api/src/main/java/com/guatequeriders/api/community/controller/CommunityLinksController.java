package com.guatequeriders.api.community.controller;

import com.guatequeriders.api.community.dto.CommunityLinksResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
public class CommunityLinksController {

    private final String whatsappGroupUrl;

    public CommunityLinksController(
            @Value("${app.community.whatsapp-group-url:}") String whatsappGroupUrl) {
        this.whatsappGroupUrl = whatsappGroupUrl;
    }

    @GetMapping("/links")
    public ResponseEntity<CommunityLinksResponse> getLinks() {
        String normalizedWhatsappUrl = whatsappGroupUrl == null || whatsappGroupUrl.isBlank()
                ? null
                : whatsappGroupUrl.trim();

        return ResponseEntity.ok(new CommunityLinksResponse(normalizedWhatsappUrl));
    }
}
