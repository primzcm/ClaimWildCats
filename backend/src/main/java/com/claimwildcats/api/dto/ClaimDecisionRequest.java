package com.claimwildcats.api.dto;

import com.claimwildcats.api.domain.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClaimDecisionRequest(
        @NotNull ClaimStatus status,
        @Size(max = 140) String reviewerNote) {
}

