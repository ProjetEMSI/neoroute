package com.neoroute.logistics.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques d'un chauffeur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatsResponse {
    private Long driverId;
    private String driverName;
    private Integer totalTrips;
    private Integer completedTrips;
    private Double averageScore;
    private Double averageEfficiency;
}
