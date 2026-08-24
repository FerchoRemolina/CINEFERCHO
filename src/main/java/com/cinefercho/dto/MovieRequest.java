package com.cinefercho.dto;

import com.cinefercho.entity.enums.MovieFormat;
import com.cinefercho.entity.enums.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MovieRequest(
        @Size(max = 500) String posterUrl,
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotBlank @Size(max = 20) String ageRating,
        @NotNull MovieFormat format,
        @NotNull LocalDate releaseDate,
        @Min(1) int durationMinutes,
        @Size(max = 80) String genre,
        MovieStatus status
) {
}
