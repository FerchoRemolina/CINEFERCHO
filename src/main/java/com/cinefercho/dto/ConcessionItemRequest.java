package com.cinefercho.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConcessionItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
