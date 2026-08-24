package com.cinefercho.dto;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Long id,
        String name,
        BigDecimal monthlyPrice,
        BigDecimal discountPercentageTickets,
        BigDecimal discountPercentageConcession
) {
}
