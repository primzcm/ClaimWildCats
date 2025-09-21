package com.claimwildcats.api.service;

import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.domain.UserRole;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USERS_COLLECTION = "users";

    private final ItemService itemService;
    private final ClaimService claimService;
    private final FirebaseFacade firebaseFacade;

    public UserService(ItemService itemService, ClaimService claimService, FirebaseFacade firebaseFacade) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.firebaseFacade = firebaseFacade;
    }

    public UserProfile getProfile(String userId) {
        List<ItemSummary> reports = listMyReports(userId);
        long resolvedCount = reports.stream().filter(summary -> summary.status() == ItemStatus.CLAIMED).count();
        long openCount = reports.size() - resolvedCount;

        Optional<UserProfile> firestoreProfile = firebaseFacade.getFirestore()
                .map(firestore -> fetchUserProfile(firestore, userId, (int) openCount, (int) resolvedCount));

        return firestoreProfile.orElseGet(() -> new UserProfile(
                userId,
                userId,
                userId,
                UserRole.USER,
                false,
                (int) openCount,
                (int) resolvedCount,
                Instant.now()));
    }

    public List<ItemSummary> listMyReports(String userId) {
        return itemService.listReportsForUser(userId);
    }

    public List<ClaimSummary> listMyClaims(String userId) {
        return claimService.listClaimsForUser(userId);
    }

    private UserProfile fetchUserProfile(Firestore firestore, String userId, int openCount, int resolvedCount) {
        try {
            DocumentSnapshot snapshot = firestore.collection(USERS_COLLECTION).document(userId).get().get();
            if (!snapshot.exists()) {
                return new UserProfile(
                        userId,
                        userId,
                        userId,
                        UserRole.USER,
                        false,
                        openCount,
                        resolvedCount,
                        Instant.now());
            }

            String fullName = Optional.ofNullable(snapshot.getString("fullName")).orElse(userId);
            String email = Optional.ofNullable(snapshot.getString("email")).orElse(userId);
            boolean emailVerified = Optional.ofNullable(snapshot.getBoolean("emailVerified")).orElse(false);
            UserRole role = Optional.ofNullable(snapshot.getString("role"))
                    .map(value -> {
                        try {
                            return UserRole.valueOf(value);
                        } catch (IllegalArgumentException ex) {
                            log.warn("Unknown role '{}' for user {}", value, userId);
                            return UserRole.USER;
                        }
                    })
                    .orElse(UserRole.USER);
            Instant createdAt = Optional.ofNullable(snapshot.getTimestamp("createdAt"))
                    .map(this::toInstant)
                    .orElse(Instant.now());

            return new UserProfile(
                    userId,
                    fullName,
                    email,
                    role,
                    emailVerified,
                    openCount,
                    resolvedCount,
                    createdAt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading user profile", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to load user profile from Firestore", e);
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp.toDate().toInstant();
    }
}
