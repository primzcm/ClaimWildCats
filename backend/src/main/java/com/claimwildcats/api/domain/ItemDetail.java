package com.claimwildcats.api.domain;

import java.time.Instant;
import java.util.List;

public record ItemDetail(
        String id,
        String title,
        ItemStatus status,
        String description,
        String category,
        String color,
        String brand,
        String location,
        String custody,
        Instant lastSeenAt,
        Instant reportedAt,
        List<String> photoUrls,
        double matchConfidence,
        String reporterId) {
}
