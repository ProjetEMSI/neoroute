package com.neoroute.logistics.controller.admin;

import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.domain.trip.TripStatus;
import com.neoroute.logistics.dto.trip.AssignTripRequest;
import com.neoroute.logistics.dto.trip.CreateTripRequest;
import com.neoroute.logistics.dto.trip.TripResponse;
import com.neoroute.logistics.dto.trip.UpdateTripRequest;
import com.neoroute.logistics.service.trip.TripService;
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

/**
 * Contrôleur d'administration pour la gestion des trajets
 * 
 * <p>
 * Permet aux administrateurs de créer, assigner et gérer les trajets.
 * </p>
 */
@RestController
@RequestMapping("/admin/trips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Administration Trajets", description = "API de gestion des trajets pour les administrateurs")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Créer un nouveau trajet", description = "Crée un trajet avec une station de départ et des stations requises. Le système optimise automatiquement la route.")
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        Trip trip = tripService.createTrip(
                request.getName(),
                request.getStartStationId(),
                request.getRequiredStationIds());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toTripResponse(trip));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assigner un trajet", description = "Assigne un trajet existant à un chauffeur spécifique.")
    public ResponseEntity<TripResponse> assignTrip(
            @PathVariable Long id,
            @Valid @RequestBody AssignTripRequest request) {
        Trip trip = tripService.assignTripToDriver(id, request.getDriverId());
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un trajet", description = "Modifie le nom et optionnellement les stations d'un trajet (seulement si le trajet est CREATED ou ASSIGNED).")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTripRequest request) {
        Trip trip = tripService.updateTrip(
                id,
                request.getName(),
                request.getStartStationId(),
                request.getRequiredStationIds());
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Annuler un trajet", description = "Annule un trajet qui n'est pas encore en cours ou terminé.")
    public ResponseEntity<TripResponse> cancelTrip(@PathVariable Long id) {
        Trip trip = tripService.cancelTrip(id);
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un trajet", description = "Obtient les détails complets d'un trajet par son ID.")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(DtoMapper.toTripResponse(trip));
    }

    @GetMapping
    @Operation(summary = "Lister tous les trajets", description = "Récupère la liste de tous les trajets, avec possibilité de filtrer par statut.")
    public ResponseEntity<List<TripResponse>> getAllTrips(
            @RequestParam(required = false) TripStatus status) {
        List<Trip> trips = status != null ? tripService.getTripsByStatus(status) : tripService.getAllTrips();

        return ResponseEntity.ok(DtoMapper.toTripResponseList(trips));
    }
}
