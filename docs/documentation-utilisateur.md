# Documentation utilisateur — Client lourd

**Application :** Application de gestion (desktop) — Médiathèque de Bourg-la-Reine

Ce guide s'adresse aux agents administratifs qui utilisent l'application
sur poste de travail.

---

## Sommaire

1. [Connexion](#1-connexion)
2. [Écran principal](#2-écran-principal)
3. [Tableau de bord](#3-tableau-de-bord-accueil)
4. [Principe commun à tous les modules](#4-principe-commun-à-tous-les-modules)
5. [Modules](#5-modules)
6. [Déconnexion](#6-déconnexion)
7. [Messages courants](#7-messages-courants)
8. [Bonnes pratiques et résolution de problèmes](#8-bonnes-pratiques-et-résolution-de-problèmes)

---

## 1. Connexion

Au lancement, l'écran de connexion s'affiche.

1. Saisir votre **identifiant** (login).
2. Saisir votre **mot de passe**.
3. Cliquer sur **Se connecter** (ou appuyer sur la touche Entrée).

Comptes de démonstration :

| Identifiant | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin` | `admin123` | Administrateur |
| `agent` | `agent123` | Agent |

> **En cas d'erreur de saisie**, un message rouge apparaît en bas du
> formulaire. Vérifiez votre identifiant et votre mot de passe ; si le
> problème persiste, contactez l'administrateur.

> **En cas d'erreur de connexion à la base**, un message neutre s'affiche
> (« Connexion à la base impossible. Contactez l'administrateur. »). Les
> détails techniques sont écrits dans le journal du poste pour permettre
> le diagnostic, mais ne sont pas affichés à l'utilisateur final.

## 2. Écran principal

Après connexion, la fenêtre principale est organisée comme suit :

- **Bandeau supérieur** (bleu foncé) : titre de l'application, nom de
  l'utilisateur connecté et bouton **Déconnexion**.
- **Barre latérale gauche** (bleue) : navigation entre les modules métier
  (Accueil, Clients, Techniciens, Salles, Animations, Réservations,
  Factures, et Profils pour les administrateurs).
- **Zone centrale** : module sélectionné. Au démarrage, c'est le tableau de
  bord (« Accueil »).

Le module **Profils** n'est visible que pour les comptes ayant le rôle
ADMIN.

## 3. Tableau de bord (Accueil)

À l'ouverture, le tableau de bord affiche **6 cartes** synthétiques :

- Nombre de clients
- Nombre de techniciens
- Nombre de salles
- Nombre d'animations
- Nombre de réservations
- Nombre de factures

Ces compteurs sont calculés en **une seule requête** SQL pour rester
rapides même quand la base grossit.

> En cas d'indisponibilité de la base, un message rouge remplace les
> cartes (« Connexion à la base impossible. Vérifiez que MySQL est
> démarré. »).

## 4. Principe commun à tous les modules

Chaque module (Clients, Techniciens, Salles, Animations, Réservations,
Factures, Profils) fonctionne selon le même schéma :

| Action | Comment faire |
|--------|---------------|
| **Consulter** | Le tableau central affiche tous les enregistrements |
| **Sélectionner** | Cliquer sur une ligne du tableau |
| **Ajouter** | Bouton **Ajouter** → remplir le formulaire → **Enregistrer** |
| **Modifier** | Sélectionner une ligne → **Modifier** → ajuster → **Enregistrer** |
| **Supprimer** | Sélectionner une ligne → **Supprimer** → confirmer |

> Les champs marqués d'un astérisque (*) sont obligatoires.

Les saisies de **dates** se font au format `jj/mm/aaaa`, et les **heures**
au format `HH:mm` (24 heures). En cas de format invalide, une boîte de
dialogue d'erreur s'affiche.

Toute action réussie ferme la boîte de dialogue ; toute erreur affiche un
message expliquant la cause.

## 5. Modules

### 5.1 Clients

Gère les adhérents de la médiathèque.

- **Recherche** : une barre **Rechercher** en haut à droite permet de
  filtrer par nom, prénom ou email. Le bouton **Tout** réaffiche la liste
  complète.
- **Suppression** : confirmation explicite, avec rappel que les factures et
  réservations de l'adhérent seront aussi supprimées (cascade FK).
- **Format date d'inscription** : `jj/mm/aaaa`. Par défaut, la date du jour
  est pré-remplie.

### 5.2 Techniciens

Gère les animateurs et intervenants (nom, contact, spécialité).

> La suppression d'un technicien lié à une ou plusieurs animations est
> refusée par la base. Vous devez d'abord réaffecter ou supprimer les
> animations concernées.

### 5.3 Salles

Gère les salles de coworking.

- Le champ **Disponible** détermine si la salle peut être réservée ou
  utilisée pour une animation. Une salle marquée non disponible **disparaît
  des listes déroulantes** dans les formulaires de réservation et
  d'animation (sauf si elle est déjà liée à l'animation en cours de
  modification).
- La capacité doit être strictement positive.
- La suppression d'une salle référencée par une animation ou une
  réservation est refusée (message « salle utilisée »).

### 5.4 Animations

Planifie les animations (ateliers, conférences, club de lecture…).

- Sélection d'une **salle** et d'un **technicien** dans des listes
  déroulantes.
- Saisie de la **date** (`jj/mm/aaaa`), des **heures de début et de fin**
  (`HH:mm`) et du **nombre de places**.
- **Contrôle automatique** : deux animations ne peuvent pas être planifiées
  sur la même salle au même créneau, à la création comme à la
  modification. Le message d'erreur est explicite (« La salle est déjà
  occupée par une animation sur ce créneau »).

### 5.5 Réservations

Réserve une salle pour un client sur un créneau donné.

- **Statut** : `CONFIRMEE` ou `ANNULEE`. Une réservation annulée libère le
  créneau pour une nouvelle réservation.
- **Contrôle automatique** :
  - Refus si la salle est marquée indisponible.
  - Refus si le créneau chevauche une réservation **confirmée** existante.
  - Mêmes contrôles à la modification (pas seulement à la création).
- En cas de conflit, une boîte de dialogue d'**avertissement** explique la
  cause, et l'enregistrement n'est pas effectué.

### 5.6 Factures

Émet et suit les factures des adhérents.

- Saisie : client (liste déroulante), libellé, montant (avec virgule ou
  point décimal), date d'émission, statut (`PAYEE` / `IMPAYEE`).
- Le montant doit être positif.

### 5.7 Profils (administrateurs uniquement)

Gère les comptes agents (login, nom, prénom, rôle, mot de passe).

- **Lors de la création**, le mot de passe est obligatoire et sera haché
  (PBKDF2 salé) avant insertion.
- **Lors de la modification**, laisser le champ mot de passe vide conserve
  le mot de passe actuel. Saisir une nouvelle valeur le remplace.
- **Sécurité** : vous ne pouvez pas supprimer votre propre compte. Un
  agent (non admin) ne peut pas accéder à ce module, ni directement
  ni indirectement.

## 6. Déconnexion

Cliquer sur **Déconnexion** en haut à droite. Une boîte de confirmation
s'affiche ; en validant, vous revenez à l'écran de connexion.

> Pensez à vous déconnecter avant de quitter le poste, surtout s'il est
> partagé.

## 7. Messages courants

| Message | Signification |
|---------|---------------|
| « Identifiant ou mot de passe incorrect. » | Login/mot de passe erronés |
| « Connexion à la base impossible. Contactez l'administrateur. » | MySQL est arrêté ou mal configuré |
| « Veuillez sélectionner… » | Aucune ligne n'est sélectionnée dans le tableau |
| « Le nom et le prénom sont obligatoires. » | Champ requis manquant |
| « L'heure de fin doit être postérieure à l'heure de début. » | Plage horaire incohérente |
| « Format de date ou d'heure invalide. » | Saisie ne respectant pas `jj/mm/aaaa` / `HH:mm` |
| « Montant invalide. » | Le montant saisi n'est pas un nombre |
| « Creneau deja reserve pour cette salle. » | Une réservation confirmée existe déjà sur ce créneau |
| « Salle indisponible : reservation impossible. » | La salle est marquée non disponible |
| « La salle est deja occupee par une animation sur ce creneau. » | Conflit d'animation |
| « Suppression impossible : ce technicien est lié à des animations. » | FK RESTRICT — réaffecter d'abord les animations |
| « Suppression impossible : cette salle est utilisée (animations / réservations). » | FK RESTRICT — supprimer d'abord les animations/réservations |
| « Vous ne pouvez pas supprimer votre propre compte. » | Sécurité — un admin ne peut pas se retirer ses propres droits |
| « Accès refusé : la gestion des profils est réservée aux administrateurs. » | Tentative d'accès non autorisée |

## 8. Bonnes pratiques et résolution de problèmes

### Bonnes pratiques

- **Ne partagez pas votre compte** : chaque agent doit utiliser son propre
  identifiant.
- **Déconnectez-vous** systématiquement avant de quitter un poste partagé.
- **Vérifiez le tableau de bord** en arrivant pour avoir une vue d'ensemble
  rapide de l'activité.
- **Format de date et heure** : `jj/mm/aaaa` et `HH:mm` ; le calendrier
  système n'est pas utilisé.

### Que faire si…

| Symptôme | Action |
|----------|--------|
| Le tableau de bord affiche « Connexion impossible » | Vérifier que MySQL/Docker est démarré sur le serveur. Si la base est distante, vérifier le réseau. |
| Une réservation est refusée alors que la salle semble libre | Une réservation **confirmée** d'un autre agent existe sans doute déjà. Aller dans Réservations pour vérifier. |
| Impossible de supprimer une salle ou un technicien | C'est référencé par une animation. Aller dans Animations, supprimer / réaffecter, puis revenir. |
| Caractères accentués mal affichés | Vérifier que la base est bien en `utf8mb4` (cas déjà traité par les scripts d'initialisation). |
| L'application gèle quelques secondes | Le chargement de la base est synchrone : si la base est lente, attendez. Pour un usage intensif, prévoir l'évolution vers un pool de connexions. |

### Signalement d'un incident

Tout dysfonctionnement reproductible doit être signalé via **GLPI**
(gestionnaire d'incidents) en précisant :

- la date et l'heure,
- le module et l'action concernés,
- le message reçu,
- les étapes de reproduction.
