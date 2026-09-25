# D1 — Diagramme de cas d'utilisation

Source : `docs/CAHIER_DES_CHARGES.md` (§2 Acteurs, §4 Exigences fonctionnelles EF1–EF12).

```mermaid
flowchart LR
    Formateur([Formateur])
    Etudiant([Étudiant])
    Relecteur([Relecteur])
    Systeme([Système])

    subgraph Application["Application de suivi (KFOKAM48)"]
        UC1(Ouvrir une session<br/>et obtenir un code — EF1)
        UC2(Marquer sa présence<br/>avec un code — EF2)
        UC3(Vérifier le code<br/>validité et expiration — RG1, RG2, RG3)
        UC4(Déposer le lien<br/>de son exercice — EF5)
        UC5(Remplacer le lien avant<br/>le début de la relecture — EF6, RG11)
        UC6(Assigner un relecteur au hasard<br/>parmi les présents, jamais l'auteur — EF7, RG4, RG5)
        UC7(Noter et commenter<br/>l'exercice d'un pair — EF8, RG7, RG8)
        UC8(Consulter sa note et le commentaire<br/>sans identité du relecteur — EF9, RG6)
        UC9(Consulter le tableau de bord<br/>présences, dépôts, moyennes — EF10)
        UC10(Ajouter une présence manuelle<br/>source FORMATEUR — EF11, RG12)
        UC11(Clôturer la session<br/>plus de dépôt ni relecture — EF12, RG10)
        UC12(Calculer les moyennes<br/>et l'état des relectures)
    end

    Relecteur -. "hérite des cas de l'étudiant" .-> Etudiant

    Formateur --> UC1
    Formateur --> UC9
    Formateur --> UC10
    Formateur --> UC11

    Etudiant --> UC2
    Etudiant --> UC4
    Etudiant --> UC5
    Etudiant --> UC8

    Relecteur --> UC7

    UC2 -. "include" .-> UC3
    UC4 -. "déclenche" .-> UC6
    UC6 -. "alimente" .-> UC12
    UC7 -. "alimente" .-> UC12
    UC12 -. "alimente" .-> UC9

    UC1 -.- Systeme
    UC6 -.- Systeme
    UC12 -.- Systeme
```

Lecture :

- Le **relecteur** est un étudiant désigné automatiquement (EF7) : il hérite des cas de l'étudiant et ajoute la relecture.
- Le **système** est acteur des cas automatiques : génération du code, assignation au hasard, calcul des moyennes.
- La clôture (EF12) et l'expiration du code (RG1/RG2) sont les deux bornes temporelles distinctes tranchées au §7 du cahier des charges.
