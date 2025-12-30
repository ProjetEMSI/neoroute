package com.neoroute.logistics.service.trip;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.service.station.DistanceService;
import com.neoroute.logistics.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service pour l'optimisation des itinéraires utilisant des algorithmes de
 * graphes
 * 
 * <p>
 * Implémente l'algorithme "Greedy Nearest Neighbor" (Plus Proche Voisin) pour
 * approximer la solution du problème du voyageur de commerce (TSP).
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {

    private final DistanceService distanceService;

    /**
     * Optimiser l'itinéraire pour visiter toutes les stations requises
     * Utilise une approche gloutonne pour une approximation efficace du TSP
     * 
     * @param startStation     Station de départ
     * @param requiredStations Liste des stations à visiter obligatoirement
     * @return Résultat contenant la route optimisée, les distances et le temps
     *         estimé
     * @throws IllegalArgumentException si la liste des stations est vide ou dépasse
     *                                  la limite
     */
    public RouteResult optimizeRoute(Station startStation, List<Station> requiredStations) {
        if (requiredStations == null || requiredStations.isEmpty()) {
            throw new IllegalArgumentException("La liste des stations requises ne peut pas être vide");
        }

        if (requiredStations.size() > Constants.MAX_STATIONS_PER_TRIP) {
            throw new IllegalArgumentException(
                    "Trop de stations. Maximum autorisé : " + Constants.MAX_STATIONS_PER_TRIP);
        }

        log.info("Optimisation de la route depuis {} avec {} stations requises",
                startStation.getName(), requiredStations.size());

        List<Station> optimizedRoute = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        optimizedRoute.add(startStation);
        distances.add(0.0); // La première station n'a pas de distance depuis la précédente

        Set<Long> visited = new HashSet<>();
        visited.add(startStation.getId());

        Set<Long> requiredIds = new HashSet<>();
        for (Station s : requiredStations) {
            requiredIds.add(s.getId());
        }

        Station currentStation = startStation;
        double totalDistance = 0.0;

        // Approche Gloutonne du Plus Proche Voisin (Greedy Nearest Neighbor)
        while (visited.size() <= requiredStations.size()) {
            Station nearestStation = null;
            double nearestDistance = Double.MAX_VALUE;

            // Trouver la station requise non visitée la plus proche
            for (Station candidate : requiredStations) {
                if (!visited.contains(candidate.getId())) {
                    double distance = distanceService.calculateDistance(currentStation, candidate);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestStation = candidate;
                    }
                }
            }

            if (nearestStation == null) {
                break; // Toutes les stations ont été visitées
            }

            visited.add(nearestStation.getId());
            optimizedRoute.add(nearestStation);
            distances.add(nearestDistance);
            totalDistance += nearestDistance;
            currentStation = nearestStation;

            log.debug("Station {} ajoutée à la route, distance depuis la précédente : {} km",
                    nearestStation.getName(), nearestDistance);
        }

        long estimatedTime = distanceService.estimateTravelTime(totalDistance);

        log.info("Optimisation terminée. Distance totale : {} km, Temps estimé : {} minutes",
                totalDistance, estimatedTime);

        return new RouteResult(optimizedRoute, distances, totalDistance, estimatedTime);
    }

    /**
     * Trouver le chemin le plus court entre deux stations (simplifié)
     * 
     * <p>
     * Dans cette version simplifiée, nous utilisons la distance directe.
     * Dans une version future avec Neo4j complet, cela utiliserait Dijkstra sur le
     * graphe routier.
     * </p>
     * 
     * @param start       Station de départ
     * @param end         Station d'arrivée
     * @param allStations Liste de toutes les stations disponibles (pour le contexte
     *                    de graphe)
     * @return Résultat du chemin le plus court
     */
    public PathResult findShortestPath(Station start, Station end, List<Station> allStations) {
        // Pour la simplicité, nous utilisons la distance directe (vol d'oiseau
        // amélioré)
        // Dans un système de production, cela utiliserait les capacités de graphe de
        // Neo4j
        // pour trouver le chemin réel à travers le réseau routier

        double distance = distanceService.calculateDistance(start, end);
        List<Station> path = Arrays.asList(start, end);
        long time = distanceService.estimateTravelTime(distance);

        return new PathResult(path, distance, time);
    }

    /**
     * Objet résultat pour l'optimisation de route
     */
    @Data
    @AllArgsConstructor
    public static class RouteResult {
        /** Liste ordonnée des stations à visiter */
        private List<Station> route;

        /** Distance depuis la station précédente pour chaque étape */
        private List<Double> distances;

        /** Distance totale du parcours en km */
        private double totalDistance;

        /** Temps total estimé en minutes */
        private long estimatedTimeMinutes;
    }

    /**
     * Objet résultat pour le chemin le plus court
     */
    @Data
    @AllArgsConstructor
    public static class PathResult {
        private List<Station> path;
        private double distance;
        private long estimatedTimeMinutes;
    }
}
