package com.pharmacy.smart_pharmacy.controller;

import com.pharmacy.smart_pharmacy.dto.request.LoginRequest;
import com.pharmacy.smart_pharmacy.dto.request.RegisterRequest;
import com.pharmacy.smart_pharmacy.dto.response.ApiResponse;
import com.pharmacy.smart_pharmacy.dto.response.AuthResponse;
import com.pharmacy.smart_pharmacy.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }
}