package com.cinefercho.dto;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Long id,
        String name,
        BigDecimal monthlyPrice,
        int durationDays,
        String durationLabel,
        BigDecimal discountPercentageTickets,
        BigDecimal discountPercentageConcession
) {
}
