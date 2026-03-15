package com.imes.gateway.filter;

import com.imes.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 * Validates JWT tokens and injects user information headers for downstream services
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/refresh",
            "/eureka",
            // Health / actuator endpoints — always public
            "/auth/health",
            "/assignments/health",
            "/attendances/health",
            "/interns/health",
            "/users/health",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("[GATEWAY] Processing request: {} {}", request.getMethod(), path);

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("[GATEWAY] Public endpoint, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[GATEWAY] Missing or invalid Authorization header for: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("[GATEWAY] Invalid or expired token for: {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information
            Long userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if (userId == null) {
                log.warn("[GATEWAY] Token missing userId claim for: {}", path);
                return onError(exchange, "Token missing userId claim", HttpStatus.UNAUTHORIZED);
            }

            log.debug("[GATEWAY] Authenticated user: userId={}, username={}, role={}", userId, username, role);

            // Inject headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            log.debug("[GATEWAY] Injected headers: X-User-Id={}, X-Username={}, X-User-Role={}", userId, username, role);

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            log.error("[GATEWAY] JWT validation error for path {}: {}", path, e.getMessage());
            return onError(exchange, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Return error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.warn("[GATEWAY] Returning error: {} - {}", status, message);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // High priority - run before other filters
    }
}
