package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimItemRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    private final FirebaseFacade firebaseFacade;

    public ClaimService(FirebaseFacade firebaseFacade) {
        this.firebaseFacade = firebaseFacade;
    }

    public List<ClaimSummary> listClaimsForItem(String itemId) {
        firebaseFacade.logReadiness();
        return List.of(
                new ClaimSummary(
                        "claim-001",
                        itemId,
                        "user-789",
                        ClaimStatus.PENDING,
                        Instant.now().minusSeconds(1800),
                        null,
                        null));
    }

    public ClaimSummary submitClaim(String itemId, ClaimItemRequest request) {
        return new ClaimSummary(
                "claim-" + UUID.randomUUID(),
                itemId,
                "user-789",
                ClaimStatus.PENDING,
                Instant.now(),
                null,
                null);
    }

    public ClaimSummary reviewClaim(String claimId, ClaimStatus newStatus, String reviewerId) {
        return new ClaimSummary(
                claimId,
                "item-123",
                "user-789",
                newStatus,
                Instant.now().minusSeconds(900),
                Instant.now(),
                reviewerId);
    }
}
