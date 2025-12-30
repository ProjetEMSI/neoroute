package com.neoroute.logistics.domain.trip;

/**
 * Cycle de vie d'un trajet de livraison
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
public enum TripStatus {
    CREATED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
