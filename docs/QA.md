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



## Backup milestone — 29 October 2026



- Local build and 4 JUnit tests passed (expiry, CSV, encryption round trip/randomisation, wrong password/tamper/invalid-container rejection).

- 4 Fire tablet instrumentation tests passed, adding backup copy/link preservation and malformed-input validation to the previous storage/UI tests.

- Android lint passed. The file-picker restore/export journey and cryptographic provider interoperability across Fire versions still require verification.

- CI's first run failed because setup-android requested the removed `tools` SDK package. The workflow now requests explicit supported SDK components; the rerun status is tracked on GitHub.

- Localisation audit now reports 23 inline Text calls. Added backup copy remains pending full translation.



## Localisation and release-tooling milestone — 29 October 2026



- Debug and unsigned release builds passed; signed release and publication were not performed.

- Current Android unit results: {'tests': 6, 'failures': 0, 'errors': 0}. Fire tablet instrumentation: {'tests': 7, 'failures': 0, 'errors': 0}. Three Python localisation-tool tests also passed.

- Lint: {'Error': 0, 'Warning': 45, 'Fatal': 0}. Dependency/toolchain warnings remain review items.

- 179 source string keys, two native plural groups, 22 UI review queues, 22 voice queues and 22 metadata queues. Three source English variants generate usable resources; the other 19 packs remain pending because no translation provider was configured, as the owner requested.

- Language tests cover persistent selection, UK/US resource differences, pending-language rejection and notification singular/plural output. Locale-aware number parser tests cover regional decimal separators and invalid input. Full RTL, text expansion, TalkBack, microphone engines and every launch market are not verified.

- Resource catalogue drift and placeholder checks passed. The single remaining inline Text call formats a supply quantity together with private user fields; those private fields are intentionally untranslated.

- Production preflight intentionally exits nonzero and records remaining gates in release/readiness.json. Unsigned packaging tool ran successfully and wrote an SHA-256 manifest beside the unsigned APK. Signing credentials remain external; no keys were created or uploaded.

- Latest debug APK SHA-256: `61cd4d1b9eab5730fbc67682cf3b0cb2cdc1c69a515c681e3392f647de8d8178`



## Amazon billing and planner milestone — 29 October 2026



- Debug and unsigned release builds passed. Final Android unit results: {'tests': 8, 'failures': 0, 'errors': 0}. Fire tablet instrumentation: {'tests': 16, 'failures': 0, 'errors': 0}. Python tooling: 3 tests passed.

- Lint: {'Error': 0, 'Fatal': 0, 'Warning': 54}. The OS credential confirmation API is deprecated on newer Android but retained for Fire-compatible device-credential support; real credential flows remain unverified.

- New automated coverage: receipt account/SKU/type validation and cancellation precedence; disabled billing configuration; Room v1-to-v2 migration; independent kit-template supplies; recurring completion/history; structured metadata backup round trip; bounded private photo handling; multipage PDF rendering; structured-plan UI save; and lock-screen content isolation.

- No real purchase or App Tester transaction occurred. The owner has not configured the SKU/public key and requested that checkout remain disabled. Checkout additionally defaults OFF behind amazonPurchasesEnabled. Server-side RVS verification and production commercial limits remain release gates.

- App-lock tests verify content isolation only. Actual credential entry/cancel, background/file-picker interactions, OS engine speech and notification timing require manual device validation. Image tests use generated fixtures; a broad photo/EXIF/orientation corpus and real picker flows remain pending. PDF pagination is tested; global typography requires visual review.

- Photos and completion history are not included in JSON/PTB backups. This limitation is disclosed in export confirmation and docs/BACKUP.md.

- Latest debug APK SHA-256: `a505e66830d52666aaf0e2e4de295284b94d6588bd19efc50723f34e8a3bc394`



## Submission preparation — 30 October 2026

