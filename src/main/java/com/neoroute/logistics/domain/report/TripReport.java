package com.neoroute.logistics.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

/**
 * Entité représentant un rapport d'analyse de trajet avec métriques de
 * performance
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Node("TripReport")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripReport {

    @Id
    @GeneratedValue
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
    private Integer stationsVisited;
    private Integer totalStations;
    private Boolean routeFollowed;
    private Double routeCompliancePercentage;
    private Integer stationsInCorrectOrder;
    private String routeDeviations;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
