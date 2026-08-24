package com.cinefercho.dto;

import com.cinefercho.entity.enums.HallType;

public record CinemaHallResponse(
        Long id,
        Long theaterId,
        String theaterName,
        String name,
        HallType hallType,
        int totalCapacity,
        int totalRows,
        int totalColumns
) {
}
