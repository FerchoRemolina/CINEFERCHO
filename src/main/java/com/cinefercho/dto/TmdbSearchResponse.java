package com.cinefercho.dto;

import java.util.List;

public record TmdbSearchResponse(
        boolean enabled,
        List<TmdbMovieSearchResultDto> results
) {
}
