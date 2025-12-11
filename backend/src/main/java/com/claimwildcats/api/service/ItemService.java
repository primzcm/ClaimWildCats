package com.claimwildcats.api.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.ItemSearchResponse;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.claimwildcats.api.entity.Item;
import com.claimwildcats.api.repository.ItemRepository;

// ito poy service backends sa items
@Service
public class ItemService{

    @Autowired
    private ItemRepository itemRepository;

    public ItemDetail createLostItem (CreateLostItemRequest request, String userId){
        Item item = new Item();
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setStatus(com.claimwildcats.api.domain.ItemStatus.LOST);
        item.setCampusZone(request.campusZone());
        item.setLastSeenAt(request.lastSeenAt());
        item.setReporterId(userId);
        item.setTags(request.tags());
        item.setDocUrls(Collections.emptyList());


        // save to mysql
        Item savedItem = itemRepository.save(item);
        return mapToDetail(savedItem); // return natin yung full object instead of the id ra
    }

    public ItemDetail createFoundItem(CreateFoundItemRequest request, String userId){
        Item item = new Item();
        item.setTitle(request.title());
        item.setCampusZone(request.campusZone());
        item.setDescription(request.description());
        item.setStatus(com.claimwildcats.api.domain.ItemStatus.FOUND);
        item.setLastSeenAt(Instant.now());
        item.setReporterId(userId);
        item.setTags(request.tags());
        // item.setDocUrls(request.imageUrls()); to confirm

        Item savedItem = itemRepository.save(item);
        return mapToDetail(savedItem);

    }

    public ItemSearchResponse searchItems(ItemStatus status, CampusZone zone, String query, int page, int pageSize) {
        // Create pagination request (Sort by newest first)
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        // Call the custom query in Repository
        Page<Item> itemPage = itemRepository.search(status, zone, query, pageable);

        // Convert Entities to Summaries
        List<ItemSummary> summaries = itemPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        // Return the Record exactly as defined in your DTO
        return new ItemSearchResponse(
            summaries, 
            page, 
            pageSize, 
            itemPage.getTotalElements()
        );
    }

     public ItemDetail findById(String id) {
        return itemRepository.findById(id)
                .map(this::mapToDetail)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }

    public List<ItemSummary> findSimilar(String id) {
        // logic find items with same campus zone or similar title tags
        // for now, returning empty list to prevent crash
        return Collections.emptyList(); 
    }

    // read
    public List<ItemDetail> getAllItems() {
        List<Item> entities = itemRepository.findAll();
        return entities.stream().map(this::mapToDetail).collect(Collectors.toList());
    }

    public ItemDetail updateStatus(String id, UpdateItemStatusRequest request, String currentUserId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // security check: only the reporter can update status
        if (!item.getReporterId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this item");
        }

        item.setStatus(request.status());
        Item updatedItem = itemRepository.save(item);
        return mapToDetail(updatedItem);
    }

    


     // convert entity (mysql) to record (frontend) 
     private ItemDetail mapToDetail(Item item) {
        return new ItemDetail(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getStatus(),
            item.getLocationText(),
            item.getCampusZone(),
            item.getLastSeenAt(),
            item.getCreatedAt(),
            item.getTags(),
            item.getDocUrls(),
            item.getReporterId()
        );
    }

    public List<ItemSummary> listReportsForUser(String userId){
        return itemRepository.findByReporterId(userId).stream()
            .map(this::mapToSummary)
            .collect(Collectors.toList());
    }

    private ItemSummary mapToSummary(Item item) {
        return new ItemSummary(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStatus(),
                item.getLastSeenAt()
        );
    }

    public List<ItemSummary> browseItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'browseItems'");
    }

    



}