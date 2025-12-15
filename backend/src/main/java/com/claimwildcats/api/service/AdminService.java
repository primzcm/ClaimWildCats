package com.claimwildcats.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import com.claimwildcats.api.domain.AdminDashboardSnapshot;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.entity.Claim;
import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.repository.ClaimRepository;
import com.claimwildcats.api.repository.ItemRepository;
import com.claimwildcats.api.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;

    public AdminService(ItemService itemService, 
                        UserRepository userRepository, 
                        ItemRepository itemRepository, 
                        ClaimRepository claimRepository) {
        this.itemService = itemService;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
    }

    /*public AdminDashboardSnapshot dashboard() {
        long userCount = userRepository.count();
        long itemCount = itemRepository.count();

        return new AdminDashboardSnapshot(
                0.0,
                0.0,
                userCount,
                itemCount,
                List.of("Library", "Student Center", "Cafeteria"));
    }
    */

    public List<UserProfile> listUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserProfile)
                .collect(Collectors.toList());
    }

    public List<ItemSummary> flaggedReports() {
        return itemService.browseItems(); 
    }

    public List<ClaimSummary> pendingClaims() {
        return claimRepository.findByStatus(ClaimStatus.PENDING).stream()
        .map(claim -> mapToClaimSummary(claim))
                .collect(Collectors.toList());
    }

    private UserProfile mapToUserProfile(User user) {
        return new UserProfile(
                String.valueOf(user.getUserId()),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                false,
                0,
                0,
                user.getCreatedAt()
        );
    }

    private ClaimSummary mapToClaimSummary(Claim claim) {
        return new ClaimSummary(
                claim.getId(),
                claim.getItemId(),
                claim.getClaimantId(),
                claim.getStatus(),
                claim.getSubmittedAt(),
                claim.getReviewedAt(),
                claim.getReviewerId()
        );
    }
}