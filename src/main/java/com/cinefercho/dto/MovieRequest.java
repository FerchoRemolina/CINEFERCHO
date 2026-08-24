package com.cinefercho.dto;

import com.cinefercho.entity.enums.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovieRequest(
        @NotBlank @Size(max = 200) String title,
        String synopsis,
        @Min(1) int durationMinutes,
        @Size(max = 80) String genre,
        @Size(max = 20) String rating,
        @Size(max = 500) String posterUrl,
        @NotNull MovieStatus status
) {
}
