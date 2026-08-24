package com.cinefercho.service;

import com.cinefercho.dto.AuthResponse;
import com.cinefercho.dto.LoginRequest;
import com.cinefercho.dto.RegisterRequest;
import com.cinefercho.entity.User;
import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.UserRole;
import com.cinefercho.exception.UnauthorizedException;
import com.cinefercho.mapper.AuthMapper;
import com.cinefercho.repository.UserRepository;
import com.cinefercho.security.JwtTokenProvider;
import com.cinefercho.security.UserPrincipal;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado.");
        }
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_CLIENT)
                .membershipType(MembershipType.NONE)
                .build();
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved);
        return authMapper.toResponse(token, saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas."));
            String token = jwtTokenProvider.generateToken(principal);
            return authMapper.toResponse(token, user);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Credenciales inválidas.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
