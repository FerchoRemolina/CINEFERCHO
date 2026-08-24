package com.cinefercho.dto;

import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String nationalId,
        String phone,
        UserRole role,
        MembershipType membershipType,
        Instant membershipExpiresAt,
        boolean membershipActive
) {
}
