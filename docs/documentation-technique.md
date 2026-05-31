# Documentation technique

**Projet :** Application Client Lourd — Médiathèque de Bourg-la-Reine
**Auteur :** SEBAH Nassim — BTS SIO SLAM — Session 2026

---

## 1. Présentation générale

L'application est un **client lourd** (application de bureau) permettant aux agents
administratifs de gérer l'ensemble des services de la médiathèque : adhérents,
techniciens, salles de coworking, animations, réservations et factures.

| Élément | Choix |
|---------|-------|
| Langage | Java 17 |
| Interface | Java Swing |
| Persistance | JDBC |
| SGBD | MySQL 8.x |
| Architecture | MVC (Modèle-Vue-Contrôleur) |
| Build | Maven (ou scripts `build.sh` / `run.sh`) |
| Versionnage | Git / GitHub |

## 2. Architecture MVC

```
┌──────────────┐      ┌────────────────┐      ┌──────────────┐
│     VUE       │ ───▶ │   CONTRÔLEUR    │ ───▶ │     DAO       │ ───▶ MySQL
│  (Swing)      │ ◀─── │  (validation)   │ ◀─── │   (JDBC)      │
└──────────────┘      └────────────────┘      └──────────────┘
                              │
                              ▼
                          MODÈLE (POJO)
```

- **`model`** — classes métier sans logique de persistance (`Client`, `Salle`, …).
- **`dao`** — interface générique `DAO<T>` (create / read / update / delete) et une
  implémentation par entité ; encapsule toutes les requêtes SQL via `PreparedStatement`.
- **`controller`** — valide les saisies puis délègue au DAO ; gère la session connectée.
- **`view`** — fenêtres Swing (`LoginFrame`, `MainFrame`) et panneaux/dialogues par module.
- **`config`** — `DatabaseConnection`, connexion JDBC en singleton, paramétrée par
  `database.properties`.
- **`util`** — `PasswordUtil` (SHA-256) et `UITheme` (charte graphique).

### Avantages de ce découpage
- Séparation nette des responsabilités → maintenance et évolutions facilitées.
- Couche DAO interchangeable (on pourrait changer de SGBD sans toucher aux vues).
- Réutilisation : tous les DAO partagent le contrat `DAO<T>`.

## 3. Modèle de données

### Tables

| Table | Rôle | Clés étrangères |
|-------|------|-----------------|
| `profil` | Comptes agents (login, mot de passe SHA-256, rôle) | — |
| `client` | Adhérents | — |
| `technicien` | Animateurs | — |
| `salle` | Salles de coworking | — |
| `animation` | Animations | `salle_id`, `technicien_id` |
| `reservation` | Réservations de salles | `client_id`, `salle_id` |
| `facture` | Factures | `client_id` |

### Schéma relationnel (simplifié)

```
profil

client 1 ──── * facture
client 1 ──── * reservation * ──── 1 salle
salle  1 ──── * animation   * ──── 1 technicien
```

### Intégrité
- Clés étrangères avec `ON DELETE CASCADE` (factures/réservations d'un client) ou
  `ON DELETE RESTRICT` (salle/technicien référencés).
- Contraintes `CHECK` (capacité > 0, montant ≥ 0, cohérence des horaires).

## 4. Triggers — automatisation des disponibilités

Conformément au cahier des charges, des **triggers** automatisent la vérification des
disponibilités côté base (la règle métier est garantie même hors application) :

| Trigger | Table / moment | Rôle |
|---------|----------------|------|
| `trg_reservation_before_insert` | `reservation` BEFORE INSERT | Refuse une salle indisponible ou un créneau qui chevauche une réservation existante |
| `trg_reservation_before_update` | `reservation` BEFORE UPDATE | Même contrôle lors d'une modification |
| `trg_animation_before_insert` | `animation` BEFORE INSERT | Empêche deux animations dans la même salle au même créneau |

En cas de conflit, le trigger lève `SIGNAL SQLSTATE '45000'` avec un message explicite.
La couche DAO (`ReservationDAO`) détecte ce code et **remonte le message métier** à la vue,
qui l'affiche à l'utilisateur.

## 5. Sécurité

- **Authentification** obligatoire (écran de connexion) avant accès aux modules.
- Mots de passe stockés en **empreinte SHA-256** (jamais en clair) — `PasswordUtil`.
- **Requêtes paramétrées** (`PreparedStatement`) systématiques → protection contre les
  injections SQL.
- **Gestion des rôles** : le module « Profils » n'est accessible qu'aux comptes `ADMIN`.

## 6. Organisation du code source

```
mediatheque-lourd/
├── pom.xml                     # Configuration Maven
├── build.sh / run.sh           # Compilation / lancement sans Maven
├── docker-compose.yml          # Base MySQL pré-initialisée
├── sql/                        # Scripts SQL (schéma + données)
├── docs/                       # Documentations
└── src/main/
    ├── java/com/mediatheque/   # Code source (MVC)
    └── resources/              # database.properties
```

## 7. Compilation et déploiement

- **Maven** : `mvn clean package` génère un JAR exécutable « fat jar » incluant le
  connecteur JDBC.
- **Sans Maven** : `build.sh` compile via `javac` (télécharge le connecteur au besoin),
  `run.sh` lance l'application.

## 8. Pistes d'évolution

- Filtrage multicritères avancé sur les collections.
- Module d'archivage automatique des prêts/réservations terminés.
- Export PDF des factures.
- Pool de connexions (HikariCP) pour un usage multi-postes intensif.
