# Worldwide localisation validation

Evidence snapshot: 2 November 2026. This document distinguishes AI-reviewed draft coverage from production release approval. Purchases remain disabled.

## Current implementation

All 22 locale queues have every non-safety UI string and plural form populated and marked as linguistically reviewed. Review provenance explicitly identifies Codex AI; it is not independent human, safety-specialist, speech-device or store approval. The current catalogue has **251 source string keys** plus three plural groups. Queue entry totals differ because languages require different plural categories.

There are **308 translated store fields** (14 per locale: title, short description, long description, ten feature bullets and keywords) and **235 localised command templates** covering eight documented actions. These are prepared drafts; they do not establish store-language eligibility or live multilingual speech recognition. Store copy still describes the currently released English interface/command scope where appropriate. Update that copy only when the supported production scope actually changes.

Current UI coverage measured directly from `localization/queues/*.json`:

| Locale | Non-safety entries complete | Safety entries |
| --- | ---: | --- |
| ar | 258 | 11 null |
| de-DE | 246 | 11 null |
| en-AU | 246 | 11 source English |
| en-GB | 246 | 11 source English |
| en-US | 246 | 11 source English |
| es-419 | 249 | 11 null |
| es-ES | 249 | 11 null |
| fr-CA | 249 | 11 null |
| fr-FR | 249 | 11 null |
| hi-IN | 246 | 11 null |
| id-ID | 246 | 11 null |
| it-IT | 249 | 11 null |
| ja-JP | 246 | 11 null |
| ko-KR | 246 | 11 null |
| nl-NL | 246 | 11 null |
| pl-PL | 252 | 11 null |
| pt-BR | 249 | 11 null |
| pt-PT | 249 | 11 null |
| sv-SE | 246 | 11 null |
| tr-TR | 246 | 11 null |
| zh-Hans | 246 | 11 null |
| zh-Hant | 246 | 11 null |

The 19 non-English packs each deliberately retain **11 null safety translations** (209 pending entries). All queues keep `qualifiedSafetyReview: false`. English source safety wording appearing in QA builds is a fallback, not translated or country-specific safety approval. AI linguistic review does not satisfy the separate qualified safety gate. Production language approval must not be inferred from draft coverage.

## Isolated native QA

`tools/localization_qa.py` validates current source keys, review flags, placeholders and authoritative safety flags before generating test-only resources. The `.localizationqa` application ID, `qa_only_not_for_release` resource marker and visible “LOCALISATION QA — NOT FOR RELEASE” banner distinguish these builds from the installed production app. The test fixtures use private sample records. QA screenshots are not marketing assets or approved store screenshots.

`LocalizationQaTest` exercises all 22 locales. It compares every packaged string against its expected value, checks plural resources across quantities 0, 1, 2, 3, 5, 11, 101 and 1,000,000, and explicitly checks Arabic and Polish plural selection. It renders the locale's layout direction, records overview and validation-error screens, stresses Settings at 200% font scale and submits a localised typed expiry command. These checks include RTL resource/layout behaviour; static screenshots alone cannot certify every RTL interaction or accessibility journey.

`tools/capture_localization_qa.py` verifies APK expectations are current, collects four screenshots per locale and records hashes in the capture manifest. It targets the isolated package and removes its test installations after a successful run.

## Evidence and fixes

An earlier native run passed **5,500 string comparisons** (250 keys × 22 locales), plural and typed-command checks, and generated **88 captures**. Its saved results are in `artifacts/localization-qa-captures/localization-qa/results.json`. This is evidence for that earlier source/build snapshot only.

The native resource comparison exposed two real packaging issues: Indonesian required Android's legacy `values-in-rID` qualifier, and AAPT whitespace normalisation changed unquoted text. The resource-folder mapping now handles Indonesian explicitly, and generated XML quotes text to preserve whitespace while escaping formatting characters and line breaks.

AI visual inspection of the earlier captures found the fixed top-bar subtitle clipped at 200% font scale and long date placeholders extending beyond their input outlines. The implementation now hides the secondary tagline at large font scales, widens the large-font sidebar, gives date fields full available width and separates short field labels from wrapping format/help text. New purchase-date wording increased the source catalogue to 251 keys. The final rebuild and native rerun passed: **5,522 string comparisons** (251×22), plural/typed-command checks and **88 fresh captures**, in 113.686 seconds. Final source/test APK and expectation hashes are bound in `artifacts/localization-qa-capture-manifest.json`. Sample visual reinspection confirms the repaired date help and large-font header.

In the sampled French, Polish, Swedish, Turkish, Indonesian and Hindi editor/settings captures, validation errors and Save/Cancel controls were visible and no missing glyphs were observed. Long scrollable content outside the viewport was not classified as clipping. This was sampled AI review, not all-screen certification.

## Remaining release boundaries

- Obtain qualified review of safety translations and country-specific guidance before approving the remaining production languages.
- Produce and review actual localised store screenshots after production language eligibility is established; the QA banner images are unsuitable for submission.
- Verify microphone recognition, TTS voices, accents and platform fallbacks on the actual intended devices. Typed-command tests do not establish human speech support.
- Complete VoiceView/screen-reader journeys and the required API 33+ device coverage separately. No such completion is claimed here.
- Retain disabled purchases until Amazon product configuration and purchase/restore validation are available. Amazon-managed signing accepts unsigned APKs; resolve the outstanding console requirements before submission.

## Reproducing the evidence

Run the localisation unit checks and the isolated capture workflow from the repository root with the documented Android SDK/JDK available:

```powershell
python -m unittest discover -s tools -p 'test_*.py'
python tools/capture_localization_qa.py
```

The capture workflow builds its QA APKs, requires an unlocked connected device and checks APK expectations before instrumentation. Inspect its current manifest/results and the actual images; a generated resource report alone is not a native-test pass. Do not promote a queue or alter production-approved languages solely because the QA run succeeds.
