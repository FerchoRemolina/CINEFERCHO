package com.cinefercho.dto;

import com.cinefercho.entity.enums.MovieStatus;

public record MovieResponse(
        Long id,
        String title,
        String synopsis,
        int durationMinutes,
        String genre,
        String rating,
        String posterUrl,
        MovieStatus status
) {
}
