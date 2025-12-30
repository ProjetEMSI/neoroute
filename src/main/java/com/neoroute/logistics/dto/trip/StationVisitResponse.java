package com.neoroute.logistics.dto.trip;

import com.neoroute.logistics.dto.station.StationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Station visit response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationVisitResponse {
    private StationResponse station;
    private Integer visitOrder;
    private Double distanceFromPrevious;
    private Boolean visited;
    private LocalDateTime visitedAt;
}
