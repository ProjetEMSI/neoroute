package com.neoroute.logistics.controller.auth;

import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.dto.auth.AuthResponse;
import com.neoroute.logistics.dto.auth.LoginRequest;
import com.neoroute.logistics.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur d'authentification
 * 
 * <p>
 * Gère uniquement la connexion des utilisateurs.
 * La création de comptes est réservée aux administrateurs via /admin/users.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Endpoint public pour la connexion")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        User user = authService.getUserByUsername(request.getUsername());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .build();

        return ResponseEntity.ok(response);
    }
}
