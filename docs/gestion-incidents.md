# Gestion des incidents

**Projet :** Application Client Lourd — Médiathèque de Bourg-la-Reine
**Suivi assuré via :** GLPI (gestionnaire d'incidents) durant les phases de test.

Ce document recense les principaux incidents rencontrés pendant le développement et les
tests, ainsi que leur résolution.

---

| N° | Date | Gravité | Description | Cause | Résolution | Statut |
|----|------|---------|-------------|-------|------------|--------|
| INC-01 | 2026-02-03 | Majeure | `ClassNotFoundException: com.mysql.cj.jdbc.Driver` au lancement | Connecteur JDBC absent du classpath | Ajout de la dépendance dans `pom.xml` / téléchargement automatique dans `build.sh` | Résolu |
| INC-02 | 2026-02-05 | Majeure | Caractères accentués mal affichés en base | Jeu de caractères par défaut (latin1) | Base créée en `utf8mb4` + `serverTimezone` dans l'URL JDBC | Résolu |
| INC-03 | 2026-02-09 | Critique | Possibilité de réserver deux fois la même salle au même créneau | Aucun contrôle de chevauchement | Mise en place du trigger `trg_reservation_before_insert` (SIGNAL 45000) | Résolu |
| INC-04 | 2026-02-10 | Mineure | Message d'erreur SQL brut affiché à l'utilisateur lors d'un conflit | Exception non interprétée | Interception du `SQLSTATE 45000` dans `ReservationDAO` et remontée d'un message métier lisible | Résolu |
| INC-05 | 2026-02-12 | Majeure | Mots de passe stockés en clair | Sécurité insuffisante | Hachage initial SHA-256 puis migration vers **PBKDF2-HMAC-SHA256** (600 000 itérations, sel par compte) — cf. `util/PasswordUtil` | Résolu |
| INC-06 | 2026-02-15 | Mineure | La connexion à la base se fermait de façon inattendue | Connexion fermée par un `try-with-resources` sur le singleton | Suppression de la fermeture de connexion dans les méthodes `findAll()` des DAO | Résolu |
| INC-07 | 2026-02-18 | Mineure | Suppression d'une salle liée à des animations provoquait une erreur SQL | Contrainte de clé étrangère `RESTRICT` | Message explicite remonté à l'utilisateur (« salle utilisée ») | Résolu |
| INC-08 | 2026-02-20 | Mineure | Format de date saisi librement provoquait des plantages | Absence de validation | Validation du format `jj/mm/aaaa` et `HH:mm` dans les boîtes de dialogue | Résolu |
| INC-09 | 2026-02-24 | Mineure | Un agent non administrateur accédait à la gestion des profils | Pas de contrôle de rôle | Affichage conditionnel du module selon `Session.estAdmin()` | Résolu |
| INC-10 | 2026-02-27 | Majeure | Risque d'injection SQL via les champs de recherche | Concaténation de chaînes | Passage systématique aux `PreparedStatement` paramétrés | Résolu |
| INC-11 | 2026-03-04 | Mineure | Texte de la bannière et de la barre latérale illisible (couleur noire sur fond bleu foncé) | Couleur explicitement `Color.black` au lieu de blanc | Remplacement par `UITheme.WHITE` et `setOpaque(true)` sur les composants concernés | Résolu |
| INC-12 | 2026-03-06 | Majeure | Modification d'une animation pouvait recréer un chevauchement de créneau | Trigger uniquement déclenché sur INSERT | Ajout d'un trigger `BEFORE UPDATE` symétrique sur la table `animation` | Résolu |
| INC-13 | 2026-03-08 | Mineure | Le tableau de bord exécutait 6 `SELECT *` complets juste pour des compteurs | Utilisation directe de `findAll().size()` | Introduction d'un `StatsDAO` réalisant une seule requête agrégée `SELECT (SELECT COUNT(*)…)` | Résolu |
| INC-14 | 2026-03-10 | Mineure | Mot de passe BD en clair dans le dépôt (`database.properties`) | Aucun moyen de surcharger par environnement | Lecture prioritaire des variables `MEDIATHEQUE_DB_*` dans `DatabaseConnection`, valeurs du fichier conservées comme valeurs de développement | Résolu |
| INC-15 | 2026-03-13 | Mineure | Détails JDBC (URL, schéma) affichés sur l'écran de connexion en cas d'erreur | `messageLabel.setText("… " + ex.getMessage())` | Message générique à l'écran, détails écrits sur `System.err` pour le diagnostic | Résolu |
| INC-16 | 2026-03-16 | Mineure | Animation planifiable dans une salle marquée indisponible | `AnimationDialog` listait toutes les salles | Filtre `s.isDisponible()` sur le combo (sauf si la salle était déjà liée à l'animation en cours de modification) | Résolu |
| INC-17 | 2026-03-19 | Majeure | Un AGENT pouvait théoriquement déclencher `ProfilController` hors UI (modèle de menace : code tiers) | Contrôle de rôle uniquement visuel | Vérification `Session.estAdmin()` ajoutée dans toutes les méthodes de `ProfilController` (défense en profondeur) | Résolu |

## Procédure de signalement

1. L'agent décrit l'anomalie (capture d'écran, étapes de reproduction,
   message éventuel) dans **GLPI**.
2. Le ticket est qualifié (gravité, module concerné, priorité).
3. Correction sur une branche Git dédiée (convention :
   `fix/INC-XX-description`).
4. Tests de non-régression manuels sur les fonctionnalités impactées.
5. Fusion (`merge`) après validation et clôture du ticket avec un compte
   rendu de la correction.

## Classification des gravités

| Gravité | Critère | Délai cible |
|---------|---------|-------------|
| **Critique** | Indisponibilité, perte de données, faille de sécurité exploitable | < 24 h |
| **Majeure** | Fonction métier dégradée, contournement existant | < 1 semaine |
| **Mineure** | Affichage, ergonomie, message peu clair, log parasite | Itération suivante |
