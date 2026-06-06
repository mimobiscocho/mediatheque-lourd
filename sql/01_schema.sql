-- =====================================================================
--  Médiathèque de Bourg-la-Reine — Base de données UNIFIÉE
--  Schéma : structure des tables + triggers
--  SGBD : MySQL 8.x
--
--  Cette base est partagée par les DEUX applications :
--    - le client léger (PHP / web)     — gestion quotidienne, multiposte
--    - le client lourd (Java / Swing)  — gestion approfondie sur poste dédié
-- =====================================================================

DROP DATABASE IF EXISTS mediatheque;
CREATE DATABASE mediatheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mediatheque;

-- =====================================================================
--  AUTHENTIFICATION
--    - agent  : utilisé par le client léger (login = email,  hash bcrypt)
--    - profil : utilisé par le client lourd (login = libre,  hash PBKDF2)
-- =====================================================================

CREATE TABLE agent (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(60)  NOT NULL,
    prenom        VARCHAR(60)  NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,          -- empreinte bcrypt
    role          ENUM('admin','agent') NOT NULL DEFAULT 'agent',
    actif         TINYINT(1)   NOT NULL DEFAULT 1,
    date_creation DATE         NOT NULL
) ENGINE=InnoDB;

CREATE TABLE profil (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    login         VARCHAR(50)  NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,          -- empreinte PBKDF2-HMAC-SHA256 salée
    nom           VARCHAR(80)  NOT NULL,
    prenom        VARCHAR(80)  NOT NULL,
    role          ENUM('ADMIN','AGENT') NOT NULL DEFAULT 'AGENT',
    date_creation DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================================
--  ABONNEMENTS (utilisés principalement par le client léger)
-- =====================================================================
CREATE TABLE abonnement (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    libelle        VARCHAR(60)  NOT NULL,
    tarif          DECIMAL(6,2) NOT NULL DEFAULT 0,
    duree_mois     INT          NOT NULL DEFAULT 12,
    quota_emprunts INT          NOT NULL DEFAULT 5
) ENGINE=InnoDB;

-- =====================================================================
--  ADHÉRENT : table commune aux deux applications
-- =====================================================================
CREATE TABLE adherent (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nom                 VARCHAR(80)  NOT NULL,
    prenom              VARCHAR(80)  NOT NULL,
    email               VARCHAR(120) UNIQUE,
    telephone           VARCHAR(20),
    adresse             VARCHAR(200),
    abonnement_id       INT,
    type_abonnement     ENUM('STANDARD','PREMIUM','ETUDIANT') NOT NULL DEFAULT 'STANDARD',
    date_inscription    DATE         NOT NULL DEFAULT (CURRENT_DATE),
    date_fin_abonnement DATE,
    actif               TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT fk_adherent_abonnement
        FOREIGN KEY (abonnement_id) REFERENCES abonnement(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
--  COLLECTION (gérée par le client léger)
-- =====================================================================
CREATE TABLE livre (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    titre               VARCHAR(150) NOT NULL,
    auteur              VARCHAR(120) NOT NULL,
    isbn                VARCHAR(20),
    editeur             VARCHAR(120),
    annee_publication   INT,
    genre               VARCHAR(60),
    quantite_totale     INT NOT NULL DEFAULT 1,
    quantite_disponible INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE materiel (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(120) NOT NULL,
    categorie   VARCHAR(60),
    description VARCHAR(255),
    etat        ENUM('neuf','bon','use','hors_service') NOT NULL DEFAULT 'bon',
    disponible  TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =====================================================================
--  SALLE : table partagée
-- =====================================================================
CREATE TABLE salle (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80) NOT NULL UNIQUE,
    capacite    INT NOT NULL DEFAULT 1 CHECK (capacite > 0),
    equipement  VARCHAR(255),
    disponible  TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =====================================================================
--  TECHNICIENS et ANIMATIONS (gérés par le client lourd)
-- =====================================================================
CREATE TABLE technicien (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80)  NOT NULL,
    prenom      VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) UNIQUE,
    telephone   VARCHAR(20),
    specialite  VARCHAR(120)
) ENGINE=InnoDB;

CREATE TABLE animation (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    titre          VARCHAR(150) NOT NULL,
    description    TEXT,
    date_animation DATE NOT NULL,
    heure_debut    TIME NOT NULL,
    heure_fin      TIME NOT NULL,
    nb_places      INT NOT NULL DEFAULT 10 CHECK (nb_places >= 0),
    salle_id       INT NOT NULL,
    technicien_id  INT NOT NULL,
    CONSTRAINT fk_animation_salle      FOREIGN KEY (salle_id)      REFERENCES salle(id)      ON DELETE RESTRICT,
    CONSTRAINT fk_animation_technicien FOREIGN KEY (technicien_id) REFERENCES technicien(id) ON DELETE RESTRICT,
    CONSTRAINT chk_horaire_anim CHECK (heure_fin > heure_debut)
) ENGINE=InnoDB;

-- =====================================================================
--  PRÊTS (gérés par le client léger)
-- =====================================================================
CREATE TABLE pret (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    adherent_id           INT NOT NULL,
    livre_id              INT,
    materiel_id           INT,
    date_pret             DATE NOT NULL,
    date_retour_prevue    DATE NOT NULL,
    date_retour_effective DATE,
    statut                ENUM('en_cours','rendu','en_retard') NOT NULL DEFAULT 'en_cours',
    CONSTRAINT fk_pret_adherent FOREIGN KEY (adherent_id) REFERENCES adherent(id) ON DELETE CASCADE,
    CONSTRAINT fk_pret_livre    FOREIGN KEY (livre_id)    REFERENCES livre(id)    ON DELETE CASCADE,
    CONSTRAINT fk_pret_materiel FOREIGN KEY (materiel_id) REFERENCES materiel(id) ON DELETE CASCADE,
    CONSTRAINT chk_pret_produit CHECK (
        (livre_id IS NOT NULL AND materiel_id IS NULL) OR
        (livre_id IS NULL AND materiel_id IS NOT NULL)
    )
) ENGINE=InnoDB;

-- =====================================================================
--  RÉSERVATION : table partagée — statut en MAJUSCULES
-- =====================================================================
CREATE TABLE reservation (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    adherent_id      INT NOT NULL,
    salle_id         INT NOT NULL,
    date_reservation DATE NOT NULL,
    heure_debut      TIME NOT NULL,
    heure_fin        TIME NOT NULL,
    statut           ENUM('CONFIRMEE','ANNULEE','TERMINEE') NOT NULL DEFAULT 'CONFIRMEE',
    CONSTRAINT fk_resa_adherent FOREIGN KEY (adherent_id) REFERENCES adherent(id) ON DELETE CASCADE,
    CONSTRAINT fk_resa_salle    FOREIGN KEY (salle_id)    REFERENCES salle(id)    ON DELETE RESTRICT,
    CONSTRAINT chk_resa_heures  CHECK (heure_fin > heure_debut)
) ENGINE=InnoDB;

-- =====================================================================
--  FACTURE (gérée par le client lourd)
-- =====================================================================
CREATE TABLE facture (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    adherent_id   INT NOT NULL,
    libelle       VARCHAR(200) NOT NULL,
    montant       DECIMAL(10,2) NOT NULL CHECK (montant >= 0),
    date_emission DATE NOT NULL DEFAULT (CURRENT_DATE),
    statut        ENUM('PAYEE','IMPAYEE') NOT NULL DEFAULT 'IMPAYEE',
    CONSTRAINT fk_facture_adherent FOREIGN KEY (adherent_id) REFERENCES adherent(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
--  TRIGGERS — automatisation des règles métier
-- =====================================================================
DELIMITER //

-- PRÊTS : disponibilité du produit + mise à jour du stock
CREATE TRIGGER trg_pret_before_insert
BEFORE INSERT ON pret
FOR EACH ROW
BEGIN
    DECLARE dispo INT;
    IF NEW.livre_id IS NOT NULL THEN
        SELECT quantite_disponible INTO dispo FROM livre WHERE id = NEW.livre_id;
        IF dispo IS NULL OR dispo < 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Livre indisponible : aucun exemplaire en stock.';
        END IF;
    ELSEIF NEW.materiel_id IS NOT NULL THEN
        SELECT disponible INTO dispo FROM materiel WHERE id = NEW.materiel_id;
        IF dispo IS NULL OR dispo < 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Materiel indisponible (deja emprunte ou hors service).';
        END IF;
    END IF;
END//

CREATE TRIGGER trg_pret_after_insert
AFTER INSERT ON pret
FOR EACH ROW
BEGIN
    IF NEW.livre_id IS NOT NULL THEN
        UPDATE livre SET quantite_disponible = quantite_disponible - 1 WHERE id = NEW.livre_id;
    ELSEIF NEW.materiel_id IS NOT NULL THEN
        UPDATE materiel SET disponible = 0 WHERE id = NEW.materiel_id;
    END IF;
END//

CREATE TRIGGER trg_pret_after_update
AFTER UPDATE ON pret
FOR EACH ROW
BEGIN
    IF NEW.date_retour_effective IS NOT NULL AND OLD.date_retour_effective IS NULL THEN
        IF NEW.livre_id IS NOT NULL THEN
            UPDATE livre SET quantite_disponible = quantite_disponible + 1 WHERE id = NEW.livre_id;
        ELSEIF NEW.materiel_id IS NOT NULL THEN
            UPDATE materiel SET disponible = 1 WHERE id = NEW.materiel_id;
        END IF;
    END IF;
END//

CREATE TRIGGER trg_pret_after_delete
AFTER DELETE ON pret
FOR EACH ROW
BEGIN
    IF OLD.date_retour_effective IS NULL THEN
        IF OLD.livre_id IS NOT NULL THEN
            UPDATE livre SET quantite_disponible = quantite_disponible + 1 WHERE id = OLD.livre_id;
        ELSEIF OLD.materiel_id IS NOT NULL THEN
            UPDATE materiel SET disponible = 1 WHERE id = OLD.materiel_id;
        END IF;
    END IF;
END//

-- RÉSERVATIONS : disponibilité salle + chevauchement
CREATE TRIGGER trg_reservation_before_insert
BEFORE INSERT ON reservation
FOR EACH ROW
BEGIN
    DECLARE v_dispo TINYINT;
    DECLARE nb INT;

    SELECT disponible INTO v_dispo FROM salle WHERE id = NEW.salle_id;
    IF v_dispo IS NULL OR v_dispo = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Salle indisponible : reservation impossible.';
    END IF;

    SELECT COUNT(*) INTO nb
    FROM reservation
    WHERE salle_id = NEW.salle_id
      AND date_reservation = NEW.date_reservation
      AND statut = 'CONFIRMEE'
      AND NEW.heure_debut < heure_fin
      AND NEW.heure_fin   > heure_debut;
    IF nb > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Creneau deja reserve pour cette salle.';
    END IF;
END//

CREATE TRIGGER trg_reservation_before_update
BEFORE UPDATE ON reservation
FOR EACH ROW
BEGIN
    DECLARE nb INT;
    IF NEW.statut = 'CONFIRMEE' THEN
        SELECT COUNT(*) INTO nb
        FROM reservation
        WHERE salle_id = NEW.salle_id
          AND id <> NEW.id
          AND date_reservation = NEW.date_reservation
          AND statut = 'CONFIRMEE'
          AND NEW.heure_debut < heure_fin
          AND NEW.heure_fin   > heure_debut;
        IF nb > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Creneau deja reserve pour cette salle.';
        END IF;
    END IF;
END//

-- ANIMATIONS : pas de double-occupation d'une salle
CREATE TRIGGER trg_animation_before_insert
BEFORE INSERT ON animation
FOR EACH ROW
BEGIN
    DECLARE nb INT;
    SELECT COUNT(*) INTO nb
    FROM animation
    WHERE salle_id = NEW.salle_id
      AND date_animation = NEW.date_animation
      AND NEW.heure_debut < heure_fin
      AND NEW.heure_fin   > heure_debut;
    IF nb > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La salle est deja occupee par une animation sur ce creneau.';
    END IF;
END//

CREATE TRIGGER trg_animation_before_update
BEFORE UPDATE ON animation
FOR EACH ROW
BEGIN
    DECLARE nb INT;
    SELECT COUNT(*) INTO nb
    FROM animation
    WHERE salle_id = NEW.salle_id
      AND id <> NEW.id
      AND date_animation = NEW.date_animation
      AND NEW.heure_debut < heure_fin
      AND NEW.heure_fin   > heure_debut;
    IF nb > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La salle est deja occupee par une animation sur ce creneau.';
    END IF;
END//

DELIMITER ;
