package com.neoroute.logistics.service.report;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.neoroute.logistics.domain.report.TripReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    // Couleurs modernes
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Bleu moderne
    private static final Color SECONDARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(251, 146, 60);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color GRAY_LIGHT = new Color(243, 244, 246);
    private static final Color GRAY_DARK = new Color(107, 114, 128);
    private static final Color WHITE = Color.WHITE;

    public byte[] generatePdfReport(List<TripReport> reports, String title) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            
            // En-tête et pied de page personnalisés
            writer.setPageEvent(new HeaderFooterPageEvent());
            
            document.open();

            // Page de couverture
            addCoverPage(document, title, reports.size());
            document.newPage();

            // Résumé exécutif avec KPIs
            addExecutiveSummary(document, reports);
            document.add(Chunk.NEWLINE);

            // Graphiques analytiques
            addAnalyticsCharts(document, writer, reports);
            document.newPage();

            // Analyse détaillée par chauffeur
            addDriverAnalysis(document, reports);
            document.add(Chunk.NEWLINE);

            // Tableau détaillé
            addDetailedTable(document, reports);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rapport PDF", e);
            throw new RuntimeException("Échec de la génération du rapport PDF", e);
        }
    }

    private void addCoverPage(Document document, String title, int totalReports) throws DocumentException {
        // Fond coloré
        PdfPTable bgTable = new PdfPTable(1);
        bgTable.setWidthPercentage(100);
        PdfPCell bgCell = new PdfPCell();
        bgCell.setBackgroundColor(PRIMARY_COLOR);
        bgCell.setFixedHeight(250);
        bgCell.setBorder(Rectangle.NO_BORDER);
        bgTable.addCell(bgCell);
        document.add(bgTable);

        document.add(new Paragraph("\n\n\n"));

        // Logo/Icône (simulé avec texte)
        com.lowagie.text.Font logoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 48, WHITE);
        Paragraph logo = new Paragraph("NeoRoute", logoFont);
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.setSpacingBefore(-200);
        document.add(logo);

        // Titre principal
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32, WHITE);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingBefore(20);
        document.add(titlePara);

        // Sous-titre
        com.lowagie.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, GRAY_LIGHT);
        Paragraph subtitle = new Paragraph("Rapport d'Analyse et de Performance", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        document.add(subtitle);

        document.add(new Paragraph("\n\n\n\n"));

        // Statistiques rapides
        PdfPTable statsTable = new PdfPTable(3);
        statsTable.setWidthPercentage(80);
        statsTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        statsTable.setSpacingBefore(50);

        addCoverStatCell(statsTable, String.valueOf(totalReports), "Trajets Analysés");
        addCoverStatCell(statsTable, 
            java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            "Date de Génération");
        addCoverStatCell(statsTable, "Admin", "Généré par");

        document.add(statsTable);
    }

    private void addCoverStatCell(PdfPTable table, String value, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setBackgroundColor(WHITE);
        cell.setBorder(Rectangle.NO_BORDER);

        com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
        Paragraph pValue = new Paragraph(value, valueFont);
        pValue.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pValue);

        com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, GRAY_DARK);
        Paragraph pLabel = new Paragraph(label, labelFont);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);

        table.addCell(cell);
    }

    private void addExecutiveSummary(Document document, List<TripReport> reports) throws DocumentException {
        // Titre de section
        addSectionTitle(document, "Résumé Exécutif");

        double avgScore = reports.stream()
            .mapToDouble(r -> r.getCompletionScore() != null ? r.getCompletionScore() : 0.0)
            .average().orElse(0.0);

        double avgCompliance = reports.stream()
            .mapToDouble(r -> r.getRouteCompliancePercentage() != null ? r.getRouteCompliancePercentage() : 0.0)
            .average().orElse(0.0);

        long conformeCount = reports.stream()
            .filter(r -> Boolean.TRUE.equals(r.getRouteFollowed()))
            .count();

        double conformeRate = (conformeCount * 100.0) / reports.size();

        // KPIs en cartes
        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(15);
        kpiTable.setSpacingAfter(15);

        addKpiCard(kpiTable, String.valueOf(reports.size()), "Total Trajets", PRIMARY_COLOR);
        addKpiCard(kpiTable, String.format("%.1f/100", avgScore), "Score Moyen", getScoreColor(avgScore));
        addKpiCard(kpiTable, String.format("%.1f%%", avgCompliance), "Conformité Moyenne", SUCCESS_COLOR);
        addKpiCard(kpiTable, String.format("%.1f%%", conformeRate), "Taux Conforme", SUCCESS_COLOR);

        document.add(kpiTable);
    }

    private void addKpiCard(PdfPTable table, String value, String label, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(20);
        cell.setBackgroundColor(WHITE);
        cell.setBorderColor(GRAY_LIGHT);
        cell.setBorderWidth(1);

        // Icône (ligne colorée en haut)
        PdfPTable iconTable = new PdfPTable(1);
        iconTable.setWidthPercentage(100);
        PdfPCell iconCell = new PdfPCell();
        iconCell.setBackgroundColor(color);
        iconCell.setFixedHeight(5);
        iconCell.setBorder(Rectangle.NO_BORDER);
        iconTable.addCell(iconCell);
        cell.addElement(iconTable);

        cell.addElement(new Paragraph("\n"));

        // Valeur
        com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, color);
        Paragraph pValue = new Paragraph(value, valueFont);
        pValue.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pValue);

        // Label
        com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, GRAY_DARK);
        Paragraph pLabel = new Paragraph(label, labelFont);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        pLabel.setSpacingBefore(5);
        cell.addElement(pLabel);

        table.addCell(cell);
    }

    private void addAnalyticsCharts(Document document, PdfWriter writer, List<TripReport> reports)
            throws IOException, DocumentException {
        
        addSectionTitle(document, "Analyse Visuelle des Performances");

        // Ligne 1: Pie Chart + Line Chart
        PdfPTable row1 = new PdfPTable(2);
        row1.setWidthPercentage(100);
        row1.setSpacingBefore(15);
        row1.setSpacingAfter(15);

        // Graphique de conformité (Pie)
        DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
        long followed = reports.stream().filter(r -> Boolean.TRUE.equals(r.getRouteFollowed())).count();
        pieDataset.setValue("Conforme (" + followed + ")", followed);
        pieDataset.setValue("Non Conforme (" + (reports.size() - followed) + ")", reports.size() - followed);

        JFreeChart pieChart = ChartFactory.createPieChart(
            "Distribution de la Conformité", pieDataset, true, true, false);
        customizePieChart(pieChart);

        // Graphique des scores par chauffeur (Bar)
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        Map<Long, Double> driverScores = reports.stream()
            .collect(Collectors.groupingBy(
                TripReport::getDriverId,
                Collectors.averagingDouble(r -> r.getCompletionScore() != null ? r.getCompletionScore() : 0.0)));

        driverScores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(8)
            .forEach(entry -> {
                String driverName = reports.stream()
                    .filter(r -> r.getDriverId().equals(entry.getKey()))
                    .findFirst()
                    .map(TripReport::getDriverName)
                    .orElse("Chauffeur " + entry.getKey());
                barDataset.addValue(entry.getValue(), "Score", driverName);
            });

        JFreeChart barChart = ChartFactory.createBarChart(
            "Top 8 Chauffeurs - Score Moyen", 
            "Chauffeur", 
            "Score",
            barDataset, 
            PlotOrientation.VERTICAL, 
            false, 
            true, 
            false);
        customizeBarChart(barChart);

        java.awt.image.BufferedImage pieImage = pieChart.createBufferedImage(280, 220);
        java.awt.image.BufferedImage barImage = barChart.createBufferedImage(280, 220);

        Image piePdfImage = Image.getInstance(writer, pieImage, 1.0f);
        Image barPdfImage = Image.getInstance(writer, barImage, 1.0f);

        row1.addCell(createChartCell(piePdfImage));
        row1.addCell(createChartCell(barPdfImage));

        document.add(row1);

        // Ligne 2: Graphique d'efficacité
        addEfficiencyChart(document, writer, reports);
    }

    private void addEfficiencyChart(Document document, PdfWriter writer, List<TripReport> reports)
            throws IOException, DocumentException {
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Calculer l'efficacité moyenne par chauffeur
        Map<String, Double> driverEfficiency = reports.stream()
            .collect(Collectors.groupingBy(
                TripReport::getDriverName,
                Collectors.averagingDouble(r -> r.getRouteCompliancePercentage() != null 
                    ? r.getRouteCompliancePercentage() : 0.0)));

        driverEfficiency.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(8)
            .forEach(entry -> dataset.addValue(entry.getValue(), "Conformité %", entry.getKey()));

        JFreeChart chart = ChartFactory.createBarChart(
            "Taux de Conformité par Chauffeur",
            "Chauffeur",
            "Conformité (%)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false);

        customizeBarChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0, 100);

        java.awt.image.BufferedImage chartImage = chart.createBufferedImage(560, 250);
        Image pdfImage = Image.getInstance(writer, chartImage, 1.0f);
        
        PdfPTable chartTable = new PdfPTable(1);
        chartTable.setWidthPercentage(100);
        chartTable.setSpacingBefore(15);
        chartTable.addCell(createChartCell(pdfImage));
        
        document.add(chartTable);
    }

    private void customizePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 14));
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Conforme (" + plot.getDataset().getValue(0) + ")", SUCCESS_COLOR);
        plot.setSectionPaint("Non Conforme (" + plot.getDataset().getValue(1) + ")", DANGER_COLOR);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.setSimpleLabels(true);
    }

    private void customizeBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 14));
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(GRAY_LIGHT);
        plot.setOutlineVisible(false);
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new GradientPaint(0, 0, PRIMARY_COLOR, 0, 100, SECONDARY_COLOR));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 9));
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
    }

    private PdfPCell createChartCell(Image image) {
        PdfPCell cell = new PdfPCell(image, true);
        cell.setBorderColor(GRAY_LIGHT);
        cell.setBorderWidth(1);
        cell.setPadding(10);
        cell.setBackgroundColor(WHITE);
        return cell;
    }

    private void addDriverAnalysis(Document document, List<TripReport> reports) throws DocumentException {
        addSectionTitle(document, "Analyse Détaillée par Chauffeur");

        Map<String, List<TripReport>> reportsByDriver = reports.stream()
            .collect(Collectors.groupingBy(TripReport::getDriverName));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});
        table.setSpacingBefore(15);

        // En-têtes
        String[] headers = {"Chauffeur", "Trajets", "Score Moy.", "Conformité", "Stations", "Performance"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE)));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Données
        reportsByDriver.forEach((driverName, driverReports) -> {
            double avgScore = driverReports.stream()
                .mapToDouble(r -> r.getCompletionScore() != null ? r.getCompletionScore() : 0.0)
                .average().orElse(0.0);
            
            double avgCompliance = driverReports.stream()
                .mapToDouble(r -> r.getRouteCompliancePercentage() != null ? r.getRouteCompliancePercentage() : 0.0)
                .average().orElse(0.0);
            
            int totalStations = driverReports.stream()
                .mapToInt(r -> r.getStationsVisited() != null ? r.getStationsVisited() : 0)
                .sum();

            addDriverRow(table, driverName, driverReports.size(), avgScore, avgCompliance, totalStations);
        });

        document.add(table);
    }

    private void addDriverRow(PdfPTable table, String name, int trips, double avgScore, 
                              double avgCompliance, int stations) {
        // Nom
        PdfPCell nameCell = new PdfPCell(new Phrase(name, 
            FontFactory.getFont(FontFactory.HELVETICA, 9)));
        nameCell.setPadding(8);
        table.addCell(nameCell);

        // Trajets
        addDataCell(table, String.valueOf(trips));
        
        // Score
        addColoredCell(table, String.format("%.1f", avgScore), getScoreColor(avgScore));
        
        // Conformité
        addDataCell(table, String.format("%.1f%%", avgCompliance));
        
        // Stations
        addDataCell(table, String.valueOf(stations));
        
        // Performance (badge)
        String performance = avgScore >= 80 ? "Excellent" : avgScore >= 60 ? "Bon" : "À améliorer";
        Color perfColor = avgScore >= 80 ? SUCCESS_COLOR : avgScore >= 60 ? WARNING_COLOR : DANGER_COLOR;
        addBadgeCell(table, performance, perfColor);
    }

    private void addDetailedTable(Document document, List<TripReport> reports) throws DocumentException {
        addSectionTitle(document, "Liste Détaillée des Trajets");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 2.5f, 1.5f, 1.5f, 1.5f});
        table.setSpacingBefore(15);

        // En-têtes
        String[] headers = {"Date", "Trajet", "Chauffeur", "Score", "Conformité", "Statut"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, WHITE)));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Données avec alternance de couleurs
        int rowIndex = 0;
        for (TripReport report : reports) {
            Color bgColor = rowIndex % 2 == 0 ? WHITE : GRAY_LIGHT;
            
            // Date
            addStyledCell(table, report.getCreatedAt().format(formatter), bgColor);
            
            // Trajet
            addStyledCell(table, report.getTripName(), bgColor);
            
            // Chauffeur
            addStyledCell(table, report.getDriverName(), bgColor);
            
            // Score avec couleur
            PdfPCell scoreCell = new PdfPCell(new Phrase(
                String.format("%.1f", report.getCompletionScore()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, getScoreColor(report.getCompletionScore()))));
            scoreCell.setBackgroundColor(bgColor);
            scoreCell.setPadding(6);
            scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(scoreCell);
            
            // Conformité
            String compliance = report.getRouteCompliancePercentage() != null
                ? String.format("%.0f%%", report.getRouteCompliancePercentage())
                : "N/A";
            addStyledCell(table, compliance, bgColor);
            
            // Statut (badge)
            String status = Boolean.TRUE.equals(report.getRouteFollowed()) ? "✓ Conforme" : "✗ Non conforme";
            Color statusColor = Boolean.TRUE.equals(report.getRouteFollowed()) ? SUCCESS_COLOR : DANGER_COLOR;
            
            PdfPCell statusCell = new PdfPCell();
            statusCell.setBackgroundColor(bgColor);
            statusCell.setPadding(6);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPTable badgeTable = new PdfPTable(1);
            PdfPCell badge = new PdfPCell(new Phrase(status, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, WHITE)));
            badge.setBackgroundColor(statusColor);
            badge.setPadding(3);
            badge.setBorder(Rectangle.NO_BORDER);
            badge.setHorizontalAlignment(Element.ALIGN_CENTER);
            badgeTable.addCell(badge);
            
            statusCell.addElement(badgeTable);
            table.addCell(statusCell);
            
            rowIndex++;
        }

        document.add(table);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(20);
        titleTable.setSpacingAfter(10);
        
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(3);
        cell.setBorderColorBottom(PRIMARY_COLOR);
        cell.setPaddingBottom(8);
        
        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PRIMARY_COLOR);
        Paragraph p = new Paragraph(title, font);
        cell.addElement(p);
        
        titleTable.addCell(cell);
        document.add(titleTable);
    }

    private void addDataCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addStyledCell(PdfPTable table, String text, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addColoredCell(PdfPTable table, String text, Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, color)));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addBadgeCell(PdfPTable table, String text, Color color) {
        PdfPCell outerCell = new PdfPCell();
        outerCell.setPadding(8);
        outerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        PdfPTable innerTable = new PdfPTable(1);
        PdfPCell badgeCell = new PdfPCell(new Phrase(text, 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, WHITE)));
        badgeCell.setBackgroundColor(color);
        badgeCell.setPadding(4);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        innerTable.addCell(badgeCell);
        
        outerCell.addElement(innerTable);
        table.addCell(outerCell);
    }

    private Color getScoreColor(double score) {
        if (score >= 80) return SUCCESS_COLOR;
        if (score >= 60) return WARNING_COLOR;
        return DANGER_COLOR;
    }

    /**
     * Classe pour gérer les en-têtes et pieds de page
     */
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            
            // En-tête
            Phrase header = new Phrase("NeoRoute - Système de Gestion Logistique", 
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRAY_DARK));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                header,
                document.left(), document.top() + 20,
                0);
            
            // Ligne de séparation en-tête
            cb.setLineWidth(0.5f);
            cb.setColorStroke(GRAY_LIGHT);
            cb.moveTo(document.left(), document.top() + 10);
            cb.lineTo(document.right(), document.top() + 10);
            cb.stroke();
            
            // Pied de page
            Phrase footer = new Phrase(
                "Page " + writer.getPageNumber() + " | Généré le " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 8, GRAY_DARK));
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                footer,
                (document.right() + document.left()) / 2,
                document.bottom() - 20,
                0);
            
            // Ligne de séparation pied de page
            cb.setLineWidth(0.5f);
            cb.setColorStroke(GRAY_LIGHT);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();
        }
    }
}