package com.cinefercho.dto;

import com.cinefercho.entity.enums.ProductCategory;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        ProductCategory category,
        int stock,
        String imageUrl
) {
}
