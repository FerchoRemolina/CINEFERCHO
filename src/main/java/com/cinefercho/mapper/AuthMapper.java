package com.cinefercho.mapper;

import com.cinefercho.dto.AuthResponse;
import com.cinefercho.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getMembershipType());
    }
}
