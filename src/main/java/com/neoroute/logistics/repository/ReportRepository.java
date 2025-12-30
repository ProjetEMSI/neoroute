package com.neoroute.logistics.repository;

import com.neoroute.logistics.domain.report.TripReport;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TripReport entity
 */
@Repository
public interface ReportRepository extends Neo4jRepository<TripReport, Long> {

    List<TripReport> findByDriverId(Long driverId);

    @Query("MATCH (r:TripReport) WHERE r.driverId = $driverId " +
            "RETURN r ORDER BY r.createdAt DESC LIMIT 10")
    List<TripReport> findRecentReportsForDriver(Long driverId);

    @Query("MATCH (r:TripReport) " +
            "RETURN AVG(r.completionScore) as avgScore")
    Double calculateAverageScore();

    @Query("MATCH (r:TripReport) WHERE r.driverId = $driverId " +
            "RETURN AVG(r.completionScore) as avgScore")
    Double calculateDriverAverageScore(Long driverId);
}
