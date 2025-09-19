package com.claimwildcats.api.dto;

import com.claimwildcats.api.domain.ItemStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateItemStatusRequest(
        @NotNull ItemStatus status,
        @Size(max = 140) String note) {
}
