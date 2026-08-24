package com.cinefercho.service;

import com.cinefercho.dto.CityResponse;
import com.cinefercho.dto.MembershipPlanResponse;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.TheaterResponse;
import com.cinefercho.entity.enums.MovieStatus;
import com.cinefercho.exception.ResourceNotFoundException;
import com.cinefercho.mapper.CatalogMapper;
import com.cinefercho.repository.CityRepository;
import com.cinefercho.repository.MembershipPlanRepository;
import com.cinefercho.repository.MovieRepository;
import com.cinefercho.repository.ProductRepository;
import com.cinefercho.repository.TheaterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final ProductRepository productRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final CatalogMapper catalogMapper;

    public CatalogService(
            CityRepository cityRepository,
            TheaterRepository theaterRepository,
            MovieRepository movieRepository,
            ProductRepository productRepository,
            MembershipPlanRepository membershipPlanRepository,
            CatalogMapper catalogMapper) {
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
        this.productRepository = productRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.catalogMapper = catalogMapper;
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
        if (status == null) {
            return catalogMapper.toMovieResponses(movieRepository.findAll());
        }
        return catalogMapper.toMovieResponses(movieRepository.findByStatus(status));
    }

    public List<ProductResponse> findProducts() {
        return catalogMapper.toProductResponses(productRepository.findAll());
    }

    public List<MembershipPlanResponse> findMembershipPlans() {
        return catalogMapper.toMembershipPlanResponses(membershipPlanRepository.findAll());
    }
}
