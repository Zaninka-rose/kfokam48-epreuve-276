# Cahier des charges — Gestion de présence, exercices et relecture (KFOKAM48)

Auteur : KF48-276 · Version 1 · Frontend choisi : <Angular22>, parce que Angular 22 est un choix pertinent car cette version marque un tournant dans l'histoire du framework : c'est la version de la maturité pour son nouveau modèle réactif. Elle offre un socle technique, stabilise des outils qui étaient expérimentaux ou en développement depuis plusieurs versions.

## 1. Contexte et objectif

La direction de la formation KFOKAM48 doit pouvoir, pour chaque session de cours :
suivre la présence des étudiants, recueillir leurs exercices, organiser une relecture
par les pairs, et disposer d'une vue d'ensemble par étudiant (présence, dépôts, notes).
L'application répond au besoin de remplacer un suivi manuel, non centralisé et non fiable,
par un outil unique consulté par le formateur en temps réel.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |

| Formateur | Ouvre une session, obtient un code, ajoute une présence manuelle, clôture la session, consulte le tableau de bord |
| Étudiant | Saisit un code pour marquer sa présence, dépose ou remplace le lien de son exercice |
| Relecteur | Un étudiant, désigné automatiquement, qui note et commente l'exercice d'un pair |
| Système | Génère les codes, assigne les relecteurs au hasard, calcule les moyennes |

## 3. Périmètre

**Inclus** : ouverture de session et code de présence, marquage de présence, dépôt
d'exercice, assignation et saisie de relecture, tableau de bord formateur.

**Exclu** : authentification par mot de passe (Q1), gestion du contenu pédagogique des
cours, notifications email/SMS, gestion de plusieurs formateurs par session, appel
d'une note après validation (RG8), création/gestion des promotions et des comptes
étudiants (supposées préexistantes, hors périmètre de cette application).

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |

| EF1 | Le formateur ouvre une session et obtient un code | `POST /api/sessions` renvoie 201 avec un code, une date d'ouverture et d'expiration | Must |
| EF2 | L'étudiant marque sa présence avec un code valide | Code valide → présence créée, visible dans le tableau formateur | Must |
| EF3 | Un code expiré est refusé | Code utilisé après 15 min → 410 `CODE_EXPIRE` | Must |
| EF4 | Une double présence est refusée | Deuxième tentative du même étudiant sur la même session → 409 `DEJA_PRESENT` | Must |
| EF5 | L'étudiant dépose le lien de son exercice | `POST /api/exercices` renvoie 201, statut initial "en attente d'assignation" ou "en attente de relecture" | Must |
| EF6 | L'étudiant remplace le lien avant le début de la relecture | Remplacement accepté tant qu'aucune relecture n'a commencé, refusé après (RG11) | Should |
| EF7 | Le système assigne un relecteur au hasard parmi les présents, jamais l'auteur | Un exercice sans relecteur éligible reste "en attente d'assignation" (RG13) | Must |
| EF8 | Le relecteur envoie une note et un commentaire | `POST /api/relectures/{id}` 200, note entière 0–20, refus si auteur (403) ou déjà rendue (409) | Must |
| EF9 | L'étudiant relu consulte sa note et le commentaire, sans l'identité du relecteur | Réponse ne contient jamais l'identifiant du relecteur | Must |
| EF10 | Le formateur consulte le tableau de bord | `GET /api/tableau` renvoie, par étudiant : présences, exercices déposés, moyenne, relectures en attente | Must |
| EF11 | Le formateur ajoute une présence manuelle | Présence créée avec `source: FORMATEUR`, visible comme telle | Should |
| EF12 | Le formateur clôture une session | Après clôture, plus aucun dépôt ni relecture possible sur cette session | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |

| ENF1 | Interface utilisable sur mobile (étudiants en cours) | Test manuel sur viewport réduit, responsive |
| ENF2 | Marquage de présence rapide même avec toute une promotion connectée en même temps | Temps de réponse observé < 1s sur `POST /api/presences` en local |
| ENF3 | Format d'erreur JSON homogène sur toute l'API | Vérifié par les tests d'intégration (B6) |
| ENF4 | Aucune donnée sensible (stack trace) renvoyée au client | Vérifié par `@RestControllerAdvice` (B4) |

## 6. Règles de gestion

| Réf | Règle | Source |

| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Passé ce délai, toute tentative de présence est refusée (410) | Q2, Q3 |
| RG3 | Après 5 tentatives de code erronées consécutives, l'étudiant est bloqué 2 minutes | Q4 |
| RG4 | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| RG5 | Un seul relecteur par exercice, choisi au hasard parmi les étudiants présents à la session | Q6, Q7 |
| RG6 | L'étudiant relu voit sa note et le commentaire, jamais l'identité du relecteur | Q8 |
| RG7 | La note est un entier compris entre 0 et 20 | Q9 |
| RG8 | Une relecture envoyée est définitive : aucune correction possible après validation | Q15 (retenu), Q10 (écarté — voir section 7) |
| RG9 | Un exercice sans relecture rendue reste au statut "en attente", visible dans le tableau du formateur | Q11 |
| RG10 | Le dépôt (ou remplacement) d'exercice reste possible jusqu'à la clôture explicite de la session, indépendamment de l'expiration du code de présence | Q12 |
| RG11 | Le lien d'un exercice peut être remplacé tant qu'aucune relecture n'a commencé | Q13 |
| RG12 | Une présence ajoutée par le formateur est marquée `source: FORMATEUR`, distincte d'une présence auto-déclarée (`source: ETUDIANT`) | Q14 |
| RG13 | Si aucun étudiant présent éligible ne peut être désigné relecteur, l'exercice reste "en attente d'assignation" ; une réassignation est retentée à chaque nouvelle présence marquée sur la session | Hypothèse (trou non couvert par CLIENT.md) |

## 7. Zones d'ombre, hypothèses et contradictions tranchées

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |

| Correction d'une note après envoi | Q10 dit oui (jusqu'à clôture), Q15 dit non (définitif dès l'envoi) — contradiction directe | Q15 retenue, Q10 écartée | Le contrat d'API n'expose qu'une seule opération `POST /api/relectures/{id}` avec un 409 en cas de second appel ; aucune opération de correction n'existe dans le contrat imposé (contrainte B2) |
| Absence de relecteur éligible | Non traité par le client | L'exercice reste "en attente d'assignation" ; réassignation tentée à chaque nouvelle présence | Le client n'a jamais envisagé ce cas ; solution la plus proche de sa logique existante (assignation automatique) sans intervention manuelle non demandée |
| Distinction "fin de session" (Q3) vs "clôture de session" (Q10/Q12/Q15) | Le client emploie les deux termes sans les distinguer explicitement | "Fin de session" = expiration du code (15 min, affecte seulement la présence) ; "clôture" = action explicite du formateur (affecte dépôt et relecture) | Cohérent avec Q12, qui autorise le dépôt bien après les 15 minutes, jusqu'à la clôture |
| Existence préalable des promotions et comptes étudiants | Non traité par le client, mais nécessaire (`promotionId`, `etudiantId` utilisés partout dans le contrat) | Supposées préexistantes, gérées hors périmètre de cette application | Le contrat d'API les référence par identifiant sans jamais prévoir leur création |

## 8. Contraintes techniques

- Backend : Java 17+, Spring Boot, Maven, wrapper `mvnw` commité (B1)
- Contrat `api/contrat.yaml` respecté à la lettre (B2)
- Architecture en couches contrôleur / service / repository, DTO obligatoires (B3)
- Validation des entrées, gestion centralisée des erreurs via `@RestControllerAdvice` (B4)
- Schéma de base versionné par Flyway ou Liquibase (B5)
- Deux tests significatifs : un unitaire sur une règle métier, un d'intégration sur un endpoint (B6)
- Frontend : React, Angular ou Next.js au choix, couche d'appel API dédiée, aucune règle métier dupliquée côté client (F1–F3)

## 9. Livrables

- Dépôt GitHub public `kfokam48-epreuve-276`
- `docs/CAHIER_DES_CHARGES.md`, `docs/JOURNAL.md`, `docs/diagrammes/` (D1, D2, D3)
- `api/contrat.yaml` complété
- Backlog en issues GitHub
- Code `/backend` et `/frontend`
- `README.md` d'installation testé depuis un clone vierge
- `CHANGELOG.md`
- `SOUMISSION.md` déposé sur la plateforme avant 18h00

## 10. Démarche prévue

1. Analyse complète (ce document, diagrammes, backlog, contrat) avant tout code — commit `[JALON] analyse`
2. Construction des stories Must uniquement, une branche/PR par ticket — commit `[JALON] v0.1`
3. Ouverture de l'enveloppe, traitement du bug et du changement de besoin, mise à jour de l'analyse
4. Livraison finale — commit `[JALON] v1.0`, `CHANGELOG.md`, README testé
5. Épreuve Git indépendante
6. Rédaction et dépôt de `SOUMISSION.md`

