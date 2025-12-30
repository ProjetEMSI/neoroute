package com.neoroute.logistics.repository;

import com.neoroute.logistics.domain.trip.Trip;
import com.neoroute.logistics.domain.trip.TripStatus;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Trip entity
 */
@Repository
public interface TripRepository extends Neo4jRepository<Trip, Long> {

        List<Trip> findByAssignedDriverId(Long driverId);

        List<Trip> findByStatus(TripStatus status);

        List<Trip> findByAssignedDriverIdAndStatus(Long driverId, TripStatus status);

        // Derived query method - automatically loads relationships by default
        List<Trip> findByAssignedDriverIdOrderByCreatedAtDesc(Long driverId);

        List<Trip> findByAssignedDriverIdAndStatusOrderByCreatedAtDesc(Long driverId, TripStatus status);

        long countByStatus(TripStatus status);
}
