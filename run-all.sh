#!/usr/bin/env bash
# Build all modules and run every service + the gateway in the background.
# Usage:  ./run-all.sh         (logs go to ./logs/, stop with ./stop-all.sh)
# Requires set-env.sh (MONGODB_URI + JWT_SECRET). Needs JDK 17+.
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f set-env.sh ]; then
  echo "ERROR: set-env.sh not found (it holds MONGODB_URI + JWT_SECRET)." >&2
  exit 1
fi
# shellcheck disable=SC1091
source ./set-env.sh

echo "==> Building (mvn clean package)…"
mvn -q clean package -DskipTests

mkdir -p logs
# name:port — gateway must hold :8080
SERVICES=(
  "identity-service:8081"
  "verification-service:8082"
  "catalog-service:8083"
  "sourcing-service:8084"
  "quotation-service:8085"
  "messaging-service:8086"
  "orders-service:8087"
  "documents-service:8088"
)

start() {  # start <jar> <name> <port>
  local jar="$1" name="$2" port="$3"
  java -jar "$jar" > "logs/$name.log" 2>&1 &
  echo $! > "logs/$name.pid"
  printf '  %-22s :%s  (pid %s)\n' "$name" "$port" "$(cat "logs/$name.pid")"
}

echo "==> Starting services…"
for s in "${SERVICES[@]}"; do
  name="${s%%:*}"; port="${s##*:}"
  start "services/$name/target/$name-0.0.1-SNAPSHOT.jar" "$name" "$port"
done
start "gateway/target/gateway-0.0.1-SNAPSHOT.jar" "gateway" "8080"

echo
echo "All started. Tail logs: tail -f logs/<name>.log"
echo "Gateway:  http://localhost:8080      Swagger e.g. http://localhost:8081/swagger-ui.html"
echo "Stop all: ./stop-all.sh"
