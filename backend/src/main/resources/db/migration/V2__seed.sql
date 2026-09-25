-- V2 : donnees de reference (promotions/etudiants preexistants, hors perimetre)
-- et session de demo pour developper le frontend sans dependre du formateur.

INSERT INTO promotion (id, nom) VALUES
    (1, 'KFOKAM48');

INSERT INTO etudiant (id, nom, promotion_id) VALUES
    (1, 'Awa Ndiaye', 1),
    (2, 'Bruno Kabongo', 1),
    (3, 'Chantal Iyanga', 1);

-- Formateur : identifiant applicatif hors perimetre, session pre-ouverte pour la demo.
INSERT INTO session (id, formateur_id, code, ouverture, expiration_code, cloturee) VALUES
    (1, 900, 'DEMO1234', '2026-09-25 08:00:00+00:00', '2026-09-25 08:15:00+00:00', FALSE);
