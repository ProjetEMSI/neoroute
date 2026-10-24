# NeoRoute - API de gestion des livraisons

NeoRoute est une plateforme **web et mobile** de planification et de suivi des livraisons. Ce dépôt contient l'API backend Spring Boot. L'interface web React et l'application mobile React Native sont dans des dépôts séparés.

## Dépôts du projet

| Composant | Dépôt |
| --- | --- |
| API Spring Boot | [ProjetEMSI/neoroute](https://github.com/ProjetEMSI/neoroute) |
| Interface web React | [ProjetEMSI/intelligent-delivery-hub](https://github.com/ProjetEMSI/intelligent-delivery-hub) |
| Application mobile Expo | [aymenyassine/intelligent-delivery-mobile](https://github.com/aymenyassine/intelligent-delivery-mobile) |

## Fonctionnalités présentes dans le code

- Connexion avec jeton JWT et accès selon les rôles `ADMIN` et `DRIVER`.
- Administration des utilisateurs, des stations et des trajets ; attribution des trajets aux chauffeurs.
- Espace chauffeur : trajets assignés, démarrage, validation des stations visitées, clôture et historique.
- Calcul des distances et de l'ordre de visite des stations pour les trajets.
- Rapports, statistiques et export PDF ; services de notifications par WebSocket et par email.
- Persistance des données dans Neo4j.

La [documentation de l'API](API_DOCUMENTATION.md) décrit les routes, les corps de requête et les rôles requis.

## Stack technique

Java 17 · Spring Boot 3.5.9 · Spring Data Neo4j · Spring Security · JWT · WebSocket · Spring Mail · OpenPDF · SpringDoc OpenAPI · Maven

## Démarrage local

Prérequis : **JDK 17** et une instance **Neo4j**. Le projet inclut le Maven Wrapper. La configuration actuelle utilise l'adresse Bolt `bolt://localhost:7687` ; adaptez `src/main/resources/application.yml` à votre instance et définissez des secrets de développement pour JWT et, si nécessaire, pour l'envoi d'emails. Ne publiez pas de véritables identifiants dans le dépôt.

```bash
git clone https://github.com/ProjetEMSI/neoroute.git
cd neoroute
./mvnw spring-boot:run
```

Sous Windows, utilisez `mvnw.cmd spring-boot:run`. La [documentation de l'API](API_DOCUMENTATION.md) utilise `http://localhost:8080/api` comme URL de base.

## Quelques routes

| Fonction | Méthode et route |
| --- | --- |
| Connexion | `POST /api/auth/login` |
| Liste des utilisateurs | `GET /api/admin/users` |
| Liste des stations | `GET /api/admin/stations` |
| Liste des trajets | `GET /api/admin/trips` |
| Trajets assignés au chauffeur | `GET /api/driver/trips/assigned` |
| Rapports de trajets | `GET /api/admin/reports/trips` |

Les routes protégées attendent un en-tête `Authorization: Bearer <token>`. Consultez [API_DOCUMENTATION.md](API_DOCUMENTATION.md) pour les chemins exacts des sous-routes et les exemples de requêtes.

## Organisation du code

- `controller/auth`, `controller/admin`, `controller/driver` : points d'entrée HTTP.
- `service/` : logique métier pour les trajets, stations, utilisateurs, rapports, scores et notifications.
- `domain/` et `repository/` : modèle et accès aux données Neo4j.
- `security/` et `config/` : JWT, droits d'accès et configuration technique.
- `src/test/` : tests du projet.

Pour lancer les tests avec le Maven Wrapper :

```bash
./mvnw test
```
