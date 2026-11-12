package com.guatequeriders.api.admin.controller;

import com.guatequeriders.api.admin.dto.AdminStatsResponse;
import com.guatequeriders.api.admin.dto.UpdateMembershipRequest;
import com.guatequeriders.api.admin.dto.UpdateMemberNumberRequest;
import com.guatequeriders.api.admin.dto.UpdateRoleRequest;
import com.guatequeriders.api.admin.service.AdminService;
import com.guatequeriders.api.auth.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminService.updateRole(id, request.role()));
    }

    @PatchMapping("/users/{id}/membership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateMembership(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateMembershipRequest request) {
        return ResponseEntity.ok(adminService.updateMembership(id, request.active()));
    }

    @PatchMapping("/users/{id}/member-number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateMemberNumber(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateMemberNumberRequest request) {
        return ResponseEntity.ok(adminService.updateMemberNumber(id, request.memberNumber()));
    }
}
