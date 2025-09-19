package com.claimwildcats.api.controller;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "User profile", description = "Fetch profile information for a campus community member.")
    public UserProfile profile(@PathVariable String userId) {
        return userService.getProfile(userId);
    }

    @GetMapping("/{userId}/reports")
    @Operation(summary = "User reports", description = "List lost and found reports created by the user.")
    public List<ItemSummary> reports(@PathVariable String userId) {
        return userService.listMyReports(userId);
    }

    @GetMapping("/{userId}/claims")
    @Operation(summary = "User claims", description = "List claims submitted by the user.")
    public List<ClaimSummary> claims(@PathVariable String userId) {
        return userService.listMyClaims(userId);
    }
}
