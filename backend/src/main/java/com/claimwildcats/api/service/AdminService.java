package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.AdminDashboardSnapshot;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.domain.UserRole;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final ItemService itemService;
    private final ClaimService claimService;

    public AdminService(ItemService itemService, ClaimService claimService) {
        this.itemService = itemService;
        this.claimService = claimService;
    }

    public AdminDashboardSnapshot dashboard() {
        return new AdminDashboardSnapshot(
                0.68,
                36.4,
                24,
                102,
                List.of("Library Atrium", "Engineering West", "Student Center"));
    }

    public List<UserProfile> listUsers() {
        return List.of(
                new UserProfile(
                        "user-123",
                        "Jordan Wildcat",
                        "jordan.wildcat@campus.edu",
                        UserRole.USER,
                        true,
                        2,
                        5,
                        Instant.now().minusSeconds(120_000)),
                new UserProfile(
                        "admin-001",
                        "Casey Admin",
                        "casey.admin@campus.edu",
                        UserRole.ADMIN,
                        true,
                        0,
                        12,
                        Instant.now().minusSeconds(320_000)));
    }

    public List<ItemSummary> flaggedReports() {
        return itemService.browseItems();
    }

    public List<ClaimSummary> pendingClaims() {
        return List.of(
                new ClaimSummary(
                        "claim-002",
                        "lost-001",
                        "user-456",
                        ClaimStatus.PENDING,
                        Instant.now().minusSeconds(2400),
                        null,
                        null));
    }
}
