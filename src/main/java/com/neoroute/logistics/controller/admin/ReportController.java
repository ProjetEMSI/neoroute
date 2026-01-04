package com.neoroute.logistics.controller.admin;

import com.neoroute.logistics.domain.report.TripReport;
import com.neoroute.logistics.domain.user.Role;
import com.neoroute.logistics.domain.user.User;
import com.neoroute.logistics.dto.report.TripReportResponse;
import com.neoroute.logistics.repository.UserRepository;
import com.neoroute.logistics.service.report.TripReportService;
import com.neoroute.logistics.service.score.DriverScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur d'administration pour les rapports et analyses
 * 
 * <p>
 * Fournit des statistiques sur les trajets et les performances des chauffeurs.
 * </p>
 */
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Rapports & Statistiques", description = "API d'analyse et de reporting pour l'administration")
public class ReportController {

        private final TripReportService reportService;
        private final UserRepository userRepository;
        private final DriverScoreService scoreService;
        private final com.neoroute.logistics.service.stats.StatsService statsService;
        private final com.neoroute.logistics.service.report.PdfReportService pdfReportService;

        @GetMapping("/trips")
        @Operation(summary = "Rapports de trajets", description = "Récupère les rapports détaillés des trajets, avec possibilité de filtrer par chauffeur et par date.")
        public ResponseEntity<List<TripReportResponse>> getTripReports(
                        @RequestParam(required = false) Long driverId,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {

                List<TripReport> reports;
                if (driverId != null) {
                        reports = reportService.getReportsForDriver(driverId, startDate, endDate);
                } else {
                        reports = reportService.getAllReports(startDate, endDate);
                }

                List<TripReportResponse> response = reports.stream()
                                .map(report -> TripReportResponse.builder()
                                                .id(report.getId())
                                                .tripId(report.getTripId())
                                                .tripName(report.getTripName())
                                                .driverId(report.getDriverId())
                                                .driverName(report.getDriverName())
                                                .plannedDistance(report.getPlannedDistance())
                                                .actualDistance(report.getActualDistance())
                                                .plannedTimeMinutes(report.getPlannedTimeMinutes())
                                                .actualTimeMinutes(report.getActualTimeMinutes())
                                                .completionScore(report.getCompletionScore())
                                                .scoreGrade(scoreService.getScoreGrade(report.getCompletionScore()))
                                                .stationsVisited(report.getStationsVisited())
                                                .totalStations(report.getTotalStations())
                                                // Conformité
                                                .routeFollowed(report.getRouteFollowed())
                                                .routeCompliancePercentage(report.getRouteCompliancePercentage())
                                                .stationsInCorrectOrder(report.getStationsInCorrectOrder())
                                                .routeDeviations(report.getRouteDeviations())
                                                .createdAt(report.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        @GetMapping("/export/pdf")
        @Operation(summary = "Exporter en PDF", description = "Génère un rapport PDF avec graphiques pour la période donnée.")
        public ResponseEntity<byte[]> exportPdf(
                        @RequestParam(required = false) Long driverId,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {

                List<TripReport> reports;
                if (driverId != null) {
                        reports = reportService.getReportsForDriver(driverId, startDate, endDate);
                } else {
                        reports = reportService.getAllReports(startDate, endDate);
                }

                String title = "Rapport d'Activité NeoRoute";
                if (startDate != null && endDate != null) {
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy");
                        title += " (" + startDate.format(fmt) + " - " + endDate.format(fmt) + ")";
                }

                byte[] pdfBytes = pdfReportService.generatePdfReport(reports, title);

                return ResponseEntity.ok()
                                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=rapport.pdf")
                                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                                .body(pdfBytes);
        }

        @GetMapping("/drivers")
        @Operation(summary = "Performances chauffeurs", description = "Récupère les statistiques de performance de tous les chauffeurs.")
        public ResponseEntity<List<com.neoroute.logistics.dto.stats.DriverStatsResponse>> getDriverStatistics() {
                List<User> drivers = userRepository.findByRole(Role.DRIVER);

                List<com.neoroute.logistics.dto.stats.DriverStatsResponse> response = drivers.stream()
                                .map(driver -> {
                                        List<TripReport> driverReports = reportService
                                                        .getReportsForDriver(driver.getId(), null, null);

                                        int completedTrips = (int) driverReports.stream()
                                                        .filter(report -> report.getCompletionScore() != null)
                                                        .count();

                                        // Calculer l'efficacité moyenne (% de conformité des routes)
                                        Double avgEfficiency = driverReports.stream()
                                                        .filter(report -> report.getRouteCompliancePercentage() != null)
                                                        .mapToDouble(TripReport::getRouteCompliancePercentage)
                                                        .average()
                                                        .orElse(0.0);

                                        return com.neoroute.logistics.dto.stats.DriverStatsResponse.builder()
                                                        .driverId(driver.getId())
                                                        .driverName(driver.getUsername())
                                                        .totalTrips(driver.getTotalTrips() != null
                                                                        ? driver.getTotalTrips()
                                                                        : 0)
                                                        .completedTrips(completedTrips)
                                                        .averageScore(driver.getAverageScore() != null
                                                                        ? driver.getAverageScore()
                                                                        : 0.0)
                                                        .averageEfficiency(avgEfficiency)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        @GetMapping("/summary")
        @Operation(summary = "Résumé du système", description = "Fournit des métriques globales sur l'activité du système (total chauffeurs, trajets, score moyen).")
        public ResponseEntity<com.neoroute.logistics.dto.stats.DashboardStatsResponse> getSystemSummary() {
                return ResponseEntity.ok(statsService.getDashboardStats());
        }
}
