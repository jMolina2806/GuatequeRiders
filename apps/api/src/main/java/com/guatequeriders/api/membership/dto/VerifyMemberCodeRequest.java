package com.guatequeriders.api.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyMemberCodeRequest(
        @NotBlank
        @Size(max = 32)
        String memberNumber,
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String code
) {
}
