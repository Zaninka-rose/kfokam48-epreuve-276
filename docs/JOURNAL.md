# Journal de projet

Ce journal trace toutes les évolutions et décisions notables du projet.
Une entrée par évolution, en ordre antichronologique (la plus récente en premier).
Versionnement : SemVer (MAJEUR.MINEUR.CORRECTIF).

| Date | Version | Auteur | Type | Description |
|---|---|---|---|---|
| 25/09/2026 | 0.8.0 | Zaninka-rose | Fonctionnel | Issue #18 — écran formateur « tableau de bord » : saisie de session, GET /api/sessions/{id}/tableau via la couche API dédiée, lignes par étudiant (présences, exercices déposés avec statut, moyenne calculée sur les relectures rendues RG9, relectures en attente) ; route /tableau-bord, tests vitest (affichage, moyenne null, 404, réseau, validation). |
| 25/09/2026 | 0.7.0 | Zaninka-rose | Fonctionnel | Issue #17 — écran formateur « Ouvrir une session » : formulaire d'identifiant, POST /api/sessions via la couche API dédiée, affichage du code de présence et de son expiration ; route /ouvrir-session, tests vitest des trois critères (201, erreur réseau, erreur HTTP). |
| 25/09/2026 | 0.6.6 | Zaninka-rose | Fonctionnel | Issue #11 — EF12 clôture de session : 200 + marque session clôturée, dépôt et présence refusés (409 SESSION_CLOTUREE, RG10) ; vérifié aussi que le dépôt reste possible après simple expiration du code (§7). |
| 25/09/2026 | 0.6.0 | Zaninka-rose | Fonctionnel | Issue #5 — EF1 ouverture de session : code unique généré, expiration à ouverture + 15 min (RG1), tests d'acceptation. Réparation des incohérences de fusion (CodeErreur/RegleMetierException repositionnés dans exception). |
| 25/09/2026 | 0.5.1 | Zaninka-rose | Fonctionnel | Issue #4 — marquage de présence par code : tests d'intégration des trois critères d'acceptation (201 présence visible au tableau, 410 CODE_EXPIRE, 409 DEJA_PRESENT), code métier exposé dans l'enveloppe d'erreur, séquences IDENTITY recalées après le seed. |
| 25/09/2026 | 0.5.0 | Zaninka-rose | Décision | Contrat OpenAPI complété (v0.2.0) : les 5 opérations imposées (POST /api/sessions, POST /api/presences, POST /api/exercices, POST /api/relectures/{idExercice}, GET /api/tableau) plus clôture, présence manuelle, remplacement de lien et résultat ; enveloppe d'erreur enrichie du code métier (ENF3). |
| 25/09/2026 | 0.4.1 | Zaninka-rose | Fonctionnel | Données de démonstration au démarrage : 1 promotion, 10 étudiants, session ouverte avec code valide 15 min (V2__seed.sql relative à l'horodatage) ; test d'intégration du seed. |
| 25/09/2026 | 0.4.0 | Zaninka-rose | Technique | Squelettes conformes aux contraintes de dépôt : packages backend renommés (entity/dto/exception), driver PostgreSQL + profil dédié et docker-compose, couche API dédiée côté frontend (api/client.ts) avec page de vérification, README d'installation (B1, B3, F1, F2). |
| 25/09/2026 | 0.3.0 | Zaninka-rose | Fonctionnel | Squelette backend : couches contrôleur/service/repository, entités et migrations Flyway (sessions, présences, exercices, relectures), gestion centralisée des erreurs (B3, B4, B5). |
| 25/09/2026 | 0.2.0 | Zaninka-rose | Fonctionnel | Création de `docs/diagrammes/` : D1 cas d'utilisation, D2 classes, D3 séquence (Mermaid), dérivés du cahier des charges. |
| 25/09/2026 | 0.1.1 | Zaninka-rose | Décision | Rétrogradé le backend de Java 21 à Java 17 (`pom.xml`, README, cahier des charges). |
| 25/09/2026 | 0.1.0 | Zaninka-rose | Initialisation | Création de la structure du projet, backend Spring Boot (Java 21, mvnw), frontend Angular 22, contrat OpenAPI initial (`api/contrat.yaml`), cahier des charges et dossier diagrammes. |

Types admis : `Initialisation` · `Fonctionnel` · `Correctif` · `Technique` · `Décision`

Règle : toute modification structurante du projet (nouvelle exigence, changement de stack, décision d'architecture) donne lieu à une entrée dans ce journal.
