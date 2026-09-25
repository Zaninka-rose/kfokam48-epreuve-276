# Journal de projet

Ce journal trace toutes les évolutions et décisions notables du projet.
Une entrée par évolution, en ordre antichronologique (la plus récente en premier).
Versionnement : SemVer (MAJEUR.MINEUR.CORRECTIF).

| Date | Version | Auteur | Type | Description |
|---|---|---|---|---|
| 25/09/2026 | 0.4.0 | <Nom, prénom> | Technique | Squelettes conformes aux contraintes de dépôt : packages backend renommés (entity/dto/exception), driver PostgreSQL + profil dédié et docker-compose, couche API dédiée côté frontend (api/client.ts) avec page de vérification, README d'installation (B1, B3, F1, F2). |
| 25/09/2026 | 0.3.0 | <Nom, prénom> | Fonctionnel | Squelette backend : couches contrôleur/service/repository, entités et migrations Flyway (sessions, présences, exercices, relectures), gestion centralisée des erreurs (B3, B4, B5). |
| 25/09/2026 | 0.2.0 | <Nom, prénom> | Fonctionnel | Création de `docs/diagrammes/` : D1 cas d'utilisation, D2 classes, D3 séquence (Mermaid), dérivés du cahier des charges. |
| 25/09/2026 | 0.1.1 | <Nom, prénom> | Décision | Rétrogradé le backend de Java 21 à Java 17 (`pom.xml`, README, cahier des charges). |
| 25/09/2026 | 0.1.0 | <Nom, prénom> | Initialisation | Création de la structure du projet, backend Spring Boot (Java 21, mvnw), frontend Angular 22, contrat OpenAPI initial (`api/contrat.yaml`), cahier des charges et dossier diagrammes. |

Types admis : `Initialisation` · `Fonctionnel` · `Correctif` · `Technique` · `Décision`

Règle : toute modification structurante du projet (nouvelle exigence, changement de stack, décision d'architecture) donne lieu à une entrée dans ce journal.
