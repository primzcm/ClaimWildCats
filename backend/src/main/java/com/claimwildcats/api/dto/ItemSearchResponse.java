package com.claimwildcats.api.dto;

import com.claimwildcats.api.domain.ItemSummary;
import java.util.List;

public record ItemSearchResponse(List<ItemSummary> items, int page, int pageSize, long totalItems) {
}
