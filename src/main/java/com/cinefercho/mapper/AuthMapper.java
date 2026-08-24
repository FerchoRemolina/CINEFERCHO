package com.cinefercho.mapper;

import com.cinefercho.dto.AuthResponse;
import com.cinefercho.dto.UserResponse;
import com.cinefercho.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getNationalId(),
                user.getPhone(),
                user.getRole(),
                user.getMembershipType(),
                user.getMembershipExpiresAt(),
                user.hasActiveMembership());
    }

    public AuthResponse toResponse(String token, User user, Instant sessionExpiresAt) {
        UserResponse profile = toUserResponse(user);
        return new AuthResponse(
                token,
                profile.id(),
                profile.fullName(),
                profile.email(),
                profile.nationalId(),
                profile.phone(),
                profile.role(),
                profile.membershipType(),
                profile.membershipExpiresAt(),
                profile.membershipActive(),
                sessionExpiresAt);
    }
}
