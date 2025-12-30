package com.neoroute.logistics.service.station;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.util.DistanceUtil;
import com.neoroute.logistics.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service pour les calculs de distance et de temps de trajet
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class DistanceService {

    @Value("${neoroute.routing.average-speed-kmh:40}")
    private double averageSpeedKmh;

    /**
     * Calculer la distance entre deux stations (formule de Haversine)
     * 
     * @param from Station de départ
     * @param to   Station d'arrivée
     * @return Distance arrondie en kilomètres
     */
    public double calculateDistance(Station from, Station to) {
        return DistanceUtil.roundDistance(
                DistanceUtil.calculateDistance(
                        from.getLatitude(), from.getLongitude(),
                        to.getLatitude(), to.getLongitude()));
    }

    /**
     * Calculer la distance entre deux coordonnées GPS
     * 
     * @param lat1 Latitude point 1
     * @param lon1 Longitude point 1
     * @param lat2 Latitude point 2
     * @param lon2 Longitude point 2
     * @return Distance arrondie en kilomètres
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return DistanceUtil.roundDistance(
                DistanceUtil.calculateDistance(lat1, lon1, lat2, lon2));
    }

    /**
     * Estimer le temps de trajet en minutes
     * 
     * @param distanceKm Distance en kilomètres
     * @return Temps estimé en minutes
     */
    public long estimateTravelTime(double distanceKm) {
        return TimeUtil.calculateTimeInMinutes(distanceKm, averageSpeedKmh);
    }

    /**
     * Formater une durée dans un format lisible
     * 
     * @param minutes Nombre de minutes
     * @return Chaîne formatée (ex: "2h 15m")
     */
    public String formatDuration(long minutes) {
        return TimeUtil.formatDuration(minutes);
    }
}
