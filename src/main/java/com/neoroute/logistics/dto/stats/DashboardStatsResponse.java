package com.neoroute.logistics.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques complètes du dashboard
 * 
 * <p>
 * Contient toutes les métriques nécessaires pour afficher le tableau de bord
 * administrateur sans nécessiter plusieurs appels API.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Statistiques des trajets
    private Long totalTrips;
    private Long completedTrips;
    private Long inProgressTrips;
    private Long assignedTrips;
    private Long cancelledTrips;

    // Distances et efficacité
    private Double totalDistance;
    private Double averageSystemScore;
    private Double completionRate;

    // Statistiques des chauffeurs
    private Integer activeDrivers;
    private Integer totalDrivers;

    // Statistiques des stations
    private Integer totalStations;
    private Integer activeStations;
}
