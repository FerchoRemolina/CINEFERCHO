package com.cinefercho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank
        @Pattern(regexp = "\\d{6,10}", message = "La cédula debe tener entre 6 y 10 dígitos")
        String nationalId,
        @Size(max = 20)
        @Pattern(regexp = "^$|^\\d{7,10}$", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String phone
) {
}
