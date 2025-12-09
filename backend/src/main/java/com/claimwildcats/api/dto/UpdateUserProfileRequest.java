package com.claimwildcats.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Schema(
                        description = "Unique username for the user, used when displaying reporters in the UI.",
                        example = "wildcat123")
                @NotBlank
                @Size(min = 3, max = 32)
                @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores.")
                String username) {
}