- Debug and unsigned release builds and Android lint passed. Current XML results: 11 JVM unit tests passed; 19 Fire tablet instrumentation tests passed and one screenshot-only test skipped in the regular suite; no failures/errors. Three Python tooling tests passed.
- Lint reports 0 errors/fatals, 60 warnings and one hint. These warnings are not claimed resolved.
- The isolated screenshot instrumentation passed separately, producing eight actual 1920x1200 tablet panels for each of en-US, en-GB and en-AU. A visual pass identified an obscuring keyboard on the voice panel; the capture test now dismisses it before capture. See the screenshot manifest for final capture hashes and review status.
- New tests cover reminder draft parsing, exact-name inspection matching, unit-preserving quantity answers, export-field redaction and draft recovery after activity recreation. Actual OS microphone/TTS services and credential entry remain manual acceptance work.
- 246 catalogue entries per locale; three English source variants are generated. The other 19 locale packs remain queued with no translation provider, as requested. Human and qualified safety review remain required.
- Amazon draft forms, English listing, original icons and GitHub policy/deletion links were saved. These are draft declarations requiring publisher review. No production APK or final submission was sent; no paid transaction occurred.
- Production preflight remains intentionally blocked; release/readiness.json records the current outstanding gates. Only unsigned QA release packaging is available.


## Unsigned package and portrait acceptance — 30 October 2026

- Final debug/unsigned release builds, JVM tests and lint passed. 11 JVM unit tests passed. Physical KFTRWI (Android API 28 / Fire OS 7) results: 22 tests recorded, 21 passed, one screenshot-only test skipped, zero failures/errors. Six Python tooling tests passed. Lint remains at 60 warnings and one hint, with zero errors/fatals.
- Added physical portrait Settings/export/PDF preview journey and PDF renderer valid-page/clamping/corrupt-input checks. The original orientation is restored after the test.
- The release APK is non-debuggable, targets API 35, requires API 26 and touchscreen hardware, and retains microphone as optional. ZIP alignment was checked with Android build tools. Signature verification establishes that this review artifact is unsigned. Checkout is compiled disabled. No release-signed runtime or real purchase was tested.
- Submission-tool tests reject wrong packages/debug variants/unreviewed permissions, altered or escaping screenshot files, and failing test evidence; missing device evidence is distinct from a pass.
- API 36 emulator launch failed before Android boot: Android Emulator hypervisor driver is not installed. This is an untested platform boundary, not an app test success. No driver/system security change was attempted.
- Resource queues contain 247 entries per locale, including native plural forms. Three English variants are generated; other 19 remain pending provider/human review. The new PDF-error message and privacy wording are included in these queues.
- Latest unsigned APK SHA-256: `a7dfd149dd761fc5e770573ba21fe28c15648dde209e621087b4562d3c82e07a`. The review report records the APK and every packaged asset/document hash. Packaging does not certify production readiness.


## Completion history and accessibility checks — 30 October 2026

- Final debug/unsigned release builds, unit suite, full physical Fire suite and lint passed. JVM: 11 passed. KFTRWI / Fire OS 7.3.3.1 / API 28: 27 tests recorded, 26 passed, one screenshot-only test skipped, zero failures/errors. Six Python tooling tests passed.
- New coverage includes schema 1 compatibility, opt-in schema 2 history, encrypted history round trip, selected-record scoping, independent restore IDs, invalid history and byte-limit rejection, and transaction rollback when a history insert conflicts.
- The Settings/export-history control remained reachable at 200% Compose text scale with RTL layout direction. This does not establish full RTL translation, all-screen large-text support or TalkBack acceptance. Export choices now expose their labels as part of a checkbox row with a minimum 48dp target.
- On this device, the no-recognizer test passed: it displayed the fallback without requesting microphone permission and retained typed-command access. TTS engine availability and real audible output remain separate checks in docs/VOICE.md.
- Two earlier runs stopped in the structured-plan test: first while locating its field, then while asserting a saved detail outside the visible list. The test now waits after navigation and searches its unique plan name before asserting the detail; the complete rerun passed. No acceptance gate was bypassed or test removed.
- Latest unsigned APK SHA-256: `a80eded602e5cff2c46060c4c409df1d773f2e64d84eb789257688e46fd5d6d6`.
- There are 251 catalogue entries per locale including plural forms. Three source English variants are generated; the other 19 remain pending. The saved Amazon description now matches the optional history backup behavior.

The final rerun also verifies the fixed export-history selection snapshot and uniquely scoped kit/supply/plan searches. All 27 recorded device cases completed with zero failures/errors and the one expected screenshot-only skip.
