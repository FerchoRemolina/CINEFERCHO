package com.cinefercho.dto;

import com.cinefercho.entity.enums.HallType;
import com.cinefercho.entity.enums.ScreeningFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningResponse(
        Long id,
        MovieResponse movie,
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
