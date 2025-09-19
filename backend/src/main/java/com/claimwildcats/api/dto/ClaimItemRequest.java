package com.claimwildcats.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ClaimItemRequest(
        @NotBlank String secretDetail,
        @NotBlank String justification,
        @Size(max = 4) List<@NotBlank String> attachmentUrls) {
}
