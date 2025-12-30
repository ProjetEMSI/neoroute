package com.neoroute.logistics.dto.trip;

import com.neoroute.logistics.domain.trip.TripStatus;
import com.neoroute.logistics.dto.station.StationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Trip response DTO with optimized route
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private String name;
    private StationResponse startStation;
    private List<StationResponse> requiredStations;
    private List<StationVisitResponse> optimizedRoute;
    private Long assignedDriverId;
    private String assignedDriverName;
    private TripStatus status;
    private Double totalDistance;
    private Long estimatedTimeMinutes;
    private String estimatedTimeFormatted;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createdAt;
}
