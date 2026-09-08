package com.bankguard.banking.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.bankguard.banking.dto.request.LoginRequest;
import com.bankguard.banking.dto.request.RegisterRequest;
import com.bankguard.banking.dto.response.LoginResponse;
import com.bankguard.banking.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader) {

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Bearer token is required");
        }

        String token =
                authHeader.substring(7);

        authService.logout(token);

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }
}