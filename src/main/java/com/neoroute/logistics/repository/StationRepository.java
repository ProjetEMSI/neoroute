package com.neoroute.logistics.repository;

import com.neoroute.logistics.domain.station.Station;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Station entity
 */
@Repository
public interface StationRepository extends Neo4jRepository<Station, Long> {

    List<Station> findByActiveTrue();

    @Query("MATCH (s:Station) WHERE s.active = true RETURN s ORDER BY s.name")
    List<Station> findAllActiveOrderedByName();

    @Query("MATCH (s1:Station)-[r:CONNECTS_TO]->(s2:Station) " +
            "WHERE id(s1) = $fromStationId AND id(s2) = $toStationId " +
            "RETURN r.distance")
    Double findDistanceBetweenStations(Long fromStationId, Long toStationId);

    long countByActiveTrue();
}
