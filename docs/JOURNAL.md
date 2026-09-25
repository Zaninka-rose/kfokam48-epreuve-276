# Journal de projet

Ce journal trace toutes les évolutions et décisions notables du projet.
Une entrée par évolution, en ordre antichronologique (la plus récente en premier).
Versionnement : SemVer (MAJEUR.MINEUR.CORRECTIF).

| Date | Version | Auteur | Type | Description |
|---|---|---|---|---|
| 25/09/2026 | 0.5.0 | <Nom, prénom> | Décision | Contrat OpenAPI complété (v0.2.0) : les 5 opérations imposées (POST /api/sessions, POST /api/presences, POST /api/exercices, POST /api/relectures/{idExercice}, GET /api/tableau) plus clôture, présence manuelle, remplacement de lien et résultat ; enveloppe d'erreur enrichie du code métier (ENF3). |
| 25/09/2026 | 0.2.0 | <Nom, prénom> | Fonctionnel | Création de `docs/diagrammes/` : D1 cas d'utilisation, D2 classes, D3 séquence (Mermaid), dérivés du cahier des charges. |
| 25/09/2026 | 0.1.1 | <Nom, prénom> | Décision | Rétrogradé le backend de Java 21 à Java 17 (`pom.xml`, README, cahier des charges). |
| 25/09/2026 | 0.1.0 | <Nom, prénom> | Initialisation | Création de la structure du projet, backend Spring Boot (Java 21, mvnw), frontend Angular 22, contrat OpenAPI initial (`api/contrat.yaml`), cahier des charges et dossier diagrammes. |

Types admis : `Initialisation` · `Fonctionnel` · `Correctif` · `Technique` · `Décision`

Règle : toute modification structurante du projet (nouvelle exigence, changement de stack, décision d'architecture) donne lieu à une entrée dans ce journal.
