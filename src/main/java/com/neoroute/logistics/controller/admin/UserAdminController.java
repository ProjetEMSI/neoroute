package com.neoroute.logistics.controller.admin;

import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.dto.user.CreateUserRequest;
import com.neoroute.logistics.dto.user.UpdateUserRequest;
import com.neoroute.logistics.dto.user.UserResponse;
import com.neoroute.logistics.service.user.UserService;
import com.neoroute.logistics.util.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur d'administration pour la gestion des utilisateurs
 * 
 * <p>
 * Permet aux administrateurs de gérer tous les utilisateurs du système :
 * création, modification, consultation et désactivation.
 * </p>
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Administration Utilisateurs", description = "API de gestion des utilisateurs pour les administrateurs")
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs", description = "Récupère la liste complète de tous les utilisateurs du système (admins et chauffeurs).")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> response = users.stream()
                .map(DtoMapper::toUserResponse)
                .collect(Collectors.toList());

        log.info("Liste de {} utilisateurs récupérée", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur", description = "Obtient les détails complets d'un utilisateur par son ID.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(DtoMapper.toUserResponse(user));
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur", description = "Crée un nouveau compte utilisateur (Admin ou Chauffeur). Seuls les administrateurs peuvent créer des comptes.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toUserResponse(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Modifie les informations d'un utilisateur existant (email, mot de passe, statut actif).")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(DtoMapper.toUserResponse(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un utilisateur", description = "Bascule le statut actif/inactif d'un utilisateur. Les utilisateurs inactifs ne peuvent pas se connecter. Un admin ne peut pas désactiver son propre compte.")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id, java.security.Principal principal) {
        User user = userService.toggleUserStatus(id, principal.getName());
        return ResponseEntity.ok(DtoMapper.toUserResponse(user));
    }
}
