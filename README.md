# mon-projet

Projet full-stack : backend Spring Boot + frontend Angular, guidés par un contrat OpenAPI.

## Arborescence

| Dossier | Contenu |
|---|---|
| docs/ | Cahier des charges, journal de projet, diagrammes |
| api/ | Contrat OpenAPI (`contrat.yaml`) — source de vérité de l'interface |
| backend/ | API REST Spring Boot (Java 21+, Maven, mvnw) |
| frontend/ | Application Angular 22 |

## Démarrage local

| Service | Commande | URL |
|---|---|---|
| Backend | `cd backend && ./mvnw spring-boot:run` | http://localhost:8080 |
| Frontend | `cd frontend && npm start` | http://localhost:4200 |

Le serveur de dev Angular proxifie les appels `/api` vers http://localhost:8080 (voir `frontend/proxy.conf.json`).

Toute évolution de l'API commence par une modification de `api/contrat.yaml`.
