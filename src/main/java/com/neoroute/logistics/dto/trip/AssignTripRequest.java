package com.neoroute.logistics.dto.trip;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for assigning a trip to a driver
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTripRequest {

    @NotNull(message = "Driver ID is required")
    private Long driverId;
}
