package com.claimwildcats.api.controller;

import com.claimwildcats.api.domain.AdminDashboardSnapshot;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard snapshot", description = "High level KPIs for moderation.")
    public AdminDashboardSnapshot dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/users")
    @Operation(summary = "Manage users", description = "List campus community members and their roles.")
    public List<UserProfile> users() {
        return adminService.listUsers();
    }

    @GetMapping("/reports/flagged")
    @Operation(summary = "Flagged reports", description = "Items requiring moderator review.")
    public List<ItemSummary> flaggedReports() {
        return adminService.flaggedReports();
    }

    @GetMapping("/claims/pending")
    @Operation(summary = "Pending claims", description = "Claims awaiting review decisions.")
    public List<ClaimSummary> pendingClaims() {
        return adminService.pendingClaims();
    }
}
