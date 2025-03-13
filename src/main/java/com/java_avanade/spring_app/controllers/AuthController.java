package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.AuthDTO;
import com.java_avanade.spring_app.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<AuthDTO.JwtResponse> authenticateUser(@Valid @RequestBody AuthDTO.LoginRequest loginRequest) {
        AuthDTO.JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Registra um novo usuário como cliente ou afiliado (requer autenticação como ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthDTO.RegisterResponse> registerUser(@Valid @RequestBody AuthDTO.RegisterRequest registerRequest) {
        AuthDTO.RegisterResponse registerResponse = authService.registerUser(registerRequest);
        return ResponseEntity.ok(registerResponse);
    }
}