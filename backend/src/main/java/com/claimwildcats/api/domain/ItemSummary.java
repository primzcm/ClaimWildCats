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

                public ItemSummary(String id, String title, String locationText, ItemStatus status, Instant lastSeenAt){
                        this(id, title, status, locationText, null, null, lastSeenAt, List.of(), List.of());
                    }
}


