package com.cinefercho.dto;

import com.cinefercho.entity.enums.ScreeningFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningRequest(
        @NotNull Long movieId,
        @NotNull Long hallId,
        @NotNull LocalDateTime startTime,
        LocalDateTime endTime,
        @NotNull @DecimalMin("0.00") BigDecimal ticketPrice,
        @NotNull ScreeningFormat format
) {
}
