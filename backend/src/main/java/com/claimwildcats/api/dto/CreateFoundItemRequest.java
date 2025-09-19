package com.claimwildcats.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record CreateFoundItemRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String location,
        @NotNull Instant foundAt,
        @NotBlank String description,
        @NotBlank String custody,
        @Size(max = 48) String color,
        @Size(max = 48) String brand,
        @Size(max = 48) String serialNumber,
        @Size(max = 6) List<@NotBlank String> photoUrls,
        @Size(max = 120) String contactPreference) {
}
