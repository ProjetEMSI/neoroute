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

### 1.2 Connexion (Login)
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

## 2. Administration (Stations)

Nécessite le rôle `ADMIN`.

### 2.1 Liste des Stations
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
        "acive": true
      },
      ...
    ]
    ```

### 2.2 Créer une Station
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
*   **Réponse (201 Created)** : Retourne l'objet station créé avec son `id`.

### 2.3 Modifier une Station
*   **URL** : `/admin/stations/{id}`
*   **Méthode** : `PUT`
*   **Body (JSON)** : Idem création.

### 2.4 Supprimer une Station (Soft Delete)
*   **URL** : `/admin/stations/{id}`
*   **Méthode** : `DELETE`
*   **Réponse (204 No Content)**.

---

## 3. Administration (Trajets / Trips)

Nécessite le rôle `ADMIN`.

### 3.1 Créer un Trajet (Optimisation de Route)
Planifie un trajet en spécifiant la station de départ et les stations à visiter. L'algorithme optimisera l'ordre.

*   **URL** : `/admin/trips`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "name": "Tournée Nord Matin",
      "startStationId": 1,         // ID de la station de départ (Hub)
      "requiredStationIds": [      // IDs des stations à visiter
          2,
          4,
          5
      ]
    }
    ```
*   **Réponse (201 Created)** : Retourne le trajet créé avec l'ordre optimisé des stations, la distance estimée et la durée.

### 3.2 Détails d'un Trajet
*   **URL** : `/admin/trips/{id}`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Contient les détails du trajet, le statut (`PLANNED`, `IN_PROGRESS`, `COMPLETED`), et la liste ordonnée des étapes (`routeKeyPoints`).

### 3.3 Assigner un Chauffeur
*   **URL** : `/admin/trips/{id}/assign`
*   **Méthode** : `POST`
*   **Body (JSON)** :
    ```json
    {
      "driverId": 2  // ID de l'utilisateur (rôle DRIVER)
    }
    ```
*   **Réponse (200 OK)**.

### 3.4 Annuler un Trajet
*   **URL** : `/admin/trips/{id}/cancel`
*   **Méthode** : `PUT`

---

## 4. Espace Chauffeur (Driver)

Nécessite le rôle `DRIVER`.

### 4.1 Mes Trajets Assignés
*   **URL** : `/driver/trips/assigned`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Liste des trajets assignés au chauffeur connecté.

### 4.2 Démarrer un Trajet
*   **URL** : `/driver/trips/{id}/start`
*   **Méthode** : `POST`
*   **Réponse (200 OK)** : Le statut du trajet passe à `IN_PROGRESS`.

### 4.3 Valider une visite (Station)
Indique que le chauffeur est arrivé à une station.

*   **URL** : `/driver/trips/{tripId}/visit/{stationId}`
*   **Méthode** : `POST`
*   **Réponse (200 OK)** : Marque l'étape comme visitée.

### 4.4 Terminer le Trajet
*   **URL** : `/driver/trips/{id}/complete`
*   **Méthode** : `POST`
*   **Réponse (200 OK)** : Clôture le trajet, calcule le score du chauffeur et génère le rapport.

### 4.5 Mon Profil et Statistiques
*   **URL** : `/driver/profile`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Informations du chauffeur, score global, nombre de livraisons.

### 4.6 Historique des Trajets
*   **URL** : `/driver/profile/history`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Liste des trajets passés.

---

## 5. Rapports & Statistiques (Admin)

Nécessite le rôle `ADMIN`.

### 5.1 Rapports de Trajets
*   **URL** : `/admin/reports/trips`
*   **Méthode** : `GET`
*   **Paramètres (Optionnels)** : `?driverId=X` pour filtrer par chauffeur.
*   **Réponse (200 OK)** : Liste détaillée des performances par trajet (temps réel vs estimé, score).

### 5.2 Statistiques des Chauffeurs
*   **URL** : `/admin/reports/drivers`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Classement et performance globale de chaque chauffeur.

### 5.3 Résumé Système
*   **URL** : `/admin/reports/summary`
*   **Méthode** : `GET`
*   **Réponse (200 OK)** : Métriques globales (total km, total trajets, efficacité moyenne).

---

## 6. Notifications en Temps Réel (WebSocket)

Les chauffeurs peuvent recevoir des notifications en temps réel via WebSocket lorsqu'un trajet leur est assigné.

### 6.1 Connexion WebSocket
*   **URL** : `ws://localhost:8080/api/ws`
*   **Protocole** : STOMP over WebSocket / SockJS

### 6.2 Abonnement aux Notifications
Les chauffeurs doivent s'abonner à leur topic personnel :
*   **Topic** : `/topic/driver/{driverId}`
*   **Exemple** : `/topic/driver/2`

**Code JavaScript exemple** :
```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  stompClient.subscribe('/topic/driver/' + driverId, function(message) {
    console.log('Notification:', message.body);
    // Afficher notification à l'utilisateur
  });
});
```

### 6.3 Format du Message
Lorsqu'un trajet est assigné, le message WebSocket contient :
```
"Nouveau trajet assigné : [Nom du trajet]"
```

> **Note** : En parallèle, un email est également envoyé à l'adresse du chauffeur.
