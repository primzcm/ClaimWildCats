package com.claimwildcats.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.claimwildcats.api.service.ItemService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ItemControllerTest {

    private final ItemService itemService = mock(ItemService.class);
    private final ItemController controller = new ItemController(itemService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reportLost_requiresAuthentication() {
        CreateLostItemRequest request = new CreateLostItemRequest(
                "Laptop", "Electronics", "Library", Instant.now(), "MacBook", null, null, false, null, List.of());
        assertThrows(AccessDeniedException.class, () -> controller.reportLost(request));
    }

    @Test
    void reportLost_returnsDetailForAuthenticatedUser() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user-7", "token");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ItemDetail detail = new ItemDetail(
                "item-1",
                "Laptop",
                ItemStatus.LOST,
                "MacBook",
                "Electronics",
                null,
                null,
                "Library",
                "WITH_OWNER",
                Instant.now(),
                Instant.now(),
                List.of(),
                0.0,
                "user-7");
        when(itemService.createLostItem(any(), eq("user-7"))).thenReturn(detail);

        ItemDetail response = controller.reportLost(new CreateLostItemRequest(
                "Laptop", "Electronics", "Library", Instant.now(), "MacBook", null, null, false, null, List.of()));

        assertThat(response).isEqualTo(detail);
    }

    @Test
    void updateStatus_requiresAuthentication() {
        assertThrows(AccessDeniedException.class,
                () -> controller.updateStatus("item-1", new UpdateItemStatusRequest(ItemStatus.RESOLVED, "done")));
    }
}
