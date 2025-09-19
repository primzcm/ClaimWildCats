package com.claimwildcats.api.domain;

import java.time.Instant;

public record ClaimSummary(
        String id,
        String itemId,
        String claimantId,
        ClaimStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewerId) {
}
