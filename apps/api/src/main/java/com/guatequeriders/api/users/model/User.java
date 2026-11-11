package com.guatequeriders.api.users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "privacy_acknowledged_at")
    private LocalDateTime privacyAcknowledgedAt;

    @Column(name = "privacy_policy_version", length = 40)
    private String privacyPolicyVersion;

    @Column(name = "member_number", unique = true, length = 32)
    private String memberNumber;

    @Column(name = "membership_active", nullable = false)
    private boolean membershipActive;

    @Column(name = "membership_valid_until")
    private LocalDate membershipValidUntil;

    @Column(name = "membership_version", nullable = false)
    private long membershipVersion;

    public User() {
    }

    public User(Long id, String username, String email, String password, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPrivacyAcknowledgedAt() { return privacyAcknowledgedAt; }
    public void setPrivacyAcknowledgedAt(LocalDateTime privacyAcknowledgedAt) { this.privacyAcknowledgedAt = privacyAcknowledgedAt; }

    public String getPrivacyPolicyVersion() { return privacyPolicyVersion; }
    public void setPrivacyPolicyVersion(String privacyPolicyVersion) { this.privacyPolicyVersion = privacyPolicyVersion; }

    public String getMemberNumber() { return memberNumber; }
    public void setMemberNumber(String memberNumber) { this.memberNumber = memberNumber; }

    public boolean isMembershipActive() { return membershipActive; }
    public void setMembershipActive(boolean membershipActive) { this.membershipActive = membershipActive; }

    public LocalDate getMembershipValidUntil() { return membershipValidUntil; }
    public void setMembershipValidUntil(LocalDate membershipValidUntil) { this.membershipValidUntil = membershipValidUntil; }

    public long getMembershipVersion() { return membershipVersion; }
    public void setMembershipVersion(long membershipVersion) { this.membershipVersion = membershipVersion; }
}
