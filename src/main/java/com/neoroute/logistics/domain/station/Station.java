package com.neoroute.logistics.domain.station;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Entité représentant une station de livraison
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Node("Station")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    @Builder.Default
    private boolean active = true;
}
