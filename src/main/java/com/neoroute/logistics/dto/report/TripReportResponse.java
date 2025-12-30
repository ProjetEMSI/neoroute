package com.neoroute.logistics.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les rapports de trajet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripReportResponse {
    private Long id;
    private Long tripId;
    private String tripName;
    private Long driverId;
    private String driverName;
    private Double plannedDistance;
    private Double actualDistance;
    private Long plannedTimeMinutes;
    private Long actualTimeMinutes;
    private Double completionScore;
    private String scoreGrade;
    private Integer stationsVisited;
    private Integer totalStations;

    // Métriques de conformité
    private Boolean routeFollowed;
    private Double routeCompliancePercentage;
    private Integer stationsInCorrectOrder;
    private String routeDeviations;

    private LocalDateTime createdAt;
}
