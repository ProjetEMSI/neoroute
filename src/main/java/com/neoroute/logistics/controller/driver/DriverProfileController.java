package com.neoroute.logistics.controller.driver;

import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.dto.trip.TripResponse;
import com.neoroute.logistics.dto.user.UserResponse;
import com.neoroute.logistics.security.JwtProvider;
import com.neoroute.logistics.service.auth.AuthService;
import com.neoroute.logistics.service.trip.TripService;
import com.neoroute.logistics.util.Constants;
import com.neoroute.logistics.util.DtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour le profil chauffeur
 * 
 * <p>
 * Gère les informations du profil et l'historique des trajets pour les
 * chauffeurs.
 * </p>
 */
@RestController
@RequestMapping("/driver/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Slf4j
@Tag(name = "Profil Chauffeur", description = "API pour la gestion du profil chauffeur et historique")
public class DriverProfileController {

    private final AuthService authService;
    private final TripService tripService;
    private final JwtProvider jwtProvider;

    @GetMapping
    @Operation(summary = "Mon profil", description = "Récupère les informations du profil du chauffeur connecté, y compris ses statistiques de performance.")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
        String username = jwtProvider.extractUsername(token);

        User user = authService.getUserByUsername(username);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .totalTrips(user.getTotalTrips())
                .averageScore(user.getAverageScore())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Historique des trajets", description = "Récupère l'historique complet des trajets effectués par le chauffeur.")
    public ResponseEntity<List<TripResponse>> getTripHistory(
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
        Long driverId = jwtProvider.extractUserId(token);

        List<Trip> trips = tripService.getTripsForDriver(driverId);
        return ResponseEntity.ok(DtoMapper.toTripResponseList(trips));
    }
}
