package com.claimwildcats.api.domain;

import java.time.Instant;
import java.util.List;

public record ClaimDetail(
        String id,
        String itemId,
        String claimantId,
        ClaimStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewerId,
        String secretDetail,
        String justification,
        List<String> attachmentUrls,
        String reviewerNote) {
}

