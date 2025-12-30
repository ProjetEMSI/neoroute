package com.neoroute.logistics.service.notification;

import com.neoroute.logistics.domain.trip.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service centralisé pour les notifications (Email + WebSocket).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notifie un chauffeur de l'assignation d'un trajet.
     *
     * @param driverEmail Email du chauffeur
     * @param driverId    ID du chauffeur
     * @param trip        Trajet assigné
     */
    public void notifyTripAssignment(String driverEmail, Long driverId, Trip trip) {
        // 1. Notification Email
        String subject = "Nouveau trajet assigné : " + trip.getName();
        String message = String.format("""
                Bonjour,

                Un nouveau trajet vous a été assigné.

                Nom du trajet : %s
                Statut : %s

                Connectez-vous à l'application NeoRoute pour voir les détails.

                Cordialement,
                L'équipe NeoRoute
                """, trip.getName(), trip.getStatus());

        emailService.sendEmail(driverEmail, subject, message);

        // 2. Notification WebSocket en temps réel
        String destination = "/topic/driver/" + driverId;
        String wsMessage = "Nouveau trajet assigné : " + trip.getName();

        try {
            messagingTemplate.convertAndSend(destination, wsMessage);
            log.info("Notification WebSocket envoyée au driver {} sur {}", driverId, destination);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification WebSocket : {}", e.getMessage());
        }
    }
}
