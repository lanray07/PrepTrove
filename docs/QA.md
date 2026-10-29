# Verification report — 29 October 2026

- Debug APK: built successfully with Gradle 8.13 / JDK 17 / SDK 35.
- Final build and lint command: `gradlew.bat :app:assembleDebug :app:lintDebug` — passed.
- Lint: {'Error': 0, 'Warning': 17, 'Fatal': 0}. Remaining warnings are listed in app/build/reports/lint-results-debug.html; a clean lint command does not establish release readiness.
- JUnit: 2 passed, 0 failed. Expiry-window boundaries and CSV formula-prefix sanitisation.
- Fire tablet instrumentation: 2 passed, 0 failed on KFTRWI, Android API 28. Kit creation through the UI survives Activity recreation; deleting a kit preserves and unassigns its supplies.
- Initial UI test selector ambiguity was corrected by matching the clickable navigation control. The successful rerun is in device-verification-log.txt.
- Tests preceded the final manifest-only backup-exclusion change. The updated manifest passed resource processing, build and lint; final APK installed successfully.
- Device launch and screenshot review: onboarding and dashboard displayed on the connected Fire tablet in landscape. Raw captures: artifacts/screenshots/. These are not store-approved marketing assets.
- Localisation audit: 52 resource keys valid; 11 inline Text calls remain, plus other non-resource messages. 22 target locale entries are tracked; no complete translation coverage is claimed.
- CI workflow authored but not run on GitHub in this session.

## Not yet verified

PDF/CSV/JSON save flows end to end, real microphone recognition and TTS across engines, long-running notification delivery, portrait/phone/RTL layouts, large fonts/TalkBack, country content outside GB, payment/restore, encryption, release signing and Amazon submission. Those are release gates, not implied successes.

APK SHA-256: `59006f688af27cb84850cde92efc1267fa613ffce1100aab59731712cafdd470`
