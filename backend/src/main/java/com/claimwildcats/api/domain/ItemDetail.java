package com.claimwildcats.api.domain;

import java.time.Instant;
import java.util.List;

public record ItemDetail(
        String id,
        String title,
        String description,
        ItemStatus status,
        String locationText,
        CampusZone campusZone,
        Instant lastSeenAt,
        Instant createdAt,
        List<String> tags,
        List<String> docUrls,
        String reporterId) {
}
