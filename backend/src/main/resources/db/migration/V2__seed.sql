-- V2 : donnees de demonstration chargees automatiquement au demarrage :
-- 1 promotion, 10 etudiants, 1 session ouverte avec un code de presence
-- valide 15 minutes a compter de la migration (RG1).
-- Objectif : application utilisable des le premier demarrage, sans action manuelle.
--
-- NB (profil "postgres" uniquement, volume persistant) : la session de demo
-- expire 15 minutes apres la PREMIERE application de cette migration. Pour la
-- regenerer : docker compose down -v && docker compose up -d db.
-- Avec le H2 en memoire (defaut), la base est recreee a chaque demarrage :
-- le code DEMO1234 est donc toujours valide au lancement.

INSERT INTO promotion (id, nom) VALUES
    (1, 'KFOKAM48');

INSERT INTO etudiant (id, nom, promotion_id) VALUES
    (1,  'Awa Ndiaye',       1),
    (2,  'Bruno Kabongo',    1),
    (3,  'Chantal Iyanga',   1),
    (4,  'Dimitri Mbala',    1),
    (5,  'Esther Kouadio',   1),
    (6,  'Fabrice Nkemba',   1),
    (7,  'Grace Mputu',      1),
    (8,  'Herve Tchoumi',    1),
    (9,  'Ines Barambanga',  1),
    (10, 'Jonas Ilunga',     1);

-- Formateur : identifiant applicatif hors perimetre (900).
-- Code saisi par l'etudiant pour marquer sa presence : DEMO1234.
INSERT INTO session (id, formateur_id, code, ouverture, expiration_code, cloturee) VALUES
    (1, 900, 'DEMO1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '15' MINUTE, FALSE);

-- Les identifiants etant inseres explicitement, on replace les sequences
-- IDENTITY apres eux (syntaxe commune H2 / PostgreSQL) pour que les
-- prochains INSERT applicatifs ne entrent pas en conflit de cle primaire.
ALTER TABLE promotion ALTER COLUMN id RESTART WITH 2;
ALTER TABLE etudiant ALTER COLUMN id RESTART WITH 11;
ALTER TABLE session ALTER COLUMN id RESTART WITH 2;
