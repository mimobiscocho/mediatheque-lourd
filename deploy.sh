#!/usr/bin/env bash
# =============================================================================
#  Médiathèque lourd — Déploiement sur un serveur Linux
#
#  Modèle :  le serveur héberge la BASE DE DONNÉES centrale ; les clients
#            Swing tournent sur les postes utilisateurs et s'y connectent
#            par le réseau. Ce script :
#              - installe JDK + MariaDB
#              - crée la base, l'utilisateur, charge le schéma + jeu de données
#              - active l'accès BD distant (par défaut OUI pour le lourd)
#              - compile le projet et package un archive client distribuable
#                (mediatheque-client.tar.gz) prêt à scp vers les postes.
#
#  Cible :   Debian/Ubuntu (testé), RHEL/Fedora/Rocky (best-effort), Arch
#
#  Usage :   sudo ./deploy.sh [all|prereqs|database|build|package|verify|status|help]
#
#  Variables surchageables :
#    DIST_DIR=./dist                            répertoire d'archive client
#    DB_NAME=mediatheque                        nom de la base
#    DB_USER=mediatheque                        utilisateur BD
#    DB_PASSWORD=...                            mdp (généré aléatoirement sinon)
#    DB_REMOTE_HOST=%                           hôte autorisé (% = tous, ou IP)
#    DB_PUBLIC_HOST=auto                        adresse du serveur côté client
#
#  Re-run safe : idempotent (CREATE OR ALTER, archive régénérée).
# =============================================================================
set -euo pipefail

# --- Couleurs ---------------------------------------------------------------
if [ -t 1 ]; then
  C_OK=$'\033[0;32m'; C_KO=$'\033[0;31m'
  C_WARN=$'\033[0;33m'; C_INFO=$'\033[0;34m'
  C_DIM=$'\033[0;90m'; C_RESET=$'\033[0m'
else
  C_OK=''; C_KO=''; C_WARN=''; C_INFO=''; C_DIM=''; C_RESET=''
fi

log()   { printf "${C_INFO}>>${C_RESET} %s\n" "$*"; }
ok()    { printf "  ${C_OK}[OK]${C_RESET} %s\n" "$*"; }
warn()  { printf "  ${C_WARN}[!]${C_RESET}  %s\n" "$*"; }
err()   { printf "  ${C_KO}[KO]${C_RESET} %s\n" "$*" >&2; }
title() { printf "\n${C_INFO}=== %s ===${C_RESET}\n" "$*"; }

# --- sudo / root ------------------------------------------------------------
SUDO=""
if [ "$(id -u)" -ne 0 ]; then
  if command -v sudo >/dev/null 2>&1; then
    SUDO="sudo"
  else
    err "Ce script doit être exécuté en root (ou via sudo)."
    exit 1
  fi
fi

# --- Détection de la distribution -------------------------------------------
detect_distro() {
  if [ -f /etc/os-release ]; then
    . /etc/os-release
    case "${ID:-}${ID_LIKE:-}" in
      *debian*|*ubuntu*) echo "debian"; return ;;
      *rhel*|*fedora*|*centos*|*rocky*|*alma*) echo "rhel"; return ;;
      *arch*|*manjaro*) echo "arch"; return ;;
    esac
  fi
  echo "unknown"
}
DISTRO=$(detect_distro)

# --- État persistant --------------------------------------------------------
STATE_DIR=/etc/mediatheque-lourd
STATE_FILE="$STATE_DIR/deploy.state"
if [ -r "$STATE_FILE" ]; then
  # shellcheck disable=SC1090
  . "$STATE_FILE"
fi

# --- Valeurs par défaut ----------------------------------------------------
SOURCE_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="${DIST_DIR:-$SOURCE_DIR/dist}"
DB_NAME="${DB_NAME:-mediatheque}"
DB_USER="${DB_USER:-mediatheque}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_REMOTE_HOST="${DB_REMOTE_HOST:-%}"
DB_PUBLIC_HOST="${DB_PUBLIC_HOST:-auto}"

