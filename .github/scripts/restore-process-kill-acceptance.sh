#!/usr/bin/env bash
set -euo pipefail

ADB="${ANDROID_HOME}/platform-tools/adb"
APP_APK="app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
OUTPUT_DIR="app/build/outputs/deviceAcceptance/restoreProcessKill"
RUNNER="com.gymtracker.test/androidx.test.runner.AndroidJUnitRunner"
MARKER="files/restore-process-kill-ready"
PACKAGE_NAME="com.gymtracker"

mkdir -p "${OUTPUT_DIR}"

"${ADB}" install -r -t "${APP_APK}"
"${ADB}" install -r -t "${TEST_APK}"
"${ADB}" shell pm clear "${PACKAGE_NAME}" >/dev/null

run_phase() {
  local class_name="$1"
  local output_file="$2"
  local output
  output="$(
    "${ADB}" shell am instrument -w -r \
      -e notAnnotation com.gymtracker.data.photos.NoExcludedAnnotation \
      -e class "${class_name}" \
      "${RUNNER}"
  )"
  printf '%s\n' "${output}" | tee "${output_file}"
  grep -Eq 'OK \(1 test\)' "${output_file}"
}

run_phase \
  com.gymtracker.data.portability.RestoreProcessKillSeedTest \
  "${OUTPUT_DIR}/seed.txt"

set +e
"${ADB}" shell am instrument -w -r \
  -e notAnnotation com.gymtracker.data.photos.NoExcludedAnnotation \
  -e class com.gymtracker.data.portability.RestoreProcessKillTriggerTest \
  "${RUNNER}" >"${OUTPUT_DIR}/trigger.txt" 2>&1 &
instrument_pid=$!
set -e

marker_ready=""
for _ in $(seq 1 120); do
  if "${ADB}" shell run-as "${PACKAGE_NAME}" test -f "${MARKER}" 2>/dev/null; then
    marker_ready="ready"
    break
  fi
  sleep 1
done

if [[ "${marker_ready}" != "ready" ]]; then
  "${ADB}" shell am force-stop "${PACKAGE_NAME}" || true
  wait "${instrument_pid}" || true
  cat "${OUTPUT_DIR}/trigger.txt"
  echo "Restore did not reach the pre-commit checkpoint within 120 seconds." >&2
  exit 1
fi

old_pid="$("${ADB}" shell pidof "${PACKAGE_NAME}" | tr -d '\r')"
if [[ -z "${old_pid}" ]]; then
  echo "Target process was absent at the restore checkpoint." >&2
  exit 1
fi

"${ADB}" shell am force-stop "${PACKAGE_NAME}"
wait "${instrument_pid}" || true

for _ in $(seq 1 50); do
  if [[ -z "$("${ADB}" shell pidof "${PACKAGE_NAME}" | tr -d '\r')" ]]; then
    break
  fi
  sleep 0.2
done

if [[ -n "$("${ADB}" shell pidof "${PACKAGE_NAME}" | tr -d '\r')" ]]; then
  echo "Target process ${old_pid} survived force-stop." >&2
  exit 1
fi

printf 'Killed target PID %s after the restore pre-commit checkpoint.\n' "${old_pid}" \
  | tee -a "${OUTPUT_DIR}/trigger.txt"

run_phase \
  com.gymtracker.data.portability.RestoreProcessKillVerifyTest \
  "${OUTPUT_DIR}/verify.txt"
