#!/usr/bin/env bash
# Compile l'application sans Maven (utilise le connecteur JDBC dans lib/).
set -e
cd "$(dirname "$0")"

CONNECTOR="lib/mysql-connector-j.jar"
if [ ! -f "$CONNECTOR" ]; then
  echo "==> Téléchargement du connecteur JDBC MySQL..."
  mkdir -p lib
  curl -sSL -o "$CONNECTOR" \
    "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
fi

echo "==> Compilation des sources Java..."
rm -rf build
mkdir -p build
find src/main/java -name '*.java' > sources.txt
javac -encoding UTF-8 -d build -cp "lib/mysql-connector-j.jar" @sources.txt
rm -f sources.txt

echo "==> Copie des ressources..."
cp -r src/main/resources/* build/ 2>/dev/null || true

echo "Compilation terminée. Lancez ./run.sh pour démarrer l'application."
