package com.guatequeriders.api.membership.controller;

import com.guatequeriders.api.membership.dto.MemberCardResponse;
import com.guatequeriders.api.membership.dto.MembershipVerificationResponse;
import com.guatequeriders.api.membership.service.MembershipService;
import com.guatequeriders.api.users.model.User;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/me/card")
    public ResponseEntity<MemberCardResponse> getMyCard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(membershipService.getCard(user));
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<MembershipVerificationResponse> verify(@PathVariable String token) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(membershipService.verify(token));
    }
}
