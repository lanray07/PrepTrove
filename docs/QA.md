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


## Document providers and reminder opt-in — 30 October 2026

- Final debug and unsigned release builds, JVM suite, lint and full physical Fire suite passed. JVM: 11 passed. KFTRWI / Fire OS 7.3.3.1 / API 28: 32 tests recorded, 31 passed, one screenshot-only test skipped, zero failures/errors. Seven Python tooling tests passed. Lint severity totals: {'Warning': 62, 'Hint': 1}; no errors/fatals.
- A cross-process test ContentProvider with synthetic fixtures exercised encrypted restore and selected-history filtering, wrong-password rejection, CSV/JSON/PDF streams, invalid-format non-truncation, oversized input and missing documents. Non-content URIs were rejected without changing an existing app-local fixture. The production UI uses this tested PlannerDocuments path.
- The fixture is Java because the standalone test-provider process does not inherit the target APK's Kotlin runtime. It is only in the test APK, restricts callers to target/test UIDs and cannot appear in the release manifest; the bundle checker rejects that component or authority.
- Native NotificationManager checks passed for initial opt-out, localized singular/plural count text, replacement, empty-due cancellation and disable cancellation. The test used a unique tag and removed its own notice. The API 28 UI test enabled the persisted setting, recreated the activity and disabled it; it restored the original setting afterward. Real API 33+ permission dialogs and long-running delivery remain manual checks.
- A single-job rerun followed an initial overlapping-build file lock. Another run exposed the test fixture's missing Kotlin runtime; conversion to Java resolved it. No tests or production gates were bypassed.
- Source catalogue: 254 entries per locale including plural forms. Three source English variants remain available; the other 19 queues still await provider/human review.
- Latest unsigned APK SHA-256: `a3decd2297ce5b4efdcafdd41de9bf04a976b8c63f04cc2c7b214a32f4c78e11`. The package remains unsigned, non-debuggable and compiled with purchases disabled. The report records all packaged file hashes and verifies the test provider is absent.


## Final handoff — 2 November 2026

- Debug and unsigned release builds, 11 JVM unit tests and lint passed. Seven Python tooling tests passed. The fresh unlocked-device run recorded 33 tests: 32 passed, one screenshot-only skip, zero failures/errors.
- The initial hardware run was stalled by the sleeping/locked tablet and was interrupted; it is not counted as a pass. The owner unlocked the device and the complete suite was rerun successfully.
- The new provider test confirms that transported backup exports and legacy/external restores drop local photo references, preserve other structured fields, and preserve local draft photo references through EntryStateSaver. Photo pixels remain excluded.
- Publisher country United Kingdom was supplied by the owner and added to the privacy review copy. This is not legal approval or an Amazon account-country change.
- Unsigned APK SHA-256: `d2b4858b4af2ece99e79beb2f4c90acaf91b4dfc92d5a6da4e6926ede1c5c678`. Purchases remain disabled. Signing, professional/human reviews and the manual acceptance boundaries in RELEASE.md remain open.


## Worldwide localisation architecture — 2 November 2026

- Debug and unsigned release APKs built successfully. Thirteen JVM tests passed, including device-preference order, regional fallback and Chinese script separation.
- Physical KFTRWI instrumentation recorded 34 cases: 33 passed, one screenshot-only skip, zero failures/errors. The new Android test verifies unsupported Arabic falls through to a preferred complete English pack with LTR direction, and withdrawn persisted language choices cannot reactivate unavailable packs.
- Lint: 62 warnings, one hint, zero Error/Fatal. This is not a complete accessibility or global typography review.
- Fifteen Python tooling tests passed. Coverage includes locale-specific plural categories, repeated/escaped printf placeholders, rejecting stale/wrong-locale/duplicate CSV rows, clearing old approvals after import, and allowing paid-release checks to be deferred only for a verified checkout-disabled build.
- Source extraction and resource audit pass: 248 string keys, no missing resource references; two inline Text calls are a user-field quantity row and the PDF brand/page footer.
- AI translation/linguistic review covers 1,478 non-English entries. French and Canadian French each contain 246/257 entries, with the 11 safety-tagged entries pending qualified review. The other 17 targets each contain 58 entries. No non-English pack is enabled in the release app; screenshots and actual speech/typography for those packs remain unverified.
- Complete offline CSV/store/voice review packets are exported for all 22 targets; no private records enter the packets. Store sources now match the complete current English listing, including feature bullets and keywords.
- Release APK SHA-256: `2668ebca642da02764c71c9de2cbb73b270c13b2ee1324dbb772faa5b7e5036e`. Purchases compile disabled.
- Logs: worldwide-build-log.txt and worldwide-device-log.txt (local ignored evidence). Production preflight remains intentionally unsuccessful while worldwide acceptance is incomplete.


## API 36 and PDF locale validation — 2 November 2026

