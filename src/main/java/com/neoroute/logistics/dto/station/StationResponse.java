package com.neoroute.logistics.dto.station;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Station response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponse {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;
    private boolean active;
}
