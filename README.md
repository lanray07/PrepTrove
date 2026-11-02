# PrepTrove

Native Kotlin / Compose Android preparedness planner. Development preview, not ready for store submission.

## Build

JDK 17, Android SDK 36 and Gradle 8.13. Set `sdk.dir` in untracked `local.properties`, then:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
python tools/localize.py
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for development only.

## Implemented

Room persistence for kits, supplies, plan notes, manually entered contacts, grouped checklist tasks and reminders. Add, edit, duplicate, delete, search, assign kit, inspect supply, record completion, expiry review, optional onboarding and notifications. Adaptive navigation rail for tablets, scrollable phone tabs, persisted light/dark theme. Optional push-to-talk with transcript review, locale-specific typed commands and device TTS. SAF CSV, JSON, password-protected PTB and paginated PDF exports; validated copy-based JSON/PTB restore with explicit contact/plan inclusion. Official-source directory with country choice and external links. No account, advertising, analytics, location or contact-list permission.

## Limits

Read [release gates](docs/RELEASE.md) before distributing. This is a working foundation, not completion of the full master brief. Source brief supplied by the user stops partway through section 23; later requirements were not available.

Architecture uses one application module with UI, ViewModel, Room repository/DAO and a WorkManager task. Feature module separation, complete locale support and production entitlement handling are still required. No encryption or security guarantee is claimed.

## Localisation and release tooling

- UI resources now cover all 22 target locales under the owner-approved neutral planner scope. Private records are never automatically translated.
- Official-source links have a manually chosen country independent of language; external advice is not downloaded, translated or cached. Future critical guidance requires qualified review before activation.
- 22 locale queues, 308 store fields and 235 typed-command templates have Codex AI linguistic review; it is not professional safety approval or human speech testing.
- Run `python tools/localize.py`, `python tools/generate_voice.py`, `python tools/extract_resources.py --check`, and `python -m unittest discover -s tools -p 'test_*.py'`.
- The hosted `native-qa` workflow tests API 36, real system document/credential UI in a fresh disposable emulator, 22-language QA and real localized screenshots. It does not substitute for Fire/VoiceView/human speech evidence.
- `python tools/release_check.py` remains nonzero while release acceptance is open. `python tools/ci_bundle.py` records a missing-assets status instead of producing a misleading package; strict `submission_bundle.py` requires current source-bound raw and marketing assets for every active locale.

See [translation review instructions](docs/LOCALISATION.md).

## Billing and expanded planner features

Amazon SDK integration is present with checkout OFF by default. See [configuration and test boundaries](docs/AMAZON-BILLING.md). The unconfigured build never simulates a purchase or invents a price.

Database version 2 adds structured household/contact fields, supply purchase dates, templates and recurring reminders. A tested migration preserves version 1 records. Completion history is local. Kit templates include copies of their supplies. Supply photos are bounded and stored privately; photo pixels are excluded from backups; completion history can be explicitly included in JSON/PTB backups. Optional app lock uses OS device credentials, and PDFs include the structured plan fields.


## Amazon review package

Run `python tools/submission_bundle.py` after the validation commands in [the submission handoff](docs/AMAZON-SUBMISSION.md). It produces an unsigned review ZIP, checksum and report in ignored `artifacts/`. It checks the actual APK identity, debug flag, permissions, ZIP alignment, unsigned status, checkout build setting and screenshot hashes; failing test/lint evidence blocks assembly. CI uploads this review package automatically. A generated package does not close legal, regional safety, worldwide assets, real-store or device acceptance gates.


Reminder notifications now have a persistent Settings opt-in and disable action; they default off until enabled, including after upgrading an earlier preview without a saved choice. Generic count-only notices clear when off or no longer due, and data changes request a best-effort refresh. See [notification behavior and acceptance boundaries](docs/NOTIFICATIONS.md). Document I/O is covered through a synthetic cross-process provider; real picker/cloud-provider and credential-lock acceptance remain open.

See [worldwide validation evidence and limits](docs/WORLDWIDE-VALIDATION.md). The isolated localisation QA package cannot be submitted: its marker is rejected by the submission packager.
