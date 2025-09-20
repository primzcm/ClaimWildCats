package com.claimwildcats.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.claimwildcats.api.service.ClaimService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ClaimControllerTest {

    private final ClaimService claimService = mock(ClaimService.class);
    private final ClaimController controller = new ClaimController(claimService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitClaim_requiresAuthentication() {
        assertThrows(AccessDeniedException.class,
                () -> controller.submit("item-1", new ClaimItemRequest("detail", "justification", List.of())));
    }

    @Test
    void submitClaim_returnsSummaryWhenAuthenticated() {
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
}
