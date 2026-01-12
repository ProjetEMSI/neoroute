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
 * Implémente l'algorithme de Dijkstra pour trouver le chemin le plus court
 * entre les stations dans le graphe.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationService {

    private final DistanceService distanceService;

    /**
     * Classe interne représentant un nœud dans la file de priorité de Dijkstra
     */
    @Data
    @AllArgsConstructor
    private static class DijkstraNode implements Comparable<DijkstraNode> {
        private Station station;
        private double distance;

        @Override
        public int compareTo(DijkstraNode other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * Optimiser l'itinéraire pour visiter toutes les stations requises
     * Utilise Dijkstra combiné avec une heuristique gloutonne pour le TSP
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

        log.info("Optimisation de la route avec Dijkstra depuis {} avec {} stations requises",
                startStation.getName(), requiredStations.size());

        // Construire la liste complète des stations (départ + requises)
        List<Station> allStations = new ArrayList<>();
        allStations.add(startStation);
        allStations.addAll(requiredStations);

        List<Station> optimizedRoute = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        optimizedRoute.add(startStation);
        distances.add(0.0);

        Set<Long> visited = new HashSet<>();
        visited.add(startStation.getId());

        Station currentStation = startStation;
        double totalDistance = 0.0;

        // Utiliser Dijkstra pour trouver le prochain nœud optimal à chaque étape
        while (visited.size() <= requiredStations.size()) {
            // Exécuter Dijkstra depuis la station courante
            Map<Long, Double> shortestDistances = dijkstra(currentStation, allStations);

            Station nextStation = null;
            double minDistance = Double.MAX_VALUE;

            // Trouver la station non visitée avec la plus courte distance
            for (Station candidate : requiredStations) {
                if (!visited.contains(candidate.getId())) {
                    Double dist = shortestDistances.get(candidate.getId());
                    if (dist != null && dist < minDistance) {
                        minDistance = dist;
                        nextStation = candidate;
                    }
                }
            }

            if (nextStation == null) {
                break; // Toutes les stations ont été visitées
            }

            visited.add(nextStation.getId());
            optimizedRoute.add(nextStation);
            distances.add(minDistance);
            totalDistance += minDistance;
            currentStation = nextStation;

            log.debug("Station {} ajoutée via Dijkstra, distance : {} km",
                    nextStation.getName(), minDistance);
        }

        long estimatedTime = distanceService.estimateTravelTime(totalDistance);

        log.info("Optimisation Dijkstra terminée. Distance totale : {} km, Temps estimé : {} minutes",
                totalDistance, estimatedTime);

        return new RouteResult(optimizedRoute, distances, totalDistance, estimatedTime);
    }

    /**
     * Algorithme de Dijkstra pour calculer les distances les plus courtes
     * depuis une station source vers toutes les autres stations
     * 
     * @param source      Station de départ
     * @param allStations Liste de toutes les stations du graphe
     * @return Map associant chaque ID de station à sa distance minimale depuis la
     *         source
     */
    private Map<Long, Double> dijkstra(Station source, List<Station> allStations) {
        // Map pour stocker les distances minimales
        Map<Long, Double> distances = new HashMap<>();

        // Initialiser toutes les distances à l'infini
        for (Station station : allStations) {
            distances.put(station.getId(), Double.MAX_VALUE);
        }
        distances.put(source.getId(), 0.0);

        // File de priorité pour sélectionner le nœud avec la distance minimale
        PriorityQueue<DijkstraNode> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(new DijkstraNode(source, 0.0));

        // Ensemble des stations déjà traitées
        Set<Long> processed = new HashSet<>();

        while (!priorityQueue.isEmpty()) {
            DijkstraNode currentNode = priorityQueue.poll();
            Station current = currentNode.getStation();

            // Ignorer si déjà traité
            if (processed.contains(current.getId())) {
                continue;
            }
            processed.add(current.getId());

            // Explorer tous les voisins (dans un graphe complet, tous les autres nœuds)
            for (Station neighbor : allStations) {
                if (neighbor.getId().equals(current.getId()) || processed.contains(neighbor.getId())) {
                    continue;
                }

                // Calculer la distance entre la station courante et le voisin
                double edgeWeight = distanceService.calculateDistance(current, neighbor);
                double newDistance = distances.get(current.getId()) + edgeWeight;

                // Si on trouve un chemin plus court, mettre à jour
                if (newDistance < distances.get(neighbor.getId())) {
                    distances.put(neighbor.getId(), newDistance);
                    priorityQueue.add(new DijkstraNode(neighbor, newDistance));
                }
            }
        }

        return distances;
    }

    /**
     * Trouver le chemin le plus court entre deux stations en utilisant Dijkstra
     * 
     * @param start       Station de départ
     * @param end         Station d'arrivée
     * @param allStations Liste de toutes les stations disponibles
     * @return Résultat du chemin le plus court avec la liste des stations
     *         intermédiaires
     */
    public PathResult findShortestPath(Station start, Station end, List<Station> allStations) {
        log.info("Recherche du chemin le plus court avec Dijkstra de {} à {}",
                start.getName(), end.getName());

        // Map pour stocker les distances minimales
        Map<Long, Double> distances = new HashMap<>();
        // Map pour stocker le prédécesseur de chaque nœud (pour reconstruire le chemin)
        Map<Long, Station> predecessors = new HashMap<>();

        // Initialiser toutes les distances à l'infini
        for (Station station : allStations) {
            distances.put(station.getId(), Double.MAX_VALUE);
        }
        distances.put(start.getId(), 0.0);

        // File de priorité pour Dijkstra
        PriorityQueue<DijkstraNode> priorityQueue = new PriorityQueue<>();
        priorityQueue.add(new DijkstraNode(start, 0.0));

        Set<Long> processed = new HashSet<>();

        while (!priorityQueue.isEmpty()) {
            DijkstraNode currentNode = priorityQueue.poll();
            Station current = currentNode.getStation();

            if (processed.contains(current.getId())) {
                continue;
            }
            processed.add(current.getId());

            // Si on a atteint la destination, on peut arrêter
            if (current.getId().equals(end.getId())) {
                break;
            }

            // Explorer les voisins
            for (Station neighbor : allStations) {
                if (neighbor.getId().equals(current.getId()) || processed.contains(neighbor.getId())) {
                    continue;
                }

                double edgeWeight = distanceService.calculateDistance(current, neighbor);
                double newDistance = distances.get(current.getId()) + edgeWeight;

                if (newDistance < distances.get(neighbor.getId())) {
                    distances.put(neighbor.getId(), newDistance);
                    predecessors.put(neighbor.getId(), current);
                    priorityQueue.add(new DijkstraNode(neighbor, newDistance));
                }
            }
        }

        // Reconstruire le chemin depuis la destination vers la source
        List<Station> path = new ArrayList<>();
        Station current = end;

        while (current != null) {
            path.add(0, current); // Ajouter au début pour avoir l'ordre correct
            current = predecessors.get(current.getId());
        }

        double totalDistance = distances.get(end.getId());
        long estimatedTime = distanceService.estimateTravelTime(totalDistance);

        log.info("Chemin trouvé avec Dijkstra : {} stations, distance : {} km",
                path.size(), totalDistance);

        return new PathResult(path, totalDistance, estimatedTime);
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
        /** Chemin complet avec toutes les stations intermédiaires */
        private List<Station> path;
        /** Distance totale du chemin en km */
        private double distance;
        /** Temps estimé en minutes */
        private long estimatedTimeMinutes;
    }
}
