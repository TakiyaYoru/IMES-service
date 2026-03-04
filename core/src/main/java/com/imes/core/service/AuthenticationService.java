package com.imes.core.service;

import com.imes.common.dto.LoginRequest;
import com.imes.common.dto.LoginResponse;
import com.imes.infra.entity.RefreshTokenEntity;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration:3600}") // Default: 1 hour in seconds
    private long jwtExpiration;

    public LoginResponse login(LoginRequest request) {
        return login(request, null, null);
    }

    public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        log.info("Authenticating user: {}", request.getEmail());
        
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        log.info("User found, checking password...");
        
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("Password match result: {}", authenticated);
        
        if (!authenticated) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadCredentialsException("User account is inactive");
        }

        // Generate access token (JWT) with userId
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // Generate refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.generateRefreshToken(
                user.getId(), 
                deviceInfo != null ? deviceInfo : "Unknown Device",
                ipAddress != null ? ipAddress : "Unknown IP"
        );

        return LoginResponse.builder()
                .id(user.getId())
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .build();
    }
}
