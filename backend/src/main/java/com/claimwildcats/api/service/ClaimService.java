package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.SetOptions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private static final String COLLECTION = "claims";

    private final FirebaseFacade firebaseFacade;

    public ClaimService(FirebaseFacade firebaseFacade) {
        this.firebaseFacade = firebaseFacade;
    }

    public List<ClaimSummary> listClaimsForItem(String itemId) {
        return queryClaims("itemId", itemId);
    }

    public List<ClaimSummary> listClaimsForUser(String userId) {
        return queryClaims("claimantId", userId);
    }

    public ClaimSummary submitClaim(String itemId, ClaimItemRequest request, String claimantId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> persistClaim(firestore, itemId, request, claimantId))
                .orElseGet(() -> new ClaimSummary(
                        "claim-fallback",
                        itemId,
                        claimantId,
                        ClaimStatus.PENDING,
                        Instant.now(),
                        null,
                        null));
    }

    public ClaimSummary reviewClaim(String claimId, ClaimStatus newStatus, String reviewerId) {
        return firebaseFacade.getFirestore()
                .map(firestore -> updateClaimStatus(firestore, claimId, newStatus, reviewerId))
                .orElseGet(() -> new ClaimSummary(
                        claimId,
                        "item-123",
                        "user-789",
                        newStatus,
                        Instant.now().minusSeconds(900),
                        Instant.now(),
                        reviewerId));
    }

    private List<ClaimSummary> queryClaims(String fieldName, String value) {
        return firebaseFacade.getFirestore()
                .map(firestore -> fetchClaims(firestore, fieldName, value))
                .orElseGet(() -> List.of(new ClaimSummary(
                        "claim-001",
                        "item-001",
                        value,
                        ClaimStatus.PENDING,
                        Instant.now().minusSeconds(1800),
                        null,
                        null)));
    }

    private List<ClaimSummary> fetchClaims(Firestore firestore, String fieldName, String value) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION)
                    .whereEqualTo(fieldName, value)
                    .orderBy("submittedAt", Query.Direction.DESCENDING)
                    .get()
                    .get()
                    .getDocuments();
            List<ClaimSummary> summaries = new ArrayList<>(documents.size());
            for (QueryDocumentSnapshot doc : documents) {
                map(doc).ifPresent(summaries::add);
            }
            return summaries;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching claims", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch claims from Firestore", e);
        }
    }

    private ClaimSummary persistClaim(
            Firestore firestore, String itemId, ClaimItemRequest request, String claimantId) {
        DocumentReference doc = firestore.collection(COLLECTION).document();
        Map<String, Object> document = new HashMap<>();
        document.put("itemId", itemId);
        document.put("claimantId", claimantId);
        document.put("status", ClaimStatus.PENDING.name());
        document.put("submittedAt", Timestamp.now());
        document.put("secretDetail", request.secretDetail());
        document.put("justification", request.justification());
        document.put("attachmentUrls", request.attachmentUrls() == null ? List.of() : new ArrayList<>(request.attachmentUrls()));

        try {
            doc.set(document).get();
            DocumentSnapshot snapshot = doc.get().get();
            return map(snapshot).orElseThrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while saving claim", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save claim to Firestore", e);
        }
    }

    private ClaimSummary updateClaimStatus(
            Firestore firestore, String claimId, ClaimStatus status, String reviewerId) {
        try {
            DocumentReference doc = firestore.collection(COLLECTION).document(claimId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status.name());
            updates.put("reviewedAt", Timestamp.now());
            updates.put("reviewerId", reviewerId);
            doc.set(updates, SetOptions.merge()).get();
            DocumentSnapshot refreshed = doc.get().get();
            return map(refreshed).orElseThrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while updating claim", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to update claim in Firestore", e);
        }
    }

    private java.util.Optional<ClaimSummary> map(DocumentSnapshot doc) {
        try {
            ClaimStatus status = ClaimStatus.valueOf(doc.getString("status"));
            Instant submitted = doc.getTimestamp("submittedAt") != null
                    ? toInstant(doc.getTimestamp("submittedAt"))
                    : Instant.now();
            Instant reviewed = doc.getTimestamp("reviewedAt") != null
                    ? toInstant(doc.getTimestamp("reviewedAt"))
                    : null;
            return java.util.Optional.of(new ClaimSummary(
                    doc.getId(),
                    doc.getString("itemId"),
                    doc.getString("claimantId"),
                    status,
                    submitted,
                    reviewed,
                    doc.getString("reviewerId")));
        } catch (Exception ex) {
            log.warn("Skipping claim {} due to mapping error: {}", doc.getId(), ex.getMessage());
            return java.util.Optional.empty();
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null
                ? null
                : timestamp.toDate().toInstant();
    }
}
