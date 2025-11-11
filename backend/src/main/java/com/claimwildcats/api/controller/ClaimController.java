package com.claimwildcats.api.controller;

import com.claimwildcats.api.domain.ClaimDetail;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.dto.ClaimDecisionRequest;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.claimwildcats.api.security.SecurityUtils;
import com.claimwildcats.api.service.ClaimService;
import com.claimwildcats.api.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Claims")
public class ClaimController {

    private final ClaimService claimService;
    private final ItemService itemService;

    public ClaimController(ClaimService claimService, ItemService itemService) {
        this.claimService = claimService;
        this.itemService = itemService;
    }

    @GetMapping("/items/{itemId}/claims")
    @Operation(summary = "List claims", description = "Get all claims associated with an item.")
    public List<ClaimDetail> list(@PathVariable String itemId) {
        String viewerId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        ItemDetail item = itemService.findById(itemId);
        ensureReporter(viewerId, item);
        return claimService.listClaimsForItem(itemId);
    }

    @PostMapping("/items/{itemId}/claims")
    @Operation(summary = "Submit claim", description = "Claim ownership of a found item.")
    public ClaimSummary submit(@PathVariable String itemId, @Valid @RequestBody ClaimItemRequest request) {
        String claimantId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        ItemDetail item = itemService.findById(itemId);
        ensureClaimable(item, claimantId);
        return claimService.submitClaim(itemId, request, claimantId);
    }

    @PatchMapping("/claims/{claimId}/decision")
    @Operation(summary = "Review claim", description = "Approve or deny a claim as a finder or admin.")
    public ClaimSummary review(@PathVariable String claimId, @Valid @RequestBody ClaimDecisionRequest request) {
        String reviewerId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        ClaimDetail claim = claimService.getClaimDetail(claimId);
        ItemDetail item = itemService.findById(claim.itemId());
        ensureReporter(reviewerId, item);

        ClaimSummary summary = claimService.reviewClaim(claim, request, reviewerId);
        if (request.status() == ClaimStatus.APPROVED) {
            itemService.updateStatus(
                    item.id(),
                    new UpdateItemStatusRequest(ItemStatus.CLAIMED, request.reviewerNote()),
                    reviewerId);
        }
        return summary;
    }

    private void ensureReporter(String userId, ItemDetail item) {
        if (item == null || item.reporterId() == null || !Objects.equals(item.reporterId(), userId)) {
            throw new AccessDeniedException("Only the original reporter can view or review claims.");
        }
    }

    private void ensureClaimable(ItemDetail item, String claimantId) {
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        if (item.status() != ItemStatus.FOUND) {
            throw new IllegalStateException("Only found reports can be claimed.");
        }
        if (item.reporterId() != null && item.reporterId().equals(claimantId)) {
            throw new AccessDeniedException("You cannot claim an item you reported.");
        }
    }
}
