package com.cinefercho.service;

import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.dto.SeatMapResponse;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Seat;
import com.cinefercho.entity.enums.CatalogClass;
import com.cinefercho.exception.ResourceNotFoundException;
import com.cinefercho.mapper.CatalogMapper;
import com.cinefercho.mapper.SeatMapMapper;
import com.cinefercho.repository.MovieRepository;
import com.cinefercho.repository.ScreeningRepository;
import com.cinefercho.repository.SeatRepository;
import com.cinefercho.repository.TheaterRepository;
import com.cinefercho.repository.TicketItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final TicketItemRepository ticketItemRepository;
    private final CatalogMapper catalogMapper;
    private final SeatMapMapper seatMapMapper;
    private final MovieCatalogPolicy movieCatalogPolicy;

    public ScreeningService(
            ScreeningRepository screeningRepository,
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            SeatRepository seatRepository,
            TicketItemRepository ticketItemRepository,
            CatalogMapper catalogMapper,
            SeatMapMapper seatMapMapper,
            MovieCatalogPolicy movieCatalogPolicy) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.seatRepository = seatRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.catalogMapper = catalogMapper;
        this.seatMapMapper = seatMapMapper;
        this.movieCatalogPolicy = movieCatalogPolicy;
    }

    public List<ScreeningResponse> findByMovieTheaterAndDate(Long movieId, Long theaterId, LocalDate date) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la película con id " + movieId));
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("No existe el teatro con id " + theaterId);
        }
        CatalogClass catalogClass = movieCatalogPolicy.classify(movie);
        if (catalogClass == CatalogClass.UPCOMING) {
            return List.of();
        }
        List<Screening> screenings = screeningRepository.findByMovieTheaterAndDate(movieId, theaterId, date);
        if (catalogClass == CatalogClass.PRESALE) {
            LocalDate premiere = movie.getReleaseDate();
            screenings = screenings.stream()
                    .filter(screening -> screening.getStartTime().toLocalDate().equals(premiere))
                    .toList();
        }
        return catalogMapper.toScreeningResponses(screenings);
    }

    public SeatMapResponse getSeatMap(Long screeningId) {
        Screening screening = screeningRepository.findDetailedById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la función con id " + screeningId));
        movieCatalogPolicy.assertPurchasable(screening);
        List<Seat> seats = seatRepository.findByHall_IdOrderByRowLetterAscSeatNumberAsc(screening.getHall().getId());
        Set<Long> occupiedSeatIds = ticketItemRepository.findOccupiedSeatIdsByScreeningId(screeningId);
        if (occupiedSeatIds == null) {
            occupiedSeatIds = Set.of();
        }
        return seatMapMapper.toResponse(screening, seats, occupiedSeatIds);
    }
}
