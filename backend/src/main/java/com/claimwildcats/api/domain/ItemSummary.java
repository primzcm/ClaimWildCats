package com.claimwildcats.api.domain;

import java.time.Instant;

public record ItemSummary(
        String id,
        String title,
        ItemStatus status,
        String category,
        String location,
        Instant reportedAt,
        String thumbnailUrl) {
}
