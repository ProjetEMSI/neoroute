package com.neoroute.logistics.service.auth;

import com.neoroute.logistics.domain.user.Role;
import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.UserRepository;
import com.neoroute.logistics.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service de gestion de l'authentification et de l'enregistrement
 * 
 * <p>
 * Ce service gère toutes les opérations liées à l'authentification des
 * utilisateurs :
 * création de compte, connexion, et génération de tokens JWT.
 * </p>
 * 
 * <p>
 * <strong>Sécurité :</strong>
 * </p>
 * <ul>
 * <li>Mots de passe hashés avec BCrypt</li>
 * <li>Tokens JWT avec expiration configurable</li>
 * <li>Validation de l'unicité des usernames et emails</li>
 * </ul>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Enregistrer un nouvel utilisateur dans le système
     * 
     * @param username Nom d'utilisateur unique
     * @param email    Adresse email unique
     * @param password Mot de passe en clair (sera hashé)
     * @param role     Rôle de l'utilisateur (ADMIN ou DRIVER)
     * @return L'utilisateur créé et sauvegardé
     * @throws BusinessException si le username ou l'email existe déjà
     */
    @Transactional
    @SuppressWarnings("null")
    public User register(String username, String email, String password, Role role) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Cette adresse email est déjà utilisée");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .totalTrips(0)
                .averageScore(0.0)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré : {} avec le rôle : {}", username, role);

        return saved;
    }

    /**
     * Authentifier un utilisateur et générer un token JWT
     * 
     * @param username Nom d'utilisateur
     * @param password Mot de passe en clair
     * @return Token JWT valide pour l'authentification
     * @throws BusinessException si les identifiants sont invalides ou le compte
     *                           inactif
     */
    public String login(String username, String password) {
        // Authentifier l'utilisateur via Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // Récupérer les détails de l'utilisateur
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        if (!user.isActive()) {
            throw new BusinessException("Compte utilisateur inactif");
        }

        // Générer le token JWT
        String token = jwtProvider.generateToken(user.getUsername(), user.getRole(), user.getId());

        log.info("Utilisateur {} connecté avec succès", username);

        return token;
    }

    /**
     * Récupérer un utilisateur par son nom d'utilisateur
     * 
     * @param username Nom d'utilisateur
     * @return L'utilisateur correspondant
     * @throws BusinessException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
    }
}
