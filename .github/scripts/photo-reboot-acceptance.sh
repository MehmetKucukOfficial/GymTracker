#!/usr/bin/env bash
set -euo pipefail

ADB="${ANDROID_HOME}/platform-tools/adb"
APP_APK="app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
OUTPUT_DIR="app/build/outputs/deviceAcceptance/photoReboot"
RUNNER="com.gymtracker.test/androidx.test.runner.AndroidJUnitRunner"

mkdir -p "${OUTPUT_DIR}"

"${ADB}" install -r -t "${APP_APK}"
"${ADB}" install -r -t "${TEST_APK}"
"${ADB}" shell pm clear com.gymtracker >/dev/null

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
  com.gymtracker.data.photos.PhotoRebootSeedTest \
  "${OUTPUT_DIR}/seed.txt"

"${ADB}" reboot
"${ADB}" wait-for-device

boot_completed=""
for _ in $(seq 1 180); do
  boot_completed="$("${ADB}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [[ "${boot_completed}" == "1" ]]; then
    break
  fi
  sleep 1
done

if [[ "${boot_completed}" != "1" ]]; then
  echo "Android emulator did not finish rebooting within 180 seconds." >&2
  exit 1
fi

"${ADB}" shell cmd package wait-for-handler --timeout 60000 >/dev/null 2>&1 || true

run_phase \
  com.gymtracker.data.photos.PhotoRebootVerifyTest \
  "${OUTPUT_DIR}/verify.txt"
