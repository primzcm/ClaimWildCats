package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ClaimDetail;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimDecisionRequest;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private static final String COLLECTION = "claims";
    private static final int MAX_ATTACHMENTS = 4;

    private final FirebaseFacade firebaseFacade;

    public ClaimService(FirebaseFacade firebaseFacade) {
        this.firebaseFacade = firebaseFacade;
    }

    public List<ClaimDetail> listClaimsForItem(String itemId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchClaimDetails(firestore, "itemId", itemId))
                .orElseGet(() -> fallbackDetails(itemId));
    }

    public List<ClaimSummary> listClaimsForUser(String userId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchClaimSummaries(firestore, "claimantId", userId))
                .orElseGet(() -> fallbackSummaries(userId));
    }

    public ClaimDetail getClaimDetail(String claimId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchClaimDetail(firestore, claimId))
                .orElseGet(() -> fallbackDetail(claimId, "item-001", "user-123", ClaimStatus.PENDING));
    }

    public ClaimSummary submitClaim(String itemId, ClaimItemRequest request, String claimantId) {
        List<String> attachments = sanitizeAttachmentUrls(request.attachmentUrls(), itemId);
        String secretDetail = normalized(request.secretDetail());
        String justification = normalized(request.justification());

        return firebaseFacade.getFirestore()
                .map(firestore -> persistClaim(firestore, itemId, claimantId, secretDetail, justification, attachments))
                .orElseGet(() -> fallbackSummary("claim-fallback", itemId, claimantId, ClaimStatus.PENDING, null));
    }

    public ClaimSummary reviewClaim(ClaimDetail claim, ClaimDecisionRequest request, String reviewerId) {
        ClaimStatus desiredStatus = request.status();
        if (desiredStatus == null || desiredStatus == ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Provide a valid decision status.");
        }
        if (claim.status() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Claim " + claim.id() + " has already been decided.");
        }

        String reviewerNote = normalized(request.reviewerNote());
        return firebaseFacade.getFirestore()
                .map(firestore -> updateClaimStatus(firestore, claim.id(), desiredStatus, reviewerId, reviewerNote))
                .orElseGet(() -> fallbackSummary(claim.id(), claim.itemId(), claim.claimantId(), desiredStatus, reviewerId));
    }

    private List<ClaimDetail> fetchClaimDetails(Firestore firestore, String fieldName, String value) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .whereEqualTo(fieldName, value)
                    .orderBy("submittedAt", Query.Direction.DESCENDING)
                    .get()
                    .get()
                    .getDocuments();
            List<ClaimDetail> details = new ArrayList<>(documents.size());
            for (QueryDocumentSnapshot doc : documents) {
                mapDetail(doc).ifPresent(details::add);
            }
            return details;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching claims", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch claims from Firestore", e);
        }
    }

    private List<ClaimSummary> fetchClaimSummaries(Firestore firestore, String fieldName, String value) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .whereEqualTo(fieldName, value)
                    .orderBy("submittedAt", Query.Direction.DESCENDING)
                    .get()
                    .get()
                    .getDocuments();
            List<ClaimSummary> summaries = new ArrayList<>(documents.size());
            for (QueryDocumentSnapshot doc : documents) {
                mapSummary(doc).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching claims", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch claims from Firestore", e);
        }
    }

    private ClaimDetail fetchClaimDetail(Firestore firestore, String claimId) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(claimId).get().get();
            if (!snapshot.exists()) {
                throw new IllegalArgumentException("Claim not found: " + claimId);
            }
            return mapDetail(snapshot)
                    .orElseThrow(() -> new IllegalStateException("Unable to map claim " + claimId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading claim " + claimId, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to load claim " + claimId, e);
        }
    }

    private ClaimSummary persistClaim(
            Firestore firestore,
            String itemId,
            String claimantId,
            String secretDetail,
            String justification,
            List<String> attachments) {
        ensureNoDuplicatePendingClaim(firestore, itemId, claimantId);

        DocumentReference doc = firestore.collection(COLLECTION).document();
        Map<String, Object> document = new HashMap<>();
        document.put("itemId", itemId);
        document.put("claimantId", claimantId);
        document.put("status", ClaimStatus.PENDING.name());
        document.put("submittedAt", Timestamp.now());
        document.put("secretDetail", secretDetail);
        document.put("justification", justification);
        document.put("attachmentUrls", attachments);

        try {
            doc.set(document).get();
            DocumentSnapshot snapshot = doc.get().get();
            return mapSummary(snapshot).orElseThrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving claim", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save claim to Firestore", e);
        }
    }

    private ClaimSummary updateClaimStatus(
            Firestore firestore,
            String claimId,
            ClaimStatus status,
            String reviewerId,
            String reviewerNote) {
        try {
            DocumentReference doc = firestore.collection(COLLECTION).document(claimId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status.name());
            updates.put("reviewedAt", Timestamp.now());
            updates.put("reviewerId", reviewerId);
            if (reviewerNote != null && !reviewerNote.isBlank()) {
                updates.put("reviewerNote", reviewerNote);
            }
            doc.set(updates, SetOptions.merge()).get();
            DocumentSnapshot refreshed = doc.get().get();
            return mapSummary(refreshed).orElseThrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while updating claim", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to update claim in Firestore", e);
        }
    }

    private void ensureNoDuplicatePendingClaim(Firestore firestore, String itemId, String claimantId) {
        try {
            List<QueryDocumentSnapshot> matches = firestore.collection(COLLECTION)
                    .whereEqualTo("itemId", itemId)
                    .whereEqualTo("claimantId", claimantId)
                    .get()
                    .get()
                    .getDocuments();
            for (QueryDocumentSnapshot snapshot : matches) {
                String rawStatus = snapshot.getString("status");
                try {
                    ClaimStatus status = ClaimStatus.valueOf(rawStatus);
                    if (status == ClaimStatus.PENDING) {
                        throw new IllegalStateException("You already have a pending claim for this item.");
                    }
                } catch (IllegalArgumentException ex) {
                    log.debug("Skipping duplicate validation for claim {}: {}", snapshot.getId(), rawStatus);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while checking duplicate claims", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to check duplicate claims", e);
        }
    }

    private List<String> sanitizeAttachmentUrls(List<String> attachmentUrls, String itemId) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            return List.of();
        }
        if (attachmentUrls.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("You can attach up to " + MAX_ATTACHMENTS + " files.");
        }
        List<String> sanitized = new ArrayList<>(attachmentUrls.size());
        String requiredPrefix = "claims/" + itemId + "/";
        for (String raw : attachmentUrls) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!trimmed.startsWith("gs://")) {
                throw new IllegalArgumentException("Attachments must point to Firebase Storage (gs://).");
            }
            int bucketSeparator = trimmed.indexOf('/', 5);
            if (bucketSeparator < 0 || bucketSeparator + 1 >= trimmed.length()) {
                throw new IllegalArgumentException("Invalid attachment URL: " + trimmed);
            }
            String path = trimmed.substring(bucketSeparator + 1);
            if (!path.startsWith(requiredPrefix)) {
                throw new IllegalArgumentException("Attachments must live under " + requiredPrefix);
            }
            sanitized.add(trimmed);
        }
        return sanitized;
    }

    private List<ClaimDetail> fallbackDetails(String itemId) {
        return List.of(fallbackDetail("claim-sample", itemId, "user-abc", ClaimStatus.PENDING));
    }

    private List<ClaimSummary> fallbackSummaries(String claimantId) {
        return List.of(fallbackSummary(
                "claim-001",
                "item-001",
                claimantId,
                ClaimStatus.PENDING,
                null));
    }

    private ClaimDetail fallbackDetail(String claimId, String itemId, String claimantId, ClaimStatus status) {
        Instant now = Instant.now();
        return new ClaimDetail(
                claimId,
                itemId,
                claimantId,
                status,
                now.minusSeconds(900),
                status == ClaimStatus.PENDING ? null : now,
                status == ClaimStatus.PENDING ? null : "reviewer-001",
                "Custom engraving inside the pouch",
                "Matches my issued backpack from the library.",
                List.of("https://example.com/proof.jpg"),
                status == ClaimStatus.PENDING ? null : "Fallback reviewer note");
    }

    private ClaimSummary fallbackSummary(
            String claimId, String itemId, String claimantId, ClaimStatus status, String reviewerId) {
        Instant now = Instant.now();
        Instant reviewedAt = status == ClaimStatus.PENDING ? null : now;
        return new ClaimSummary(claimId, itemId, claimantId, status, now.minusSeconds(600), reviewedAt, reviewerId);
    }

    private Optional<ClaimDetail> mapDetail(DocumentSnapshot doc) {
        try {
            ClaimStatus status = ClaimStatus.valueOf(doc.getString("status"));
            Instant submitted = toInstant(doc.getTimestamp("submittedAt"));
            Instant reviewed = toInstant(doc.getTimestamp("reviewedAt"));
            List<String> attachments = extractStringList(doc, "attachmentUrls");
            return Optional.of(new ClaimDetail(
                    doc.getId(),
                    doc.getString("itemId"),
                    doc.getString("claimantId"),
                    status,
                    submitted,
                    reviewed,
                    doc.getString("reviewerId"),
                    doc.getString("secretDetail"),
                    doc.getString("justification"),
                    attachments,
                    doc.getString("reviewerNote")));
        } catch (Exception ex) {
            log.warn("Skipping claim {} due to mapping error: {}", doc.getId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<ClaimSummary> mapSummary(DocumentSnapshot doc) {
        return mapDetail(doc).map(this::toSummary);
    }

    private ClaimSummary toSummary(ClaimDetail detail) {
        return new ClaimSummary(
                detail.id(),
                detail.itemId(),
                detail.claimantId(),
                detail.status(),
                detail.submittedAt(),
                detail.reviewedAt(),
                detail.reviewerId());
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
        return values;
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toDate().toInstant();
    }

    private String normalized(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

