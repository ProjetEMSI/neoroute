package com.neoroute.logistics.domain.trip;

import com.neoroute.logistics.domain.station.Station;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

/**
 * Relation représentant la visite d'une station dans un trajet optimisé
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationVisit {

    @RelationshipId
    private Long id;
    @TargetNode
    private Station station;
    private Integer visitOrder;
    private Double distanceFromPrevious;
    @Builder.Default
    private Boolean visited = false;
    private LocalDateTime visitedAt;
}
