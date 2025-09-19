package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.domain.UserRole;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final ItemService itemService;
    private final ClaimService claimService;

    public UserService(ItemService itemService, ClaimService claimService) {
        this.itemService = itemService;
        this.claimService = claimService;
    }

    public UserProfile getProfile(String userId) {
        return new UserProfile(
                userId,
                "Jordan Wildcat",
                "jordan.wildcat@campus.edu",
                UserRole.USER,
                true,
                2,
                5,
                Instant.now().minusSeconds(86_400));
    }

    public List<ItemSummary> listMyReports(String userId) {
        return itemService.browseItems();
    }

    public List<ClaimSummary> listMyClaims(String userId) {
        return List.of(
                new ClaimSummary(
                        "claim-002",
                        "found-002",
                        userId,
                        ClaimStatus.APPROVED,
                        Instant.now().minusSeconds(7200),
                        Instant.now().minusSeconds(3600),
                        "admin-001"));
    }
}
