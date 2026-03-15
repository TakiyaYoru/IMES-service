#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

echo "[SMOKE] Base URL: ${BASE_URL}"

check_status() {
  local name="$1"
  local expected="$2"
  local path="$3"
  local actual
  actual=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}${path}" || true)

  if [[ "$actual" == "$expected" ]]; then
    echo "[PASS] ${name}: ${actual}"
  else
    echo "[FAIL] ${name}: expected ${expected}, got ${actual}"
    return 1
  fi
}

# Public/health checks
check_status "Auth health" "200" "/auth/health"
check_status "Assignment health" "200" "/assignments/health"
check_status "Attendance health" "200" "/attendances/health"

# Protected route should reject missing token
check_status "Evaluations without token" "401" "/evaluations"

echo "[DONE] Gateway smoke checks completed."
