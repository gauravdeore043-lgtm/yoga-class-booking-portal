#!/usr/bin/env bash
# Tasks 13–14 – provision a CLEAN target node with Ansible and prove reliability:
#   first run -> upgrade -> idempotent re-run -> health check -> rollback -> crash recovery -> roll forward.
# Every step is logged to <log-dir>/NN-*.log.
#
# Usage: scripts/provision-node.sh [log-dir]
# Requires: docker, ansible (with community.docker), git, mvn; run from the repo root.
set -uo pipefail

LOG_DIR="${1:-$PWD/ansible-logs}"
mkdir -p "$LOG_DIR" && LOG_DIR="$(cd "$LOG_DIR" && pwd)"
REPO="$(pwd)"
NODE=yoga-node
HOST_PORT=8095
ART="$REPO/target/releases"
mkdir -p "$ART"

log() { printf '\n### %s\n' "$*"; }
run_logged() {  # run_logged <logfile> <cmd...>
    local file="$LOG_DIR/$1"; shift
    { echo "\$ $*"; echo; } > "$file"
    "$@" 2>&1 | tee -a "$file"
    return "${PIPESTATUS[0]}"
}
host_check() {
    echo "\$ curl http://localhost:$HOST_PORT/actuator/health"; curl -sS "http://localhost:$HOST_PORT/actuator/health"; echo
    echo "\$ curl http://localhost:$HOST_PORT/api/version";     curl -sS "http://localhost:$HOST_PORT/api/version"; echo
}

log "Build release artefacts (v1.0.0 from its git tag, v1.1.0 from the working tree)"
if [ ! -f "$ART/yoga-portal-1.0.0.war" ]; then
    rm -rf /tmp/yoga-v1.0.0 && git worktree prune
    git worktree add --detach /tmp/yoga-v1.0.0 v1.0.0 >/dev/null
    (cd /tmp/yoga-v1.0.0 && mvn -B -q -ntp -DskipTests package) || exit 1
    cp /tmp/yoga-v1.0.0/target/yoga-portal.war "$ART/yoga-portal-1.0.0.war"
    git worktree remove --force /tmp/yoga-v1.0.0
fi
if [ ! -f "$ART/yoga-portal-1.1.0.war" ]; then
    mvn -B -q -ntp -DskipTests package || exit 1
    cp target/yoga-portal.war "$ART/yoga-portal-1.1.0.war"
fi
ls -l "$ART"

log "Create a clean target node (Ubuntu 24.04, nothing installed)"
docker rm -f "$NODE" >/dev/null 2>&1 || true
{
  # --init gives the node a real PID 1 that reaps processes (like systemd would on a VM)
  echo "\$ docker run -d --init --name $NODE --hostname $NODE -p $HOST_PORT:8080 ubuntu:24.04 sleep infinity"
  docker run -d --init --name "$NODE" --hostname "$NODE" -p "$HOST_PORT:8080" ubuntu:24.04 sleep infinity
  echo; echo "\$ docker exec $NODE sh -c 'cat /etc/os-release | head -2; which java python3 || echo \"java/python3: not installed\"'"
  docker exec "$NODE" sh -c 'cat /etc/os-release | head -2; which java python3 || echo "java/python3: not installed"'
} 2>&1 | tee "$LOG_DIR/00-clean-node.log"

cd "$REPO/ansible" || exit 1
{ ansible --version; echo; ansible-inventory --graph; } 2>&1 | tee "$LOG_DIR/00-ansible-version.log"

log "1. First execution: provision + deploy 1.0.0"
run_logged 01-first-run.log ansible-playbook site.yml -e release_version=1.0.0 -e war_source="$ART/yoga-portal-1.0.0.war" || exit 1

log "2. Deploy the new release 1.1.0"
run_logged 02-upgrade-1.1.0.log ansible-playbook site.yml -e release_version=1.1.0 -e war_source="$ART/yoga-portal-1.1.0.war" || exit 1

log "3. Idempotency: re-run with identical inputs (expect changed=0)"
run_logged 03-idempotency.log ansible-playbook site.yml -e release_version=1.1.0 -e war_source="$ART/yoga-portal-1.1.0.war" || exit 1
if grep -Eq 'changed=0 ' "$LOG_DIR/03-idempotency.log"; then
    echo "IDEMPOTENT: second run reported changed=0" | tee -a "$LOG_DIR/03-idempotency.log"
else
    echo "WARNING: second run reported changes" | tee -a "$LOG_DIR/03-idempotency.log"
fi

log "4. Health check from the host"
host_check 2>&1 | tee "$LOG_DIR/04-health-check.log"
ansible yoga_nodes -m ansible.builtin.command -a "/opt/yoga/bin/yoga-ctl status" 2>&1 | tee -a "$LOG_DIR/04-health-check.log"

log "5. Rollback to the previous stable release 1.0.0"
run_logged 05-rollback-to-1.0.0.log ansible-playbook rollback.yml -e rollback_version=1.0.0 || exit 1
host_check 2>&1 | tee -a "$LOG_DIR/05-rollback-to-1.0.0.log"

log "6. Recovery: simulate a crash, then let Ansible restore the service"
{
  echo "\$ docker exec $NODE pkill -9 -f yoga-portal.war   # simulated crash"
  docker exec "$NODE" pkill -9 -f yoga-portal.war; sleep 2
  echo "\$ curl http://localhost:$HOST_PORT/actuator/health   # expected to fail"
  curl -sS -m 5 "http://localhost:$HOST_PORT/actuator/health" || echo "=> service DOWN (as expected)"
  echo
  echo "\$ ansible-playbook site.yml -e release_version=1.0.0 ...   # desired state re-applied"
  ansible-playbook site.yml -e release_version=1.0.0 -e war_source="$ART/yoga-portal-1.0.0.war"
  host_check
} 2>&1 | tee "$LOG_DIR/06-recovery.log"

log "7. Roll forward to 1.1.0 (final state)"
run_logged 07-roll-forward-1.1.0.log ansible-playbook rollback.yml -e rollback_version=1.1.0 || exit 1
host_check 2>&1 | tee -a "$LOG_DIR/07-roll-forward-1.1.0.log"
docker exec "$NODE" cat /opt/yoga/logs/release-history.log 2>/dev/null | tee -a "$LOG_DIR/07-roll-forward-1.1.0.log"

echo
echo "Provisioning evidence written to $LOG_DIR"
