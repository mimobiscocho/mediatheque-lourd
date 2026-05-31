# Documentation utilisateur

**Application de gestion — Médiathèque de Bourg-la-Reine**

Ce guide s'adresse aux agents administratifs qui utilisent l'application au quotidien.

---

## 1. Connexion

Au lancement, l'écran de connexion s'affiche.

1. Saisissez votre **identifiant** et votre **mot de passe**.
2. Cliquez sur **Se connecter** (ou appuyez sur Entrée).

Comptes de démonstration :

| Identifiant | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin` | `admin123` | Administrateur |
| `agent` | `agent123` | Agent |

> En cas d'identifiants incorrects, un message s'affiche en rouge. Vérifiez la saisie.

## 2. Écran principal

Après connexion, la fenêtre principale présente :

- **Une barre latérale gauche** : navigation entre les modules.
- **Une zone centrale** : le module sélectionné.
- **Un bouton Déconnexion** en haut à droite.

Le module **Profils** n'apparaît que pour les administrateurs.

## 3. Tableau de bord (Accueil)

À l'ouverture, le tableau de bord affiche des **indicateurs** : nombre de clients,
techniciens, salles, animations, réservations et factures.

## 4. Principe commun à tous les modules

Chaque module (Clients, Techniciens, Salles, etc.) fonctionne de la même façon :

| Action | Comment faire |
|--------|---------------|
| **Consulter** | Le tableau liste tous les enregistrements |
| **Ajouter** | Bouton **Ajouter** → remplir le formulaire → **Enregistrer** |
| **Modifier** | Sélectionner une ligne → **Modifier** → ajuster → **Enregistrer** |
| **Supprimer** | Sélectionner une ligne → **Supprimer** → confirmer |

> Les champs marqués d'une astérisque (*) sont obligatoires.

## 5. Modules

### 5.1 Clients
Gère les adhérents. Une **barre de recherche** (en haut à droite) permet de filtrer par
nom, prénom ou email. Le bouton **Tout** réaffiche la liste complète.

### 5.2 Techniciens
Gère les animateurs/intervenants (nom, contact, spécialité).

### 5.3 Salles
Gère les salles de coworking. Le champ **Disponible** détermine si la salle peut être
réservée. Une salle marquée indisponible sera refusée à la réservation.

### 5.4 Animations
Planifie les animations. Sélectionnez une **salle** et un **technicien** dans les listes
déroulantes, et renseignez la date et les horaires.

### 5.5 Réservations
Réserve une salle pour un client sur un créneau donné.
**Contrôle automatique** : si la salle est indisponible ou si le créneau chevauche une
réservation existante, l'application affiche un message et bloque l'enregistrement.

### 5.6 Factures
Émet et suit les factures des adhérents (libellé, montant, statut payée/impayée).

### 5.7 Profils (administrateurs uniquement)
Gère les comptes agents. Lors de la modification, **laisser le champ mot de passe vide**
conserve le mot de passe actuel. Vous ne pouvez pas supprimer votre propre compte.

## 6. Déconnexion

Cliquez sur **Déconnexion** en haut à droite, puis confirmez. Vous revenez à l'écran de
connexion.

## 7. Messages courants

| Message | Signification |
|---------|---------------|
| « Veuillez sélectionner… » | Aucune ligne n'est sélectionnée dans le tableau |
| « Le nom et le prénom sont obligatoires. » | Champ requis manquant |
| « Creneau deja reserve pour cette salle. » | La salle est déjà réservée sur ce créneau |
| « Salle indisponible : reservation impossible. » | La salle est marquée non disponible |
| « Erreur de connexion à la base… » | La base de données n'est pas démarrée / mal configurée |
