#!/usr/bin/env bash
set -euo pipefail
collect_failure_evidence() { mkdir -p artifacts/device-acceptance; adb shell ls -la /sdcard/Android/data/com.preptrove.app/files/acceptance || true; adb pull /sdcard/Android/data/com.preptrove.app/files/acceptance artifacts/device-acceptance || true; }
trap collect_failure_evidence EXIT
adb shell wm size 1920x1200
adb shell wm density 240
./gradlew :app:assembleDebugAndroidTest :app:assembleRelease :app:testDebugUnitTest :app:lintDebug --no-parallel --console=plain
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.preptrove.app.EphemeralUiAcceptanceTest -Pandroid.testInstrumentationRunnerArguments.ephemeralCi=true --no-parallel --console=plain
mkdir -p artifacts/system-ui-results
cp -R app/build/outputs/androidTest-results/connected/debug/. artifacts/system-ui-results/
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=com.preptrove.app.EphemeralUiAcceptanceTest -Pandroid.testInstrumentationRunnerArguments.ephemeralCi=true --no-parallel --console=plain
python3 tools/capture_localization_qa.py
python3 tools/capture_store.py
adb pull /sdcard/Android/data/com.preptrove.app/files/acceptance artifacts/device-acceptance
python3 tools/submission_bundle.py
