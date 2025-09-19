package com.claimwildcats.api.controller;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.claimwildcats.api.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@Validated
@Tag(name = "Items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "Browse items", description = "Returns a filtered feed of lost and found posts.")
    public List<ItemSummary> browse() {
        return itemService.browseItems();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item", description = "Fetch detail for a single lost or found report.")
    public ItemDetail get(@PathVariable String id) {
        return itemService.findById(id);
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Similar items", description = "Suggests potential matches for the given item.")
    public List<ItemSummary> similar(@PathVariable String id) {
        return itemService.findSimilar(id);
    }

    @PostMapping("/lost")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create lost item", description = "Submit a new lost item report.")
    public ItemDetail reportLost(@Valid @RequestBody CreateLostItemRequest request) {
        return itemService.createLostItem(request);
    }

    @PostMapping("/found")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create found item", description = "Submit a new found item report.")
    public ItemDetail reportFound(@Valid @RequestBody CreateFoundItemRequest request) {
        return itemService.createFoundItem(request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update item status", description = "Moderators or owners can adjust the lifecycle state.")
    public ItemDetail updateStatus(@PathVariable String id, @Valid @RequestBody UpdateItemStatusRequest request) {
        return itemService.updateStatus(id, request);
    }
}
