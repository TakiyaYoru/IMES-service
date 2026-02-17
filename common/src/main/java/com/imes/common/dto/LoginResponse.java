package com.imes.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String token; // Access token (JWT)
    private String refreshToken; // Refresh token
    private String email;
    private String fullName;
    private String role;
    private String tokenType; // Bearer
    private Long expiresIn; // Seconds until access token expires
}
