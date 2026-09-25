# mon-projet — KFOKAM48

Suivi des présences, dépôt d'exercices et relecture par les pairs pour la direction de la formation KFOKAM48.

Projet full-stack : backend Spring Boot + frontend **Angular 22**, guidés par un contrat OpenAPI.

## Framework frontend

**Angular 22** — retenu car cette version stabilise le modèle réactif du framework (signals) et offre un socle outillé complet (CLI, build, tests) cohérent avec le contrat d'interface.

## Démarrage local (3 commandes depuis un clone vierge)

```bash
# 1. (optionnel) base PostgreSQL pour le backend — sinon H2 en mémoire, rien à faire
docker compose up -d db

# 2. Backend — http://localhost:8080 (H2 par défaut ; SPRING_PROFILES_ACTIVE=postgres pour PostgreSQL)
cd backend && ./mvnw spring-boot:run

# 3. Frontend — http://localhost:4200 (proxifie /api vers le backend)
cd frontend && npm install && npm start
```

Le serveur de dev Angular proxifie les appels `/api` vers http://localhost:8080 (voir `frontend/proxy.conf.json`). Vérification : la page d'accueil du frontend appelle `GET /api/ping` du backend et affiche son état.

### Données de démonstration

Au premier démarrage, la migration Flyway `V2__seed.sql` charge automatiquement : 1 promotion (KFOKAM48), 10 étudiants et 1 session déjà ouverte avec le code de présence **`DEMO1234`**, valide 15 minutes (RG1). Aucune action manuelle n'est requise après `docker compose up`.

Avec la base H2 en mémoire (défaut), ces données sont recréées à chaque lancement. Avec PostgreSQL (volume persistant), la session de démo correspond au tout premier démarrage ; régénérer avec `docker compose down -v`.

## Architecture

| Dossier | Contenu |
|---|---|
| docs/ | Cahier des charges, journal de projet, diagrammes (D1, D2, D3) |
| api/ | Contrat OpenAPI (`contrat.yaml`) — source de vérité de l'interface |
| backend/ | API REST Spring Boot (Java 17, Maven, mvnw), Flyway, H2/PostgreSQL |
| frontend/ | Application Angular 22, couche API dédiée (`src/app/api/client.ts`) |

### Backend — organisation par couches

```
backend/src/main/java/com/exemple/backend/
├── web/          contrôleurs REST (Session, Exercice, Relecture) + PingController
├── service/      règles de gestion (RG1-RG13)
├── repository/   accès données Spring Data JPA
├── dto/          objets de transfert entrée/sortie
├── entity/       entités JPA
└── exception/    RegleMetierException + gestionnaire global d'erreurs
```

Schéma de base versionné par Flyway (`backend/src/main/resources/db/migration/`).

Toute évolution de l'API commence par une modification de `api/contrat.yaml`.
