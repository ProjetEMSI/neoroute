package com.neoroute.logistics.service.user;

import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.dto.user.CreateUserRequest;
import com.neoroute.logistics.dto.user.UpdateUserRequest;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des utilisateurs
 * 
 * <p>
 * Ce service gère toutes les opérations CRUD sur les utilisateurs.
 * Accessible uniquement aux administrateurs.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupérer tous les utilisateurs du système
     * 
     * @return Liste de tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        return userRepository.findAll();
    }

    /**
     * Récupérer un utilisateur par son ID
     * 
     * @param id ID de l'utilisateur
     * @return L'utilisateur correspondant
     * @throws BusinessException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé avec l'ID: " + id));
    }

    /**
     * Créer un nouvel utilisateur (réservé aux administrateurs)
     * 
     * @param request Requête de création avec username, email, password et role
     * @return L'utilisateur créé et sauvegardé
     * @throws BusinessException si le username ou l'email existe déjà
     */
    @Transactional
    @SuppressWarnings("null")
    public User createUser(CreateUserRequest request) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Cette adresse email est déjà utilisée");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .totalTrips(0)
                .averageScore(0.0)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur créé par admin: {} avec le rôle: {}", request.getUsername(), request.getRole());

        return saved;
    }

    /**
     * Mettre à jour un utilisateur existant
     * 
     * @param id      ID de l'utilisateur à mettre à jour
     * @param request Requête de mise à jour (email, password, active)
     * @return L'utilisateur mis à jour
     * @throws BusinessException si l'utilisateur n'existe pas ou l'email est déjà
     *                           utilisé
     */
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        // Mettre à jour l'email si fourni
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BusinessException("Cette adresse email est déjà utilisée");
            }
            user.setEmail(request.getEmail());
        }

        // Mettre à jour le mot de passe si fourni
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Mettre à jour le statut actif si fourni
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        log.info("Utilisateur mis à jour: {}", user.getUsername());

        return updated;
    }

    /**
     * Basculer le statut actif d'un utilisateur (activer/désactiver)
     * 
     * @param id ID de l'utilisateur
     * @return L'utilisateur avec le statut modifié
     * @throws BusinessException si l'utilisateur n'existe pas
     */
    @Transactional
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        log.info("Statut de l'utilisateur {} changé à: {}", user.getUsername(), user.isActive() ? "actif" : "inactif");

        return updated;
    }

    /**
     * Récupérer tous les chauffeurs actifs
     * 
     * @return Liste des chauffeurs actifs
     */
    @Transactional(readOnly = true)
    public List<User> getActiveDrivers() {
        return userRepository.findByRoleAndActiveTrue(com.neoroute.logistics.domain.user.Role.DRIVER);
    }
}
