package com.claimwildcats.api.domain;

import java.time.Instant;
import java.util.List;

public record ItemSummary(
        String id,
        String title,
        ItemStatus status,
        String locationText,
        CampusZone campusZone,
        Instant createdAt,
        Instant lastSeenAt,
        List<String> tags,
        List<String> docUrls) {
}
