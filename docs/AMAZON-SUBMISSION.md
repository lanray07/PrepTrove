# Amazon submission handoff

Updated 2 November 2026. This is a review package, not an approval or a production-ready claim.

## Current destination and release scope

The existing draft is recorded in `release/amazon-draft.json`. The saved U.S. English listing, two accepted tablet icons and eight accepted screenshots describe the implemented planner. Three English UI resource variants exist. Other languages remain queued; do not select them in Amazon's language declaration before review. The app requires a touchscreen; Fire TV is outside this release's implemented experience. Final Fire tablet targeting must be checked after uploading the signed binary.

The owner confirmed that no production keystore is available and requested an unsigned review package. Do not upload the unsigned APK as if it were a finished release. Checkout remains disabled; no SKU, price or paid entitlement is asserted. The app-specific Amazon public verification key already exists in assets and is unrelated to the missing production signing key.

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

1. Review the public privacy/deletion documents, identify the legal publisher/controller and approve the console's privacy and content-rating declarations. Verify the support mailbox can receive and answer customer mail. The GitHub pages remain review copies until approved.
2. Complete human translation, accessibility and regional safety review required by the product brief. Keep unavailable translations queued. Review marketing screenshots in each intended listing language.
3. Validate actual speech recognition/TTS services, microphone denial/unavailability, credential lock entry/cancel/background, document-provider export/restore and notification delivery on the intended Fire devices. Automated tests do not cover all service/provider combinations.
4. Create and securely back up the publisher's signing keystore outside the repository. Set the four `PREPTROVE_KEYSTORE_*`/`PREPTROVE_KEY_*` environment variables documented in `.env.example`. Never send passwords in chat or commit them. `tools/package_release.py` signs only after production gates pass.
5. Upload the resulting signed APK to the existing Amazon draft, review actual supported Fire tablets, choose final territories and release timing, and review Amazon's generated validation results. Do not add Fire TV without a remote-first implementation.
6. If paid features are enabled in a later build, configure the real Amazon entitlement SKU and complete the App Tester, restore, cancellation, receipt-verification and commercial-limit matrix in `AMAZON-BILLING.md` before enabling checkout. These are not tested by the disabled build.
7. Review the final listing, policies, binary hash and console checklist, then give action-time approval for final submission and any binding declaration. No submit button is clicked by the packaging tools.

## Official requirements checked

- [Amazon presubmission checklist](https://developer.amazon.com/docs/app-submission/presubmission-checklist.html): physical-device testing, accurate privacy declarations, required listing images and compatible device targeting.
- [Understanding Amazon submission](https://developer.amazon.com/docs/app-submission/understanding-submission.html): APK alignment and Amazon re-signing/wrapping. Amazon's distribution wrapper has its own store communication and data handling even when DRM is not selected; distinguish this from publisher-added analytics.
- [Fire OS compatibility](https://developer.amazon.com/docs/app-submission/device-filtering-and-compatibility.html): manifest/API filtering and final console device selection. This tablet build targets API 35 with minimum API 26; no support for untested Fire OS versions is claimed.


Latest functionality: completion-history backup is an explicit JSON/PTB option, with selected-record filtering and atomic copy-based restore. The saved Amazon English draft has been updated to match. Photos remain excluded. See BACKUP.md and VOICE.md for format and service boundaries.


Reminder notifications now have a persistent in-app opt-in and an off action; prior preview users must enable the new saved choice. The unsigned package includes NOTIFICATIONS.md. Provider-stream integration tests use synthetic content in a test-only component; this does not replace real picker/cloud-provider or app-lock testing. The package checker verifies that the component is absent from the actual release manifest.


The owner supplied United Kingdom as the publisher country on 2 November 2026; the privacy review copy and draft manifest now record it. Final legal approval remains pending. Signing remains unavailable, purchases stay disabled and no final submission is authorized by the unsigned packaging workflow.


## Amazon signing correction — 2 November 2026

Amazon's official Appstore SDK submission documentation explicitly states that Amazon applies its signature whether the uploaded APK is signed or unsigned: https://developer.amazon.com/docs/in-app-purchasing/iap-get-started.html . A local production keystore is therefore not inherently required for Amazon-managed APK submission. Earlier statements in this handoff requiring a locally signed binary for every Amazon submission are superseded by this correction. The review ZIP is not itself an app binary: select the APK within it when uploading, after resolving release acceptance and declarations. The optional local signing tool remains available for distributions that require publisher signing. Amazon console validation and final agreement requirements must still be checked.

The owner explicitly requested submission on 2 November 2026. That authorizes submission work; it does not establish completed translation/safety/privacy reviews or acceptance of unseen binding declarations. Browser control currently fails to initialize, so no binary upload or final submission has been attempted in this request.


## Confirmed worldwide launch scope — 2 November 2026

The owner chose to wait for the full worldwide release rather than submit an English-only initial version. Keep the Amazon draft unsubmitted until the planned locale packs, localized listings/screenshots and regional safety reviews are complete. Purchases remain disabled. No translation provider is configured; the existing instruction is to prepare review queues. Do not infer human translation or safety approval from generated resource files, machine output or this submission request. Amazon-managed signing remains a possible route; it does not close the localization and acceptance gates.
