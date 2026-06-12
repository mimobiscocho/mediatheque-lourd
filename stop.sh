#!/usr/bin/env bash
# =============================================================================
#  Arrête le client lourd (Java) démarré par start.sh.
#  Si aucun PID n'est trouvé, tente un fallback par nom de classe.
# =============================================================================
set -e
cd "$(dirname "$0")"

if [ ! -f .pid ]; then
  if pgrep -f "com.mediatheque.Main" >/dev/null 2>&1; then
    echo "Aucun .pid mais une JVM tourne — arrêt par nom..."
    pkill -f "com.mediatheque.Main" || true
    echo "Arrêté."
  else
    echo "Client lourd non démarré."
  fi
  exit 0
fi

PID=$(cat .pid)
if kill -0 "$PID" 2>/dev/null; then
  echo "Arrêt du client lourd (PID $PID)..."
  kill "$PID" 2>/dev/null || true
  for _ in 1 2 3 4 5; do
    kill -0 "$PID" 2>/dev/null || break
    sleep 1
  done
  if kill -0 "$PID" 2>/dev/null; then
    kill -9 "$PID" 2>/dev/null || true
  fi
  echo "Arrêté."
else
  echo "Processus déjà arrêté."
fi
rm -f .pid
