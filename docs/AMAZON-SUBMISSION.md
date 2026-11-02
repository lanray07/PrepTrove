# Amazon submission handoff

Updated 2 November 2026. This is a review package, not an approval or a production-ready claim.

## Current destination and release scope

The existing draft is recorded in `release/amazon-draft.json`. The saved U.S. English listing, two accepted tablet icons and eight accepted screenshots describe the implemented planner. Three English UI resource variants exist. Other languages remain queued; do not select them in Amazon's language declaration before review. The app requires a touchscreen; Fire TV is outside this release's implemented experience. Final Fire tablet targeting must be checked after uploading the release APK through Amazon-managed signing.

The owner confirmed that no production keystore is available and requested an unsigned review package. Amazon supports signed or unsigned APK upload and applies its own signature. A local production keystore is therefore optional for this Amazon-managed route. The current APK still lacks worldwide release acceptance. Checkout remains disabled; no SKU, price or paid entitlement is asserted. The app-specific Amazon public verification key already exists in assets and is unrelated to APK signing.

## Reproduce the package

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
python tools/extract_resources.py --check
python tools/audit_localization.py
python -m unittest discover -s tools -p 'test_*.py'
python tools/release_check.py
python tools/submission_bundle.py
```

The production preflight deliberately returns nonzero until the open gates and human review queues are complete. It is separate from assembling the unsigned review ZIP. The ZIP is written to `artifacts/amazon-review.zip`, alongside its SHA-256 and JSON evidence report. The report records the source revision and whether the working tree was dirty, APK identity, unsigned status, alignment, permissions, compiled checkout setting, test result timestamps, lint counts, open gates and hashes for all included files. Unit tests and lint must pass; missing device evidence is explicitly marked `not_run`, as on CI. Existing recorded device results are not a claim about all devices or test freshness. CI uploads the unsigned review bundle with the normal development build artifacts.

Only allowlisted app assets and documents are bundled. Keystores, passwords, environment files, private browser screenshots, raw user records and local logs are excluded. The review ZIP itself is ignored by Git. The APK is unsigned and cannot be installed directly; use the separately built debug APK only for development tests.

## Remaining publisher actions

1. Review the public privacy/deletion documents for the supplied publisher/controller O.Bankole, United Kingdom, and approve the console's privacy and content-rating declarations. Verify the support mailbox can receive and answer customer mail. The GitHub pages remain review copies until approved.
2. Complete human translation, accessibility and regional safety review required by the product brief. Keep unavailable translations queued. Review marketing screenshots in each intended listing language.
3. Validate actual speech recognition/TTS services, microphone denial/unavailability, credential lock entry/cancel/background, document-provider export/restore and notification delivery on the intended Fire devices. Automated tests do not cover all service/provider combinations.
4. Use Amazon-managed signing for this submission; no local keystore is required. Publisher signing remains optional for other distribution routes. If needed, keep the keystore outside the repository and configure the environment variables in `.env.example`; never send passwords in chat. `tools/package_release.py` signs only after production gates pass.
5. Upload a tested APK into the existing Amazon draft for binary validation; once worldwide acceptance is complete, replace it with the final candidate, review actual supported Fire tablets, choose final territories and release timing, and review Amazon's generated validation results. Do not add Fire TV without a remote-first implementation.
6. If paid features are enabled in a later build, configure the real Amazon entitlement SKU and complete the App Tester, restore, cancellation, receipt-verification and commercial-limit matrix in `AMAZON-BILLING.md` before enabling checkout. These are not tested by the disabled build.
7. Review the final listing, policies, binary hash and console checklist, then give action-time approval for final submission and any binding declaration. No submit button is clicked by the packaging tools.

## Official requirements checked

- [Amazon presubmission checklist](https://developer.amazon.com/docs/app-submission/presubmission-checklist.html): physical-device testing, accurate privacy declarations, required listing images and compatible device targeting.
- [Understanding Amazon submission](https://developer.amazon.com/docs/app-submission/understanding-submission.html): APK alignment and Amazon re-signing/wrapping. Amazon's distribution wrapper has its own store communication and data handling even when DRM is not selected; distinguish this from publisher-added analytics.
- [Fire OS compatibility](https://developer.amazon.com/docs/app-submission/device-filtering-and-compatibility.html): manifest/API filtering and final console device selection. This tablet build now targets API 36 with minimum API 26; no support for untested Fire OS versions is claimed.


Latest functionality: completion-history backup is an explicit JSON/PTB option, with selected-record filtering and atomic copy-based restore. The saved Amazon English draft has been updated to match. Photos remain excluded. See BACKUP.md and VOICE.md for format and service boundaries.


Reminder notifications now have a persistent in-app opt-in and an off action; prior preview users must enable the new saved choice. The unsigned package includes NOTIFICATIONS.md. Provider-stream integration tests use synthetic content in a test-only component; this does not replace real picker/cloud-provider or app-lock testing. The package checker verifies that the component is absent from the actual release manifest.


The owner supplied United Kingdom as the publisher country on 2 November 2026; the privacy review copy and draft manifest now record it. Final legal approval remains pending. No local keystore is configured. Amazon-managed signing is available as a route; purchases stay disabled and packaging never submits an app.


## Amazon signing correction â€” 2 November 2026

Amazon's official Appstore SDK submission documentation explicitly states that Amazon applies its signature whether the uploaded APK is signed or unsigned: https://developer.amazon.com/docs/in-app-purchasing/iap-get-started.html . A local production keystore is therefore not inherently required for Amazon-managed APK submission. Earlier statements in this handoff requiring a locally signed binary for every Amazon submission are superseded by this correction. The review ZIP is not itself an app binary: select the APK within it when uploading, after resolving release acceptance and declarations. The optional local signing tool remains available for distributions that require publisher signing. Amazon console validation and final agreement requirements must still be checked.

The owner explicitly requested submission on 2 November 2026. That authorizes submission work; it does not establish completed translation/safety/privacy reviews or acceptance of unseen binding declarations. Browser access was restored and the existing draft rechecked on 2 November 2026: the app remains Incomplete, with English (U.S.) description and images marked Complete. The first tested unsigned APK has now been accepted into the draft, but no final submission has occurred. Worldwide translations and acceptance remain the blockers.


## Confirmed worldwide launch scope â€” 2 November 2026

The owner chose to wait for the full worldwide release rather than submit an English-only initial version. Keep the Amazon draft unsubmitted until the planned locale packs, localized listings/screenshots and regional safety reviews are complete. Purchases remain disabled. No translation provider is configured; the existing instruction is to prepare review queues. Do not infer human translation or safety approval from generated resource files, machine output or this submission request. Amazon-managed signing remains a possible route; it does not close the localization and acceptance gates.


## Draft binary validation — 2 November 2026

Amazon accepted the tested unsigned API 35 APK (version code 1, DRM No) into the draft. The saved classification was corrected to Productivity > Organizers & Assistants. The console selected 12 tablets and zero Fire TV/automotive devices. Its disabled-device panel identified six newer Alexa/Kids tablet variants; Amazon documentation requires target API 36 for Fire OS 16. Compile/target SDK and the CI/package validation baseline have therefore been raised to 36. This does not establish runtime compatibility with new devices. The first API 36 test attempt was interrupted by a locked/asleep tablet. After the owner unlocked it, the final suite passed (34 cases and one screenshot-only skip). The API 36 replacement was then accepted; Amazon Manifest Details confirms minimum SDK 26 and target SDK 36. No lock was bypassed.

The uploaded binary is a validation draft, not approval to release English-only. The final binary, declared languages and reviewed localised listing assets must match before submission. The owner assigned linguistic review to Codex; 1,478 AI-reviewed entries now exist. Qualified safety approval and the remaining acceptance requirements are still open.


## Final tested draft update — 2 November 2026

The uploaded API 36 APK SHA-256 is `63a3749220384bc33546d6c20adcd2e639149b8fc6c1c8727129bacfd2fb156a`. The console parsed package com.preptrove.app, version 0.1.0/code 1, minimum SDK 26 and target SDK 36. The earlier minimum-target warning cleared after data loaded. Replacement preserves twelve selected Fire tablets and zero TV/automotive selections. Six newer Alexa/Kids tablet models still appear disabled; their runtime compatibility is not asserted. Amazon's manifest locale list contains dependency resources and must not be treated as completed PrepTrove language coverage.

The public project support website was saved and confirmed on Review & Submit. That page displays Productivity > Organizers & Assistants while reopening the edit form still shows Business > Organizers & Assistants; this inconsistency needs final verification. Prepared tester instructions are included in release/amazon-testing-instructions.txt; entered text did not persist after navigating steps, so keep the file for the final submission action.

The export-compliance checkbox explicitly certifies import/export and encryption-law compliance. It remains unchecked, and Submit App remains disabled. No declaration was accepted. This is an actual final console requirement, separate from the still-incomplete worldwide translation, localised assets and safety acceptance. Final confirmation should be sought only once the app and declarations are ready for review.

All 24 real English-variant screenshots were refreshed locally and hashed; the eight older accepted en-US assets in Amazon have not been replaced. The unsigned review ZIP now includes the tested API 36 APK, latest local screenshots and prepared tester instructions. Purchases remain disabled.
