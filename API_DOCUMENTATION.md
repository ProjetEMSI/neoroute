# Documentation de l'API NeoRoute

Ce document détaille les endpoints de l'API NeoRoute pour l'intégration Front-End.

## Informations Générales

*   **URL de base** : `http://localhost:8080/api`
*   **Format des données** : JSON (`Content-Type: application/json`)
*   **Authentification** : Token JWT (Bearer Token).
    *   Après la connexion (`/auth/login`), le serveur renvoie un token.
    *   Ce token doit être inclus dans le header `Authorization` de toutes les requêtes sécurisées (Admin et Driver/Chauffeur).
    *   **Format du Header** : `Authorization: Bearer <votre_token_jwt>`

---

## 1. Authentification (Public)

Ces endpoints sont accessibles sans token.

### 1.1 Connexion (Login)
S'authentifier pour récupérer le token JWT.

*   **URL** : `/auth/login`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "username": "admin",
      "password": "admin123"
    }
    ```
*   **Réponse (200 OK)** :
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "type": "Bearer",
      "username": "admin",
      "role": "ADMIN",
      "userId": 1
    }
    ```
    > **Note** : Sauvegardez le `token` pour les requêtes suivantes. `role` indique si l'interface Admin ou Driver doit être affichée.

---

## 2. Administration (Utilisateurs)

Nécessite le rôle `ADMIN`.

### 2.1 Liste des Utilisateurs
*   **URL** : `/admin/users`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Liste de tous les utilisateurs (Admins et Chauffeurs).

### 2.2 Créer un Utilisateur
*   **URL** : `/admin/users`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "username": "chauffeur1",
      "email": "chauffeur1@neoroute.com",
      "password": "password123",
      "role": "DRIVER" // ou "ADMIN"
    }
    ```
*   **Réponse (201 Created)**.

### 2.3 Modifier un Utilisateur
*   **URL** : `/admin/users/{id}`
*   **Méthode** : `PUT`
*   **Body (JSON)** :
    ```json
    {
      "email": "newemail@example.com", // optionnel
      "password": "newpassword",       // optionnel
      "active": true                   // optionnel
    }
    ```

### 2.4 Désactiver/Activer un Utilisateur
*   **URL** : `/admin/users/{id}`
*   **Méthode** : `DELETE`
*   **Description** : Bascule le statut actif/inactif.
*   **Réponse (200 OK)**.

---

## 3. Administration (Stations)

Nécessite le rôle `ADMIN`.

### 3.1 Liste des Stations
*   **URL** : `/admin/stations`
*   **Méthode** : `GET`
*   **Paramètre (Optionnel)** : `?activeOnly=true` (par défaut `true`). Mettre `false` pour voir aussi les stations supprimées.
*   **Réponse (200 OK)** :
    ```json
    [
      {
        "id": 1,
        "name": "Hub Central Casablanca",
        "latitude": 33.5731,
        "longitude": -7.5898,
        "active": true
      }
    ]
    ```

### 3.2 Créer une Station
*   **URL** : `/admin/stations`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "name": "Hub Central Casablanca",
      "latitude": 33.5731,
      "longitude": -7.5898
    }
    ```
*   **Réponse (201 Created)**.

### 3.3 Modifier une Station
*   **URL** : `/admin/stations/{id}`
*   **Méthode** : `PUT`
*   **Body (JSON)** : Idem création.

### 3.4 Supprimer une Station (Soft Delete)
*   **URL** : `/admin/stations/{id}`
*   **Méthode** : `DELETE`
*   **Réponse (204 No Content)**.

---

## 4. Administration (Trajets / Trips)

Nécessite le rôle `ADMIN`.

