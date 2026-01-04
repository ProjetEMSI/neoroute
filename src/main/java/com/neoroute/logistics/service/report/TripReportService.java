package com.neoroute.logistics.service.report;

import com.neoroute.logistics.domain.report.TripReport;
import com.neoroute.logistics.domain.trip.StationVisit;
import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.repository.ReportRepository;
import com.neoroute.logistics.service.score.DriverScoreService;
import com.neoroute.logistics.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de génération et gestion des rapports de trajets
 * 
 * <p>
 * Génère les analyses post-trajet incluant les scores de performance et
 * la vérification de la conformité avec l'itinéraire optimisé.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripReportService {

    private final ReportRepository reportRepository;
    private final DriverScoreService scoreService;

    /**
     * Générer un rapport après la complétion d'un trajet
     * 
     * @param trip Le trajet terminé
     * @return Le rapport généré
     */
    @Transactional
    public TripReport generateTripReport(Trip trip) {
        if (trip.getActualStartTime() == null || trip.getActualEndTime() == null) {
            throw new IllegalArgumentException("Le trajet doit avoir des heures de début et de fin");
        }

        long actualTimeMinutes = TimeUtil.calculateDurationMinutes(
                trip.getActualStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),
                trip.getActualEndTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli());

        // Calculer la distance réelle (pour l'instant égale à la distance prévue sauf
        // si GPS tracking actif)
        double actualDistance = trip.getTotalDistance();

        // Compter les stations visitées
        List<StationVisit> route = trip.getOptimizedRoute();
        int visitedCount = (int) route.stream()
                .filter(StationVisit::getVisited)
                .count();

        // Calculer le score de complétion
        double completionScore = scoreService.calculateTripScore(
                trip.getEstimatedTimeMinutes(),
                actualTimeMinutes,
                trip.getTotalDistance(),
                actualDistance,
                visitedCount,
                route.size());

        // Analyser la conformité avec le trajet optimisé
        RouteComplianceResult compliance = analyzeRouteCompliance(trip);

        TripReport report = TripReport.builder()
                .tripId(trip.getId())
                .tripName(trip.getName())
                .driverId(trip.getAssignedDriverId())
                .driverName(trip.getAssignedDriverName())
                .plannedDistance(trip.getTotalDistance())
                .actualDistance(actualDistance)
                .plannedTimeMinutes(trip.getEstimatedTimeMinutes())
                .actualTimeMinutes(actualTimeMinutes)
                .completionScore(completionScore)
                .stationsVisited(visitedCount)
                .totalStations(route.size())
                // Conformité du trajet
                .routeFollowed(compliance.routeFollowed)
                .routeCompliancePercentage(compliance.compliancePercentage)
                .stationsInCorrectOrder(compliance.stationsInCorrectOrder)
                .routeDeviations(compliance.deviations)
                .createdAt(LocalDateTime.now())
                .build();

        TripReport saved = reportRepository.save(report);

        // Mettre à jour le score moyen du chauffeur
        scoreService.updateDriverScore(trip.getAssignedDriverId(), completionScore);

        log.info("Rapport généré pour le trajet {} - Score: {} - Conformité: {}%",
                trip.getId(), completionScore, compliance.compliancePercentage);

        return saved;
    }

    /**
     * Analyser si le chauffeur a suivi le trajet optimisé
     */
    private RouteComplianceResult analyzeRouteCompliance(Trip trip) {
        List<StationVisit> optimizedRoute = trip.getOptimizedRoute();
        List<Long> actualVisitOrder = trip.getActualVisitOrder();

        if (actualVisitOrder == null || actualVisitOrder.isEmpty()) {
            return new RouteComplianceResult(false, 0.0, 0, "Aucune donnée de visite disponible");
        }

        int stationsInCorrectOrder = 0;
        StringBuilder deviations = new StringBuilder();

        // Comparer l'ordre optimisé avec l'ordre réel
        // On compare jusqu'à la longueur minimale des deux listes
        for (int i = 0; i < Math.min(optimizedRoute.size(), actualVisitOrder.size()); i++) {
            Long plannedStationId = optimizedRoute.get(i).getStation().getId();
            Long actualStationId = actualVisitOrder.get(i);

            if (plannedStationId.equals(actualStationId)) {
                stationsInCorrectOrder++;
            } else {
                // Trouver la position planifiée de la station réellement visitée
                int plannedPosition = findStationPosition(optimizedRoute, actualStationId);
                if (deviations.length() > 0)
                    deviations.append("; ");

                String deviationMsg = String.format("Position %d: Station #%d visitée au lieu de #%d",
                        i + 1, actualStationId, plannedStationId);

                if (plannedPosition != -1) {
                    deviationMsg += String.format(" (prévue en position %d)", plannedPosition + 1);
                }

                deviations.append(deviationMsg);
            }
        }

        double compliancePercentage = (stationsInCorrectOrder * 100.0) / optimizedRoute.size();
        boolean routeFollowed = compliancePercentage >= 80.0; // 80% de conformité minimum

        String deviationText = deviations.length() > 0 ? deviations.toString() : "Aucune déviation";

        return new RouteComplianceResult(routeFollowed, compliancePercentage, stationsInCorrectOrder, deviationText);
    }

    private int findStationPosition(List<StationVisit> route, Long stationId) {
        for (int i = 0; i < route.size(); i++) {
            if (route.get(i).getStation().getId().equals(stationId)) {
                return i;
            }
        }
        return -1;
    }

    @Transactional(readOnly = true)
    public List<TripReport> getReportsForDriver(Long driverId, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return reportRepository.findByDriverAndDateRange(driverId, start, end);
        }
        return reportRepository.findByDriverId(driverId);
    }

    @Transactional(readOnly = true)
    public List<TripReport> getAllReports(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return reportRepository.findByDateRange(start, end);
        }
        return reportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Double getSystemAverageScore() {
        return reportRepository.calculateAverageScore();
    }

    /**
     * Classe interne pour le résultat de l'analyse de conformité
     */
    private static class RouteComplianceResult {
        boolean routeFollowed;
        double compliancePercentage;
        int stationsInCorrectOrder;
        String deviations;

        RouteComplianceResult(boolean routeFollowed, double compliancePercentage,
                int stationsInCorrectOrder, String deviations) {
            this.routeFollowed = routeFollowed;
            this.compliancePercentage = compliancePercentage;
            this.stationsInCorrectOrder = stationsInCorrectOrder;
            this.deviations = deviations;
        }
    }
}
