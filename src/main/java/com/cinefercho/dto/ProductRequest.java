package com.cinefercho.dto;

import com.cinefercho.entity.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull ProductCategory category,
        @Min(0) int stock,
        @Size(max = 500) String imageUrl
) {
}
