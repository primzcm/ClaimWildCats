package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final FirebaseFacade firebaseFacade;

    public ItemService(FirebaseFacade firebaseFacade) {
        this.firebaseFacade = firebaseFacade;
    }

    public List<ItemSummary> browseItems() {
        firebaseFacade.logReadiness();
        return List.of(
                new ItemSummary(
                        "lost-001",
                        "Blue Backpack",
                        ItemStatus.LOST,
                        "Bags",
                        "Library Atrium",
                        Instant.now().minus(2, ChronoUnit.HOURS),
                        "https://placehold.co/96x96"),
                new ItemSummary(
                        "found-002",
                        "Campus ID Card",
                        ItemStatus.FOUND,
                        "ID",
                        "Student Union Desk",
                        Instant.now().minus(5, ChronoUnit.HOURS),
                        "https://placehold.co/96x96"));
    }

    public ItemDetail findById(String id) {
        return new ItemDetail(
                id,
                "Blue Backpack",
                ItemStatus.LOST,
                "Blue North Face backpack with Wildcats keychain.",
                "Bags",
                "Blue",
                "North Face",
                "Library Atrium",
                "With owner",
                Instant.now().minus(4, ChronoUnit.HOURS),
                Instant.now().minus(3, ChronoUnit.HOURS),
                List.of("https://placehold.co/400x320", "https://placehold.co/400x320"),
                0.82,
                "user-123");
    }

    public ItemDetail createLostItem(CreateLostItemRequest request) {
        String id = "lost-" + UUID.randomUUID();
        return new ItemDetail(
                id,
                request.title(),
                ItemStatus.LOST,
                request.description(),
                request.category(),
                request.color(),
                request.brand(),
                request.location(),
                "WITH_OWNER",
                request.lastSeenAt(),
                Instant.now(),
                request.photoUrls() == null ? List.of() : List.copyOf(request.photoUrls()),
                0.0,
                "user-123");
    }

    public ItemDetail createFoundItem(CreateFoundItemRequest request) {
        String id = "found-" + UUID.randomUUID();
        return new ItemDetail(
                id,
                request.title(),
                ItemStatus.FOUND,
                request.description(),
                request.category(),
                request.color(),
                request.brand(),
                request.location(),
                request.custody(),
                request.foundAt(),
                Instant.now(),
                request.photoUrls() == null ? List.of() : List.copyOf(request.photoUrls()),
                0.0,
                "user-123");
    }

    public ItemDetail updateStatus(String id, UpdateItemStatusRequest request) {
        ItemDetail existing = findById(id);
        return new ItemDetail(
                existing.id(),
                existing.title(),
                request.status(),
                existing.description(),
                existing.category(),
                existing.color(),
                existing.brand(),
                existing.location(),
                existing.custody(),
                existing.lastSeenAt(),
                Instant.now(),
                existing.photoUrls(),
                existing.matchConfidence(),
                existing.reporterId());
    }

    public List<ItemSummary> findSimilar(String id) {
        ItemDetail item = findById(id);
        return List.of(
                new ItemSummary(
                        item.id() + "-match",
                        "Possible Match",
                        ItemStatus.FOUND,
                        item.category(),
                        "Engineering Building",
                        Instant.now().minus(1, ChronoUnit.HOURS),
                        "https://placehold.co/96x96"));
    }
}
