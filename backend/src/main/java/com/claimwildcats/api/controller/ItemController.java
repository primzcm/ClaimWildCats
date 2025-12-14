package com.claimwildcats.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus; // Imported all annotations
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.ItemSearchResponse;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.claimwildcats.api.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}) // Fix CORS
@Tag(name = "Items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "Search items")
    public ItemSearchResponse browse(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "campusZone", required = false) String campusZone,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {
        ItemStatus parsedStatus = ItemStatus.fromValue(status);
        CampusZone parsedZone = CampusZone.fromValue(campusZone);
        return itemService.searchItems(parsedStatus, parsedZone, query, page, pageSize);
    }

    @GetMapping("/{id}")
    public ItemDetail get(@PathVariable String id) {
        return itemService.findById(id);
    }

    @GetMapping("/{id}/similar")
    public List<ItemSummary> similar(@PathVariable String id) {
        return itemService.findSimilar(id);
    }

    // --- MODIFIED ENDPOINT: REPORT LOST ---
    @PostMapping("/lost")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDetail reportLost(
        @Valid @RequestBody CreateLostItemRequest request,
        @RequestParam String reporterId 
    ) {
        return itemService.createLostItem(request, reporterId);
    }

    // --- MODIFIED ENDPOINT: REPORT FOUND ---
    @PostMapping("/found")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDetail reportFound(
        @Valid @RequestBody CreateFoundItemRequest request,
        @RequestParam String reporterId 
    ) {
        return itemService.createFoundItem(request, reporterId);
    }

    @PatchMapping("/{id}/status")
    public ItemDetail updateStatus(
        @PathVariable String id, 
        @Valid @RequestBody UpdateItemStatusRequest request,
        @RequestParam String reporterId 
    ) {
        return itemService.updateStatus(id, request, reporterId);
    }
}