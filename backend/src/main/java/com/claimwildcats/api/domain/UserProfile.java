package com.claimwildcats.api.domain;

import java.time.Instant;

public record UserProfile(
        String id,
        String fullName,
        String email,
        UserRole role,
        boolean emailVerified,
        int openReports,
        int resolvedReports,
        Instant createdAt) {
}
