package com.neoroute.logistics.service.trip;

import com.neoroute.logistics.domain.trip.StationVisit;
import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.domain.trip.TripStatus;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.TripRepository;
import com.neoroute.logistics.service.report.TripReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service gérant l'exécution des trajets par les chauffeurs
 * 
 * <p>
 * Ce service contient la logique métier pour le cycle de vie opérationnel d'un
 * trajet :
 * démarrage, visite des stations, et complétion.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripExecutionService {

    private final TripRepository tripRepository;
    private final TripReportService reportService;

    /**
     * Démarrer un trajet assigné
     * 
     * @param tripId   Identifiant du trajet
     * @param driverId Identifiant du chauffeur (pour vérification)
     * @return Le trajet mis à jour avec le statut IN_PROGRESS
     * @throws BusinessException si le trajet n'est pas assigné ou n'appartient pas
     *                           au chauffeur
     */
    @Transactional
    public Trip startTrip(Long tripId, Long driverId) {
        Trip trip = getTripAndValidateDriver(tripId, driverId);

        if (trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BusinessException("Le trajet ne peut pas être démarrer. Statut actuel : " + trip.getStatus());
        }

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setActualStartTime(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);
        log.info("Chauffeur {} a démarré le trajet {}", driverId, tripId);

        return updated;
    }

    /**
     * Marquer une station comme visitée
     * 
     * @param tripId    Identifiant du trajet
     * @param driverId  Identifiant du chauffeur
     * @param stationId Identifiant de la station visitée
     * @return Le trajet mis à jour
     * @throws BusinessException si la station n'est pas dans le trajet ou déjà
     *                           visitée
     */
    @Transactional
    public Trip visitStation(Long tripId, Long driverId, Long stationId) {
        Trip trip = getTripAndValidateDriver(tripId, driverId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new BusinessException("Le trajet n'est pas en cours");
        }

        List<StationVisit> route = trip.getOptimizedRoute();
        StationVisit targetVisit = null;

        // Trouver la station dans la route
        for (StationVisit visit : route) {
            if (visit.getStation().getId().equals(stationId)) {
                targetVisit = visit;
                break;
            }
        }

        if (targetVisit == null) {
            throw new BusinessException("Station non trouvée dans le trajet");
        }

        if (targetVisit.getVisited()) {
            throw new BusinessException("Station déjà visitée");
        }

        // Enregistrer l'ordre réel de visite pour analyse de conformité
        trip.getActualVisitOrder().add(stationId);

        targetVisit.setVisited(true);
        targetVisit.setVisitedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);
        log.info("Chauffeur {} a visité la station {} sur le trajet {} (position {} dans l'ordre réel)",
                driverId, stationId, tripId, trip.getActualVisitOrder().size());

        return updated;
    }

    /**
     * Terminer un trajet
     * 
     * @param tripId   Identifiant du trajet
     * @param driverId Identifiant du chauffeur
     * @return Le trajet complété
     * @throws BusinessException si toutes les stations n'ont pas été visitées
     */
    @Transactional
    public Trip completeTrip(Long tripId, Long driverId) {
        Trip trip = getTripAndValidateDriver(tripId, driverId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new BusinessException("Le trajet n'est pas en cours");
        }

        // Vérifier si toutes les stations requises ont été visitées
        List<StationVisit> route = trip.getOptimizedRoute();
        long visitedCount = route.stream()
                .filter(StationVisit::getVisited)
                .count();

        if (visitedCount < route.size()) {
            throw new BusinessException(
                    String.format("Toutes les stations n'ont pas été visitées. %d/%d complétées",
                            visitedCount, route.size()));
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setActualEndTime(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);

        // Générer le rapport de trajet
        reportService.generateTripReport(updated);

        log.info("Chauffeur {} a terminé le trajet {}", driverId, tripId);

        return updated;
    }

    /**
     * Récupère le trajet et valide que le chauffeur est bien celui assigné
     */
    private Trip getTripAndValidateDriver(Long tripId, Long driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException("Trajet non trouvé avec l'id : " + tripId));

        if (!trip.getAssignedDriverId().equals(driverId)) {
            throw new BusinessException("Ce trajet n'est pas assigné à ce chauffeur");
        }

        return trip;
    }
}
