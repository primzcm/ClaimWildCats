package com.claimwildcats.api.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // generates a unique string id automaticaly.
    private String id;

    private String title;

    @Column(length = 1000) // allow longer descriptions
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private String locationText;

    @Enumerated(EnumType.STRING)
    private CampusZone campusZone;


    private Instant lastSeenAt;
    private Instant createdAt;
    private String reporterId;

    // mysql cannot store lists directly in one cell.
    // @elementcollection creates a separate hidden table for these lists automatically.
    @Column(name = "url")
    private List<String> tags = new ArrayList<>();

     @Column(name = "url")
    private List<String> docUrls = new ArrayList<>();

    public Item(){
        this.createdAt = Instant.now();
    }


    // getters & setters ! 
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public CampusZone getCampusZone() { return campusZone; }
    public void setCampusZone(CampusZone campusZone) { this.campusZone = campusZone; }

    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getDocUrls() { return docUrls; }
    public void setDocUrls(List<String> docUrls) { this.docUrls = docUrls; }


}
