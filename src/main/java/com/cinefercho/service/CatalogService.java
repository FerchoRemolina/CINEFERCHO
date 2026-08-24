package com.cinefercho.service;

import com.cinefercho.dto.CityResponse;
import com.cinefercho.dto.MembershipPlanResponse;
import com.cinefercho.dto.MovieCatalogResponse;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.ScreeningSlotResponse;
import com.cinefercho.dto.TheaterResponse;
import com.cinefercho.entity.Movie;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.enums.CatalogClass;
import com.cinefercho.entity.enums.MovieStatus;
import com.cinefercho.exception.ResourceNotFoundException;
import com.cinefercho.mapper.CatalogMapper;
import com.cinefercho.repository.CityRepository;
import com.cinefercho.repository.MembershipPlanRepository;
import com.cinefercho.repository.MovieRepository;
import com.cinefercho.repository.ProductRepository;
import com.cinefercho.repository.ScreeningRepository;
import com.cinefercho.repository.TheaterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final ProductRepository productRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final CatalogMapper catalogMapper;
    private final MovieCatalogPolicy movieCatalogPolicy;

    public CatalogService(
            CityRepository cityRepository,
            TheaterRepository theaterRepository,
            MovieRepository movieRepository,
            ScreeningRepository screeningRepository,
            ProductRepository productRepository,
            MembershipPlanRepository membershipPlanRepository,
            CatalogMapper catalogMapper,
            MovieCatalogPolicy movieCatalogPolicy) {
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.productRepository = productRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.catalogMapper = catalogMapper;
        this.movieCatalogPolicy = movieCatalogPolicy;
    }

    public List<CityResponse> findCities() {
        return catalogMapper.toCityResponses(cityRepository.findAll());
    }

    public List<TheaterResponse> findTheatersByCity(Long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("No existe la ciudad con id " + cityId);
        }
        return catalogMapper.toTheaterResponses(theaterRepository.findActiveByCityId(cityId));
    }

    public List<MovieResponse> findMovies(MovieStatus status) {
        List<Movie> movies = status == null
                ? movieRepository.findAll()
                : movieRepository.findByStatus(status);
        return movies.stream().map(this::toMovieResponse).toList();
    }

    public List<MovieCatalogResponse> findNowShowing(Long theaterId) {
        if (theaterId == null) {
            return List.of();
        }
        requireTheater(theaterId);
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.findNowShowingAtTheater(now, theaterId).stream()
                .map(movie -> toCatalogResponse(movie, CatalogClass.NOW_SHOWING, now, theaterId))
                .toList();
    }

    public List<MovieCatalogResponse> findPresale(Long theaterId) {
        if (theaterId == null) {
            return List.of();
        }
        requireTheater(theaterId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        return movieRepository
                .findPresaleAtTheater(today, today.plusDays(MovieCatalogPolicy.PRESALE_WINDOW_DAYS), now, theaterId)
                .stream()
                .map(movie -> toCatalogResponse(movie, CatalogClass.PRESALE, now, theaterId))
                .filter(movie -> !movie.screenings().isEmpty())
                .toList();
    }

    public List<MovieCatalogResponse> findUpcoming(Long theaterId) {
        if (theaterId == null) {
            return List.of();
        }
        requireTheater(theaterId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate limit = now.toLocalDate().plusDays(MovieCatalogPolicy.PRESALE_WINDOW_DAYS);
        return movieRepository.findUpcomingAtTheater(limit, now, theaterId).stream()
                .map(movie -> toCatalogResponse(movie, CatalogClass.UPCOMING, now, theaterId))
                .toList();
    }

    public List<ProductResponse> findProducts() {
        return catalogMapper.toProductResponses(productRepository.findAll());
    }

    public List<MembershipPlanResponse> findMembershipPlans() {
        return catalogMapper.toMembershipPlanResponses(membershipPlanRepository.findAll());
    }

    private MovieResponse toMovieResponse(Movie movie) {
        List<Screening> screenings = screeningRepository.findByMovieIdWithHall(movie.getId());
        return catalogMapper.toResponse(movie, catalogMapper.toDistinctHallResponses(screenings));
    }

    private MovieCatalogResponse toCatalogResponse(
            Movie movie, CatalogClass catalogClass, LocalDateTime now, Long theaterId) {
        List<Screening> allScreenings = screeningRepository.findByMovieIdAndTheaterIdWithHall(movie.getId(), theaterId);
        List<Screening> visible = switch (catalogClass) {
            case NOW_SHOWING -> allScreenings.stream()
                    .filter(screening -> !screening.getStartTime().isBefore(now))
                    .toList();
            case PRESALE -> allScreenings.stream()
                    .filter(screening -> screening.getStartTime().toLocalDate().equals(movie.getReleaseDate()))
                    .toList();
            case UPCOMING -> List.of();
        };
        List<ScreeningSlotResponse> slots = visible.stream().map(catalogMapper::toSlotResponse).toList();
        boolean ticketsEnabled = movieCatalogPolicy.ticketsEnabled(movie);
        return catalogMapper.toCatalogResponse(
                movie,
                catalogMapper.toDistinctHallResponses(allScreenings),
                slots,
                ticketsEnabled);
    }

    private void requireTheater(Long theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("No existe el teatro con id " + theaterId);
        }
    }
}
