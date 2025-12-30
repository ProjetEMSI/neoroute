package com.neoroute.logistics.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système (Administrateur ou Chauffeur)
 * 
 * <p>
 * Cette classe modélise un utilisateur stocké dans Neo4j. Les utilisateurs
 * peuvent avoir
 * deux rôles distincts : ADMIN (pour la gestion) ou DRIVER (pour l'exécution
 * des trajets).
 * </p>
 * 
 * <p>
 * <strong>Champs spécifiques aux chauffeurs :</strong>
 * </p>
 * <ul>
 * <li>{@code totalTrips} - Nombre total de trajets complétés</li>
 * <li>{@code averageScore} - Score moyen de performance (0-100)</li>
 * </ul>
 * 
 * @author NeoRoute Team
 * @version 1.0
 * @see Role
 */
@Node("User")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean active;
    private Integer totalTrips;
    private Double averageScore;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    /**
     * Vérifie si l'utilisateur est un administrateur
     * 
     * @return {@code true} si l'utilisateur a le rôle ADMIN
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Vérifie si l'utilisateur est un chauffeur
     * 
     * @return {@code true} si l'utilisateur a le rôle DRIVER
     */
    public boolean isDriver() {
        return role == Role.DRIVER;
    }
}
