package com.claimwildcats.api.controller;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.claimwildcats.api.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/items/{itemId}/claims")
    @Operation(summary = "List claims", description = "Get all claims associated with an item.")
    public List<ClaimSummary> list(@PathVariable String itemId) {
        return claimService.listClaimsForItem(itemId);
    }

    @PostMapping("/items/{itemId}/claims")
    @Operation(summary = "Submit claim", description = "Claim ownership of a found item.")
    public ClaimSummary submit(@PathVariable String itemId, @Valid @RequestBody ClaimItemRequest request) {
        return claimService.submitClaim(itemId, request);
    }

    @PatchMapping("/claims/{claimId}/decision")
    @Operation(summary = "Review claim", description = "Approve or deny a claim as a finder or admin.")
    public ClaimSummary review(
            @PathVariable String claimId,
            @RequestParam ClaimStatus status,
            @RequestParam(defaultValue = "admin-001") String reviewerId) {
        return claimService.reviewClaim(claimId, status, reviewerId);
    }
}
