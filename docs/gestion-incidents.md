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
| INC-05 | 2026-02-12 | Majeure | Mots de passe stockés en clair | Sécurité insuffisante | Hachage **SHA-256** via `PasswordUtil` avant insertion | Résolu |
| INC-06 | 2026-02-15 | Mineure | La connexion à la base se fermait de façon inattendue | Connexion fermée par un `try-with-resources` sur le singleton | Suppression de la fermeture de connexion dans les méthodes `findAll()` des DAO | Résolu |
| INC-07 | 2026-02-18 | Mineure | Suppression d'une salle liée à des animations provoquait une erreur SQL | Contrainte de clé étrangère `RESTRICT` | Message explicite remonté à l'utilisateur (« salle utilisée ») | Résolu |
| INC-08 | 2026-02-20 | Mineure | Format de date saisi librement provoquait des plantages | Absence de validation | Validation du format `jj/mm/aaaa` et `HH:mm` dans les boîtes de dialogue | Résolu |
| INC-09 | 2026-02-24 | Mineure | Un agent non administrateur accédait à la gestion des profils | Pas de contrôle de rôle | Affichage conditionnel du module selon `Session.estAdmin()` | Résolu |
| INC-10 | 2026-02-27 | Majeure | Risque d'injection SQL via les champs de recherche | Concaténation de chaînes | Passage systématique aux `PreparedStatement` paramétrés | Résolu |

## Procédure de signalement

1. L'agent décrit l'anomalie (capture, étapes de reproduction) dans **GLPI**.
2. Le ticket est qualifié (gravité, module concerné).
3. Correction sur une branche Git dédiée, puis test de non-régression.
4. Fusion (`merge`) après validation et clôture du ticket.
