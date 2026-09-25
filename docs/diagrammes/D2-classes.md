# D2 — Diagramme de classes

Source : `docs/CAHIER_DES_CHARGES.md` (§4, §6) et endpoints du contrat `api/contrat.yaml` à compléter. Les statuts d'exercice reprennent EF5, RG9 et RG13.

```mermaid
classDiagram
    direction LR

    class Promotion {
        +Long id
        +String nom
    }

    class Etudiant {
        +Long id
        +String nom
        +Long promotionId
    }

    class Session {
        +Long id
        +Long formateurId
        +String code
        +DateTime ouverture
        +DateTime expirationCode
        +Boolean cloturee
        +genererCode() Session
        +cloturer() void
    }

    class Presence {
        +Long id
        +Long sessionId
        +Long etudiantId
        +DateTime horodatage
        +SourcePresence source
    }

    class Exercice {
        +Long id
        +Long sessionId
        +Long auteurId
        +String lien
        +StatutExercice statut
        +DateTime deposeLe
    }

    class Relecture {
        +Long id
        +Long exerciceId
        +Long relecteurId
        +Integer note
        +String commentaire
        +DateTime rendueLe
    }

    class SourcePresence {
        <<enumeration>>
        ETUDIANT
        FORMATEUR
    }

    class StatutExercice {
        <<enumeration>>
        EN_ATTENTE_ASSIGNATION
        EN_ATTENTE_RELECTURE
        RELUE
    }

    class ReglesMetier {
        <<service>>
        +verifierCode(session) boolean
        +assignerRelecteur(exercice, presents) Relecteur
        +calculerMoyenne(etudiantId) Double
    }
    Cardinalité: 

    Promotion "1" --> "0..*" Etudiant : regroupe
    Session "1" --> "0..*" Presence : recueille
    Etudiant "1" --> "0..*" Presence : marque
    Etudiant "1" --> "0..*" Exercice : dépose (auteur)
    Session "1" --> "0..*" Exercice : concerne
    Exercice "1" --> "0..1" Relecture : reçoit (RG5)
    Etudiant "1" --> "0..*" Relecture : rédige (relecteur, RG4)

    Presence ..> SourcePresence
    Exercice ..> StatutExercice
    ReglesMetier ..> Session : RG1-RG3
    ReglesMetier ..> Exercice : RG5, RG9, RG13
    ReglesMetier ..> Relecture : RG6-RG8
```

Points de vigilance repris des règles de gestion :

- `Relecture.relecteurId` n'est **jamais** exposé à l'étudiant relu (RG6) — c'est une contrainte de la couche DTO, pas du schéma.
- `Exercice.statut` traverse les trois états d'EF5/RG9/RG13 ; le remplacement du lien (EF6/RG11) n'est possible que depuis `EN_ATTENTE_ASSIGNATION` et `EN_ATTENTE_RELECTURE`.
- `Session.cloturee` bloque dépôt et relecture (EF12/RG10), alors que `expirationCode` ne bloque que la présence (RG1/RG2) — distinction tranchée au §7.
- `Promotion` et `Etudiant` sont hors périmètre de création (supposés préexistants), d'où l'attribut `promotionId` minimal.
