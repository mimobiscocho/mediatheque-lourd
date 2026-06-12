# Médiathèque de Bourg-la-Reine — Application Client Lourd

Application **desktop** de gestion de la médiathèque de Bourg-la-Reine, développée en
**Java 17 (Swing)** avec une base de données **MySQL** et une persistance via **JDBC**.

Situation professionnelle n°2 (SP2) — BTS SIO option SLAM — Session 2026
**SEBAH Nassim**

## Sommaire

1. [Fonctionnalités](#fonctionnalités)
2. [Architecture](#architecture)
3. [Base de données](#base-de-données)
4. [Installation et lancement](#installation-et-lancement)
5. [Comptes de démonstration](#comptes-de-démonstration)
6. [Documentation](#documentation)
7. [Technologies](#technologies)

---

## Fonctionnalités

L'application permet aux agents administratifs de gérer, depuis une interface centralisée :

| Module | Description |
|--------|-------------|
| **Profils** | Comptes des agents (authentification, rôles ADMIN / AGENT) |
| **Clients** | Adhérents de la médiathèque (avec recherche multicritères) |
| **Techniciens** | Animateurs / intervenants |
| **Salles** | Salles de coworking (capacité, équipement, disponibilité) |
| **Animations** | Ateliers liés à une salle et un technicien |
| **Réservations** | Réservation de salles avec **contrôle de disponibilité automatique** (triggers) |
| **Factures** | Facturation des adhérents |

Chaque module propose un **CRUD complet** (Créer, Lire, Mettre à jour, Supprimer).

## Architecture

Le projet applique le patron **MVC (Modèle-Vue-Contrôleur)** :

```
src/main/java/com/mediatheque/
├── Main.java                  # Point d'entrée
├── config/                    # Connexion JDBC (singleton)
├── model/                     # Modèles métier (POJO)
├── dao/                       # Accès aux données (JDBC) + interface générique DAO<T>
├── controller/                # Logique métier et validation
├── view/                      # Interfaces graphiques Swing
└── util/                      # Hachage PBKDF2-HMAC-SHA256 salé, charte graphique
```

- **Modèle** : POJO + DAO (couche d'accès aux données isolée).
- **Vue** : fenêtres et panneaux Swing (aucune logique métier).
- **Contrôleur** : validation et orchestration entre vue et DAO.

## Base de données — schéma unifié

L'application partage **une seule base** `mediatheque` avec le client léger
(application web PHP). Le schéma est strictement identique dans les deux dépôts.

- Tables exploitées par cette application (client lourd) : `profil`,
  `adherent`, `technicien`, `animation`, `facture`.
- Tables **partagées** avec le client léger : `adherent`, `salle`,
  `reservation`.
- Tables ignorées par cette application (mais présentes pour le léger) :
  `agent`, `abonnement`, `livre`, `materiel`, `pret`.

> Note : la classe Java conserve le nom `Client` (DAO, Panel, Dialog) pour
> ne pas perturber l'IHM ; toutes les requêtes SQL ciblent bien la table
> `adherent` du schéma unifié.

- 4 **triggers** sur `reservation` et `animation` assurent la vérification
  des disponibilités (créneaux qui se chevauchent à l'insertion **et** à
  la modification, salle indisponible). 4 autres triggers (sur `pret`)
  sont définis pour le léger.
- Scripts dans `sql/` : `01_schema.sql` (structure + triggers), `02_data.sql` (jeu de données).

## Installation et lancement

### Prérequis
- **JDK 17** ou supérieur
- **MySQL 8.x** (ou Docker)

### 1. Démarrer la base de données

**Option A — Docker (recommandé, base pré-initialisée) :**
```bash
docker compose up -d
```

**Option B — MySQL existant :**
```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_data.sql
```

### 2. Configurer la connexion

Les valeurs par défaut conviennent à un environnement local Docker. En
production, surchargez-les via les **variables d'environnement** (priorité sur
le fichier) :
```bash
export MEDIATHEQUE_DB_URL="jdbc:mysql://serveur:3306/mediatheque?useSSL=true&serverTimezone=Europe/Paris&characterEncoding=utf8"
export MEDIATHEQUE_DB_USER="mediatheque_app"
export MEDIATHEQUE_DB_PASSWORD="********"
```
Sinon, adaptez `src/main/resources/database.properties`.

### 3. Compiler et lancer

**Linux / macOS (scripts fournis, sans Maven) :**
```bash
./build.sh      # télécharge le connecteur JDBC si besoin et compile
./run.sh        # lance l'application
```

**Windows (sans Maven) :**
```bat
:: Téléchargez puis placez mysql-connector-j-8.4.0.jar dans lib\
mkdir build
dir /S /B src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d build -cp lib\mysql-connector-j.jar @sources.txt
xcopy /Y src\main\resources\* build\
java -cp "build;lib\mysql-connector-j.jar" com.mediatheque.Main
```

**Avec Maven (toutes plateformes) :**
```bash
mvn clean package
java -jar target/mediatheque-clientlourd-jar-with-dependencies.jar
```

**Sous Eclipse :** importer comme projet Maven existant (`File > Import > Existing Maven Projects`).

## Comptes de démonstration

| Login | Mot de passe | Rôle |
|-------|--------------|------|
| `admin` | `admin123` | ADMIN (accès au module Profils) |
| `agent` | `agent123` | AGENT |

> Les mots de passe sont stockés sous forme d'empreinte **PBKDF2-HMAC-SHA256**
> (600 000 itérations, sel aléatoire de 16 octets par compte — cf.
> `util/PasswordUtil`).

## Documentation

| Document | Contenu |
|----------|---------|
| [`docs/documentation-technique.md`](docs/documentation-technique.md) | Architecture MVC + DAO, modèle de données, triggers, sécurité, build, déploiement |
| [`docs/documentation-utilisateur.md`](docs/documentation-utilisateur.md) | Guide utilisateur pas à pas (connexion, modules, messages, bonnes pratiques) |
| [`docs/gestion-incidents.md`](docs/gestion-incidents.md) | Suivi des incidents rencontrés et résolutions (GLPI) |

## Technologies

Java 17 · Swing · JDBC · MySQL 8 · Maven · Docker Compose · Git/GitHub · architecture MVC + DAO.

---

*Réalisation : SEBAH Nassim — BTS SIO SLAM — Session 2026.*
