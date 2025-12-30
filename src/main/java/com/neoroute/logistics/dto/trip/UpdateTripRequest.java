package com.neoroute.logistics.dto.trip;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO pour la mise à jour d'un trajet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequest {

    @NotBlank(message = "Le nom du trajet est requis")
    private String name;

    private Long startStationId;

    private List<Long> requiredStationIds;
}
