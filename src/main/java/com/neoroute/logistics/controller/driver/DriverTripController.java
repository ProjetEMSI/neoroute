package com.neoroute.logistics.controller.driver;

import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.dto.trip.TripResponse;
import com.neoroute.logistics.security.JwtProvider;
import com.neoroute.logistics.service.trip.TripExecutionService;
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
 * Contrôleur pour la gestion des trajets par le chauffeur
 * 
 * <p>
 * Permet aux chauffeurs de visualiser et d'exécuter leurs trajets assignés.
 * </p>
 */
@RestController
@RequestMapping("/driver/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
@Slf4j
@Tag(name = "Espaces Chauffeur", description = "API pour l'exécution des trajets par les chauffeurs")
public class DriverTripController {

    private final TripService tripService;
    private final TripExecutionService executionService;
    private final JwtProvider jwtProvider;

    @GetMapping("/assigned")
    @Operation(summary = "Mes trajets assignés", description = "Récupère la liste des trajets actifs assignés au chauffeur connecté.")
    public ResponseEntity<List<TripResponse>> getAssignedTrips(
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        Long driverId = extractDriverId(authHeader);
        List<Trip> trips = tripService.getActiveTripsForDriver(driverId);
        return ResponseEntity.ok(DtoMapper.toTripResponseList(trips));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails du trajet", description = "Récupère les détails d'un trajet spécifique. Le chauffeur doit être assigné à ce trajet.")
    public ResponseEntity<TripResponse> getTripDetails(
            @PathVariable Long id,
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        Long driverId = extractDriverId(authHeader);
        Trip trip = tripService.getTripById(id);

        // Vérification de sécurité faite dans le contrôleur (en plus du service)
        // Le service ne vérifie l'accès que pour les actions d'exécution
        if (!trip.getAssignedDriverId().equals(driverId)) {
            throw new com.neoroute.logistics.exception.BusinessException(
                    "Vous n'avez pas accès à ce trajet");
        }

        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Démarrer le trajet", description = "Marque le trajet comme 'En Cours' (IN_PROGRESS) et enregistre l'heure de début.")
    public ResponseEntity<TripResponse> startTrip(
            @PathVariable Long id,
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        Long driverId = extractDriverId(authHeader);
        Trip trip = executionService.startTrip(id, driverId);
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @PostMapping("/{id}/visit/{stationId}")
    @Operation(summary = "Visiter une station", description = "Marque une station comme visitée et enregistre l'ordre réel de passage pour le suivi de conformité.")
    public ResponseEntity<TripResponse> visitStation(
            @PathVariable Long id,
            @PathVariable Long stationId,
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        Long driverId = extractDriverId(authHeader);
        Trip trip = executionService.visitStation(id, driverId, stationId);
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Terminer le trajet", description = "Marque le trajet comme terminé. Génère automatiquement le rapport de performance.")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable Long id,
            @RequestHeader(Constants.HEADER_STRING) String authHeader) {
        Long driverId = extractDriverId(authHeader);
        Trip trip = executionService.completeTrip(id, driverId);
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    private Long extractDriverId(String authHeader) {
        String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
        return jwtProvider.extractUserId(token);
    }
}
