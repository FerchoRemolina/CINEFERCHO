package com.cinefercho.service;

import com.cinefercho.entity.Movie;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.enums.CatalogClass;
import com.cinefercho.repository.ScreeningRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class MovieCatalogPolicy {

    public static final int PRESALE_WINDOW_DAYS = 15;

    private final ScreeningRepository screeningRepository;

    public MovieCatalogPolicy(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    public boolean hasBeenProjected(Long movieId) {
        return screeningRepository.existsByMovie_IdAndStartTimeBefore(movieId, LocalDateTime.now());
    }

    public CatalogClass classify(Movie movie) {
        if (hasBeenProjected(movie.getId())) {
            return CatalogClass.NOW_SHOWING;
        }
        LocalDate today = LocalDate.now();
        LocalDate release = movie.getReleaseDate();
        if (release != null
                && !release.isBefore(today)
                && !release.isAfter(today.plusDays(PRESALE_WINDOW_DAYS))) {
            return CatalogClass.PRESALE;
        }
        return CatalogClass.UPCOMING;
    }

    public boolean ticketsEnabled(Movie movie) {
        CatalogClass catalogClass = classify(movie);
        return catalogClass == CatalogClass.NOW_SHOWING || catalogClass == CatalogClass.PRESALE;
    }

    public void assertPurchasable(Screening screening) {
        Movie movie = screening.getMovie();
        CatalogClass catalogClass = classify(movie);
        if (catalogClass == CatalogClass.UPCOMING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta película aún no está disponible para la venta de boletos.");
        }
        if (catalogClass == CatalogClass.PRESALE) {
            LocalDate premiere = movie.getReleaseDate();
            if (premiere == null || !screening.getStartTime().toLocalDate().equals(premiere)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "En preventa solo puedes comprar funciones del día de estreno.");
            }
        }
    }
}
