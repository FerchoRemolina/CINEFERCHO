package com.cinefercho.dto;

import com.cinefercho.entity.enums.HallType;
import com.cinefercho.entity.enums.ScreeningFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningSlotResponse(
        Long id,
        Long theaterId,
        String theaterName,
        Long hallId,
        String hallName,
        HallType hallType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal ticketPrice,
        ScreeningFormat format
) {
}
