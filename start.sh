#!/usr/bin/env bash
# =============================================================================
#  Démarre le client lourd (Java Swing) en arrière-plan.
#  - Charge la config depuis ../.env si présent
#  - Compile via build.sh si build/ est absent
#  - Stocke le PID dans .pid, les logs dans logs/app.log
# =============================================================================
set -e
cd "$(dirname "$0")"

# --- 1. Chargement de la configuration -------------------------------------
PROJET_ROOT="$(cd .. && pwd)"
if [ -f "$PROJET_ROOT/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "$PROJET_ROOT/.env"
  set +a
fi
: "${DB_HOST:=localhost}"
: "${DB_PORT:=3306}"
: "${DB_NAME:=mediatheque}"
: "${DB_USER:=mediatheque}"
: "${DB_PASSWORD:=mediatheque}"

# --- 2. Vérification des prérequis -----------------------------------------
if ! command -v java >/dev/null 2>&1; then
  echo "Erreur : Java n'est pas installé." >&2
  exit 1
fi

# Le client lourd nécessite un serveur graphique. Sur Linux on vérifie
# au moins une des deux variables d'affichage.
if [ -z "${DISPLAY:-}" ] && [ -z "${WAYLAND_DISPLAY:-}" ]; then
  echo "Erreur : aucun serveur graphique détecté (DISPLAY/WAYLAND_DISPLAY vides)." >&2
  echo "Lancez l'application depuis une session graphique." >&2
  exit 1
fi

# --- 3. Compilation si nécessaire ------------------------------------------
if [ ! -f build/com/mediatheque/Main.class ]; then
  echo "Compilation initiale du projet..."
  bash build.sh
fi

# Lien symbolique attendu par build.sh / run.sh sur le connecteur JDBC
if [ ! -f lib/mysql-connector-j.jar ] && [ -f lib/mysql-connector-j-8.4.0.jar ]; then
  ln -s mysql-connector-j-8.4.0.jar lib/mysql-connector-j.jar
fi

# --- 4. Si déjà lancé, on n'en relance pas un deuxième ---------------------
mkdir -p logs
if [ -f .pid ] && kill -0 "$(cat .pid 2>/dev/null)" 2>/dev/null; then
  echo "Client lourd déjà démarré (PID $(cat .pid))."
  exit 0
fi
rm -f .pid

# --- 5. Démarrage en arrière-plan ------------------------------------------
export MEDIATHEQUE_DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true&characterEncoding=utf8"
export MEDIATHEQUE_DB_USER="${DB_USER}"
export MEDIATHEQUE_DB_PASSWORD="${DB_PASSWORD}"

echo "Démarrage du client lourd..."
nohup java -cp "build:lib/mysql-connector-j.jar" com.mediatheque.Main \
  > logs/app.log 2>&1 &
echo $! > .pid

# --- 6. Vérification du démarrage ------------------------------------------
sleep 2
if kill -0 "$(cat .pid)" 2>/dev/null; then
  echo "OK : PID $(cat .pid) — logs dans logs/app.log"
  echo "La fenêtre Swing s'est ouverte sur votre écran."
else
  echo "Échec du démarrage. Dernières lignes du log :" >&2
  tail -20 logs/app.log >&2
  rm -f .pid
  exit 1
fi
