package com.neoroute.logistics.service.trip;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.domain.trip.StationVisit;
import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.domain.trip.TripStatus;
import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.TripRepository;
import com.neoroute.logistics.repository.UserRepository;
import com.neoroute.logistics.service.station.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion administrative des trajets
 * 
 * <p>
 * Gère la création, l'assignation et l'annulation des trajets.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StationService stationService;
    private final RouteOptimizationService routeOptimizationService;
    private final com.neoroute.logistics.service.notification.NotificationService notificationService;

    /**
     * Créer un nouveau trajet avec optimisation automatique de la route
     * 
     * @param tripName           Nom du trajet
     * @param startStationId     ID de la station de départ
     * @param requiredStationIds Liste des IDs des stations à visiter
     * @return Le trajet créé avec la route optimisée
     */
    @Transactional
    public Trip createTrip(String tripName, Long startStationId, List<Long> requiredStationIds) {
        Station startStation = stationService.getStationById(startStationId);

        List<Station> requiredStations = new ArrayList<>();
        for (Long stationId : requiredStationIds) {
            Station station = stationService.getStationById(stationId);
            if (station.getId().equals(startStationId)) {
                throw new BusinessException(
                        "La station de départ ne peut pas être dans la liste des stations requises");
            }
            requiredStations.add(station);
        }

        // Optimiser la route
        RouteOptimizationService.RouteResult routeResult = routeOptimizationService.optimizeRoute(startStation,
                requiredStations);

        // Construire la route optimisée avec les visites de stations
        List<StationVisit> stationVisits = new ArrayList<>();
        List<Station> optimizedRoute = routeResult.getRoute();
        List<Double> distances = routeResult.getDistances();

        for (int i = 0; i < optimizedRoute.size(); i++) {
            StationVisit visit = StationVisit.builder()
                    .station(optimizedRoute.get(i))
                    .visitOrder(i)
                    .distanceFromPrevious(distances.get(i))
                    .visited(false)
                    .build();
            stationVisits.add(visit);
        }

        Trip trip = Trip.builder()
                .name(tripName)
                .startStation(startStation)
                .requiredStations(requiredStations)
                .optimizedRoute(stationVisits)
                .status(TripStatus.CREATED)
                .totalDistance(routeResult.getTotalDistance())
                .estimatedTimeMinutes(routeResult.getEstimatedTimeMinutes())
                .createdAt(LocalDateTime.now())
                .build();

        Trip saved = tripRepository.save(trip);
        log.info("Trajet créé : {} avec route optimisée ({} stations, {} km)",
                tripName, optimizedRoute.size(), saved.getTotalDistance());

        return saved;
    }

    /**
     * Assigner un trajet à un chauffeur
     * 
     * @param tripId   ID du trajet
     * @param driverId ID du chauffeur
     * @return Le trajet mis à jour
     */
    @Transactional
    public Trip assignTripToDriver(Long tripId, Long driverId) {
        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.CREATED && trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BusinessException("Le trajet ne peut pas être assigné. Statut actuel : " + trip.getStatus());
        }

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new BusinessException("Chauffeur non trouvé avec l'id : " + driverId));

        if (!driver.isDriver()) {
            throw new BusinessException("L'utilisateur n'est pas un chauffeur");
        }

        if (!driver.isActive()) {
            throw new BusinessException("Le compte chauffeur est inactif");
        }

        trip.setAssignedDriverId(driverId);
        trip.setAssignedDriverName(driver.getUsername());
        trip.setStatus(TripStatus.ASSIGNED);
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);
        log.info("Trajet {} assigné au chauffeur {}", tripId, driver.getUsername());

        // Envoyer les notifications
        notificationService.notifyTripAssignment(driver.getEmail(), driverId, updated);

        return updated;
    }

    /**
     * Annuler un trajet
     * 
     * @param tripId ID du trajet
     * @return Le trajet annulé
     */
    @Transactional
    public Trip cancelTrip(Long tripId) {
        Trip trip = getTripById(tripId);

        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new BusinessException("Impossible d'annuler un trajet terminé");
        }

        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            throw new BusinessException("Impossible d'annuler un trajet en cours");
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);
        log.info("Trajet annulé : {}", tripId);

        return updated;
    }

    /**
     * Mettre à jour un trajet
     * 
     * @param tripId             ID du trajet
     * @param tripName           Nouveau nom du trajet
     * @param startStationId     Nouvelle station de départ (optionnel)
     * @param requiredStationIds Nouvelle liste des stations requises (optionnel)
     * @return Le trajet mis à jour
     */
    @Transactional
    public Trip updateTrip(Long tripId, String tripName, Long startStationId, List<Long> requiredStationIds) {
        Trip trip = getTripById(tripId);

        // Seuls les trajets CREATED ou ASSIGNED peuvent être modifiés
        if (trip.getStatus() != TripStatus.CREATED && trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BusinessException("Le trajet ne peut pas être modifié. Statut actuel : " + trip.getStatus());
        }

        // Mettre à jour le nom
        trip.setName(tripName);

        // Si les stations sont fournies, ré-optimiser la route
        if (startStationId != null && requiredStationIds != null && !requiredStationIds.isEmpty()) {
            Station startStation = stationService.getStationById(startStationId);

            List<Station> requiredStations = new ArrayList<>();
            for (Long stationId : requiredStationIds) {
                Station station = stationService.getStationById(stationId);
                if (station.getId().equals(startStationId)) {
                    throw new BusinessException(
                            "La station de départ ne peut pas être dans la liste des stations requises");
                }
                requiredStations.add(station);
            }

            // Optimiser la nouvelle route
            RouteOptimizationService.RouteResult routeResult = routeOptimizationService.optimizeRoute(startStation,
                    requiredStations);

            // Construire la nouvelle route optimisée
            List<StationVisit> stationVisits = new ArrayList<>();
            List<Station> optimizedRoute = routeResult.getRoute();
            List<Double> distances = routeResult.getDistances();

            for (int i = 0; i < optimizedRoute.size(); i++) {
                StationVisit visit = StationVisit.builder()
                        .station(optimizedRoute.get(i))
                        .visitOrder(i)
                        .distanceFromPrevious(distances.get(i))
                        .visited(false)
                        .build();
                stationVisits.add(visit);
            }

            trip.setStartStation(startStation);
            trip.setRequiredStations(requiredStations);
            trip.setOptimizedRoute(stationVisits);
            trip.setTotalDistance(routeResult.getTotalDistance());
            trip.setEstimatedTimeMinutes(routeResult.getEstimatedTimeMinutes());

            log.info("Trajet {} mis à jour avec nouvelle route optimisée ({} stations, {} km)",
                    tripId, optimizedRoute.size(), trip.getTotalDistance());
        } else {
            log.info("Trajet {} - nom mis à jour uniquement", tripId);
        }

        trip.setUpdatedAt(LocalDateTime.now());
        Trip updated = tripRepository.save(trip);

        return updated;
    }

    /**
     * Récupérer un trajet par son ID
     */
    @Transactional(readOnly = true)
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Trajet non trouvé avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Trip> getTripsByStatus(TripStatus status) {
        return tripRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Trip> getTripsForDriver(Long driverId) {
        return tripRepository.findByAssignedDriverId(driverId);
    }

    @Transactional(readOnly = true)
    public List<Trip> getActiveTripsForDriver(Long driverId) {
        // Fetch all trips for driver and filter by active statuses
        return tripRepository.findByAssignedDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .filter(trip -> trip.getStatus() == TripStatus.ASSIGNED ||
                        trip.getStatus() == TripStatus.IN_PROGRESS)
                .toList();
    }

    /**
     * Compter le nombre de trajets terminés pour un chauffeur
     *
     * @param driverId ID du chauffeur
     * @return Nombre de trajets terminés
     */
    @Transactional(readOnly = true)
    public Integer countCompletedTripsForDriver(Long driverId) {
        return tripRepository.findByAssignedDriverIdAndStatus(driverId, TripStatus.COMPLETED).size();
    }

    /**
     * Calculer la distance totale parcourue par un chauffeur sur ses trajets
     * terminés
     *
     * @param driverId ID du chauffeur
     * @return Distance totale en km
     */
    @Transactional(readOnly = true)
    public Double calculateTotalDistanceForDriver(Long driverId) {
        return tripRepository.findByAssignedDriverIdAndStatus(driverId, TripStatus.COMPLETED)
                .stream()
                .mapToDouble(trip -> trip.getTotalDistance() != null ? trip.getTotalDistance() : 0.0)
                .sum();
    }
}
