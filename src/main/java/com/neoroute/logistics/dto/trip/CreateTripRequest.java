package com.neoroute.logistics.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a trip
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    @NotBlank(message = "Trip name is required")
    private String name;

    @NotNull(message = "Start station ID is required")
    private Long startStationId;

    @NotEmpty(message = "Required stations list cannot be empty")
    private List<Long> requiredStationIds;
}
