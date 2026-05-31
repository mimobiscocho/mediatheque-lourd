-- =============================================================================
--  Médiathèque de Bourg-la-Reine - Application Client Lourd
--  Script de création de la base de données (schéma)
--  SGBD : MySQL 8.x
--  Auteur : SEBAH Nassim - BTS SIO SLAM - Session 2026
-- =============================================================================

DROP DATABASE IF EXISTS mediatheque;
CREATE DATABASE mediatheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mediatheque;

-- -----------------------------------------------------------------------------
--  Table PROFIL : comptes des agents administratifs (authentification)
-- -----------------------------------------------------------------------------
CREATE TABLE profil (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    login       VARCHAR(50)  NOT NULL UNIQUE,
    mot_de_passe VARCHAR(128) NOT NULL,          -- empreinte SHA-256
    nom         VARCHAR(80)  NOT NULL,
    prenom      VARCHAR(80)  NOT NULL,
    role        ENUM('ADMIN','AGENT') NOT NULL DEFAULT 'AGENT',
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table CLIENT : adhérents de la médiathèque
-- -----------------------------------------------------------------------------
CREATE TABLE client (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80)  NOT NULL,
    prenom      VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) UNIQUE,
    telephone   VARCHAR(20),
    adresse     VARCHAR(200),
    type_abonnement ENUM('STANDARD','PREMIUM','ETUDIANT') NOT NULL DEFAULT 'STANDARD',
    date_inscription DATE NOT NULL DEFAULT (CURRENT_DATE),
    actif       BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table TECHNICIEN : intervenants / animateurs
-- -----------------------------------------------------------------------------
CREATE TABLE technicien (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80)  NOT NULL,
    prenom      VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) UNIQUE,
    telephone   VARCHAR(20),
    specialite  VARCHAR(120)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table SALLE : salles de coworking mises à disposition
-- -----------------------------------------------------------------------------
CREATE TABLE salle (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80)  NOT NULL UNIQUE,
    capacite    INT NOT NULL CHECK (capacite > 0),
    equipement  VARCHAR(200),
    disponible  BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table ANIMATION : animations organisées (liées salle + technicien)
-- -----------------------------------------------------------------------------
CREATE TABLE animation (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    titre         VARCHAR(150) NOT NULL,
    description   TEXT,
    date_animation DATE NOT NULL,
    heure_debut   TIME NOT NULL,
    heure_fin     TIME NOT NULL,
    nb_places     INT NOT NULL DEFAULT 10 CHECK (nb_places >= 0),
    salle_id      INT NOT NULL,
    technicien_id INT NOT NULL,
    CONSTRAINT fk_animation_salle      FOREIGN KEY (salle_id)      REFERENCES salle(id)      ON DELETE RESTRICT,
    CONSTRAINT fk_animation_technicien FOREIGN KEY (technicien_id) REFERENCES technicien(id) ON DELETE RESTRICT,
    CONSTRAINT chk_horaire_anim CHECK (heure_fin > heure_debut)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table RESERVATION : réservation d'une salle par un client (créneau)
-- -----------------------------------------------------------------------------
CREATE TABLE reservation (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     INT NOT NULL,
    salle_id      INT NOT NULL,
    date_reservation DATE NOT NULL,
    heure_debut   TIME NOT NULL,
    heure_fin     TIME NOT NULL,
    statut        ENUM('CONFIRMEE','ANNULEE') NOT NULL DEFAULT 'CONFIRMEE',
    CONSTRAINT fk_reservation_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_salle  FOREIGN KEY (salle_id)  REFERENCES salle(id)  ON DELETE RESTRICT,
    CONSTRAINT chk_horaire_resa CHECK (heure_fin > heure_debut)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
--  Table FACTURE : factures émises pour les clients
-- -----------------------------------------------------------------------------
CREATE TABLE facture (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     INT NOT NULL,
    libelle       VARCHAR(200) NOT NULL,
    montant       DECIMAL(10,2) NOT NULL CHECK (montant >= 0),
    date_emission DATE NOT NULL DEFAULT (CURRENT_DATE),
    statut        ENUM('PAYEE','IMPAYEE') NOT NULL DEFAULT 'IMPAYEE',
    CONSTRAINT fk_facture_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================================================
--  TRIGGERS : automatisation de la vérification des disponibilités
-- =============================================================================
DELIMITER //

-- Empêche la réservation d'une salle marquée indisponible
-- ou déjà réservée sur un créneau qui se chevauche.
CREATE TRIGGER trg_reservation_before_insert
BEFORE INSERT ON reservation
FOR EACH ROW
BEGIN
    DECLARE v_dispo BOOLEAN;
    DECLARE v_conflits INT;

    SELECT disponible INTO v_dispo FROM salle WHERE id = NEW.salle_id;
    IF v_dispo = FALSE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Salle indisponible : reservation impossible.';
    END IF;

    SELECT COUNT(*) INTO v_conflits
    FROM reservation
    WHERE salle_id = NEW.salle_id
      AND date_reservation = NEW.date_reservation
      AND statut = 'CONFIRMEE'
      AND NEW.heure_debut < heure_fin
      AND NEW.heure_fin   > heure_debut;

    IF v_conflits > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Creneau deja reserve pour cette salle.';
    END IF;
END//

-- Vérifie également les conflits lors d'une modification de réservation.
CREATE TRIGGER trg_reservation_before_update
BEFORE UPDATE ON reservation
FOR EACH ROW
BEGIN
    DECLARE v_conflits INT;
    IF NEW.statut = 'CONFIRMEE' THEN
        SELECT COUNT(*) INTO v_conflits
        FROM reservation
        WHERE salle_id = NEW.salle_id
          AND id <> NEW.id
          AND date_reservation = NEW.date_reservation
          AND statut = 'CONFIRMEE'
          AND NEW.heure_debut < heure_fin
          AND NEW.heure_fin   > heure_debut;
        IF v_conflits > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Creneau deja reserve pour cette salle.';
        END IF;
    END IF;
END//

-- Empêche de planifier deux animations sur la même salle au même créneau.
CREATE TRIGGER trg_animation_before_insert
BEFORE INSERT ON animation
FOR EACH ROW
BEGIN
    DECLARE v_conflits INT;
    SELECT COUNT(*) INTO v_conflits
    FROM animation
    WHERE salle_id = NEW.salle_id
      AND date_animation = NEW.date_animation
      AND NEW.heure_debut < heure_fin
      AND NEW.heure_fin   > heure_debut;
    IF v_conflits > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La salle est deja occupee par une animation sur ce creneau.';
    END IF;
END//

DELIMITER ;
