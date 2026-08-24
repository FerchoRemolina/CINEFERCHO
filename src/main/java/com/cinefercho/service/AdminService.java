package com.cinefercho.service;

import com.cinefercho.dto.CinemaHallRequest;
import com.cinefercho.dto.CinemaHallResponse;
import com.cinefercho.dto.MovieRequest;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductRequest;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.ScreeningRequest;
import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.entity.CinemaHall;
import com.cinefercho.entity.Movie;
import com.cinefercho.entity.Product;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Theater;
import com.cinefercho.exception.ResourceNotFoundException;
import com.cinefercho.mapper.CatalogMapper;
import com.cinefercho.repository.CinemaHallRepository;
import com.cinefercho.repository.ConcessionItemRepository;
import com.cinefercho.repository.MovieRepository;
import com.cinefercho.repository.ProductRepository;
import com.cinefercho.repository.ScreeningRepository;
import com.cinefercho.repository.TheaterRepository;
import com.cinefercho.repository.TicketItemRepository;
import com.cinefercho.util.SeatFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final TheaterRepository theaterRepository;
    private final ProductRepository productRepository;
    private final TicketItemRepository ticketItemRepository;
    private final ConcessionItemRepository concessionItemRepository;
    private final CatalogMapper catalogMapper;

    public AdminService(
            MovieRepository movieRepository,
            ScreeningRepository screeningRepository,
            CinemaHallRepository cinemaHallRepository,
            TheaterRepository theaterRepository,
            ProductRepository productRepository,
            TicketItemRepository ticketItemRepository,
            ConcessionItemRepository concessionItemRepository,
            CatalogMapper catalogMapper) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.theaterRepository = theaterRepository;
        this.productRepository = productRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.concessionItemRepository = concessionItemRepository;
        this.catalogMapper = catalogMapper;
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.title())
                .synopsis(request.synopsis())
                .durationMinutes(request.durationMinutes())
                .genre(request.genre())
                .rating(request.rating())
                .posterUrl(request.posterUrl())
                .status(request.status())
                .build();
        return catalogMapper.toResponse(movieRepository.save(movie));
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la película con id " + id));
        movie.setTitle(request.title());
        movie.setSynopsis(request.synopsis());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setGenre(request.genre());
        movie.setRating(request.rating());
        movie.setPosterUrl(request.posterUrl());
        movie.setStatus(request.status());
        return catalogMapper.toResponse(movieRepository.save(movie));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe la película con id " + id);
        }
        if (screeningRepository.existsByMovie_Id(id)) {
            throw new IllegalStateException("No se puede eliminar la película porque tiene funciones asociadas.");
        }
        movieRepository.deleteById(id);
    }

    public ScreeningResponse createScreening(ScreeningRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la película con id " + request.movieId()));
        CinemaHall hall = cinemaHallRepository.findDetailedById(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + request.hallId()));
        Screening screening = Screening.builder()
                .movie(movie)
                .hall(hall)
                .startTime(request.startTime())
                .endTime(resolveEndTime(request, movie))
                .ticketPrice(request.ticketPrice())
                .format(request.format())
                .build();
        Screening saved = screeningRepository.save(screening);
        return catalogMapper.toResponse(saved);
    }

    public ScreeningResponse updateScreening(Long id, ScreeningRequest request) {
        Screening screening = screeningRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la función con id " + id));
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la película con id " + request.movieId()));
        CinemaHall hall = cinemaHallRepository.findDetailedById(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + request.hallId()));
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(request.startTime());
        screening.setEndTime(resolveEndTime(request, movie));
        screening.setTicketPrice(request.ticketPrice());
        screening.setFormat(request.format());
        return catalogMapper.toResponse(screeningRepository.save(screening));
    }

    public void deleteScreening(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe la función con id " + id);
        }
        if (ticketItemRepository.existsByScreening_Id(id)) {
            throw new IllegalStateException("No se puede eliminar la función porque ya tiene boletos vendidos.");
        }
        screeningRepository.deleteById(id);
    }

    public CinemaHallResponse createHall(CinemaHallRequest request) {
        Theater theater = theaterRepository.findDetailedById(request.theaterId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el teatro con id " + request.theaterId()));
        CinemaHall hall = CinemaHall.builder()
                .theater(theater)
                .name(request.name())
                .hallType(request.hallType())
                .totalCapacity(request.totalCapacity())
                .totalRows(request.totalRows())
                .totalColumns(request.totalColumns())
                .build();
        SeatFactory.fillHall(hall);
        CinemaHall saved = cinemaHallRepository.save(hall);
        return catalogMapper.toHallResponse(saved);
    }

    public CinemaHallResponse updateHall(Long id, CinemaHallRequest request) {
        CinemaHall hall = cinemaHallRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la sala con id " + id));
        Theater theater = theaterRepository.findDetailedById(request.theaterId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el teatro con id " + request.theaterId()));
        hall.setTheater(theater);
        hall.setName(request.name());
        hall.setHallType(request.hallType());
        hall.setTotalCapacity(request.totalCapacity());
        hall.setTotalRows(request.totalRows());
        hall.setTotalColumns(request.totalColumns());
        return catalogMapper.toHallResponse(cinemaHallRepository.save(hall));
    }

    public void deleteHall(Long id) {
        if (!cinemaHallRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe la sala con id " + id);
        }
        if (screeningRepository.existsByHall_Id(id)) {
            throw new IllegalStateException("No se puede eliminar la sala porque tiene funciones asociadas.");
        }
        cinemaHallRepository.deleteById(id);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = toProduct(new Product(), request);
        return catalogMapper.toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el producto con id " + id));
        toProduct(product, request);
        return catalogMapper.toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe el producto con id " + id);
        }
        if (concessionItemRepository.existsByProduct_Id(id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque aparece en facturas.");
        }
        productRepository.deleteById(id);
    }

    private Product toProduct(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        return product;
    }

    private LocalDateTime resolveEndTime(ScreeningRequest request, Movie movie) {
        if (request.endTime() != null) {
            return request.endTime();
        }
        return request.startTime().plusMinutes(movie.getDurationMinutes());
    }
}
