package com.cinefercho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePurchaseRequest(
        @NotNull Long screeningId,
        @NotNull @NotEmpty List<@NotNull Long> seatIds,
        @NotNull List<@Valid ConcessionItemRequest> concessionItems,
        Long buyMembershipPlanId
) {

    public CreatePurchaseRequest {
        seatIds = seatIds == null ? List.of() : List.copyOf(seatIds);
        concessionItems = concessionItems == null ? List.of() : List.copyOf(concessionItems);
    }
}
