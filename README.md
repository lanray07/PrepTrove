# PrepTrove

Native Kotlin / Compose Android preparedness planner. Development preview, not ready for store submission.

## Build

JDK 17, Android SDK 35 and Gradle 8.13. Set `sdk.dir` in untracked `local.properties`, then:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
python tools/localize.py
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for development only.

## Implemented

Room persistence for kits, supplies, plan notes, manually entered contacts, grouped checklist tasks and reminders. Add, edit, duplicate, delete, search, assign kit, inspect supply, record completion, expiry review, optional onboarding and notifications. Adaptive navigation rail for tablets, scrollable phone tabs, persisted light/dark theme. Optional push-to-talk with transcript review, English typed commands and device TTS. SAF CSV, JSON, password-protected PTB and paginated PDF exports; validated copy-based JSON/PTB restore with explicit contact/plan inclusion. Local reference summary with source provenance. No account, advertising, analytics, location or contact-list permission.

## Limits

Read [release gates](docs/RELEASE.md) before distributing. This is a working foundation, not completion of the full master brief. Source brief supplied by the user stops partway through section 23; later requirements were not available.

Architecture uses one application module with UI, ViewModel, Room repository/DAO and a WorkManager task. Feature module separation, typed structured family plans, complete locale support and production entitlement handling are still required. No encryption or security guarantee is claimed.
