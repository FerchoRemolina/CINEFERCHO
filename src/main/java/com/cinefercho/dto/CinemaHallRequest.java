package com.cinefercho.dto;

import com.cinefercho.entity.enums.HallType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CinemaHallRequest(
        @NotNull Long theaterId,
        @NotBlank @Size(max = 80) String name,
        @NotNull HallType hallType,
        @Min(1) int totalCapacity,
        @Min(1) int totalRows,
        @Min(1) int totalColumns
) {
}