# Adresse à utiliser dans le run.sh client : par défaut, on prend la première
# IP non-loopback. Override possible via DB_PUBLIC_HOST=<ip-ou-nom>.
if [ "$DB_PUBLIC_HOST" = "auto" ]; then
  # Désactive temporairement pipefail car hostname -I n'existe pas partout
  set +o pipefail
  _ip="$(hostname -I 2>/dev/null | awk '{print $1}')" || _ip=""
  if [ -z "$_ip" ] && command -v ip >/dev/null 2>&1; then
    _ip="$(ip -4 -o addr show scope global 2>/dev/null \
           | awk '{print $4}' | cut -d/ -f1 | head -1)" || _ip=""
  fi
  set -o pipefail
  if [ -n "$_ip" ]; then
    DB_PUBLIC_HOST="$_ip"
  else
    DB_PUBLIC_HOST="$(hostname -f 2>/dev/null || hostname)"
  fi
  unset _ip
fi

# --- Sanity check : présence du projet -------------------------------------
if [ ! -f "$SOURCE_DIR/build.sh" ] || [ ! -d "$SOURCE_DIR/src/main/java/com/mediatheque" ]; then
  err "Sources du client lourd introuvables (build.sh / src/main/java/com/mediatheque)."
  err "Lancez ce script depuis la racine du dépôt mediatheque-lourd."
  exit 1
fi

# --- Persistance ------------------------------------------------------------
save_state() {
  if ! $SUDO mkdir -p "$STATE_DIR" 2>/dev/null; then
    warn "Impossible d'écrire dans $STATE_DIR (sudo requis) — état non persistant."
    return 0
  fi
  $SUDO tee "$STATE_FILE" >/dev/null <<EOF || true
# Généré par deploy.sh le $(date '+%Y-%m-%d %H:%M:%S')
DB_NAME="$DB_NAME"
DB_USER="$DB_USER"
DB_PASSWORD="$DB_PASSWORD"
DB_REMOTE_HOST="$DB_REMOTE_HOST"
DB_PUBLIC_HOST="$DB_PUBLIC_HOST"
DIST_DIR="$DIST_DIR"
EOF
  $SUDO chmod 600 "$STATE_FILE" 2>/dev/null || true
}

# =============================================================================
#                              ÉTAPES
# =============================================================================

# ----- 1. Paquets prérequis -------------------------------------------------
install_prereqs() {
  title "Installation des paquets"
  case "$DISTRO" in
    debian)
      $SUDO apt-get update -qq
      DEBIAN_FRONTEND=noninteractive $SUDO apt-get install -y --no-install-recommends \
          default-jdk-headless mariadb-server \
          curl rsync openssl ca-certificates tar
      $SUDO systemctl enable --now mariadb
      ok "JDK + MariaDB installés."
      ;;
    rhel)
      $SUDO dnf install -y --allowerasing \
          java-17-openjdk-devel mariadb-server \
          curl rsync openssl tar
      $SUDO systemctl enable --now mariadb
      ok "JDK 17 + MariaDB installés."
      ;;
    arch)
      $SUDO pacman -Sy --noconfirm jdk-openjdk mariadb curl rsync openssl tar
      [ -d /var/lib/mysql/mysql ] || $SUDO mariadb-install-db \
          --user=mysql --basedir=/usr --datadir=/var/lib/mysql
      $SUDO systemctl enable --now mariadb
      ok "JDK + MariaDB installés."
      ;;
    *)
      err "Distribution non supportée automatiquement."
      err "Installez manuellement : JDK 17+, mariadb-server, curl, tar, rsync."
      exit 1
      ;;
  esac

  # Vérification rapide des versions
  log "Versions installées :"
  java -version 2>&1 | head -1 | sed 's/^/    /'
  mariadb --version 2>/dev/null | sed 's/^/    /' \
    || mysql --version 2>/dev/null | sed 's/^/    /' \
    || true
}

