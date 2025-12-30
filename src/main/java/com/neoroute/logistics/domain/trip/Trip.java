package com.neoroute.logistics.domain.trip;

import com.neoroute.logistics.domain.station.Station;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un trajet de livraison complet avec route optimisée
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Node("Trip")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Relationship(type = "STARTS_AT")
    private Station startStation;
    @Relationship(type = "MUST_VISIT")
    @Builder.Default
    private List<Station> requiredStations = new ArrayList<>();
    @Relationship(type = "OPTIMIZED_ROUTE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<StationVisit> optimizedRoute = new ArrayList<>();
    private Long assignedDriverId;
    private String assignedDriverName;
    @Builder.Default
    private TripStatus status = TripStatus.CREATED;
    private Double totalDistance;
    private Long estimatedTimeMinutes;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    @Builder.Default
    private List<Long> actualVisitOrder = new ArrayList<>();
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
