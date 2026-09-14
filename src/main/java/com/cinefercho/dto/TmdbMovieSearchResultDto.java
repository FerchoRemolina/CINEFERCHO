package com.cinefercho.dto;

public record TmdbMovieSearchResultDto(
        Long id,
        String title,
        String overview,
        String posterUrl,
        String releaseDate,
        Double voteAverage
) {
}
