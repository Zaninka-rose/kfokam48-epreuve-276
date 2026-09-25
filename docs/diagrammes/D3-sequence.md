# D3 — Diagramme de séquence

Scénario nominal complet : ouverture de session, présence de l'étudiant, dépôt d'exercice, assignation du relecteur, relecture et consultation. Dérivé de EF1–EF10 et RG1–RG9.

```mermaid
sequenceDiagram
    autonumber
    actor F as Formateur
    participant API as API (contrat.yaml)
    participant SRV as Service Sessions
    participant BDD as Base de données
    actor E as Étudiant (présent)
    actor R as Relecteur (assigné)

    F->>API: POST /api/sessions
    API->>SRV: ouvrirSession(formateurId)
    SRV->>SRV: générer code (unique)
    SRV->>BDD: persister Session (expiration = ouverture + 15 min, RG1)
    BDD-->>SRV: Session enregistrée
    SRV-->>API: 201 { code, ouverture, expiration }
    API-->>F: 201 Session ouverte

    E->>API: POST /api/presences { code }
    API->>SRV: marquerPresence(code, etudiantId)
    SRV->>BDD: charger Session par code
    BDD-->>SRV: Session

    alt code expiré (RG2)
        SRV-->>API: erreur CODE_EXPIRE
        API-->>E: 410 CODE_EXPIRE
    else 5e erreur consécutive (RG3)
        SRV-->>API: erreur BLOQUE
        API-->>E: 429 / blocage 2 min
    else déjà présent (EF4)
        SRV-->>API: erreur DEJA_PRESENT
        API-->>E: 409 DEJA_PRESENT
    else code valide
        SRV->>BDD: persister Presence (source: ETUDIANT)
        SRV->>SRV: tenter assignation relecteurs (RG13)
        SRV-->>API: Présence créée
        API-->>E: 201 Présence
    end

    E->>API: POST /api/exercices { lien }
    API->>SRV: deposerExercice(etudiantId, lien)
    SRV->>BDD: persister Exercice (statut: EN_ATTENTE_ASSIGNATION ou EN_ATTENTE_RELECTURE)
    SRV-->>API: 201 Exercice
    API-->>E: 201 Dépôt accepté

    E->>API: PUT /api/exercices/{id} { nouveauLien }
    alt aucune relecture commencée (EF6, RG11)
        SRV-->>API: Exercice mis à jour
        API-->>E: 200 Lien remplacé
    else relecture rendue
        SRV-->>API: erreur
        API-->>E: 409 Refus (RG11)
    end

    R->>API: POST /api/relectures/{id} { note, commentaire }
    API->>SRV: envoyerRelecture(exerciceId, note, commentaire)
    SRV->>SRV: contrôles : relecteur ≠ auteur (RG4), note 0-20 (RG7), non déjà rendue (RG8)
    alt contrôle échoué
        SRV-->>API: erreur (403 auteur / 409 déjà rendue / 400 note invalide)
        API-->>R: réponse d'erreur
    else contrôle OK
        SRV->>BDD: persister Relecture
        SRV->>BDD: Exercice.statut = RELUE
        SRV-->>API: Relecture enregistrée
        API-->>R: 200 Relecture acceptée
    end

    E->>API: GET /api/exercices/{id}/resultat
    SRV-->>API: note + commentaire, sans relecteurId (RG6)
    API-->>E: 200 Résultat anonymisé

    F->>API: GET /api/tableau
    SRV->>BDD: agréger présences, exercices, moyennes, relectures en attente (EF10)
    BDD-->>SRV: Données agrégées
    SRV-->>API: Tableau de bord
    API-->>F: 200 Tableau de bord
```

Alternative de clôture (EF12) :

```mermaid
sequenceDiagram
    autonumber
    actor F as Formateur
    participant API as API (contrat.yaml)
    participant SRV as Service Sessions

    F->>API: POST /api/sessions/{id}/cloture
    API->>SRV: cloturerSession(id)
    SRV-->>API: Session clôturée
    API-->>F: 200

    note over API,SRV: ensuite : tout dépôt (RG10) ou relecture → refus
```
