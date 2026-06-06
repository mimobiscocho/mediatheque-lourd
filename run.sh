#!/usr/bin/env bash
# Lance l'application (après ./build.sh).
# Pour Windows, voir les instructions équivalentes dans le README.md.
set -e
cd "$(dirname "$0")"

if [ ! -d build ]; then
  echo "Le dossier build/ est absent. Lancement de la compilation..."
  ./build.sh
fi

java -cp "build:lib/mysql-connector-j.jar" com.mediatheque.Main