Amazon accepted the earlier unsigned API 35 APK as a draft; its device targeting disabled six Fire OS 16 tablet variants. Amazon requires target API 36 for Fire OS 16, so compile/target and CI SDK/package checks now use 36. Minimum SDK remains 26. This addresses manifest eligibility, not demonstrated runtime compatibility.

PDF paragraphs now use the selected app context for language, typography and first-strong text direction; dates and page numbers use locale formatting. Added native assertions cover RTL neutral-number paragraphs, Arabic text and a rendered Arabic PDF, plus French dates.

The API 36 debug/release builds, 13 JVM tests and lint passed in fire16-offdevice-validation-log.txt. Lint counts: {"Warning": 60, "Hint": 1}. Fifteen tooling tests pass, including rejecting target API 35 review packages.

The final physical suite could not begin while the tablet was asleep and locked. It was explicitly stopped after zero completed tests, resulting in an instrumentation process-crash failure. This is not a successful native test run for the API 36/PDF changes; user unlock and a full fresh run are required. The earlier 33-pass result above predates those changes. No device lock was bypassed. Local submission bundling correctly rejects the failed/empty device evidence; the existing ZIP remains the earlier API 35 review package.

API 36 unsigned APK SHA-256: `63a3749220384bc33546d6c20adcd2e639149b8fc6c1c8727129bacfd2fb156a`. It has not replaced the earlier Amazon validation draft. Worldwide translations, localized captures and qualified safety review remain incomplete, so no final submission is made.


## Successful unlocked final run — 2 November 2026

After the owner unlocked the Fire tablet, the complete final API 36/PDF-locale suite was rerun successfully: 35 recorded cases, 34 passed, one screenshot-only skip, zero failures/errors. This supersedes the interrupted locked-device attempt above. The Arabic PDF direction/rendering and French date assertions passed. Log: fire16-unlocked-device-log.txt.

The isolated store-capture instrumentation then passed its separate test and regenerated all 24 real English-variant tablet images (en-US/en-GB/en-AU), validated for dimensions and hashes. The source/test capture packages were uninstalled; the primary installation was not seeded or cleared. Representative PDF and supplies captures were visually inspected. These are actual raw UI images, not approved worldwide marketing compositions. Log: fire16-store-capture-log.txt.

GitHub CI for code commit 971dbb1 passed: https://github.com/lanray07/PrepTrove/actions/runs/41531897101 . This CI run has no physical-device execution; hardware evidence above was recorded locally.


## Worldwide localisation and voice validation — 2 November 2026

Current source has 251 ordinary keys. All non-safety copy is AI-reviewed across 22 locale queues (4,713 non-English translated/reviewed entries), with 308 store fields and 235 closed voice templates. 209 non-English safety entries remain withheld; no specialist approval or production language activation is asserted.

Final build command assembled debug, unsigned release, isolated localisation QA/test APKs, ran 18 JVM tests and lint successfully. All 24 Python tooling tests passed. The final isolated Fire/API28 native test passed in 113.686 seconds, compared 5,522 packaged strings, checked locale/plural selection and typed commands, and captured 88 images. Hash-bound evidence: artifacts/localization-qa-capture-manifest.json; log: worldwide-fallback-final-device-log.txt. This QA app is separately packaged and visibly marked, and cannot enter the submission bundle.

AI visual reinspection of Japanese, Arabic and French editor/200%Settings captures, plus English settings, confirmed repaired date-label/help layout, wider navigation and removal of the clipped secondary header tagline. No missing glyphs or new overlap observed in those samples. This does not certify every screen, VoiceView, real microphone/TTS service or newer Fire hardware.

Real-device resource checks caught Indonesian qualifier fallback and AAPT whitespace changes. Parser tests cover all 235 templates, private slots, ambiguous inspection names, calendar-month drafts and case-sensitive units. Purchases stay disabled. See WORLDWIDE-VALIDATION.md and VOICE.md for limits. Main-app regression and fresh English store capture results are recorded below when completed.

The final zero-match quantity fallback now uses the active UI default unit, preserving saved unit text. Its regression assertion passed with all 18 JVM tests; the post-fix debug/release/localisation/screenshot app build and lint passed (worldwide-fallback-final-build-log.txt).

Latest lint has zero errors/fatal issues, 61 warnings and 1 hint. Warnings include dependency updates, unused resources, plural candidates, Android App Bundle locale delivery configuration (this Amazon package is an APK), backup/icon recommendations and style/performance suggestions; they are not presented as a warning-free result.

Post-fix main-app native regression passed:36 recorded cases, 34 passed, 2 variant-only skips, zero failures/errors (worldwide-fallback-primary-regression-log.txt, 1m23s). XML totals are authoritative; the Gradle progress console double-counts case events.

Final isolated store-capture test passed in 29.437 seconds and regenerated 24 real 1920×1200 English-variant images, dimension/hash validated. Source/test capture packages were removed; no primary app data were seeded or cleared. Log: worldwide-fallback-store-capture-log.txt. Worldwide marketing review remains pending.
