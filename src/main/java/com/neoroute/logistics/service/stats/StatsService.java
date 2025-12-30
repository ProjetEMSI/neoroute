package com.neoroute.logistics.service.stats;

import com.neoroute.logistics.domain.trip.TripStatus;
import com.neoroute.logistics.domain.user.Role;
import com.neoroute.logistics.dto.stats.DashboardStatsResponse;
import com.neoroute.logistics.repository.StationRepository;
import com.neoroute.logistics.repository.TripRepository;
import com.neoroute.logistics.repository.UserRepository;
import com.neoroute.logistics.service.report.TripReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service centralisé pour le calcul des statistiques
 * 
 * <p>
 * Fournit toutes les métriques nécessaires pour le dashboard et les rapports
 * dans un format consolidé.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TripReportService reportService;

    /**
     * Calcule toutes les statistiques pour le dashboard administrateur
     * 
     * @return Statistiques complètes du système
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public DashboardStatsResponse getDashboardStats() {
        log.info("Calcul des statistiques dashboard");

        // Compter les trajets par statut
        long totalTrips = tripRepository.count();
        long completedTrips = tripRepository.countByStatus(TripStatus.COMPLETED);
        long inProgressTrips = tripRepository.countByStatus(TripStatus.IN_PROGRESS);
        long assignedTrips = tripRepository.countByStatus(TripStatus.ASSIGNED);
        long cancelledTrips = tripRepository.countByStatus(TripStatus.CANCELLED);

        // Calculer la distance totale à partir des trajets terminés
        Double totalDistance = tripRepository.findByStatus(TripStatus.COMPLETED).stream()
                .map(trip -> trip.getTotalDistance())
                .filter(distance -> distance != null)
                .reduce(0.0, Double::sum);

        // Score moyen du système
        Double averageSystemScore = reportService.getSystemAverageScore();
        if (averageSystemScore == null) {
            averageSystemScore = 0.0;
        }

        // Taux de complétion
        double completionRate = totalTrips > 0
                ? (completedTrips * 100.0) / totalTrips
                : 0.0;

        // Statistiques des chauffeurs
        long totalDrivers = userRepository.findByRole(Role.DRIVER).size();
        long activeDrivers = userRepository.findByRoleAndActiveTrue(Role.DRIVER).size();

        // Statistiques des stations
        long totalStations = stationRepository.count();
        long activeStations = stationRepository.countByActiveTrue();

        return DashboardStatsResponse.builder()
                .totalTrips(totalTrips)
                .completedTrips(completedTrips)
                .inProgressTrips(inProgressTrips)
                .assignedTrips(assignedTrips)
                .cancelledTrips(cancelledTrips)
                .totalDistance(totalDistance)
                .averageSystemScore(averageSystemScore)
                .completionRate(completionRate)
                .activeDrivers((int) activeDrivers)
                .totalDrivers((int) totalDrivers)
                .totalStations((int) totalStations)
                .activeStations((int) activeStations)
                .build();
    }
}
