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

Architecture uses one application module with UI, ViewModel, Room repository/DAO and a WorkManager task. Feature module separation, complete locale support and production entitlement handling are still required. No encryption or security guarantee is claimed.

## Localisation and release tooling

- UI resource variants available: en-GB, en-US, en-AU. They are source-English variants, not evidence of full store-language approval.
- 19 additional locale packs are pending provider translation and review, as requested when no provider was configured. All 22 targets have UI, voice and store review queues under localization/.
- Settings offers manual language selection; pending languages are disabled. Device-language fallback never translates private records. Speech recognition/TTS use the active app locale; command parsing remains the documented English subset.
- Run `python tools/extract_resources.py --check`, `python tools/localize.py`, `python tools/audit_localization.py`, and `python -m unittest discover -s tools -p 'test_*.py'`.
- Review workflow: fill the locale queue, identify the reviewer, review each string and safety flag, then use `python tools/localize.py --approve LOCALE`. Machine output is never treated as human-reviewed.
- `python tools/release_check.py` deliberately fails while production requirements remain open. `python tools/package_release.py --unsigned` builds a QA release and checksum manifest. Signing requires completed gates and local environment secrets; the tool never publishes.

See [translation review instructions](docs/LOCALISATION.md).

## Billing and expanded planner features

Amazon SDK integration is present with checkout OFF by default. See [configuration and test boundaries](docs/AMAZON-BILLING.md). The unconfigured build never simulates a purchase or invents a price.

Database version 2 adds structured household/contact fields, supply purchase dates, templates and recurring reminders. A tested migration preserves version 1 records. Completion history is local. Kit templates include copies of their supplies. Supply photos are bounded and stored privately; photo pixels are excluded from backups; completion history can be explicitly included in JSON/PTB backups. Optional app lock uses OS device credentials, and PDFs include the structured plan fields.


## Amazon review package

Run `python tools/submission_bundle.py` after the validation commands in [the submission handoff](docs/AMAZON-SUBMISSION.md). It produces an unsigned review ZIP, checksum and report in ignored `artifacts/`. It checks the actual APK identity, debug flag, permissions, ZIP alignment, unsigned status, checkout build setting and screenshot hashes; failing test/lint evidence blocks assembly. CI uploads this review package automatically. A generated package does not close legal, translation, signing, real-store or device acceptance gates.


Reminder notifications now have a persistent Settings opt-in and disable action; they default off until enabled, including after upgrading an earlier preview without a saved choice. Generic count-only notices clear when off or no longer due, and data changes request a best-effort refresh. See [notification behavior and acceptance boundaries](docs/NOTIFICATIONS.md). Document I/O is covered through a synthetic cross-process provider; real picker/cloud-provider and credential-lock acceptance remain open.
