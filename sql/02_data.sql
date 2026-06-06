-- =====================================================================
--  Médiathèque de Bourg-la-Reine — Jeu de données de démonstration
--  À exécuter APRÈS 01_schema.sql
-- =====================================================================
USE mediatheque;

-- ---------------------------------------------------------------------
--  Comptes des deux applications (mots de passe : admin123 / agent123)
--   - agent  (léger) : empreinte bcrypt   ($2y$12$...)
--   - profil (lourd) : empreinte PBKDF2-HMAC-SHA256 salée
-- ---------------------------------------------------------------------
INSERT INTO agent (nom, prenom, email, mot_de_passe, role, actif, date_creation) VALUES
('Admin', 'Médiathèque', 'admin@mediatheque.fr', '$2y$12$8T9Jnas.F8CWgnJxZ2lZceWE9oIQnj0FPS0tQ2PxwFT0mL7BetlMi', 'admin', 1, '2026-01-05'),
('Petit', 'Julie',       'agent@mediatheque.fr', '$2y$12$ywpyIeBqfufB0t4z1tdzU.uF.wCjAoU2g.SyITK2HZ5VTWzeUU8XS', 'agent', 1, '2026-01-08');

INSERT INTO profil (login, mot_de_passe, nom, prenom, role) VALUES
('admin', 'pbkdf2_sha256$600000$kYdMj/fJowhQRkVtIRVfcw==$HP9KtqpivNhzRsWVbrZMztl2ZM50eM7iHOa549xn0cQ=', 'SEBAH',  'Nassim', 'ADMIN'),
('agent', 'pbkdf2_sha256$600000$3L3LJ+tGGTiQBhkedUdS1A==$WJXNC6YzeMb2scj0x62TyCGbrZDXdv8sM1TorHr/AFA=', 'MARTIN', 'Claire', 'AGENT');

-- ---------------------------------------------------------------------
--  Abonnements
-- ---------------------------------------------------------------------
INSERT INTO abonnement (libelle, tarif, duree_mois, quota_emprunts) VALUES
('Standard',   15.00, 12, 5),
('Étudiant',    8.00, 12, 5),
('Premium',    30.00, 12, 10),
('Découverte',  0.00,  3, 2);

-- ---------------------------------------------------------------------
--  Adhérents (table commune aux deux applications)
-- ---------------------------------------------------------------------
INSERT INTO adherent (nom, prenom, email, telephone, adresse, abonnement_id, type_abonnement, date_inscription, date_fin_abonnement, actif) VALUES
('Martin',  'Sophie',  'sophie.martin@email.fr',  '0612345678', '12 rue des Lilas, Bourg-la-Reine',         1, 'STANDARD', '2026-01-10', '2027-01-10', 1),
('Dubois',  'Karim',   'karim.dubois@email.fr',   '0623456789', '5 av. du Général Leclerc, Bourg-la-Reine', 2, 'ETUDIANT', '2026-01-15', '2027-01-15', 1),
('Nguyen',  'Camille', 'camille.nguyen@email.fr', '0634567890', '28 rue de la Bièvre, Bourg-la-Reine',      3, 'PREMIUM',  '2026-02-01', '2027-02-01', 1),
('Lefevre', 'Hugo',    'hugo.lefevre@email.fr',   '0645678901', '3 place de la Mairie, Bourg-la-Reine',     4, 'STANDARD', '2026-02-20', '2026-05-20', 1),
('Dupont',  'Jean',    'jean.dupont@mail.fr',     '0601020304', '12 rue de la Paix, Bourg-la-Reine',        1, 'STANDARD', '2025-01-15', '2026-01-15', 1),
('Bernard', 'Sophie',  'sophie.bernard@mail.fr',  '0611223344', '5 av. du Parc, Bourg-la-Reine',            3, 'PREMIUM',  '2024-09-03', '2025-09-03', 1),
('Moreau',  'Emma',    'emma.moreau@mail.fr',     '0699887766', '3 place de la Mairie, Bourg-la-Reine',     1, 'STANDARD', '2025-03-12', '2026-03-12', 1);

-- ---------------------------------------------------------------------
--  Collection
-- ---------------------------------------------------------------------
INSERT INTO livre (titre, auteur, isbn, editeur, annee_publication, genre, quantite_totale, quantite_disponible) VALUES
('L''Étranger',     'Albert Camus',             '9782070360024', 'Gallimard',         1942, 'Roman',           4, 4),
('Le Petit Prince', 'Antoine de Saint-Exupéry', '9782070612758', 'Gallimard',         1943, 'Conte',           6, 6),
('1984',            'George Orwell',            '9782070368228', 'Gallimard',         1949, 'Science-fiction', 3, 3),
('Sapiens',         'Yuval Noah Harari',        '9782226257017', 'Albin Michel',      2015, 'Essai',           2, 2),
('Les Misérables',  'Victor Hugo',              '9782253096337', 'Le Livre de Poche', 1862, 'Roman',           3, 3),
('Clean Code',      'Robert C. Martin',         '9780132350884', 'Prentice Hall',     2008, 'Informatique',    2, 2);

