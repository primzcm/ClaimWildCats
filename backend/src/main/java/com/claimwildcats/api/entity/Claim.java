package com.claimwildcats.api.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.claimwildcats.api.domain.ClaimStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "claims")
public class Claim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // we store the ids as strings to match sa item/user pattern. 

    @Column(nullable=false)
    private String itemId;

    @Column(nullable=false)
    private String claimantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

     private Instant submittedAt;
     private Instant reviewedAt;
     private String reviewerId;

     @Column(length = 1000) 
     private String secretDetail;

     @Column(length = 1000)
     private String justification;

    @Column(length = 1000)
    private String reviewerNote;

    @ElementCollection
    @CollectionTable(name = "claim_attachments", joinColumns = @JoinColumn(name = "claim_id"))
    @Column(name = "url")
    private List<String> attachmentUrls = new ArrayList<>();

    public Claim() {
        this.submittedAt = Instant.now();
        this.status = ClaimStatus.PENDING;
    }

    public Claim(String itemId, String claimantId, String secretDetail, String justification, List<String> attachmentUrls) {
        this();
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.secretDetail = secretDetail;
        this.justification = justification;
        this.attachmentUrls = attachmentUrls;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getClaimantId() { return claimantId; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getSecretDetail() { return secretDetail; }
    public void setSecretDetail(String secretDetail) { this.secretDetail = secretDetail; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }

    public List<String> getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }
}
}
