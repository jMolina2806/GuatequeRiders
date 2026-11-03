package com.guatequeriders.api.admin.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateMembershipRequest(
        @NotNull Boolean active,
        @FutureOrPresent LocalDate validUntil
) {
}
