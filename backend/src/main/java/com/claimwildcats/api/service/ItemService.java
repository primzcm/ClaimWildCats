package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private static final String COLLECTION = "items";

    private final FirebaseFacade firebaseFacade;

    public ItemService(FirebaseFacade firebaseFacade) {
        this.firebaseFacade = firebaseFacade;
    }

    public List<ItemSummary> browseItems() {
        return firebaseFacade.getFirestore()
                .map(this::fetchRecentItems)
                .orElseGet(this::stubItems);
    }

    public ItemDetail findById(String id) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchItemDetail(firestore, id))
                .orElseGet(() -> stubDetail(id));
    }

    public ItemDetail createLostItem(CreateLostItemRequest request, String reporterId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> persistItem(firestore, request, reporterId, ItemStatus.LOST, "WITH_OWNER"))
                .orElseGet(() -> fallbackCreate(request, reporterId, ItemStatus.LOST, "WITH_OWNER"));
    }

    public ItemDetail createFoundItem(CreateFoundItemRequest request, String reporterId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> persistItem(firestore, request, reporterId, ItemStatus.FOUND, request.custody()))
                .orElseGet(() -> fallbackCreate(request, reporterId, ItemStatus.FOUND, request.custody()));
    }

    public ItemDetail updateStatus(String id, UpdateItemStatusRequest request) {
        return firebaseFacade.getFirestore()
                .map(firestore -> updateStatusInternal(firestore, id, request))
                .orElseGet(() -> {
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
                });
    }

    public List<ItemSummary> findSimilar(String id) {
        return firebaseFacade.getFirestore()
                .map(firestore -> findSimilarInternal(firestore, id))
                .orElseGet(() -> {
                    ItemDetail detail = findById(id);
                    return List.of(new ItemSummary(
                            detail.id() + "-match",
                            "Possible Match",
                            ItemStatus.FOUND,
                            detail.category(),
                            detail.location(),
                            Instant.now(),
                            detail.photoUrls().isEmpty() ? null : detail.photoUrls().get(0)));
                });
    }
    public List<ItemSummary> listReportsForUser(String userId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchItemsByReporter(firestore, userId))
                .orElseGet(this::stubItems);
    }

    private List<ItemSummary> fetchItemsByReporter(Firestore firestore, String userId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .whereEqualTo("reporterId", userId)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .get()
                    .getDocuments();
            List<ItemSummary> summaries = new ArrayList<>(documents.size());
            for (QueryDocumentSnapshot doc : documents) {
                mapSummary(doc).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching user reports", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch user reports from Firestore", e);
        }
    }
    private List<ItemSummary> fetchRecentItems(Firestore firestore) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION);
            List<QueryDocumentSnapshot> documents = collection.orderBy("reportedAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .get()
                    .getDocuments();
            List<ItemSummary> summaries = new ArrayList<>(documents.size());
            for (QueryDocumentSnapshot doc : documents) {
                mapSummary(doc).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching items", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch items from Firestore", e);
        }
    }

    private ItemDetail fetchItemDetail(Firestore firestore, String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalStateException("Item %s not found".formatted(id));
            }
            return mapDetail(snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading item", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to load item from Firestore", e);
        }
    }

    private ItemDetail persistItem(
            Firestore firestore, Object request, String reporterId, ItemStatus status, String custody) {
        CollectionReference collection = firestore.collection(COLLECTION);
        DocumentReference doc = collection.document();
        Instant now = Instant.now();
        Map<String, Object> document = new HashMap<>();
        if (request instanceof CreateLostItemRequest lost) {
            document.put("title", lost.title());
            document.put("category", lost.category());
            document.put("description", lost.description());
            document.put("color", lost.color());
            document.put("brand", lost.brand());
            document.put("location", lost.location());
            document.put("lastSeenAt", lost.lastSeenAt() == null ? null : Timestamp.ofTimeSecondsAndNanos(lost.lastSeenAt().getEpochSecond(), lost.lastSeenAt().getNano()));
            document.put("photoUrls", lost.photoUrls() == null ? List.of() : new ArrayList<>(lost.photoUrls()));
            document.put("contactPreference", lost.contactPreference());
            document.put("rewardOffered", lost.rewardOffered());
        } else if (request instanceof CreateFoundItemRequest found) {
            document.put("title", found.title());
            document.put("category", found.category());
            document.put("description", found.description());
            document.put("color", found.color());
            document.put("brand", found.brand());
            document.put("location", found.location());
            document.put("lastSeenAt", found.foundAt() == null ? null : Timestamp.ofTimeSecondsAndNanos(found.foundAt().getEpochSecond(), found.foundAt().getNano()));
            document.put("photoUrls", found.photoUrls() == null ? List.of() : new ArrayList<>(found.photoUrls()));
            document.put("contactPreference", found.contactPreference());
            document.put("serialNumber", found.serialNumber());
            document.put("custody", found.custody());
        }
        document.putIfAbsent("custody", custody);
        document.put("status", status.name());
        document.put("reporterId", reporterId);
        document.put("reportedAt", Timestamp.now());
        document.put("matchConfidence", 0.0d);

        try {
            WriteResult writeResult = doc.set(document).get();
            log.debug("Saved item {} at {}", doc.getId(), writeResult.getUpdateTime());
            DocumentSnapshot saved = doc.get().get();
            return mapDetail(saved);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving item", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save item to Firestore", e);
        }
    }

    private ItemDetail updateStatusInternal(Firestore firestore, String id, UpdateItemStatusRequest request) {
        try {
            DocumentReference document = firestore.collection(COLLECTION).document(id);
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", request.status().name());
            updates.put("statusNote", request.note());
            updates.put("statusUpdatedAt", Timestamp.now());
            document.set(updates, com.google.cloud.firestore.SetOptions.merge()).get();
            DocumentSnapshot refreshed = document.get().get();
            return mapDetail(refreshed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while updating status", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to update status in Firestore", e);
        }
    }

    private List<ItemSummary> findSimilarInternal(Firestore firestore, String id) {
        ItemDetail root = fetchItemDetail(firestore, id);
        try {
            List<QueryDocumentSnapshot> matches = firestore.collection(COLLECTION)
                    .whereEqualTo("category", root.category())
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .get()
                    .getDocuments();
            List<ItemSummary> summaries = new ArrayList<>();
            for (QueryDocumentSnapshot match : matches) {
                if (match.getId().equals(id)) {
                    continue;
                }
                mapSummary(match).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while searching similar items", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to search similar items", e);
        }
    }

    private List<ItemSummary> stubItems() {
        Instant now = Instant.now();
        return List.of(
                new ItemSummary("lost-001", "Blue Backpack", ItemStatus.LOST, "Bags", "Library Atrium", now, null),
                new ItemSummary("found-002", "Campus ID Card", ItemStatus.FOUND, "ID", "Student Union Desk", now, null));
    }

    private ItemDetail stubDetail(String id) {
        Instant now = Instant.now();
        return new ItemDetail(
                id,
                "Sample Item",
                ItemStatus.LOST,
                "Sample description",
                "General",
                null,
                null,
                "Unknown",
                "WITH_OWNER",
                now,
                now,
                Collections.emptyList(),
                0.0,
                "user-123");
    }

    private ItemDetail fallbackCreate(Object request, String reporterId, ItemStatus status, String custody) {
        String id = "%s-%s".formatted(status.name().toLowerCase(), UUID.randomUUID());
        Instant now = Instant.now();
        List<String> photos = Collections.emptyList();
        String title;
        String category;
        String description;
        String color = null;
        String brand = null;
        String location;
        Instant lastSeen;
        if (request instanceof CreateLostItemRequest lost) {
            title = lost.title();
            category = lost.category();
            description = lost.description();
            color = lost.color();
            brand = lost.brand();
            location = lost.location();
            lastSeen = lost.lastSeenAt();
            if (lost.photoUrls() != null) {
                photos = List.copyOf(lost.photoUrls());
            }
        } else if (request instanceof CreateFoundItemRequest found) {
            title = found.title();
            category = found.category();
            description = found.description();
            color = found.color();
            brand = found.brand();
            location = found.location();
            lastSeen = found.foundAt();
            if (found.photoUrls() != null) {
                photos = List.copyOf(found.photoUrls());
            }
        } else {
            title = "Item";
            category = "General";
            description = "";
            location = "Unknown";
            lastSeen = now;
        }
        return new ItemDetail(
                id,
                title,
                status,
                description,
                category,
                color,
                brand,
                location,
                custody,
                lastSeen,
                now,
                photos,
                0.0,
                reporterId);
    }

    private java.util.Optional<ItemSummary> mapSummary(DocumentSnapshot doc) {
        try {
            ItemStatus status = ItemStatus.valueOf(doc.getString("status"));
            String thumbnail = null;
            List<String> photos = doc.contains("photoUrls") ? (List<String>) doc.get("photoUrls") : List.of();
            if (!photos.isEmpty()) {
                thumbnail = photos.get(0);
            }
            Instant reportedAt = doc.contains("reportedAt") && doc.getTimestamp("reportedAt") != null
                    ? toInstant(doc.getTimestamp("reportedAt"))
                    : Instant.now();
            return java.util.Optional.of(new ItemSummary(
                    doc.getId(),
                    doc.getString("title"),
                    status,
                    doc.getString("category"),
                    doc.getString("location"),
                    reportedAt,
                    thumbnail));
        } catch (Exception ex) {
            log.warn("Skipping item {} due to mapping error: {}", doc.getId(), ex.getMessage());
            return java.util.Optional.empty();
        }
    }

    private ItemDetail mapDetail(DocumentSnapshot doc) {
        ItemStatus status = ItemStatus.valueOf(doc.getString("status"));
        List<String> photos = doc.contains("photoUrls") ? (List<String>) doc.get("photoUrls") : List.of();
        Instant lastSeen = doc.getTimestamp("lastSeenAt") != null ? toInstant(doc.getTimestamp("lastSeenAt")) : null;
        Instant reportedAt = doc.getTimestamp("reportedAt") != null ? toInstant(doc.getTimestamp("reportedAt")) : Instant.now();
        double matchConfidence = doc.contains("matchConfidence") ? doc.getDouble("matchConfidence") : 0.0d;
        return new ItemDetail(
                doc.getId(),
                doc.getString("title"),
                status,
                doc.getString("description"),
                doc.getString("category"),
                doc.getString("color"),
                doc.getString("brand"),
                doc.getString("location"),
                doc.getString("custody"),
                lastSeen,
                reportedAt,
                List.copyOf(photos),
                matchConfidence,
                doc.getString("reporterId"));
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null
                ? null
                : timestamp.toDate().toInstant();
    }
}
