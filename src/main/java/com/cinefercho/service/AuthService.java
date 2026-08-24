package com.cinefercho.service;

import com.cinefercho.dto.AuthResponse;
import com.cinefercho.dto.LoginRequest;
import com.cinefercho.dto.RegisterRequest;
import com.cinefercho.dto.UpdateProfileRequest;
import com.cinefercho.dto.UserResponse;
import com.cinefercho.entity.User;
import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.UserRole;
import com.cinefercho.exception.UnauthorizedException;
import com.cinefercho.mapper.AuthMapper;
import com.cinefercho.repository.UserRepository;
import com.cinefercho.security.JwtTokenProvider;
import com.cinefercho.security.UserPrincipal;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

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
        String nationalId = normalizeNationalId(request.nationalId());
        if (userRepository.existsByNationalId(nationalId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El número de cédula ya se encuentra registrado");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado.");
        }
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .nationalId(nationalId)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_CLIENT)
                .membershipType(MembershipType.NONE)
                .build();
        try {
            User saved = userRepository.saveAndFlush(user);
            Instant sessionExpiresAt = jwtTokenProvider.issueExpiresAt(saved.getRole());
            String token = jwtTokenProvider.generateToken(saved, sessionExpiresAt);
            return authMapper.toResponse(token, saved, sessionExpiresAt);
        } catch (DataIntegrityViolationException ex) {
            throw duplicateIdentityException(ex);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas."));
            expireMembershipIfNeeded(user);
            Instant sessionExpiresAt = jwtTokenProvider.issueExpiresAt(user.getRole());
            String token = jwtTokenProvider.generateToken(principal, sessionExpiresAt);
            return authMapper.toResponse(token, user, sessionExpiresAt);
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Credenciales inválidas.");
        }
    }

    @Transactional
    public UserResponse me(UserPrincipal principal) {
        User user = requireUser(principal);
        expireMembershipIfNeeded(user);
        return authMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = requireUser(principal);
        String nationalId = normalizeNationalId(request.nationalId());
        if (userRepository.existsByNationalIdAndIdNot(nationalId, user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El número de cédula ya se encuentra registrado");
        }
        user.setFullName(request.fullName().trim());
        user.setNationalId(nationalId);
        user.setPhone(normalizePhone(request.phone()));
        expireMembershipIfNeeded(user);
        try {
            return authMapper.toUserResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateIdentityException(ex);
        }
    }

    private void expireMembershipIfNeeded(User user) {
        if (user.expireIfNeeded()) {
            userRepository.save(user);
        }
    }

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Se requiere autenticación.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("El usuario autenticado ya no existe."));
    }

    private ResponseStatusException duplicateIdentityException(DataIntegrityViolationException ex) {
        String details = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase();
        if (details.contains("national")) {
            return new ResponseStatusException(
                    HttpStatus.CONFLICT, "El número de cédula ya se encuentra registrado");
        }
        return new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado.");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeNationalId(String nationalId) {
        return nationalId.replaceAll("\\D", "");
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
