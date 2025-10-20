package com.claimwildcats.api.service;

import com.claimwildcats.api.config.FirebaseProperties;
import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.ItemSearchResponse;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private static final String COLLECTION = "items";
    private static final int MAX_FETCH = 200;
    private static final ZoneId CAMPUS_ZONE_ID = ZoneId.of("Asia/Manila");

    private final FirebaseFacade firebaseFacade;
    private final FirebaseProperties firebaseProperties;

    public ItemService(FirebaseFacade firebaseFacade, FirebaseProperties firebaseProperties) {
        this.firebaseFacade = firebaseFacade;
        this.firebaseProperties = firebaseProperties;
    }

    public ItemSearchResponse searchItems(
            ItemStatus status, CampusZone campusZone, String query, int page, int pageSize) {
        int normalisedPage = Math.max(page, 0);
        int normalisedPageSize = Math.min(Math.max(pageSize, 1), 50);
        String trimmedQuery = query == null ? null : query.trim().toLowerCase();

        return firebaseFacade.getFirestore()
                .map(firestore -> searchWithFirestore(
                        firestore, status, campusZone, trimmedQuery, normalisedPage, normalisedPageSize))
                .orElseGet(() -> fallbackSearch(status, campusZone, trimmedQuery, normalisedPage, normalisedPageSize));
    }

    public List<ItemSummary> browseItems() {
        return searchItems(null, null, null, 0, 12).items();
    }

    public ItemDetail findById(String id) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchItemDetail(firestore, id))
                .orElseGet(() -> stubDetail(id));
    }

    public ItemDetail createLostItem(CreateLostItemRequest request, String reporterId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> persistItem(firestore, request, reporterId, ItemStatus.LOST))
                .orElseGet(() -> fallbackCreate(request, reporterId, ItemStatus.LOST));
    }

    public ItemDetail createFoundItem(CreateFoundItemRequest request, String reporterId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> persistItem(firestore, request, reporterId, ItemStatus.FOUND))
                .orElseGet(() -> fallbackCreate(request, reporterId, ItemStatus.FOUND));
    }

    public ItemDetail updateStatus(String id, UpdateItemStatusRequest request, String currentUserId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> updateStatusInternal(firestore, id, request, currentUserId))
                .orElseGet(() -> {
                    ItemDetail existing = findById(id);
                    if (!Objects.equals(existing.reporterId(), currentUserId)) {
                        throw new AccessDeniedException("You can only update your own reports");
                    }
                    return new ItemDetail(
                            existing.id(),
                            existing.title(),
                            existing.description(),
                            request.status(),
                            existing.locationText(),
                            existing.campusZone(),
                            existing.lastSeenAt(),
                            Instant.now(),
                            existing.tags(),
                            existing.docUrls(),
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
                            detail.locationText(),
                            detail.campusZone(),
                            Instant.now(),
                            detail.lastSeenAt(),
                            detail.tags(),
                            detail.docUrls()));
                });
    }

    public List<ItemSummary> listReportsForUser(String userId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchItemsByReporter(firestore, userId))
                .orElseGet(this::stubItems);
    }

    private ItemSearchResponse searchWithFirestore(
            Firestore firestore,
            ItemStatus status,
            CampusZone campusZone,
            String query,
            int page,
            int pageSize) {
        try {
            Query firestoreQuery = firestore.collection(COLLECTION).orderBy("createdAt", Query.Direction.DESCENDING);
            if (status != null) {
                firestoreQuery = firestoreQuery.whereEqualTo("status", status.storageValue());
            }
            if (campusZone != null) {
                firestoreQuery = firestoreQuery.whereEqualTo("campusZone", campusZone.getJsonValue());
            }

            List<QueryDocumentSnapshot> documents = firestoreQuery.limit(MAX_FETCH).get().get().getDocuments();
            List<ItemSummary> matches = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                if (matchesQuery(doc, query)) {
                    mapSummary(doc).ifPresent(matches::add);
                }
            }

            return paginate(matches, page, pageSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while searching items", e);
        } catch (ExecutionException e) {
            if (requiresCompositeIndex(e)) {
                log.warn("Firestore is missing an index for filtered item search; applying client-side filtering instead", e.getCause());
                return searchWithoutIndex(firestore, status, campusZone, query, page, pageSize);
            }
            log.error("Failed to search items in Firestore; falling back to stub data (status={}, campusZone={}, query={})", status, campusZone, query, e);
            return fallbackSearch(status, campusZone, query, page, pageSize);
        }
    }

    private ItemSearchResponse searchWithoutIndex(
            Firestore firestore,
            ItemStatus status,
            CampusZone campusZone,
            String query,
            int page,
            int pageSize) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(MAX_FETCH)
                    .get()
                    .get()
                    .getDocuments();
            List<ItemSummary> matches = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                if (status != null) {
                    String docStatus = doc.getString("status");
                    if (docStatus == null || !docStatus.equalsIgnoreCase(status.storageValue())) {
                        continue;
                    }
                }
                if (campusZone != null) {
                    String zoneValue = safeLower(doc.getString("campusZone"));
                    if (zoneValue == null || !zoneValue.equals(campusZone.getJsonValue().toLowerCase(Locale.US))) {
                        continue;
                    }
                }
                if (!matchesQuery(doc, query)) {
                    continue;
                }
                mapSummary(doc).ifPresent(matches::add);
            }
            return paginate(matches, page, pageSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while searching items without index", e);
        } catch (ExecutionException e) {
            log.error("Failed to search items in Firestore without index fallback; returning stub data", e);
            return fallbackSearch(status, campusZone, query, page, pageSize);
        }
    }
    private ItemSearchResponse fallbackSearch(
            ItemStatus status, CampusZone campusZone, String query, int page, int pageSize) {
        List<ItemSummary> all = new ArrayList<>(stubItems());
        if (status != null) {
            all.removeIf(item -> item.status() != status);
        }
        if (campusZone != null) {
            all.removeIf(item -> !campusZone.equals(item.campusZone()));
        }
        if (query != null) {
            all.removeIf(item -> !matchesQuery(item, query));
        }
        return paginate(all, page, pageSize);
    }

    private ItemSearchResponse paginate(List<ItemSummary> source, int page, int pageSize) {
        int total = source.size();
        int fromIndex = Math.min(page * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ItemSummary> slice = source.subList(fromIndex, toIndex);
        return new ItemSearchResponse(List.copyOf(slice), page, pageSize, total);
    }

    private boolean requiresCompositeIndex(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        }
        String message = cause.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("requires an index");
    }
    private boolean matchesQuery(QueryDocumentSnapshot doc, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        String title = safeLower(doc.getString("title"));
        String description = safeLower(doc.getString("description"));
        if ((title != null && title.contains(lowerQuery)) || (description != null && description.contains(lowerQuery))) {
            return true;
        }
        for (String tag : extractStringList(doc, "tags")) {
            if (tag.toLowerCase(Locale.US).contains(lowerQuery)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesQuery(ItemSummary summary, String query) {
        String lowerQuery = query.toLowerCase();
        if (summary.title().toLowerCase(Locale.US).contains(lowerQuery)) {
            return true;
        }
        List<String> tags = summary.tags() == null ? List.of() : summary.tags();
        for (String tag : tags) {
            if (tag.toLowerCase(Locale.US).contains(lowerQuery)) {
                return true;
            }
        }
        return false;
    }

    private ItemDetail fetchItemDetail(Firestore firestore, String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(id).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Item not found: " + id);
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
            Firestore firestore, Object request, String reporterId, ItemStatus status) {
        CollectionReference collection = firestore.collection(COLLECTION);
        List<String> docUrls = docUrlsOf(request);
        String resolvedItemId = resolveItemIdFromUrls(docUrls);
        DocumentReference doc = resolvedItemId != null ? collection.document(resolvedItemId) : collection.document();
        String itemId = doc.getId();
        if (!docUrls.isEmpty()) {
            ensureDocUrlsMatchItem(docUrls, itemId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", titleOf(request));
        data.put("description", descriptionOf(request));
        data.put("status", status.storageValue());
        data.put("locationText", locationOf(request));
        data.put("campusZone", campusZoneOf(request));
        data.put("lastSeenAt", timestampOf(lastSeenAtOf(request)));
        data.put("tags", tagsOf(request));
        data.put("docUrls", docUrls);
        data.put("reporterId", reporterId);
        data.put("createdAt", timestampOf(ZonedDateTime.now(CAMPUS_ZONE_ID).toInstant()));

        try {
            WriteResult writeResult = doc.set(data).get();
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

    private ItemDetail updateStatusInternal(
            Firestore firestore, String id, UpdateItemStatusRequest request, String currentUserId) {
        try {
            DocumentReference document = firestore.collection(COLLECTION).document(id);
            DocumentSnapshot snapshot = document.get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Item not found: " + id);
            }
            ItemDetail detail = mapDetail(snapshot);
            if (!Objects.equals(detail.reporterId(), currentUserId)) {
                throw new AccessDeniedException("You can only update your own reports");
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", request.status().storageValue());
            updates.put("updatedAt", Timestamp.now());
            if (request.note() != null && !request.note().isBlank()) {
                updates.put("statusNote", request.note());
            }
            document.set(updates, SetOptions.merge()).get();
            DocumentSnapshot refreshed = document.get().get();
            return mapDetail(refreshed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while updating item", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to update item in Firestore", e);
        }
    }

    private List<ItemSummary> findSimilarInternal(Firestore firestore, String id) {
        ItemDetail root = fetchItemDetail(firestore, id);
        try {
            Query query = firestore.collection(COLLECTION)
                    .whereEqualTo("status", ItemStatus.FOUND.storageValue())
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(8);
            if (root.campusZone() != null) {
                query = query.whereEqualTo("campusZone", root.campusZone().getJsonValue());
            }
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            List<ItemSummary> summaries = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                if (doc.getId().equals(id)) {
                    continue;
                }
                mapSummary(doc).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while finding similar items", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to find similar items", e);
        }
    }

    private List<ItemSummary> fetchItemsByReporter(Firestore firestore, String userId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .whereEqualTo("reporterId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(MAX_FETCH)
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

    private Optional<ItemSummary> mapSummary(DocumentSnapshot doc) {
        try {
            ItemStatus status = ItemStatus.fromValue(doc.getString("status"));
            Instant createdAt = toInstant(doc.getTimestamp("createdAt"));
            Instant lastSeen = toInstant(doc.getTimestamp("lastSeenAt"));
            List<String> tags = extractStringList(doc, "tags");
            List<String> docUrls = extractStringList(doc, "docUrls");
            return Optional.of(new ItemSummary(
                    doc.getId(),
                    doc.getString("title"),
                    status,
                    doc.getString("locationText"),
                    CampusZone.fromValue(doc.getString("campusZone")),
                    createdAt,
                    lastSeen,
                    tags,
                    docUrls));
        } catch (Exception ex) {
            log.warn("Skipping item {} due to mapping error: {}", doc.getId(), ex.getMessage());
            return Optional.empty();
        }
    }
    private ItemDetail mapDetail(DocumentSnapshot doc) {
        List<String> tags = extractStringList(doc, "tags");
        List<String> docUrls = extractStringList(doc, "docUrls");
        return new ItemDetail(
                doc.getId(),
                doc.getString("title"),
                doc.getString("description"),
                ItemStatus.fromValue(doc.getString("status")),
                doc.getString("locationText"),
                CampusZone.fromValue(doc.getString("campusZone")),
                toInstant(doc.getTimestamp("lastSeenAt")),
                toInstant(doc.getTimestamp("createdAt")),
                tags,
                docUrls,
                doc.getString("reporterId"));
    }

    private List<String> extractStringList(DocumentSnapshot doc, String field) {
        Object raw = doc.get(field);
        if (!(raw instanceof List<?> rawList)) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (Object entry : rawList) {
            if (entry instanceof String value && !value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private ItemDetail fallbackCreate(Object request, String reporterId, ItemStatus status) {
        List<String> docUrls = docUrlsOf(request);
        String itemId = resolveItemIdFromUrls(docUrls);
        if (itemId == null) {
            itemId = "%s-%s".formatted(status.name().toLowerCase(Locale.US), UUID.randomUUID());
        } else {
            ensureDocUrlsMatchItem(docUrls, itemId);
        }
        Instant now = ZonedDateTime.now(CAMPUS_ZONE_ID).toInstant();
        return new ItemDetail(
                itemId,
                titleOf(request),
                descriptionOf(request),
                status,
                locationOf(request),
                campusZoneEnumOf(request),
                lastSeenAtOf(request),
                now,
                tagsOf(request),
                docUrls,
                reporterId);
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toDate().toInstant();
    }

    private Timestamp timestampOf(Instant instant) {
        return instant == null ? null : Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    private Instant lastSeenAtOf(Object request) {
        if (request instanceof CreateLostItemRequest lost) {
            return lost.lastSeenAt();
        }
        if (request instanceof CreateFoundItemRequest found) {
            return found.lastSeenAt();
        }
        return null;
    }

    private String titleOf(Object request) {
        if (request instanceof CreateLostItemRequest lost) {
            return lost.title();
        }
        if (request instanceof CreateFoundItemRequest found) {
            return found.title();
        }
        return "Item";
    }

    private String descriptionOf(Object request) {
        if (request instanceof CreateLostItemRequest lost) {
            return lost.description();
        }
        if (request instanceof CreateFoundItemRequest found) {
            return found.description();
        }
        return "";
    }

    private String locationOf(Object request) {
        if (request instanceof CreateLostItemRequest lost) {
            return lost.locationText();
        }
        if (request instanceof CreateFoundItemRequest found) {
            return found.locationText();
        }
        return "";
    }

    private String campusZoneOf(Object request) {
        CampusZone zone = campusZoneEnumOf(request);
        return zone == null ? null : zone.getJsonValue();
    }

    private CampusZone campusZoneEnumOf(Object request) {
        if (request instanceof CreateLostItemRequest lost) {
            return lost.campusZone();
        }
        if (request instanceof CreateFoundItemRequest found) {
            return found.campusZone();
        }
        return null;
    }

    private List<String> tagsOf(Object request) {
        List<String> tags;
        if (request instanceof CreateLostItemRequest lost) {
            tags = lost.tags();
        } else if (request instanceof CreateFoundItemRequest found) {
            tags = found.tags();
        } else {
            tags = List.of();
        }
        if (tags == null) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String tag : tags) {
            if (tag != null) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    cleaned.add(trimmed);
                }
            }
        }
        return List.copyOf(cleaned);
    }

    private List<String> docUrlsOf(Object request) {
        List<String> docUrls;
        if (request instanceof CreateLostItemRequest lost) {
            docUrls = lost.docUrls();
        } else if (request instanceof CreateFoundItemRequest found) {
            docUrls = found.docUrls();
        } else {
            docUrls = List.of();
        }
        if (docUrls == null) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String url : docUrls) {
            if (url != null) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    cleaned.add(trimmed);
                }
            }
        }
        return List.copyOf(cleaned);
    }

    private String safeLower(String value) {
        return value == null ? null : value.toLowerCase(Locale.US);
    }

    private String resolveItemIdFromUrls(List<String> docUrls) {
        if (docUrls.isEmpty()) {
            return null;
        }
        String bucket = firebaseProperties.getStorageBucket();
        requireStorageBucketConfigured(bucket);
        String resolvedId = null;
        for (String docUrl : docUrls) {
            String itemId = parseItemIdAndValidate(docUrl, bucket);
            if (resolvedId == null) {
                resolvedId = itemId;
            } else if (!resolvedId.equals(itemId)) {
                throw new IllegalArgumentException("Document URLs must share the same items/{itemId}/ prefix.");
            }
        }
        return resolvedId;
    }

    private void ensureDocUrlsMatchItem(List<String> docUrls, String itemId) {
        String bucket = firebaseProperties.getStorageBucket();
        requireStorageBucketConfigured(bucket);
        for (String docUrl : docUrls) {
            String parsedId = parseItemIdAndValidate(docUrl, bucket);
            if (!itemId.equals(parsedId)) {
                throw new IllegalArgumentException("Document URLs must live under items/" + itemId + "/");
            }
        }
    }

    private String parseItemIdAndValidate(String docUrl, String bucket) {
        String path = parseStoragePath(docUrl, bucket);
        String itemId = extractItemIdFromPath(path);
        if (!hasImageExtension(path)) {
            throw new IllegalArgumentException("Document URLs must reference image files stored in Firebase Storage.");
        }
        return itemId;
    }

    private String parseStoragePath(String rawUrl, String bucket) {
        if (rawUrl == null) {
            throw new IllegalArgumentException("Document URLs cannot be null.");
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Document URLs cannot be blank.");
        }
        String bucketLower = bucket.toLowerCase(Locale.US);
        if (trimmed.startsWith("gs://")) {
            String withoutScheme = trimmed.substring(5);
            int slashIndex = withoutScheme.indexOf('/');
            if (slashIndex <= 0) {
                throw new IllegalArgumentException("Document URLs must include an object path under items/.");
            }
            String host = withoutScheme.substring(0, slashIndex);
            if (!host.equalsIgnoreCase(bucket)) {
                throw new IllegalArgumentException("Document URLs must point to the " + bucket + " bucket.");
            }
            return withoutScheme.substring(slashIndex + 1);
        }
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Document URLs must be valid URIs.");
        }
        String hostLower = Optional.ofNullable(uri.getHost()).map(h -> h.toLowerCase(Locale.US)).orElse("");
        if ("firebasestorage.googleapis.com".equals(hostLower)) {
            String[] segments = uri.getPath().split("/");
            if (segments.length < 6) {
                throw new IllegalArgumentException("Document URLs must include an object path under items/.");
            }
            if (!bucketLower.equals(segments[3].toLowerCase(Locale.US))) {
                throw new IllegalArgumentException("Document URLs must point to the " + bucket + " bucket.");
            }
            String objectSegment = segments[5];
            if (objectSegment.isEmpty()) {
                throw new IllegalArgumentException("Document URLs must include an object path under items/.");
            }
            return URLDecoder.decode(objectSegment, StandardCharsets.UTF_8);
        }
        if ((bucketLower + ".storage.googleapis.com").equals(hostLower)) {
            String path = uri.getPath();
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Document URLs must include an object path under items/.");
            }
            return URLDecoder.decode(normalized, StandardCharsets.UTF_8);
        }
        if ("storage.googleapis.com".equals(hostLower)) {
            String path = uri.getPath();
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            if (!normalized.toLowerCase(Locale.US).startsWith(bucketLower + "/")) {
                throw new IllegalArgumentException("Document URLs must point to the " + bucket + " bucket.");
            }
            String remainder = normalized.substring(bucket.length() + 1);
            if (remainder.isEmpty()) {
                throw new IllegalArgumentException("Document URLs must include an object path under items/.");
            }
            return URLDecoder.decode(remainder, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Document URLs must point to the " + bucket + " bucket.");
    }

    private String extractItemIdFromPath(String path) {
        if (!path.startsWith("items/")) {
            throw new IllegalArgumentException("Document URLs must be stored under items/{itemId}/.");
        }
        String remainder = path.substring("items/".length());
        int slashIndex = remainder.indexOf('/');
        if (slashIndex <= 0) {
            throw new IllegalArgumentException("Document URLs must include an itemId and file name.");
        }
        return remainder.substring(0, slashIndex);
    }

    private boolean hasImageExtension(String path) {
        int lastSlash = path.lastIndexOf('/') + 1;
        String filename = lastSlash >= 0 && lastSlash < path.length() ? path.substring(lastSlash) : path;
        int queryIndex = filename.indexOf('?');
        if (queryIndex >= 0) {
            filename = filename.substring(0, queryIndex);
        }
        String lower = filename.toLowerCase(Locale.US);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp");
    }

    private void requireStorageBucketConfigured(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("firebase.storage-bucket must be configured to accept document URLs.");
        }
    }
    private List<ItemSummary> stubItems() {
        Instant now = Instant.now();
        return List.of(
                new ItemSummary(
                        "lost-001",
                        "Blue Backpack",
                        ItemStatus.LOST,
                        "Library Atrium",
                        CampusZone.LIBRARY,
                        now,
                        now.minusSeconds(3600),
                        List.of("backpack", "electronics"),
                        List.of()),
                new ItemSummary(
                        "found-002",
                        "Campus ID Card",
                        ItemStatus.FOUND,
                        "Student Union Desk",
                        CampusZone.MAIN,
                        now,
                        now.minusSeconds(5400),
                        List.of("id", "card"),
                        List.of()));
    }
    private ItemDetail stubDetail(String id) {
        Instant createdAt = Instant.now();
        return new ItemDetail(
                id,
                "Sample Item",
                "Placeholder description",
                ItemStatus.LOST,
                "Unknown hallway",
                CampusZone.OTHER,
                createdAt.minusSeconds(7200),
                createdAt,
                List.of("sample"),
                List.of("https://example.com/image.jpg"),
                "user-123");
    }
}







