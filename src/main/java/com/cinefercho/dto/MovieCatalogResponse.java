package com.cinefercho.dto;

import com.cinefercho.entity.enums.MovieFormat;

import java.time.LocalDate;
import java.util.List;

public record MovieCatalogResponse(
        Long id,
        String posterUrl,
        String title,
        String description,
        String ageRating,
        MovieFormat format,
        LocalDate releaseDate,
        int durationMinutes,
        String genre,
        List<CinemaHallResponse> halls,
        List<ScreeningSlotResponse> screenings,
        boolean ticketsEnabled
) {
}
