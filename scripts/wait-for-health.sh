#!/usr/bin/env bash
# Waits until <base-url>/actuator/health is UP and, optionally, until
# <base-url>/api/version reports the expected environment.
# Usage: wait-for-health.sh <base-url> [expected-environment] [timeout-seconds]
set -euo pipefail

BASE_URL="${1:?base url required}"
EXPECTED_ENV="${2:-}"
TIMEOUT="${3:-120}"

deadline=$(( $(date +%s) + TIMEOUT ))
while true; do
    health="$(curl -fsS "$BASE_URL/actuator/health" 2>/dev/null || true)"
    version="$(curl -fsS "$BASE_URL/api/version" 2>/dev/null || true)"
    if [[ "$health" == *'"status":"UP"'* ]]; then
        if [[ -z "$EXPECTED_ENV" || "$version" == *"\"environment\":\"$EXPECTED_ENV\""* ]]; then
            echo "HEALTHY  $BASE_URL"
            echo "health:  $health"
            echo "version: $version"
            exit 0
        fi
    fi
    if (( $(date +%s) > deadline )); then
        echo "UNHEALTHY after ${TIMEOUT}s: $BASE_URL" >&2
        echo "last health:  ${health:-<no response>}" >&2
        echo "last version: ${version:-<no response>}" >&2
        exit 1
    fi
    sleep 3
done
