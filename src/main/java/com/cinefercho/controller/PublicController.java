package com.cinefercho.controller;

import com.cinefercho.dto.CityResponse;
import com.cinefercho.dto.MembershipPlanResponse;
import com.cinefercho.dto.MovieCatalogResponse;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.dto.SeatMapResponse;
import com.cinefercho.dto.TheaterResponse;
import com.cinefercho.entity.enums.MovieStatus;
import com.cinefercho.service.CatalogService;
import com.cinefercho.service.ScreeningService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private final CatalogService catalogService;
    private final ScreeningService screeningService;

    public PublicController(CatalogService catalogService, ScreeningService screeningService) {
        this.catalogService = catalogService;
        this.screeningService = screeningService;
    }

    @GetMapping("/cities")
    public List<CityResponse> cities() {
        return catalogService.findCities();
    }

    @GetMapping("/cities/{cityId}/theaters")
    public List<TheaterResponse> theaters(@PathVariable Long cityId) {
        return catalogService.findTheatersByCity(cityId);
    }

    @GetMapping("/movies")
    public List<MovieResponse> movies(@RequestParam(required = false) MovieStatus status) {
        return catalogService.findMovies(status);
    }

    @GetMapping("/movies/now-showing")
    public List<MovieCatalogResponse> nowShowing(@RequestParam(required = false) Long theaterId) {
        return catalogService.findNowShowing(theaterId);
    }

    @GetMapping("/movies/presale")
    public List<MovieCatalogResponse> presale(@RequestParam(required = false) Long theaterId) {
        return catalogService.findPresale(theaterId);
    }

    @GetMapping("/movies/upcoming")
    public List<MovieCatalogResponse> upcoming(@RequestParam(required = false) Long theaterId) {
        return catalogService.findUpcoming(theaterId);
    }

    @GetMapping("/screenings")
    public List<ScreeningResponse> screenings(
            @RequestParam Long movieId,
            @RequestParam Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return screeningService.findByMovieTheaterAndDate(movieId, theaterId, date);
    }

    @GetMapping("/screenings/{screeningId}/seats")
    public SeatMapResponse seatMap(@PathVariable Long screeningId) {
        return screeningService.getSeatMap(screeningId);
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return catalogService.findProducts();
    }

    @GetMapping("/memberships")
    public List<MembershipPlanResponse> memberships() {
        return catalogService.findMembershipPlans();
    }
}
