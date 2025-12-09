package com.claimwildcats.api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.claimwildcats.api.domain.ClaimDetail;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimDecisionRequest;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.claimwildcats.api.entity.Claim;
import com.claimwildcats.api.repository.ClaimRepository;

@Service
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private static final int MAX_ATTACHMENTS = 4;

    private final ClaimRepository claimRepository;

    public ClaimService(ClaimRepository claimRepository){
        this.claimRepository = claimRepository;
    }

    public List<ClaimDetail> listClaimsForItem(String itemId) {
        return claimRepository.findByItemIdOrderBySubmittedAtDesc(itemId)
                .stream()
                .map(this::mapToDetail)
                .collect(Collectors.toList());
    }

    public List<ClaimSummary> listClaimsForUser(String userId) {
        return claimRepository.findByClaimantIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public ClaimDetail getClaimDetail(String claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
        return mapToDetail(claim);
    }

    public ClaimSummary submitClaim(String itemId, ClaimItemRequest request, String claimantId) {
        // validation
        if (claimRepository.existsByItemIdAndClaimantIdAndStatus(itemId, claimantId, ClaimStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending claim for this item.");
        }

        List<String> attachments = sanitizeAttachmentUrls(request.attachmentUrls(), itemId);
        String secretDetail = normalized(request.secretDetail());
        String justification = normalized(request.justification());

        Claim newClaim = new Claim(itemId, claimantId, secretDetail, justification, attachments);
        Claim savedClaim = claimRepository.save(newClaim);
        
        return mapToSummary(savedClaim);
    }
   
    public ClaimSummary reviewClaim(ClaimDetail claimDetail, ClaimDecisionRequest request, String reviewerId) {
        ClaimStatus desiredStatus = request.status();
        
        if (desiredStatus == null || desiredStatus == ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Provide a valid decision status.");
        }
        
        Claim claimEntity = claimRepository.findById(claimDetail.id())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (claimEntity.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Claim " + claimEntity.getId() + " has already been decided.");
        }
        claimEntity.setStatus(desiredStatus);
        claimEntity.setReviewerId(reviewerId);
        claimEntity.setReviewedAt(Instant.now());
        String note = normalized(request.reviewerNote());
        if (note != null) {
            claimEntity.setReviewerNote(note);
        }

        // save naten
        Claim updatedClaim = claimRepository.save(claimEntity);

        return mapToSummary(updatedClaim);
    }

    private ClaimDetail mapToDetail(Claim entity) {
        return new ClaimDetail(
                entity.getId(),
                entity.getItemId(),
                entity.getClaimantId(),
                entity.getStatus(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewerId(),
                entity.getSecretDetail(),
                entity.getJustification(),
                entity.getAttachmentUrls(),
                entity.getReviewerNote()
        );
    }

    private ClaimSummary mapToSummary(Claim entity) {
        return new ClaimSummary(
                entity.getId(),
                entity.getItemId(),
                entity.getClaimantId(),
                entity.getStatus(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewerId()
        );
    }

    private String normalized(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> sanitizeAttachmentUrls(List<String> attachmentUrls, String itemId) {
        if (attachmentUrls == null || attachmentUrls.isEmpty()) {
            return new ArrayList<>();
        }
        if (attachmentUrls.size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("You can attach up to " + MAX_ATTACHMENTS + " files.");
        }
        List<String> sanitized = new ArrayList<>(attachmentUrls.size());
        
        String requiredPrefix = "claims/" + itemId + "/";
        
        for (String raw : attachmentUrls) {
            if (raw == null) continue;
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;

            if (!trimmed.startsWith("gs://")) {
            }
            
            sanitized.add(trimmed);
        }
        return sanitized;
    }
}

