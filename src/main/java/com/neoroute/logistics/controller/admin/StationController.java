package com.neoroute.logistics.controller.admin;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.dto.station.StationRequest;
import com.neoroute.logistics.dto.station.StationResponse;
import com.neoroute.logistics.service.station.StationService;
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
 * Contrôleur d'administration pour la gestion des stations
 * 
 * <p>
 * Permet la création, modification et suppression des stations de livraison.
 * </p>
 */
@RestController
@RequestMapping("/admin/stations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Administration Stations", description = "API de gestion des points de livraison (Stations)")
public class StationController {

    private final StationService stationService;

    @PostMapping
    @Operation(summary = "Créer une station", description = "Ajoute une nouvelle station avec ses coordonnées GPS.")
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody StationRequest request) {
        Station station = stationService.createStation(
                request.getName(),
                request.getLatitude(),
                request.getLongitude());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toStationResponse(station));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une station", description = "Modifie les informations d'une station existante.")
    public ResponseEntity<StationResponse> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody StationRequest request) {
        Station station = stationService.updateStation(
                id,
                request.getName(),
                request.getLatitude(),
                request.getLongitude());

        return ResponseEntity.ok(DtoMapper.toStationResponse(station));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une station", description = "Désactive une station (suppression logique). Elle ne sera plus utilisée pour les nouveaux trajets.")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/active")
    @Operation(summary = "Activer une station", description = "Active une station (suppression logique). Elle sera utilisée pour les nouveaux trajets.")
    public ResponseEntity<Void> activeStation(@PathVariable Long id) {
        stationService.activeStation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une station", description = "Obtient les détails d'une station par son ID.")
    public ResponseEntity<StationResponse> getStation(@PathVariable Long id) {
        Station station = stationService.getStationById(id);
        return ResponseEntity.ok(DtoMapper.toStationResponse(station));
    }

    @GetMapping
    @Operation(summary = "Lister les stations", description = "Récupère la liste des stations. Par défaut, seules les stations actives sont retournées.")
    public ResponseEntity<List<StationResponse>> getAllStations(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<Station> stations = activeOnly ? stationService.getAllActiveStations() : stationService.getAllStations();

        return ResponseEntity.ok(DtoMapper.toStationResponseList(stations));
    }
}
