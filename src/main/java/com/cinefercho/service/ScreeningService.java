package com.cinefercho.service;

import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.dto.SeatMapResponse;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Seat;
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

    public ScreeningService(
            ScreeningRepository screeningRepository,
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            SeatRepository seatRepository,
            TicketItemRepository ticketItemRepository,
            CatalogMapper catalogMapper,
            SeatMapMapper seatMapMapper) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.seatRepository = seatRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.catalogMapper = catalogMapper;
        this.seatMapMapper = seatMapMapper;
    }

    public List<ScreeningResponse> findByMovieTheaterAndDate(Long movieId, Long theaterId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("No existe la película con id " + movieId);
        }
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("No existe el teatro con id " + theaterId);
        }
        return catalogMapper.toScreeningResponses(
                screeningRepository.findByMovieTheaterAndDate(movieId, theaterId, date));
    }

    public SeatMapResponse getSeatMap(Long screeningId) {
        Screening screening = screeningRepository.findDetailedById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la función con id " + screeningId));
        List<Seat> seats = seatRepository.findByHall_IdOrderByRowLetterAscSeatNumberAsc(screening.getHall().getId());
        Set<Long> occupiedSeatIds = ticketItemRepository.findOccupiedSeatIdsByScreeningId(screeningId);
        if (occupiedSeatIds == null) {
            occupiedSeatIds = Set.of();
        }
        return seatMapMapper.toResponse(screening, seats, occupiedSeatIds);
    }
}
