package com.claimwildcats.api.domain;

import java.util.List;

public record AdminDashboardSnapshot(
        double claimRate,
        double averageMatchHours,
        int itemsLast7Days,
        int itemsLast30Days,
        List<String> hotspotBuildings) {
}
