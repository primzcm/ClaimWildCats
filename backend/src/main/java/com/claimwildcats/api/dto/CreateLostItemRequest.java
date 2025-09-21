package com.claimwildcats.api.dto;

import com.claimwildcats.api.domain.CampusZone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record CreateLostItemRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String locationText,
        CampusZone campusZone,
        Instant lastSeenAt,
        @Size(max = 10) List<@NotBlank @Size(max = 32) String> tags,
        @Size(max = 6) List<@NotBlank String> docUrls) {
}
