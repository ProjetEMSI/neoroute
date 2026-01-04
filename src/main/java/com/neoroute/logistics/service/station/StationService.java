package com.neoroute.logistics.service.station;

import com.neoroute.logistics.domain.station.Station;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.StationRepository;
import com.neoroute.logistics.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des stations (CRUD)
 * 
 * <p>
 * Permet la création, la mise à jour, la suppression (désactivation)
 * et la récupération des stations géolocalisées.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationRepository stationRepository;

    /**
     * Créer une nouvelle station
     * 
     * @param name      Nom de la station
     * @param latitude  Latitude (-90 à 90)
     * @param longitude Longitude (-180 à 180)
     * @return La station créée
     */
    @Transactional
    public Station createStation(String name, double latitude, double longitude) {
        validateCoordinates(latitude, longitude);

        Station station = Station.builder()
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .active(true)
                .build();

        Station saved = stationRepository.save(station);
        log.info("Station créée : {} à ({}, {})", name, latitude, longitude);
        return saved;
    }

    /**
     * Mettre à jour une station existante
     */
    @Transactional
    public Station updateStation(Long id, String name, double latitude, double longitude) {
        Station station = getStationById(id);
        validateCoordinates(latitude, longitude);

        station.setName(name);
        station.setLatitude(latitude);
        station.setLongitude(longitude);

        Station updated = stationRepository.save(station);
        log.info("Station mise à jour : {}", id);
        return updated;
    }

    /**
     * Supprimer (désactiver) une station
     */
    @Transactional
    public void deleteStation(Long id) {
        Station station = getStationById(id);
        station.setActive(false);
        stationRepository.save(station);
        log.info("Station désactivée : {}", id);
    }

    /**
     * Récupérer une station par son ID
     * 
     * @throws BusinessException si non trouvée
     */
    @Transactional(readOnly = true)
    public Station getStationById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Station non trouvée avec l'id : " + id));
    }

    @Transactional(readOnly = true)
    public List<Station> getAllActiveStations() {
        return stationRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    /**
     * Calculer la distance entre deux stations
     * 
     * @param from Station de départ
     * @param to   Station d'arrivée
     * @return Distance en kilomètres
     */
    public double calculateDistanceBetweenStations(Station from, Station to) {
        return DistanceUtil.calculateDistance(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new BusinessException("Latitude invalide. Doit être entre -90 et 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new BusinessException("Longitude invalide. Doit être entre -180 et 180");
        }
    }
}
