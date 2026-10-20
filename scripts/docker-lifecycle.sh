#!/usr/bin/env bash
# Task 11 – walks through the full Docker container lifecycle for the Yoga portal
# and records every command with its output.
# Usage: scripts/docker-lifecycle.sh [log-file]
# Needs target/yoga-portal.war (run `mvn -DskipTests package` first).
set -uo pipefail

LOG="${1:-docker-lifecycle.log}"
IMAGE="yoga-portal"
VERSION="$(sed -n 's:.*<version>\([0-9][0-9.]*\)</version>.*:\1:p' pom.xml | sed -n 2p)"
VERSION="${VERSION:-1.0.0}"
NAME="yoga-lifecycle"
PORT=8086

: > "$LOG"
run() {
    echo "" | tee -a "$LOG"
    echo "\$ $*" | tee -a "$LOG"
    "$@" 2>&1 | tee -a "$LOG"
    return "${PIPESTATUS[0]}"
}
section() { printf '\n==================== %s ====================\n' "$1" | tee -a "$LOG"; }
wait_healthy() {
    for _ in $(seq 1 40); do
        if curl -fsS "http://localhost:$PORT/actuator/health" >/dev/null 2>&1; then return 0; fi
        sleep 3
    done
    return 1
}

section "0. Environment"
run docker version --format 'Client {{.Client.Version}} / Server {{.Server.Version}}'

section "1. Build the image"
[ -f target/yoga-portal.war ] || { echo "target/yoga-portal.war missing" | tee -a "$LOG"; exit 1; }
run docker build --build-arg APP_VERSION="$VERSION" -t "$IMAGE:$VERSION" .

section "2. Tag the image"
run docker tag "$IMAGE:$VERSION" "$IMAGE:latest"
run docker images "$IMAGE"

section "3. Inspect image details"
run docker image inspect "$IMAGE:$VERSION" --format 'Id={{.Id}}
Created={{.Created}}
Size={{.Size}} bytes
Architecture={{.Architecture}}
ExposedPorts={{json .Config.ExposedPorts}}
User={{.Config.User}}
Labels={{json .Config.Labels}}'
run docker history "$IMAGE:$VERSION" --format '{{.CreatedBy}} | {{.Size}}'

section "4. Run a container with a port mapping (host $PORT -> container 8080)"
docker rm -f "$NAME" >/dev/null 2>&1 || true
run docker run -d --name "$NAME" -p "$PORT:8080" -e APP_ENVIRONMENT=docker-lifecycle "$IMAGE:$VERSION"
run docker ps --filter "name=$NAME" --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
if wait_healthy; then
    run curl -fsS "http://localhost:$PORT/api/version"
    run curl -fsS "http://localhost:$PORT/actuator/health"
else
    echo "Container did not become healthy" | tee -a "$LOG"
fi

section "5. Inspect logs"
run docker logs --tail 25 "$NAME"

section "6. Inspect the running container"
run docker inspect "$NAME" --format 'State={{.State.Status}} StartedAt={{.State.StartedAt}} Health={{if .State.Health}}{{.State.Health.Status}}{{end}}
IP={{.NetworkSettings.IPAddress}} Ports={{json .NetworkSettings.Ports}}
Env={{json .Config.Env}}'
run docker stats --no-stream "$NAME"

section "7. Stop the container"
run docker stop "$NAME"
run docker ps -a --filter "name=$NAME" --format 'table {{.Names}}\t{{.Status}}'

section "8. Start it again"
run docker start "$NAME"
wait_healthy && run curl -fsS "http://localhost:$PORT/actuator/health"

section "9. Restart"
run docker restart "$NAME"
wait_healthy && run curl -fsS "http://localhost:$PORT/api/version"
run docker ps --filter "name=$NAME" --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

section "10. Remove the container"
run docker stop "$NAME"
run docker rm "$NAME"
run docker ps -a --filter "name=$NAME" --format 'table {{.Names}}\t{{.Status}}'
echo "(empty list above = container removed; image $IMAGE:$VERSION is kept)" | tee -a "$LOG"

section "11. Run the final container (left running for evidence)"
docker rm -f yoga-portal-local >/dev/null 2>&1 || true
run docker run -d --name yoga-portal-local --restart unless-stopped -p 8086:8080 \
    -e APP_ENVIRONMENT=docker-local "$IMAGE:$VERSION"
wait_healthy && run curl -fsS "http://localhost:$PORT/api/version"
run docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'

echo "" | tee -a "$LOG"
echo "Lifecycle complete. Log: $LOG" | tee -a "$LOG"
