package com.cinefercho.dto;

public record TheaterResponse(
        Long id,
        String name,
        String address,
        boolean active,
        CityResponse city
) {
}
