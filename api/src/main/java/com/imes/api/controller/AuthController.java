package com.imes.api.controller;

import com.imes.common.dto.LoginRequest;
import com.imes.common.dto.LoginResponse;
import com.imes.common.dto.ResponseApi;
import com.imes.core.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse response = authenticationService.login(request);
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(ResponseApi.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseApi<Void>> logout() {
        // Client-side should remove token
        return ResponseEntity.ok(ResponseApi.success());
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseApi<String>> getCurrentUser() {
        return ResponseEntity.ok(ResponseApi.success("Authenticated user"));
    }
}