### 4.1 Lister les Trajets
*   **URL** : `/admin/trips`
*   **Méthode** : `GET`
*   **Paramètre (Optionnel)** : `?status=CREATED` (ou `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
*   **Réponse (200 OK)** : Liste des trajets.

### 4.2 Créer un Trajet (Optimisation de Route)
Planifie un trajet en spécifiant la station de départ et les stations à visiter. L'algorithme optimisera l'ordre.

*   **URL** : `/admin/trips`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "name": "Tournée Nord Matin",
      "startStationId": 1,         // ID de la station de départ (Hub)
      "requiredStationIds": [      // IDs des stations à visiter
          2, 4, 5
      ]
    }
    ```
*   **Réponse (201 Created)**.

### 4.3 Détails d'un Trajet
*   **URL** : `/admin/trips/{id}`
*   **Méthode** : `GET`
*   **Réponse (200 OK)**.

### 4.4 Modifier un Trajet
MODIFIABLE SEULEMENT SI STATUT = `CREATED` ou `ASSIGNED`.
*   **URL** : `/admin/trips/{id}`
*   **Méthode** : `PUT`
*   **Body (JSON)** :
    ```json
    {
      "name": "Nouveau Nom",
      "startStationId": 1,
      "requiredStationIds": [2, 3]
    }
    ```

### 4.5 Assigner un Chauffeur
*   **URL** : `/admin/trips/{id}/assign`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "driverId": 2
    }
    ```

### 4.6 Annuler un Trajet
*   **URL** : `/admin/trips/{id}/cancel`
*   **Méthode** : `PUT`

---

## 5. Espace Chauffeur (Driver)

Nécessite le rôle `DRIVER`.

### 5.1 Mes Trajets Assignés
*   **URL** : `/driver/trips/assigned`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Liste des trajets actifs (ASSIGNED, IN_PROGRESS).

### 5.2 Détails d'un Trajet
*   **URL** : `/driver/trips/{id}`
*   **Méthode** : `GET`

### 5.3 Démarrer un Trajet
*   **URL** : `/driver/trips/{id}/start`
*   **Méthode** : `POST`
*   **Réponse (200 OK)** : Le statut passe à `IN_PROGRESS`.

### 5.4 Valider une visite (Station)
Indique que le chauffeur est arrivé à une station.

*   **URL** : `/driver/trips/{tripId}/visit/{stationId}`
*   **Méthode** : `POST`
*   **Réponse (200 OK)**.

### 5.5 Terminer le Trajet
*   **URL** : `/driver/trips/{id}/complete`
*   **Méthode** : `POST`
*   **Réponse (200 OK)** : Clôture le trajet, calcule le score.

### 5.6 Mon Profil
*   **URL** : `/driver/profile`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Stats personnelles.

### 5.7 Historique des Trajets
*   **URL** : `/driver/profile/history`
*   **Méthode** : `GET`

---

## 6. Rapports & Statistiques (Admin)

Nécessite le rôle `ADMIN`.

### 6.1 Rapports de Trajets
*   **URL** : `/admin/reports/trips`
*   **Méthode** : `GET`
*   **Paramètres (Optionnels)** :
    *   `driverId`: ID du chauffeur pour filtrer.
    *   `startDate`: Date de début (ISO Date Time), ex: `2023-01-01T00:00:00`.
    *   `endDate`: Date de fin.
*   **Réponse (200 OK)** : Liste détaillée des performances par trajet.

### 6.2 Exporter en PDF
*   **URL** : `/admin/reports/export/pdf`
*   **Méthode** : `GET`
*   **Paramètres (Optionnels)** : Idem ci-dessus (`driverId`, `startDate`, `endDate`).
*   **Réponse (200 OK)** : Fichier binaire PDF.

### 6.3 Statistiques des Chauffeurs
*   **URL** : `/admin/reports/drivers`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Performance globale par chauffeur.

### 6.4 Résumé Système
*   **URL** : `/admin/reports/summary`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Métriques globales.

---

## 7. Notifications en Temps Réel (WebSocket)

### 7.1 Connexion WebSocket
*   **URL** : `http://localhost:8080/ws` (Note: Endpoint SockJS)
*   **Protocole** : STOMP over SockJS

### 7.2 Abonnement
*   **Topic** : `/topic/driver/{driverId}`
*   **Message** : "Nouveau trajet assigné : [Nom du trajet]"
