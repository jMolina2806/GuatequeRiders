package com.guatequeriders.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberNumberRequest(
        @NotBlank
        @Size(max = 32)
        String memberNumber
) {
}
