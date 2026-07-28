#!/usr/bin/env bash
set -euo pipefail

ADB="${ANDROID_HOME}/platform-tools/adb"
APP_APK="app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
OUTPUT_DIR="app/build/outputs/deviceAcceptance/androidBackupTransport"
RUNNER="com.gymtracker.test/androidx.test.runner.AndroidJUnitRunner"
PACKAGE_NAME="com.gymtracker"
LOCAL_TRANSPORT="com.android.localtransport/.LocalTransport"

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

"${ADB}" shell bmgr enable true | tee "${OUTPUT_DIR}/enable.txt"
"${ADB}" shell bmgr transport "${LOCAL_TRANSPORT}" | tee "${OUTPUT_DIR}/transport.txt"
"${ADB}" shell bmgr wipe "${LOCAL_TRANSPORT}" "${PACKAGE_NAME}" \
  | tee "${OUTPUT_DIR}/wipe.txt"

run_phase \
  com.gymtracker.data.portability.AndroidBackupTransportSeedTest \
  "${OUTPUT_DIR}/seed.txt"

backup_output="$("${ADB}" shell bmgr backupnow --monitor "${PACKAGE_NAME}")"
printf '%s\n' "${backup_output}" | tee "${OUTPUT_DIR}/backup.txt"
grep -Fq "Package ${PACKAGE_NAME} with result: Success" "${OUTPUT_DIR}/backup.txt"

restore_sets="$("${ADB}" shell bmgr list sets | tr -d '\r')"
printf '%s\n' "${restore_sets}" | tee "${OUTPUT_DIR}/sets.txt"
restore_token="$(
  printf '%s\n' "${restore_sets}" \
    | awk -F ':' '/^[[:space:]]*[[:xdigit:]]+[[:space:]]*:/{gsub(/[[:space:]]/, "", $1); print $1; exit}'
)"
if [[ -z "${restore_token}" ]]; then
  echo "Local transport did not expose a restore-set token." >&2
  exit 1
fi

"${ADB}" shell pm clear "${PACKAGE_NAME}" >/dev/null
restore_output="$(
  "${ADB}" shell bmgr restore "${restore_token}" "${PACKAGE_NAME}" --monitor
)"
printf '%s\n' "${restore_output}" | tee "${OUTPUT_DIR}/restore.txt"

run_phase \
  com.gymtracker.data.portability.AndroidBackupTransportVerifyTest \
  "${OUTPUT_DIR}/verify.txt"
