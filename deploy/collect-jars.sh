#!/usr/bin/env bash
# Copies every built fat-jar into deploy/jars/ with a clean <name>.jar filename,
# ready to upload to the server's /opt/exim/jars/. Run AFTER `mvn clean package`.
#   ./deploy/collect-jars.sh
set -euo pipefail
cd "$(dirname "$0")/.."
OUT="deploy/jars"
mkdir -p "$OUT"
rm -f "$OUT"/*.jar

for dir in services/*-service gateway; do
  name="$(basename "$dir")"
  jar="$(ls "$dir"/target/${name}-*.jar 2>/dev/null | head -1 || true)"
  if [ -n "$jar" ]; then
    cp "$jar" "$OUT/$name.jar"
    echo "  + $name.jar"
  else
    echo "  ! missing jar for $name (run: mvn clean package -DskipTests)" >&2
  fi
done
echo "Done -> $OUT ($(ls "$OUT"/*.jar | wc -l | tr -d ' ') jars)"