# ----- 2. Base de données ---------------------------------------------------
setup_database() {
  title "Configuration de la base de données"

  if [ -z "$DB_PASSWORD" ]; then
    DB_PASSWORD="$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)"
    log "Mot de passe BD généré : $DB_PASSWORD"
  fi

  if ! $SUDO mariadb -e "SELECT 1" >/dev/null 2>&1 \
       && ! $SUDO mysql -e "SELECT 1" >/dev/null 2>&1; then
    err "Impossible de se connecter à MariaDB en root (socket Unix)."
    err "Vérifiez le service : systemctl status mariadb"
    exit 1
  fi

  MYSQL=mysql
  command -v mariadb >/dev/null && MYSQL=mariadb

  # Création / mise à jour de la base et des utilisateurs.
  # On crée TROIS variantes utilisateurs :
  #   - 'localhost' pour le léger éventuellement hébergé sur la même machine
  #   - '127.0.0.1' pour les connexions TCP locales
  #   - '$DB_REMOTE_HOST' (% par défaut) pour les clients lourd distants
  $SUDO "$MYSQL" <<SQL
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'127.0.0.1' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'$DB_REMOTE_HOST' IDENTIFIED BY '$DB_PASSWORD';
ALTER  USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
ALTER  USER '$DB_USER'@'127.0.0.1' IDENTIFIED BY '$DB_PASSWORD';
ALTER  USER '$DB_USER'@'$DB_REMOTE_HOST' IDENTIFIED BY '$DB_PASSWORD';

GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'127.0.0.1';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'$DB_REMOTE_HOST';
FLUSH PRIVILEGES;
SQL
  ok "Base + utilisateurs '$DB_USER@{localhost,127.0.0.1,$DB_REMOTE_HOST}' configurés."

  # Chargement du schéma + des données si la base est vide
  local nb_tables
  nb_tables=$($SUDO "$MYSQL" -sN -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
  if [ "${nb_tables:-0}" -lt 11 ]; then
    log "Chargement du schéma SQL ($SOURCE_DIR/sql/01_schema.sql)..."
    $SUDO "$MYSQL" "$DB_NAME" < "$SOURCE_DIR/sql/01_schema.sql"
    log "Chargement du jeu de données ($SOURCE_DIR/sql/02_data.sql)..."
    $SUDO "$MYSQL" "$DB_NAME" < "$SOURCE_DIR/sql/02_data.sql"
    ok "Schéma et données chargés."
  else
    ok "Schéma déjà présent ($nb_tables tables) — pas de rechargement."
  fi

  # --- Bind MariaDB sur le réseau si DB_REMOTE_HOST != localhost ---
  if [ "$DB_REMOTE_HOST" != "localhost" ] && [ "$DB_REMOTE_HOST" != "127.0.0.1" ]; then
    local mycnf=""
    for f in /etc/mysql/mariadb.conf.d/50-server.cnf \
             /etc/mysql/my.cnf \
             /etc/my.cnf.d/server.cnf \
             /etc/my.cnf; do
      if [ -f "$f" ]; then mycnf="$f"; break; fi
    done
    if [ -n "$mycnf" ]; then
      if $SUDO grep -qE "^\s*bind-address" "$mycnf"; then
        $SUDO sed -i 's/^\s*bind-address.*/bind-address = 0.0.0.0/' "$mycnf"
      else
        # Si la section [mysqld] existe, on ajoute après ; sinon on append
        if $SUDO grep -qE "^\[mysqld\]" "$mycnf"; then
          $SUDO sed -i '/^\[mysqld\]/a bind-address = 0.0.0.0' "$mycnf"
        else
          echo -e "\n[mysqld]\nbind-address = 0.0.0.0" | $SUDO tee -a "$mycnf" >/dev/null
        fi
      fi
      $SUDO systemctl restart mariadb
      ok "MariaDB écoute désormais sur 0.0.0.0:3306 ($mycnf)."
      warn "Pensez à ouvrir le port 3306 dans le pare-feu pour les clients."
      warn "Idéalement, restreignez par IP (DB_REMOTE_HOST=192.168.1.10 par exemple)."
    else
      warn "Fichier de config MariaDB introuvable — bind-address non modifié."
    fi
  fi
}

# ----- 3. Compilation -------------------------------------------------------
build() {
  title "Compilation du client lourd"

  if ! command -v javac >/dev/null 2>&1; then
    err "javac introuvable. Installez d'abord avec : $0 prereqs"
    exit 1
  fi

  # build.sh télécharge le connecteur JDBC et compile dans build/
  ( cd "$SOURCE_DIR" && bash build.sh )

  if [ ! -f "$SOURCE_DIR/build/com/mediatheque/Main.class" ]; then
    err "Compilation échouée : Main.class introuvable dans build/."
    exit 1
  fi
  ok "Compilation terminée."
}

# ----- 4. Packaging d'une archive client ------------------------------------
package() {
  title "Génération de l'archive client"

  # Build préalable nécessaire
  if [ ! -f "$SOURCE_DIR/build/com/mediatheque/Main.class" ]; then
    log "Compilation préalable..."
    build
  fi

  # Lien symbolique attendu par build.sh
  if [ ! -f "$SOURCE_DIR/lib/mysql-connector-j.jar" ] \
     && [ -f "$SOURCE_DIR/lib/mysql-connector-j-8.4.0.jar" ]; then
    ln -sf mysql-connector-j-8.4.0.jar "$SOURCE_DIR/lib/mysql-connector-j.jar"
  fi

  mkdir -p "$DIST_DIR"
  local CLIENT_DIR="$DIST_DIR/mediatheque-client"
  rm -rf "$CLIENT_DIR"
  mkdir -p "$CLIENT_DIR/lib"

  # 1) Création d'un JAR exécutable embarquant les .class et la classpath JDBC.
  cat > /tmp/mediatheque-manifest.txt <<EOF
Manifest-Version: 1.0
Main-Class: com.mediatheque.Main
Class-Path: lib/mysql-connector-j.jar

EOF
  ( cd "$SOURCE_DIR/build" \
      && jar cfm "$CLIENT_DIR/mediatheque-app.jar" /tmp/mediatheque-manifest.txt \
          com/ database.properties 2>/dev/null )
  rm -f /tmp/mediatheque-manifest.txt
  ok "JAR exécutable créé ($(du -h "$CLIENT_DIR/mediatheque-app.jar" | cut -f1))."

  # 2) Connecteur JDBC
  cp "$SOURCE_DIR/lib/mysql-connector-j.jar" "$CLIENT_DIR/lib/"
  ok "Connecteur JDBC inclus."

  # 3) Script de lancement côté poste utilisateur
  cat > "$CLIENT_DIR/run.sh" <<RUN
#!/usr/bin/env bash
# =============================================================================
#  Médiathèque — Client lourd (lancement côté poste utilisateur)
#  Renseignez DB_PASSWORD ci-dessous ou passez-le via env :
#     DB_PASSWORD=xxx ./run.sh
# =============================================================================
set -e
cd "\$(dirname "\$0")"

DB_HOST="\${DB_HOST:-$DB_PUBLIC_HOST}"
DB_PORT="\${DB_PORT:-3306}"
DB_NAME="\${DB_NAME:-$DB_NAME}"
DB_USER="\${DB_USER:-$DB_USER}"
DB_PASSWORD="\${DB_PASSWORD:-}"

if ! command -v java >/dev/null 2>&1; then
  echo "Erreur : Java n'est pas installé sur ce poste." >&2
  exit 1
fi

if [ -z "\$DB_PASSWORD" ]; then
  read -rsp "Mot de passe BD : " DB_PASSWORD
  echo
fi

export MEDIATHEQUE_DB_URL="jdbc:mysql://\${DB_HOST}:\${DB_PORT}/\${DB_NAME}?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true&characterEncoding=utf8"
export MEDIATHEQUE_DB_USER="\$DB_USER"
export MEDIATHEQUE_DB_PASSWORD="\$DB_PASSWORD"

exec java -jar mediatheque-app.jar
RUN
  chmod +x "$CLIENT_DIR/run.sh"
  ok "Script run.sh prêt."

  # 4) Équivalent Windows (.bat)
  cat > "$CLIENT_DIR/run.bat" <<BAT
@echo off
:: =============================================================================
::  Médiathèque - Client lourd (lancement Windows)
::  Renseignez la variable DB_PASSWORD ci-dessous.
:: =============================================================================
cd /d "%~dp0"

set "DB_HOST=$DB_PUBLIC_HOST"
set "DB_PORT=3306"
set "DB_NAME=$DB_NAME"
set "DB_USER=$DB_USER"
if "%DB_PASSWORD%"=="" set /p DB_PASSWORD=Mot de passe BD :

set "MEDIATHEQUE_DB_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true&characterEncoding=utf8"
set "MEDIATHEQUE_DB_USER=%DB_USER%"
set "MEDIATHEQUE_DB_PASSWORD=%DB_PASSWORD%"

java -jar mediatheque-app.jar
BAT
  ok "Script run.bat prêt."

  # 5) README
  cat > "$CLIENT_DIR/README.txt" <<README
Médiathèque — Client lourd (distribution)
==========================================

Contenu :
  mediatheque-app.jar          application Java
  lib/mysql-connector-j.jar    connecteur JDBC MySQL/MariaDB
  run.sh                       script de lancement Linux/macOS
  run.bat                      script de lancement Windows

Pré-requis poste utilisateur :
  - Java JRE/JDK 17 ou supérieur installé

Lancement :
  Linux/macOS :   ./run.sh
  Windows     :   double-cliquez sur run.bat (ou lancez en cmd)

Vous pouvez surcharger les variables d'environnement avant lancement :
  DB_HOST     adresse du serveur BD (par défaut : $DB_PUBLIC_HOST)
  DB_PORT     port (par défaut : 3306)
  DB_NAME     nom de la base (par défaut : $DB_NAME)
  DB_USER     utilisateur (par défaut : $DB_USER)
  DB_PASSWORD mot de passe (demandé interactivement sinon)

Comptes de démonstration :
  admin / admin123      (administrateur)
  agent / agent123      (agent)

Réalisation : SEBAH Nassim — BTS SIO SLAM — Session 2026.
README

  # 6) Tarball
  ( cd "$DIST_DIR" && tar czf mediatheque-client.tar.gz mediatheque-client/ )
  ok "Archive : $DIST_DIR/mediatheque-client.tar.gz ($(du -h "$DIST_DIR/mediatheque-client.tar.gz" | cut -f1))"

  # 7) Zip aussi pour les utilisateurs Windows (si l'outil zip est dispo)
  if command -v zip >/dev/null 2>&1; then
    ( cd "$DIST_DIR" && rm -f mediatheque-client.zip && zip -rq mediatheque-client.zip mediatheque-client/ )
    ok "Archive ZIP : $DIST_DIR/mediatheque-client.zip"
  fi

  save_state
}

# ----- 5. Vérification ------------------------------------------------------
verify() {
  title "Vérification du déploiement"

  systemctl is-active --quiet mariadb \
    && ok "mariadb actif" \
    || err "mariadb inactif"

  # Test connexion BD avec les credentials configurés
  if mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3306 \
        -e "USE $DB_NAME; SELECT COUNT(*) FROM profil;" >/dev/null 2>&1; then
    local n
    n=$(mysql -u "$DB_USER" -p"$DB_PASSWORD" -h 127.0.0.1 \
         "$DB_NAME" -sN -e "SELECT COUNT(*) FROM profil;" 2>/dev/null)
    ok "Connexion BD locale : OK ($n profils)"
  else
    err "Connexion BD locale échouée."
  fi

  # Test connexion BD distante (depuis $DB_PUBLIC_HOST si renseigné)
  if [ "$DB_REMOTE_HOST" != "localhost" ] && [ "$DB_REMOTE_HOST" != "127.0.0.1" ]; then
    if command -v ss >/dev/null && $SUDO ss -ltn 2>/dev/null \
         | awk '{print $4}' | grep -qE "[*0]:3306$|0\.0\.0\.0:3306$"; then
      ok "Port 3306 ouvert sur le réseau (clients distants OK)."
    else
      warn "Port 3306 ne semble pas écouter en 0.0.0.0 — clients distants potentiellement bloqués."
    fi
  fi

  # Archive client
  if [ -f "$DIST_DIR/mediatheque-client.tar.gz" ]; then
    ok "Archive client : $DIST_DIR/mediatheque-client.tar.gz"
  else
    warn "Aucune archive client (lancez : $0 package)"
  fi
}

# ----- 6. Résumé ------------------------------------------------------------
summary() {
  cat <<EOF

${C_OK}═══════════════════════════════════════════════════════════════${C_RESET}
${C_OK}  Déploiement serveur lourd terminé${C_RESET}
${C_OK}═══════════════════════════════════════════════════════════════${C_RESET}

  Base / user      : $DB_NAME / $DB_USER
  Mot de passe BD  : $DB_PASSWORD
  Accès réseau     : ${DB_REMOTE_HOST}  (port 3306)
  Adresse serveur  : $DB_PUBLIC_HOST
  Archive client   : $DIST_DIR/mediatheque-client.tar.gz

  ${C_INFO}→ DISTRIBUER AUX POSTES UTILISATEURS :${C_RESET}
  scp $DIST_DIR/mediatheque-client.tar.gz user@poste:~/
  ssh user@poste 'tar xzf mediatheque-client.tar.gz && cd mediatheque-client && ./run.sh'

  Comptes de démonstration (à CHANGER en production) :
    admin / admin123       (administrateur)
    agent / agent123       (agent)

  Pare-feu : pensez à ouvrir 3306/tcp aux postes clients.
    sudo ufw allow from <ip-client> to any port 3306    # Debian/Ubuntu
    sudo firewall-cmd --add-port=3306/tcp --permanent   # RHEL/Fedora

  Mise à jour du JAR (sans toucher à la BD) :
    sudo $0 build && sudo $0 package

EOF
}

# ----- 7. Status ------------------------------------------------------------
status() {
  title "État du déploiement lourd"
  [ -r "$STATE_FILE" ] && ok "État conservé : $STATE_FILE" \
                       || warn "Aucun déploiement enregistré ($STATE_FILE absent)."
  systemctl is-active --quiet mariadb \
    && ok "mariadb actif" \
    || err "mariadb inactif"
  [ -f "$SOURCE_DIR/build/com/mediatheque/Main.class" ] \
    && ok "Projet compilé" \
    || warn "Projet non compilé"
  [ -f "$DIST_DIR/mediatheque-client.tar.gz" ] \
    && ok "Archive prête : $DIST_DIR/mediatheque-client.tar.gz" \
    || warn "Archive client non générée"
}

# =============================================================================
#                              DISPATCHER
# =============================================================================
aide() {
  cat <<EOF
Médiathèque lourd — script de déploiement serveur

Contexte : ce script s'utilise sur la machine qui va héberger la BASE.
Les clients Swing s'installent ensuite sur les postes utilisateurs depuis
l'archive générée (dist/mediatheque-client.tar.gz).

Usage : sudo $(basename "$0") [commande]

Commandes :
  all        Déploiement complet (défaut) : prereqs + database + build + package
  prereqs    Installe JDK + MariaDB
  database   Crée la base + l'utilisateur, charge schéma + données,
             active l'accès distant et le bind 0.0.0.0
  build      Compile le projet (équivalent à ./build.sh)
  package    Génère l'archive client (mediatheque-client.tar.gz)
  verify     Teste MariaDB, la connexion BD et la présence de l'archive
  status     Affiche l'état du déploiement
  help       Cette aide

Variables surchageables (à passer avant la commande) :
  DB_NAME           Nom de la base                  (def. mediatheque)
  DB_USER           Utilisateur BD                  (def. mediatheque)
  DB_PASSWORD       Mot de passe                    (généré sinon)
  DB_REMOTE_HOST    Hôte autorisé à se connecter    (def. %)
                    Utilisez une IP/CIDR pour restreindre, ex. 192.168.1.0/24
  DB_PUBLIC_HOST    Adresse du serveur côté client  (def. première IP locale)
  DIST_DIR          Dossier d'archive               (def. ./dist)

Exemples :
  # Le plus simple :
  sudo ./deploy.sh

  # En restreignant aux postes d'un LAN précis :
  sudo DB_REMOTE_HOST="192.168.1.%" DB_PUBLIC_HOST="192.168.1.10" \\
       ./deploy.sh

  # Re-paquet du client après modification du code :
  sudo ./deploy.sh build && sudo ./deploy.sh package
EOF
}

cmd="${1:-all}"

case "$cmd" in
  prereqs)  install_prereqs ;;
  database) setup_database; save_state ;;
  build)    build ;;
  package)  package ;;
  verify)   verify ;;
  status)   status ;;
  all)
    install_prereqs
    setup_database
    save_state
    build
    package
    verify
    summary
    ;;
  help|-h|--help)
    aide
    ;;
  *)
    err "Commande inconnue : $cmd"
    aide
    exit 1
    ;;
esac
