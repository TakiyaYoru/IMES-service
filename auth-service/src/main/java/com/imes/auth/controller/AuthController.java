package com.imes.auth.controller;

import com.imes.common.dto.*;
import com.imes.core.service.AuthenticationService;
import com.imes.core.service.JwtService;
import com.imes.core.service.RefreshTokenService;
import com.imes.core.service.PasswordResetService;
import com.imes.infra.entity.RefreshTokenEntity;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication REST Controller
 * Base path: /auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("[AUTH-SERVICE] Login request received for email: {}", request.getEmail());
        
        // Extract device info and IP address
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        
        LoginResponse response = authenticationService.login(request, deviceInfo, ipAddress);
        
        log.info("[AUTH-SERVICE] Login successful for email: {}, role: {}", 
                request.getEmail(), response.getRole());
        
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseApi<RefreshTokenResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("[AUTH-SERVICE] Refresh token request received");
        
        try {
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);
            
            // Rotate refresh token (security best practice)
            RefreshTokenEntity newRefreshToken = refreshTokenService.rotateRefreshToken(
                    request.getRefreshToken(), 
                    deviceInfo, 
                    ipAddress
            );
            
            // Get user info from old token's userId
            // Generate new access token
            String email = getUserEmailFromRefreshToken(newRefreshToken);
            String role = getUserRoleFromRefreshToken(newRefreshToken);
            String newAccessToken = jwtService.generateToken(email, role);
            
            RefreshTokenResponse response = RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour
                    .build();
            
            log.info("[AUTH-SERVICE] Token refreshed successfully");
            return ResponseEntity.ok(ResponseApi.success(response));
            
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ResponseApi.error("1001", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<Void>> logout(@RequestBody LogoutRequest request) {
        log.info("[AUTH-SERVICE] Logout request received");
        
        try {
            refreshTokenService.revokeToken(request.getRefreshToken());
            log.info("[AUTH-SERVICE] Logout successful");
            return ResponseEntity.ok(ResponseApi.success());
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Logout failed: {}", e.getMessage());
            return ResponseEntity.ok(ResponseApi.success()); // Still return success even if token not found
        }
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ResponseApi<Void>> logoutAll(
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("[AUTH-SERVICE] Logout all devices request for user: {}", userId);
        
        int revokedCount = refreshTokenService.revokeAllUserTokens(userId);
        
        log.info("[AUTH-SERVICE] Logged out from {} devices", revokedCount);
        return ResponseEntity.ok(ResponseApi.success());
    }

    @GetMapping("/sessions")
    public ResponseEntity<ResponseApi<List<SessionInfoResponse>>> getSessions(
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestParam(required = false) String currentToken
    ) {
        log.info("[AUTH-SERVICE] Get sessions request for user: {}", userId);
        
        List<RefreshTokenEntity> sessions = refreshTokenService.getActiveSessions(userId);
        
        List<SessionInfoResponse> sessionInfos = sessions.stream()
                .map(session -> SessionInfoResponse.builder()
                        .id(session.getId())
                        .deviceInfo(session.getDeviceInfo())
                        .ipAddress(session.getIpAddress())
                        .createdAt(session.getCreatedAt())
                        .expiresAt(session.getExpiresAt())
                        .isCurrentSession(session.getToken().equals(currentToken))
                        .build())
                .collect(Collectors.toList());
        
        log.info("[AUTH-SERVICE] Found {} active sessions", sessionInfos.size());
        return ResponseEntity.ok(ResponseApi.success(sessionInfos));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ResponseApi<Void>> revokeSession(
            @PathVariable Long sessionId,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("[AUTH-SERVICE] Revoke session {} for user: {}", sessionId, userId);
        
        try {
            refreshTokenService.revokeSession(sessionId, userId);
            log.info("[AUTH-SERVICE] Session revoked successfully");
            return ResponseEntity.ok(ResponseApi.success());
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Revoke session failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseApi.error("1002", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseApi<String>> getCurrentUser() {
        log.info("[AUTH-SERVICE] Get current user request");
        return ResponseEntity.ok(ResponseApi.success("Authenticated user"));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ResponseApi<String>> health() {
        return ResponseEntity.ok(ResponseApi.success("Auth Service is running"));
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Get user email from refresh token's user ID
     */
    private String getUserEmailFromRefreshToken(RefreshTokenEntity refreshToken) {
        UserEntity user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getEmail();
    }

    /**
     * Get user role from refresh token's user ID
     */
    private String getUserRoleFromRefreshToken(RefreshTokenEntity refreshToken) {
        UserEntity user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole().name();
    }

    /**
     * Forgot password - Send reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseApi<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        log.info("[AUTH-SERVICE] Forgot password request for email: {}", request.getEmail());
        
        try {
            passwordResetService.initiateForgotPassword(request.getEmail());
            log.info("[AUTH-SERVICE] Password reset email sent to: {}", request.getEmail());
            return ResponseEntity.ok(ResponseApi.success());
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Forgot password failed: {}", e.getMessage());
            // Return success even if user not found for security (don't reveal if email exists)
            return ResponseEntity.ok(ResponseApi.success());
        }
    }

    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseApi<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("[AUTH-SERVICE] Reset password request with token");
        
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("[AUTH-SERVICE] Password reset successful");
            return ResponseEntity.ok(ResponseApi.success());
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseApi.error("1001", e.getMessage()));
        }
    }

    /**
     * Change password for authenticated user
     */
    @PostMapping("/change-password")
    public ResponseEntity<ResponseApi<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "X-User-Id") Long userId
    ) {
        log.info("[AUTH-SERVICE] Change password request for user: {}", userId);
        
        try {
            passwordResetService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            log.info("[AUTH-SERVICE] Password changed successfully for user: {}", userId);
            return ResponseEntity.ok(ResponseApi.success());
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ResponseApi.error("1001", e.getMessage()));
        }
    }
}
