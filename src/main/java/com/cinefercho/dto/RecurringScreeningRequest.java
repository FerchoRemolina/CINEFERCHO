package com.cinefercho.dto;

import com.cinefercho.entity.enums.ScreeningFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RecurringScreeningRequest(
        @NotNull Long movieId,
        @NotNull Long hallId,
        @NotNull LocalTime startTime,
        @NotEmpty @Size(max = 31) List<@NotNull LocalDate> dates,
        @NotNull @DecimalMin("0.00") BigDecimal ticketPrice,
        @NotNull ScreeningFormat format
) {
}
