package com.neoroute.logistics.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour d'un utilisateur par un administrateur
 * 
 * <p>
 * Permet de modifier l'email, le mot de passe et le statut actif d'un
 * utilisateur.
 * Tous les champs sont optionnels.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "L'email doit être valide")
    private String email;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private Boolean active;
}
