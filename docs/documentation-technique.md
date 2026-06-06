# Documentation technique — Client lourd

**Projet :** Application Client Lourd — Médiathèque de Bourg-la-Reine
**Auteur :** SEBAH Nassim — BTS SIO SLAM — Session 2026

---

## Sommaire

1. [Présentation générale](#1-présentation-générale)
2. [Architecture MVC](#2-architecture-mvc)
3. [Connexion à la base de données](#3-connexion-à-la-base-de-données)
4. [Modèle de données](#4-modèle-de-données)
5. [Triggers — règles métier en base](#5-triggers--règles-métier-en-base)
6. [Sécurité](#6-sécurité)
7. [Organisation du code source](#7-organisation-du-code-source)
8. [Compilation et déploiement](#8-compilation-et-déploiement)
9. [Tests et validations](#9-tests-et-validations)
10. [Pistes d'évolution](#10-pistes-dévolution)

---

## 1. Présentation générale

L'application est un **client lourd** (application de bureau) permettant aux
agents administratifs de gérer l'ensemble des services de la médiathèque :
adhérents, techniciens, salles de coworking, animations, réservations et
factures.

| Élément | Choix |
|---------|-------|
| Langage | Java 17 |
| Interface | Java Swing |
| Persistance | JDBC + interface DAO générique |
| SGBD | MySQL 8.x |
| Architecture | MVC (Modèle – Vue – Contrôleur) |
| Sécurité mot de passe | PBKDF2-HMAC-SHA256 salé (600 000 itérations) |
| Build | Maven _ou_ scripts `build.sh` / `run.sh` (sans Maven) |
| Conteneurisation BD | Docker Compose (MySQL 8.4 préinitialisé) |
| Versionnage | Git / GitHub |

## 2. Architecture MVC

```
┌──────────────┐      ┌────────────────┐      ┌──────────────┐
│     VUE      │ ───▶ │   CONTRÔLEUR   │ ───▶ │     DAO      │ ───▶ MySQL
│   (Swing)    │ ◀─── │  (validation)  │ ◀─── │    (JDBC)    │
└──────────────┘      └────────────────┘      └──────────────┘
                              │
                              ▼
                          MODÈLE (POJO)
```

### Rôle de chaque paquetage

- **`model`** — classes métier sans logique de persistance (POJO). Une
  classe par entité : `Profil`, `Client`, `Technicien`, `Salle`, `Animation`,
  `Reservation`, `Facture`. Quelques champs dénormalisés (`salleNom`,
  `clientNom`…) sont remplis par les requêtes de jointure pour faciliter
  l'affichage.
- **`dao`** — interface générique `DAO<T>` exposant `create`, `update`,
  `delete`, `findById`, `findAll`. Une implémentation par entité encapsule
  toutes les requêtes SQL via `PreparedStatement`. Le DAO `StatsDAO`
  retourne en une seule requête les compteurs du tableau de bord.
- **`controller`** — couche métier qui valide les saisies puis délègue au
  DAO. `Session` (singleton) conserve le profil connecté. `AuthController`
  gère la connexion/déconnexion ; `ProfilController` vérifie en plus
  `Session.estAdmin()` pour interdire la gestion des profils aux non-admins.
- **`view`** — fenêtres Swing : `LoginFrame` (écran de connexion),
  `MainFrame` (sidebar + zone centrale en `CardLayout`) et un panneau +
  un dialogue par module métier.
- **`config`** — `DatabaseConnection`, connexion JDBC en singleton
  (`synchronized`) avec lecture prioritaire des variables d'environnement.
- **`util`** — `PasswordUtil` (PBKDF2 salé) et `UITheme` (charte graphique
  centralisée : couleurs institutionnelles, polices, styles de bouton et de
  tableau).

### Avantages de ce découpage

- **Séparation nette des responsabilités** : aucune SQL dans les vues, aucun
  composant Swing dans les DAO.
- **Couche DAO interchangeable** : on pourrait changer de SGBD ou passer à
  une couche REST sans toucher aux vues.
- **Réutilisation** : tous les DAO partagent le contrat `DAO<T>`, ce qui
  facilite l'ajout d'une entité.
- **Défense en profondeur** : les triggers BD jouent le rôle de garde-fou
  même si l'application est contournée.

## 3. Connexion à la base de données

`DatabaseConnection` est un singleton initialisé à la demande. La
configuration est lue dans cet ordre de priorité :

1. **Variables d'environnement** (recommandé en production) :
   - `MEDIATHEQUE_DB_URL`
   - `MEDIATHEQUE_DB_USER`
   - `MEDIATHEQUE_DB_PASSWORD`
2. **Fichier `database.properties`** dans le classpath (`src/main/resources/`),
   pour le développement local.

L'URL JDBC par défaut active explicitement `useSSL=false`, le fuseau
`Europe/Paris` et `characterEncoding=utf8` afin de garantir un comportement
identique sur toutes les plateformes.

```properties
db.url=jdbc:mysql://localhost:3306/mediatheque?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true&characterEncoding=utf8
```

La connexion est **partagée entre tous les DAO** ; sa fermeture est
déclenchée à la fermeture de la JVM via un `Runtime.getRuntime().addShutdownHook(...)`
installé dans `Main`.

## 4. Modèle de données

7 tables, en moteur **InnoDB**, jeu de caractères `utf8mb4_unicode_ci`.

### Tables et clés étrangères

| Table | Rôle | Clés étrangères |
|-------|------|-----------------|
| `profil` | Comptes agents (login, mot de passe PBKDF2, rôle) | — |
| `client` | Adhérents | — |
| `technicien` | Animateurs / intervenants | — |
| `salle` | Salles de coworking | — |
| `animation` | Animations | `salle_id`, `technicien_id` (RESTRICT) |
| `reservation` | Réservations de salles | `client_id` (CASCADE), `salle_id` (RESTRICT) |
| `facture` | Factures | `client_id` (CASCADE) |

### Schéma relationnel (simplifié)

```
profil

client 1 ──── * facture
client 1 ──── * reservation * ──── 1 salle
salle  1 ──── * animation   * ──── 1 technicien
```

### Intégrité référentielle

- Suppression d'un **client** → ses factures et réservations sont
  supprimées en cascade.
- Suppression d'une **salle** ou d'un **technicien** : refusée (RESTRICT)
  s'ils sont référencés par une animation. Le DAO traduit l'erreur en
  message utilisateur lisible.
- Contraintes `CHECK` :
  - `salle.capacite > 0`
  - `animation.nb_places >= 0`
  - `animation.heure_fin > animation.heure_debut`
  - `reservation.heure_fin > reservation.heure_debut`
  - `facture.montant >= 0`

## 5. Triggers — règles métier en base

Conformément au cahier des charges, des **triggers** automatisent la
vérification des disponibilités côté base : la règle métier est garantie
même si l'application est contournée.

| Trigger | Table / moment | Rôle |
|---------|----------------|------|
| `trg_reservation_before_insert` | `reservation` BEFORE INSERT | Refuse une salle indisponible ou un créneau qui chevauche une réservation existante |
| `trg_reservation_before_update` | `reservation` BEFORE UPDATE | Même contrôle lors d'une modification |
| `trg_animation_before_insert` | `animation` BEFORE INSERT | Empêche deux animations dans la même salle au même créneau |
| `trg_animation_before_update` | `animation` BEFORE UPDATE | Même contrôle lors d'une modification d'animation |

En cas de conflit, le trigger lève `SIGNAL SQLSTATE '45000'` avec un message
explicite. La couche DAO (`ReservationDAO::messageMetier`) détecte ce code
et **remonte le message métier** à la vue, qui l'affiche dans une boîte de
dialogue d'avertissement.

## 6. Sécurité

### Authentification

- Mots de passe stockés en **empreinte PBKDF2-HMAC-SHA256** salée (600 000
  itérations, sel aléatoire de 16 octets par compte). Vérification à
  **temps constant** pour éviter les attaques par timing — cf.
  `util/PasswordUtil`.
- Le hash inclut son propre sel et le nombre d'itérations
  (format `pbkdf2_sha256$iter$salt$hash`), ce qui rend la vérification
  autonome et facilite la migration vers un nouvel algorithme.
- L'authentification se fait par **lookup du login** puis comparaison
  PBKDF2 en mémoire (le sel rend impossible un `WHERE mot_de_passe = ?`).

### Gestion des rôles

Vérifiée en **deux endroits** (défense en profondeur) :

- **Côté vue** : le bouton « Profils » de la sidebar n'apparaît que pour
  les administrateurs (`Session.estAdmin()`).
- **Côté contrôleur** : `ProfilController` appelle `exigerAdmin()` sur
  toutes les opérations de modification (`enregistrer`, `supprimer`).
  Toute tentative depuis un compte AGENT lève `DAOException` avec un
  message explicite.

### Anti-injection SQL

- 100 % des interactions BD passent par `PreparedStatement` avec
  paramètres positionnels (`?`).
- Aucune concaténation de saisies utilisateur dans une chaîne SQL.

### Gestion des erreurs

- Le DAO encapsule les `SQLException` dans `DAOException` (non vérifiée),
  ce qui permet aux contrôleurs et aux vues de les attraper sans surcharger
  les signatures.
- L'écran de connexion n'affiche **pas** les détails techniques de la base
  (`SQLException::getMessage`) : un message neutre « Connexion à la base
  impossible. Contactez l'administrateur. » est affiché à l'utilisateur, le
  détail est imprimé sur la sortie d'erreur du serveur.
- Tous les `*Panel.rafraichir()` interceptent les `RuntimeException` pour
  éviter qu'une indisponibilité de la base ne plante l'interface.

### Gestion des secrets

- Le fichier `database.properties` versionné contient des valeurs **de
  développement uniquement** (`localhost`, `root`/`root`).
- En production, surcharge obligatoire par variables d'environnement
  (`MEDIATHEQUE_DB_URL`, `MEDIATHEQUE_DB_USER`, `MEDIATHEQUE_DB_PASSWORD`).
- Le build script `build.sh` vérifie l'empreinte SHA-256 du connecteur
  JDBC téléchargé (anti-supply chain).

## 7. Organisation du code source

```
mediatheque-lourd/
├── pom.xml                          # Configuration Maven
├── build.sh / run.sh                # Compilation / lancement sans Maven
├── docker-compose.yml               # Base MySQL pré-initialisée
├── sql/
│   ├── 01_schema.sql                # Tables + triggers
│   └── 02_data.sql                  # Jeu de démonstration
├── docs/                            # Cette documentation
└── src/main/
    ├── java/com/mediatheque/
    │   ├── Main.java                # Point d'entrée
    │   ├── config/
    │   │   └── DatabaseConnection.java
    │   ├── model/                   # 7 POJO
    │   ├── dao/
    │   │   ├── DAO.java             # Interface générique
    │   │   ├── DAOException.java
    │   │   ├── StatsDAO.java        # Compteurs du dashboard
    │   │   └── *DAO.java            # Un par entité
    │   ├── controller/
    │   │   ├── Session.java
    │   │   ├── AuthController.java
    │   │   └── *Controller.java     # Un par entité
    │   ├── view/
    │   │   ├── LoginFrame.java
    │   │   ├── MainFrame.java
    │   │   ├── DashboardPanel.java
    │   │   └── <Entité>Panel/Dialog.java
    │   └── util/
    │       ├── PasswordUtil.java    # PBKDF2 salé
    │       └── UITheme.java         # Couleurs, polices, helpers de style
    └── resources/
        └── database.properties      # Config dev (overridable par env)
```

## 8. Compilation et déploiement

### Maven (toutes plateformes)

```bash
mvn clean package
java -jar target/mediatheque-clientlourd-jar-with-dependencies.jar
```

Le profil par défaut produit un « fat jar » (`jar-with-dependencies`)
incluant le connecteur JDBC ; aucun classpath externe n'est nécessaire au
lancement.

### Scripts sans Maven — Linux / macOS

```bash
./build.sh    # télécharge le connecteur JDBC (et vérifie son SHA-256), compile
./run.sh      # lance l'application
```

### Sans Maven — Windows

```bat
:: Téléchargez puis placez mysql-connector-j-8.4.0.jar dans lib\
mkdir build
dir /S /B src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d build -cp lib\mysql-connector-j.jar @sources.txt
xcopy /Y src\main\resources\* build\
java -cp "build;lib\mysql-connector-j.jar" com.mediatheque.Main
```

### Base de données

**Avec Docker (recommandé pour la démonstration) :**
```bash
docker compose up -d
```
La base est préinitialisée avec le schéma et le jeu de données.

**Manuellement :**
```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_data.sql
```

### En production

1. Créer un utilisateur MySQL dédié avec des privilèges restreints à la
   base `mediatheque` (`SELECT`, `INSERT`, `UPDATE`, `DELETE`,
   `EXECUTE`).
2. Exporter les variables d'environnement avec les bons identifiants
   (cf. § 3).
3. Distribuer le fat jar et un script de lancement (`.sh` Linux / `.bat`
   Windows).

## 9. Tests et validations

| Test | Méthode | Résultat attendu |
|------|---------|------------------|
| Compilation complète | `javac -Xlint:all` | Pas d'erreur (warnings Swing standard tolérés) |
| Construction du fat jar | `mvn package` | `target/mediatheque-clientlourd-jar-with-dependencies.jar` |
| Hash PBKDF2 des comptes de démo | `PasswordUtil.verify` | OK pour `admin123`/`agent123`, FAIL pour mots de passe erronés |
| Connexion BD via Docker | `docker compose up -d` puis lancement | Tableau de bord avec compteurs cohérents |
| Tentative de réservation chevauchante | UI → réservation existante | Boîte de dialogue d'avertissement avec message du trigger |
| Tentative de modification d'animation vers un créneau occupé | UI → modifier animation | Message d'avertissement (trigger `before_update`) |
| Suppression d'une salle référencée | UI → salles → supprimer | Message « salle utilisée » (FK RESTRICT) |
| Suppression de son propre compte | Profils → admin connecté | Message « Vous ne pouvez pas supprimer votre propre compte » |
| Accès gestion des profils par un AGENT | Méthode `enregistrer` invoquée hors UI | `DAOException` « Accès refusé » |

## 10. Pistes d'évolution

- **Filtrage multicritères avancé** sur les collections (animations,
  factures impayées par tranche de date…).
- **Module d'archivage automatique** des réservations terminées et des
  animations passées (déplacement dans des tables d'archive).
- **Export PDF** des factures et des plannings d'animation.
- **Pool de connexions** (HikariCP) pour un usage multi-postes intensif.
- **`SwingWorker`** pour les chargements de longue durée afin d'éviter de
  geler l'EDT (Event Dispatch Thread).
- **Journalisation** centralisée via SLF4J + Logback pour faciliter le
  diagnostic en production.
- **Tests automatisés** (JUnit + Testcontainers MySQL) pour la couche DAO.
