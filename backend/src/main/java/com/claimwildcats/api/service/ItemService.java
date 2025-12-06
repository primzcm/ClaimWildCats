package com.claimwildcats.api.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.entity.Item;
import com.claimwildcats.api.repository.ItemRepository;

// ito poy service backends sa items

public class ItemService{

    @Autowired
    private ItemRepository itemRepository;

    public String createLostItem (CreateLostItemRequest request, String userId){
        Item item = new Item();
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setStatus(com.claimwildcats.api.domain.ItemStatus.LOST);
        item.setCampusZone(request.campusZone());
        item.setLastSeenAt(request.lastSeenAt());
        item.setReporterId(userId);
        item.setTags(request.tags());


        // save to mysql
        Item savedItem = itemRepository.save(item);
        return savedItem.getId();
    }

    public String createFoundItem(CreateFoundItemRequest request, String userId){
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
        return savedItem.getId();

    }

    // read
    public List<ItemDetail> getAllItems() {
        List<Item> entities = itemRepository.findAll();
        return entities.stream().map(this::mapToDetail).collect(Collectors.toList());
    }

    public ItemDetail getItemById(String id){
        return itemRepository.findById(id)
            .map(this::mapToDetail)
            .orElseThrow(()  -> new RuntimeException("Item not found."));  

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
        List<Item> items = itemRepository.findByReporterId(userId);


        // where we convert naten yung entity to itemsummary record
        return items.stream()
        .map(item -> new ItemSummary(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getStatus(),
            item.getLastSeenAt()
        ))
        .collect(Collectors.toList());
    }



}