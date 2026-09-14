package com.cinefercho.service;

import com.cinefercho.config.TmdbProperties;
import com.cinefercho.dto.TmdbMovieSearchResultDto;
import com.cinefercho.dto.TmdbSearchResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class TmdbService {

    private static final Logger log = LoggerFactory.getLogger(TmdbService.class);
    private static final String POSTER_BASE = "https://image.tmdb.org/t/p/w500";

    private final TmdbProperties properties;
    private final RestClient restClient;

    public TmdbService(TmdbProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        String baseUrl = StringUtils.hasText(properties.url())
                ? properties.url()
                : "https://api.themoviedb.org/3";
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public TmdbSearchResponse search(String query) {
        if (!properties.enabled()) {
            return new TmdbSearchResponse(false, List.of());
        }
        if (!StringUtils.hasText(query)) {
            return new TmdbSearchResponse(true, List.of());
        }
        try {
            TmdbPage page = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("api_key", properties.key())
                            .queryParam("language", "es-CO")
                            .queryParam("query", query.trim())
                            .queryParam("include_adult", "false")
                            .build())
                    .retrieve()
                    .body(TmdbPage.class);
            List<TmdbMovieSearchResultDto> results = page == null || page.results() == null
                    ? List.of()
                    : page.results().stream().map(this::toDto).toList();
            return new TmdbSearchResponse(true, results);
        } catch (RestClientException ex) {
            log.warn("No se pudo consultar TMDB: {}", ex.getMessage());
            return new TmdbSearchResponse(true, List.of());
        }
    }

    private TmdbMovieSearchResultDto toDto(TmdbItem item) {
        String posterUrl = StringUtils.hasText(item.posterPath())
                ? POSTER_BASE + item.posterPath()
                : null;
        return new TmdbMovieSearchResultDto(
                item.id(),
                item.title(),
                item.overview(),
                posterUrl,
                item.releaseDate(),
                item.voteAverage());
    }

    private record TmdbPage(List<TmdbItem> results) {
    }

    private record TmdbItem(
            Long id,
            String title,
            String overview,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("vote_average") Double voteAverage
    ) {
    }
}
