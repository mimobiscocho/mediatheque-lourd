#!/usr/bin/env bash
# Compile l'application sans Maven (utilise le connecteur JDBC dans lib/).
# Pour Windows, voir les instructions équivalentes dans le README.md.
set -e
cd "$(dirname "$0")"

CONNECTOR="lib/mysql-connector-j.jar"
EXPECTED_SHA="d77962877d010777cff997015da90ee689f0f4bb76848340e1488f2b83332af5"

if [ ! -f "$CONNECTOR" ]; then
  echo "==> Téléchargement du connecteur JDBC MySQL..."
  mkdir -p lib
  curl -sSL -o "$CONNECTOR" \
    "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
fi

# Vérification d'intégrité du connecteur téléchargé (anti-supply chain).
if command -v sha256sum >/dev/null 2>&1; then
  ACTUAL_SHA=$(sha256sum "$CONNECTOR" | awk '{print $1}')
  if [ "$ACTUAL_SHA" != "$EXPECTED_SHA" ]; then
    echo "ERREUR : empreinte SHA-256 du connecteur JDBC inattendue." >&2
    echo "  attendue : $EXPECTED_SHA" >&2
    echo "  obtenue  : $ACTUAL_SHA"   >&2
    exit 1
  fi
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
