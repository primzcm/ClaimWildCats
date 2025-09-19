package com.claimwildcats.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record CreateLostItemRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String location,
        @NotNull Instant lastSeenAt,
        @NotBlank String description,
        @Size(max = 48) String color,
        @Size(max = 48) String brand,
        boolean rewardOffered,
        @Size(max = 120) String contactPreference,
        @Size(max = 6) List<@NotBlank String> photoUrls) {
}
