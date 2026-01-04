package com.neoroute.logistics.service.score;

import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.exception.BusinessException;
import com.neoroute.logistics.repository.ReportRepository;
import com.neoroute.logistics.repository.UserRepository;
import com.neoroute.logistics.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour le calcul et la gestion des scores des chauffeurs
 * 
 * <p>
 * Calcule les performances des chauffeurs basées sur leurs trajets
 * en prenant en compte le temps, la distance et le taux de complétion.
 * </p>
 * 
 * @author NeoRoute Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverScoreService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * Calculer le score de performance d'un trajet (0-100)
     * Basé sur :
     * <ul>
     * <li>Efficacité temporelle (réel vs planifié)</li>
     * <li>Efficacité de distance</li>
     * <li>Taux de complétion des stations</li>
     * </ul>
     * 
     * @return Score final arrondi à 2 décimales
     */
    public double calculateTripScore(
            long plannedTimeMinutes,
            long actualTimeMinutes,
            double plannedDistance,
            double actualDistance,
            int visitedStations,
            int totalStations) {

        // Score de temps (proximité avec le temps planifié)
        double timeRatio = (double) plannedTimeMinutes / actualTimeMinutes;
        double timeScore;
        if (timeRatio >= 1.0) {
            // Fini à temps ou en avance
            timeScore = 100.0;
        } else {
            // A pris plus de temps que prévu
            timeScore = Math.max(0, timeRatio * 100);
        }

        // Score de distance
        double distanceRatio = plannedDistance / actualDistance;
        double distanceScore = Math.max(0, Math.min(100, distanceRatio * 100));

        // Score de complétion
        double completionRatio = (double) visitedStations / totalStations;
        double completionScore = completionRatio * 100;

        // Moyenne pondérée
        double finalScore = (timeScore * Constants.ON_TIME_WEIGHT) +
                (distanceScore * Constants.EFFICIENCY_WEIGHT) +
                (completionScore * Constants.COMPLETION_WEIGHT);

        return Math.round(finalScore * 100.0) / 100.0;
    }

    /**
     * Mettre à jour le score moyen d'un chauffeur après un trajet
     * 
     * @param driverId     Identifiant du chauffeur
     * @param newTripScore Score du nouveau trajet
     */
    @Transactional
    public void updateDriverScore(Long driverId, double newTripScore) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new BusinessException("Chauffeur non trouvé"));

        if (!driver.isDriver()) {
            throw new BusinessException("L'utilisateur n'est pas un chauffeur");
        }

        // Calculer le nouveau score moyen
        Double currentAverage = reportRepository.calculateDriverAverageScore(driverId);

        if (currentAverage == null) {
            currentAverage = newTripScore;
        }

        driver.setAverageScore(currentAverage);

        // Incrémenter le compteur de trajets
        Integer totalTrips = driver.getTotalTrips();
        if (totalTrips == null) {
            totalTrips = 0;
        }
        driver.setTotalTrips(totalTrips + 1);

        userRepository.save(driver);

        log.info("Score du chauffeur {} mis à jour à {} (total trajets: {})",
                driverId, currentAverage, driver.getTotalTrips());
    }

    /**
     * Obtenir l'appréciation textuelle du score
     * 
     * @param score Score numérique (0-100)
     * @return Appréciation (Excellent, Bon, Acceptable, À améliorer)
     */
    public String getScoreGrade(double score) {
        if (score >= Constants.EXCELLENT_SCORE) {
            return "Excellent";
        } else if (score >= Constants.GOOD_SCORE) {
            return "Bon";
        } else if (score >= Constants.ACCEPTABLE_SCORE) {
            return "Acceptable";
        } else {
            return "À améliorer";
        }
    }
}
