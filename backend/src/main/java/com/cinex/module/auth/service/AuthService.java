package com.cinex.module.auth.service;

import com.cinex.common.exception.BusinessException;
import com.cinex.common.exception.ErrorCode;
import com.cinex.module.auth.dto.AuthResponse;
import com.cinex.module.auth.dto.LoginRequest;
import com.cinex.module.auth.dto.RefreshTokenRequest;
import com.cinex.module.auth.dto.RegisterRequest;
import com.cinex.module.auth.entity.RefreshToken;
import com.cinex.module.auth.entity.User;
import com.cinex.module.auth.repository.UserRepository;
import com.cinex.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_EXISTED, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EXISTED, "Email already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Dùng findActiveByUsername → user đã soft delete không login được
        User user = userRepository.findActiveByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is disabled");
        }

        return buildAuthResponse(user);
    }

    /**
     * Refresh token rotation: tạo access token MỚI + refresh token MỚI.
     * Token cũ bị revoke ngay → nếu bị lộ, attacker không dùng lại được.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = oldToken.getUser();

        // Revoke token cũ + tạo token mới (rotation)
        refreshTokenService.revokeAllUserTokens(user.getId());
        RefreshToken newToken = refreshTokenService.createRefreshToken(user);

        String accessToken = jwtUtil.generateToken(user.getUsername(), Map.of("role", user.getRole().name()));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newToken.getToken())
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getUsername(), Map.of("role", user.getRole().name()));

        refreshTokenService.revokeAllUserTokens(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .build();
    }
}
