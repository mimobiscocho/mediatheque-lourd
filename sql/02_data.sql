-- =============================================================================
--  Médiathèque de Bourg-la-Reine - Jeu de données de démonstration
--  À exécuter APRÈS 01_schema.sql
-- =============================================================================
USE mediatheque;

-- Profils (mots de passe : PBKDF2-HMAC-SHA256, 600 000 itérations, sel par compte)
--   admin / admin123
--   agent / agent123
-- Format : pbkdf2_sha256$iterations$saltBase64$hashBase64 (cf. util/PasswordUtil)
INSERT INTO profil (login, mot_de_passe, nom, prenom, role) VALUES
 ('admin', 'pbkdf2_sha256$600000$kYdMj/fJowhQRkVtIRVfcw==$HP9KtqpivNhzRsWVbrZMztl2ZM50eM7iHOa549xn0cQ=', 'SEBAH',  'Nassim', 'ADMIN'),
 ('agent', 'pbkdf2_sha256$600000$3L3LJ+tGGTiQBhkedUdS1A==$WJXNC6YzeMb2scj0x62TyCGbrZDXdv8sM1TorHr/AFA=', 'MARTIN', 'Claire', 'AGENT');

-- Clients (adhérents)
INSERT INTO client (nom, prenom, email, telephone, adresse, type_abonnement, date_inscription) VALUES
 ('Dupont',  'Jean',    'jean.dupont@mail.fr',   '0601020304', '12 rue de la Paix, Bourg-la-Reine', 'STANDARD', '2025-01-15'),
 ('Bernard', 'Sophie',  'sophie.bernard@mail.fr','0611223344', '5 av. du Parc, Bourg-la-Reine',     'PREMIUM',  '2024-09-03'),
 ('Petit',   'Lucas',   'lucas.petit@mail.fr',   '0655667788', '8 bd Carnot, Bourg-la-Reine',       'ETUDIANT', '2025-10-21'),
 ('Moreau',  'Emma',    'emma.moreau@mail.fr',   '0699887766', '3 place de la Mairie, Bourg-la-Reine','STANDARD','2025-03-12');

-- Techniciens / animateurs
INSERT INTO technicien (nom, prenom, email, telephone, specialite) VALUES
 ('Lefevre',  'Marc',   'marc.lefevre@medbar.fr',   '0708091011', 'Informatique / Robotique'),
 ('Garnier',  'Julie',  'julie.garnier@medbar.fr',  '0712131415', 'Atelier lecture'),
 ('Rousseau', 'Thomas', 'thomas.rousseau@medbar.fr','0716171819', 'Multimédia / Vidéo');

-- Salles de coworking
INSERT INTO salle (nom, capacite, equipement, disponible) VALUES
 ('Salle Voltaire',  8,  'Vidéoprojecteur, tableau blanc, Wifi', TRUE),
 ('Salle Curie',     4,  'Wifi, prises USB',                     TRUE),
 ('Salle Hugo',      12, 'Écran 4K, visioconférence, Wifi',      TRUE),
 ('Salle Atelier',   6,  'Imprimante 3D, ordinateurs',           FALSE);

-- Animations
INSERT INTO animation (titre, description, date_animation, heure_debut, heure_fin, nb_places, salle_id, technicien_id) VALUES
 ('Initiation Python',     'Atelier de programmation pour débutants', '2026-06-10', '14:00:00', '16:00:00', 8,  1, 1),
 ('Club de lecture',       'Échange autour des nouveautés littéraires','2026-06-12', '18:00:00', '19:30:00', 12, 3, 2),
 ('Montage vidéo',         'Découverte du montage vidéo',             '2026-06-15', '10:00:00', '12:00:00', 6,  1, 3);

-- Réservations de salles
INSERT INTO reservation (client_id, salle_id, date_reservation, heure_debut, heure_fin, statut) VALUES
 (1, 2, '2026-06-08', '09:00:00', '11:00:00', 'CONFIRMEE'),
 (2, 3, '2026-06-09', '14:00:00', '17:00:00', 'CONFIRMEE');

-- Factures
INSERT INTO facture (client_id, libelle, montant, date_emission, statut) VALUES
 (1, 'Abonnement annuel STANDARD',  25.00, '2025-01-15', 'PAYEE'),
 (2, 'Abonnement annuel PREMIUM',   45.00, '2024-09-03', 'PAYEE'),
 (3, 'Abonnement annuel ETUDIANT',  12.00, '2025-10-21', 'IMPAYEE'),
 (1, 'Location salle Curie (2h)',   10.00, '2026-06-08', 'IMPAYEE');
