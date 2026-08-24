package com.cinefercho.mapper;

import com.cinefercho.dto.CinemaHallResponse;
import com.cinefercho.dto.CityResponse;
import com.cinefercho.dto.MembershipPlanResponse;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.dto.TheaterResponse;
import com.cinefercho.entity.CinemaHall;
import com.cinefercho.entity.City;
import com.cinefercho.entity.MembershipPlan;
import com.cinefercho.entity.Movie;
import com.cinefercho.entity.Product;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Theater;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogMapper {

    public CityResponse toResponse(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getDepartment());
    }

    public List<CityResponse> toCityResponses(List<City> cities) {
        return cities.stream().map(this::toResponse).toList();
    }

    public TheaterResponse toResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getAddress(),
                theater.isActive(),
                toResponse(theater.getCity()));
    }

    public List<TheaterResponse> toTheaterResponses(List<Theater> theaters) {
        return theaters.stream().map(this::toResponse).toList();
    }

    public MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getSynopsis(),
                movie.getDurationMinutes(),
                movie.getGenre(),
                movie.getRating(),
                movie.getPosterUrl(),
                movie.getStatus());
    }

    public List<MovieResponse> toMovieResponses(List<Movie> movies) {
        return movies.stream().map(this::toResponse).toList();
    }

    public ScreeningResponse toResponse(Screening screening) {
        CinemaHall hall = screening.getHall();
        Theater theater = hall.getTheater();
        return new ScreeningResponse(
                screening.getId(),
                toResponse(screening.getMovie()),
                theater.getId(),
                theater.getName(),
                hall.getId(),
                hall.getName(),
                hall.getHallType(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getTicketPrice(),
                screening.getFormat());
    }

    public List<ScreeningResponse> toScreeningResponses(List<Screening> screenings) {
        return screenings.stream().map(this::toResponse).toList();
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStock(),
                product.getImageUrl());
    }

    public List<ProductResponse> toProductResponses(List<Product> products) {
        return products.stream().map(this::toResponse).toList();
    }

    public MembershipPlanResponse toResponse(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMonthlyPrice(),
                plan.getDiscountPercentageTickets(),
                plan.getDiscountPercentageConcession());
    }

    public List<MembershipPlanResponse> toMembershipPlanResponses(List<MembershipPlan> plans) {
        return plans.stream().map(this::toResponse).toList();
    }

    public CinemaHallResponse toHallResponse(CinemaHall hall) {
        Theater theater = hall.getTheater();
        return new CinemaHallResponse(
                hall.getId(),
                theater.getId(),
                theater.getName(),
                hall.getName(),
                hall.getHallType(),
                hall.getTotalCapacity(),
                hall.getTotalRows(),
                hall.getTotalColumns());
    }
}