INSERT INTO materiel (nom, categorie, description, etat, disponible) VALUES
('Ordinateur portable Dell', 'Informatique', 'PC portable 15" pour travail sur place',   'bon',  1),
('Liseuse Kindle',           'Multimédia',   'Liseuse électronique avec 100 titres',     'neuf', 1),
('Vidéoprojecteur Epson',    'Audiovisuel',  'Projecteur Full HD pour salle de réunion', 'bon',  1),
('Casque audio Bose',        'Audiovisuel',  'Casque à réduction de bruit',              'use',  1);

-- ---------------------------------------------------------------------
--  Salles (utilisées par les deux apps)
-- ---------------------------------------------------------------------
INSERT INTO salle (nom, capacite, equipement, disponible) VALUES
('Salle Bièvre',      4,  'Écran, tableau blanc, Wi-Fi',          1),
('Salle Coworking A', 8,  'Wi-Fi, prises individuelles, café',    1),
('Salle Conférence',  20, 'Vidéoprojecteur, micro, estrade',      1),
('Box silencieux',    2,  'Isolation phonique, Wi-Fi',            1),
('Salle Voltaire',    8,  'Vidéoprojecteur, tableau blanc, Wi-Fi', 1),
('Salle Curie',       4,  'Wi-Fi, prises USB',                    1),
('Salle Hugo',        12, 'Écran 4K, visioconférence, Wi-Fi',     1),
('Salle Atelier',     6,  'Imprimante 3D, ordinateurs',           0);

-- ---------------------------------------------------------------------
--  Techniciens
-- ---------------------------------------------------------------------
INSERT INTO technicien (nom, prenom, email, telephone, specialite) VALUES
('Lefevre',  'Marc',   'marc.lefevre@medbar.fr',   '0708091011', 'Informatique / Robotique'),
('Garnier',  'Julie',  'julie.garnier@medbar.fr',  '0712131415', 'Atelier lecture'),
('Rousseau', 'Thomas', 'thomas.rousseau@medbar.fr','0716171819', 'Multimédia / Vidéo');

-- ---------------------------------------------------------------------
--  Animations
-- ---------------------------------------------------------------------
INSERT INTO animation (titre, description, date_animation, heure_debut, heure_fin, nb_places, salle_id, technicien_id) VALUES
('Initiation Python', 'Atelier de programmation pour débutants',  '2026-06-10', '14:00:00', '16:00:00', 8,  5, 1),
('Club de lecture',   'Échange autour des nouveautés littéraires','2026-06-12', '18:00:00', '19:30:00', 12, 7, 2),
('Montage vidéo',     'Découverte du montage vidéo',              '2026-06-15', '10:00:00', '12:00:00', 6,  5, 3);

-- ---------------------------------------------------------------------
--  Prêts (les triggers décrémentent automatiquement les stocks)
-- ---------------------------------------------------------------------
INSERT INTO pret (adherent_id, livre_id, materiel_id, date_pret, date_retour_prevue, statut) VALUES
(1, 1, NULL, '2026-05-10', '2026-05-24', 'en_cours'),
(2, NULL, 2, '2026-05-15', '2026-05-29', 'en_cours');

-- ---------------------------------------------------------------------
--  Réservations
-- ---------------------------------------------------------------------
INSERT INTO reservation (adherent_id, salle_id, date_reservation, heure_debut, heure_fin, statut) VALUES
(3, 2, '2026-06-02', '09:00:00', '12:00:00', 'CONFIRMEE'),
(1, 1, '2026-06-03', '14:00:00', '16:00:00', 'CONFIRMEE'),
(5, 6, '2026-06-08', '09:00:00', '11:00:00', 'CONFIRMEE'),
(6, 7, '2026-06-09', '14:00:00', '17:00:00', 'CONFIRMEE');

-- ---------------------------------------------------------------------
--  Factures
-- ---------------------------------------------------------------------
INSERT INTO facture (adherent_id, libelle, montant, date_emission, statut) VALUES
(5, 'Abonnement annuel STANDARD',  25.00, '2025-01-15', 'PAYEE'),
(6, 'Abonnement annuel PREMIUM',   45.00, '2024-09-03', 'PAYEE'),
(2, 'Abonnement annuel ETUDIANT',  12.00, '2025-10-21', 'IMPAYEE'),
(5, 'Location salle Curie (2h)',   10.00, '2026-06-08', 'IMPAYEE');
