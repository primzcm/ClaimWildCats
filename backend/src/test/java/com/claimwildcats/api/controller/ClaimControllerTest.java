package com.claimwildcats.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.claimwildcats.api.service.ClaimService;
import com.claimwildcats.api.service.ItemService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ClaimControllerTest {

    private final ClaimService claimService = mock(ClaimService.class);
    private final ItemService itemService = mock(ItemService.class);
    private ClaimController controller;

    @BeforeEach
    void setUp() {
        controller = new ClaimController(claimService, itemService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitClaim_requiresAuthentication() {
        when(itemService.findById("item-1")).thenReturn(buildItem(ItemStatus.FOUND, "owner-2"));
        assertThrows(AccessDeniedException.class,
                () -> controller.submit("item-1", new ClaimItemRequest("detail", "justification", List.of())));
    }

    @Test
    void submitClaim_returnsSummaryWhenAuthenticated() {
        when(itemService.findById("item-1")).thenReturn(buildItem(ItemStatus.FOUND, "owner-2"));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user-4", "token");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ClaimSummary summary = new ClaimSummary(
                "claim-1",
                "item-1",
                "user-4",
                ClaimStatus.PENDING,
                Instant.now(),
                null,
                null);
        when(claimService.submitClaim(any(), any(), any())).thenReturn(summary);

        ClaimSummary result = controller.submit("item-1", new ClaimItemRequest("detail", "justification", List.of()));
        assertThat(result).isEqualTo(summary);
    }

    @Test
    void submitClaim_rejectsReporter() {
        when(itemService.findById("item-1")).thenReturn(buildItem(ItemStatus.FOUND, "user-4"));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user-4", "token");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(AccessDeniedException.class,
                () -> controller.submit("item-1", new ClaimItemRequest("detail", "justification", List.of())));
    }

    private ItemDetail buildItem(ItemStatus status, String reporterId) {
        Instant now = Instant.now();
        return new ItemDetail(
                "item-1",
                "Wallet",
                "Lost wallet",
                status,
                "Library",
                CampusZone.LIBRARY,
                now.minusSeconds(3600),
                now,
                List.of(),
                List.of(),
                reporterId);
    }
}

