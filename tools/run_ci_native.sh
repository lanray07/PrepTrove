#!/usr/bin/env bash
set -euo pipefail
collect_failure_evidence() {
  mkdir -p artifacts
  adb pull /data/local/tmp/preptrove-ci-evidence artifacts/ || true
  adb pull /sdcard/Android/data/com.preptrove.app/files/acceptance artifacts/device-acceptance || true
}
trap collect_failure_evidence EXIT
# A phone-sized display exercises orientation requests on API 36; Android 16
# intentionally ignores them on large screens. Tablet captures follow separately.
adb shell wm size 1080x1920
adb shell wm density 420
./gradlew :app:assembleDebugAndroidTest :app:assembleRelease :app:testDebugUnitTest :app:lintDebug --no-parallel --console=plain
acceptance_failed=0
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.preptrove.app.EphemeralUiAcceptanceTest -Pandroid.testInstrumentationRunnerArguments.ephemeralCi=true --no-parallel --console=plain || acceptance_failed=1
mkdir -p artifacts/system-ui-results
cp -R app/build/outputs/androidTest-results/connected/debug/. artifacts/system-ui-results/
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=com.preptrove.app.EphemeralUiAcceptanceTest -Pandroid.testInstrumentationRunnerArguments.ephemeralCi=true --no-parallel --console=plain || acceptance_failed=1
adb shell wm size 1920x1200
adb shell wm density 240
python3 tools/capture_localization_qa.py || acceptance_failed=1
python3 tools/capture_store.py || acceptance_failed=1
collect_failure_evidence
# Keep independent capture evidence on a failed test run, but never package it as
# accepted or turn a test failure into a successful workflow.
if [ "$acceptance_failed" -ne 0 ]; then exit 1; fi
python3 tools/submission_bundle.py
