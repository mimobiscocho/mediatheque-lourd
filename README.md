# Médiathèque de Bourg-la-Reine — Application Client Lourd

Application **desktop** de gestion de la médiathèque de Bourg-la-Reine, développée en
**Java 17 (Swing)** avec une base de données **MySQL** et une persistance via **JDBC**.

Réalisation professionnelle n°2 — BTS SIO option SLAM — Session 2026
**SEBAH Nassim**

---

## 🎯 Fonctionnalités

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

## 🏗️ Architecture

Le projet applique le patron **MVC (Modèle-Vue-Contrôleur)** :

```
src/main/java/com/mediatheque/
├── Main.java                  # Point d'entrée
├── config/                    # Connexion JDBC (singleton)
├── model/                     # Modèles métier (POJO)
├── dao/                       # Accès aux données (JDBC) + interface générique DAO<T>
├── controller/                # Logique métier et validation
├── view/                      # Interfaces graphiques Swing
└── util/                      # Hachage SHA-256, charte graphique
```

- **Modèle** : POJO + DAO (couche d'accès aux données isolée).
- **Vue** : fenêtres et panneaux Swing (aucune logique métier).
- **Contrôleur** : validation et orchestration entre vue et DAO.

## 🗄️ Base de données

- 7 tables : `profil`, `client`, `technicien`, `salle`, `animation`, `reservation`, `facture`.
- 3 **triggers** assurant l'automatisation de la vérification des disponibilités
  (créneaux qui se chevauchent, salle indisponible).
- Scripts dans `sql/` : `01_schema.sql` (structure + triggers), `02_data.sql` (jeu de données).

## 🚀 Installation et lancement

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

Adaptez si besoin `src/main/resources/database.properties` :
```properties
db.url=jdbc:mysql://localhost:3306/mediatheque?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true
db.user=root
db.password=root
```

### 3. Compiler et lancer

**Avec les scripts fournis (sans Maven) :**
```bash
./build.sh      # télécharge le connecteur JDBC si besoin et compile
./run.sh        # lance l'application
```

**Avec Maven :**
```bash
mvn clean package
java -jar target/mediatheque-clientlourd-jar-with-dependencies.jar
```

**Sous Eclipse :** importer comme projet Maven existant (`File > Import > Existing Maven Projects`).

## 🔑 Comptes de démonstration

| Login | Mot de passe | Rôle |
|-------|--------------|------|
| `admin` | `admin123` | ADMIN (accès au module Profils) |
| `agent` | `agent123` | AGENT |

> Les mots de passe sont stockés sous forme d'empreinte **SHA-256**.

## 📚 Documentation

- [`docs/documentation-technique.md`](docs/documentation-technique.md) — architecture, modèle de données, choix techniques.
- [`docs/documentation-utilisateur.md`](docs/documentation-utilisateur.md) — guide d'utilisation pas à pas.
- [`docs/gestion-incidents.md`](docs/gestion-incidents.md) — incidents rencontrés et résolutions.

## 🧰 Technologies

Java 17 · Swing · JDBC · MySQL 8 · Maven · Git/GitHub · architecture MVC.
