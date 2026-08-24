package com.cinefercho.dto;

import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.UserRole;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        UserRole role,
        MembershipType membershipType
) {
}
