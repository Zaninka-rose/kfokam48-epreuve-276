# Cahier des charges — mon-projet

| | |
|---|---|
| Version du document | 0.1.0 |
| Date | 25/09/2026 |
| Rédacteur(s) | <Nom, prénom> |
| Statut | Brouillon |

## 1. Présentation

### 1.1 Contexte
<À COMPLÉTER — problème à résoudre, origine du besoin, enjeux.>

### 1.2 Objectifs
- <Objectif principal>
- <Objectif secondaire>

### 1.3 Périmètre
- **Inclus :** <fonctionnalités couvertes>
- **Exclus :** <fonctionnalités hors périmètre, hypothèses limites>

## 2. Acteurs

| Acteur | Rôle |
|---|---|
| <Utilisateur> | <Utilise l'application via le frontend Angular> |
| <Administrateur> | <Gère les données et les accès> |

## 3. Exigences fonctionnelles

| Réf. | Exigence | Priorité (MoSCoW) |
|---|---|---|
| RF-01 | <À COMPLÉTER — décrire chaque fonctionnalité attendue> | Must |
| RF-02 | <À COMPLÉTER> | Should |

## 4. Exigences non fonctionnelles

| Réf. | Exigence | Cible |
|---|---|---|
| RNF-01 | Performance | Réponse API < 500 ms (P95) |
| RNF-02 | Sécurité | Couverture OWASP Top 10, authentification des endpoints sensibles |
| RNF-03 | Disponibilité | 99 % en production |
| RNF-04 | Compatibilité | 2 dernières versions majeures des navigateurs principaux |
| RNF-05 | Accessibilité | Conforme WCAG 2.1 niveau AA |
| RNF-06 | Testabilité | Tests unitaires backend et frontend, taux de couverture >= 70 % |

## 5. Contraintes techniques (imposées)

- **Backend :** Spring Boot (Java 21+), Maven, wrapper `mvnw` commité.
- **Frontend :** Angular 22.
- **Interface :** contrat OpenAPI `api/contrat.yaml` — source de vérité unique entre backend et frontend.
- **Documentation :** dossier `docs/` (cahier des charges, journal de projet, diagrammes).

## 6. Livrables

- Code source backend et frontend versionné (Git).
- Contrat d'interface `api/contrat.yaml` maintenu à jour.
- Documentation fonctionnelle et technique dans `docs/`.
- Procédure de démarrage local (README racine).

## 7. Planning et jalons

| Jalon | Contenu | Date cible |
|---|---|---|
| J1 | Initialisation projet et contrat API | 25/09/2026 |
| J2 | <À COMPLÉTER> | <JJ/MM/AAAA> |
| J3 | <À COMPLÉTER> | <JJ/MM/AAAA> |

## 8. Critères d'acceptation

- <À COMPLÉTER — conditions mesurables de validation du projet.>

## 9. Glossaire

| Terme | Définition |
|---|---|
| Contrat API | Spécification OpenAPI décrivant l'interface backend/frontend |
| <À COMPLÉTER> | <À COMPLÉTER> |
