package com.cinefercho.dto;

import com.cinefercho.entity.enums.MembershipType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        Long id,
        UUID invoiceNumber,
        Instant createdAt,
        ClientResponse client,
        TheaterResponse theater,
        List<TicketLineResponse> tickets,
        List<ConcessionLineResponse> concessions,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {

    public record ClientResponse(
            Long id,
            String fullName,
            String email,
            MembershipType membershipType
    ) {
    }

    public record TicketLineResponse(
            Long id,
            Long screeningId,
            String movieTitle,
            LocalDateTime startTime,
            Long seatId,
            String rowLetter,
            int seatNumber,
            boolean vip,
            BigDecimal price
    ) {
    }

    public record ConcessionLineResponse(
            Long id,
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
