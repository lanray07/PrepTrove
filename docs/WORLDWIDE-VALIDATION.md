# Worldwide localisation validation

Evidence snapshot: 2 November 2026. Purchases remain disabled. Worldwide resources and build checks are complete; native acceptance, new screenshot production and Amazon submission remain outstanding.

## Approved product scope and content policy

The owner explicitly approved the recommended worldwide planner scope: neutral prompts for recording the household's own arrangements and a directory of official-source links. PrepTrove does not cache emergency recommendations, assess personal safety, issue emergency alerts, download external guidance or translate the advice on linked websites. Official links require internet; saved planner records work offline. Synthetic screenshot inventories are examples, not recommended quantities or safety instructions.

`localization/content-policy.json` records the approval, `cachedSafetyRecommendationsAllowed: false`, `qualifiedSafetyReviewClaimed: false` and an empty `safetyCriticalKeys` list. All locale queues retain `qualifiedSafetyReview: false`. Tools require qualified review for actual critical keys; the former eleven strings were substantively removed or rewritten into neutral UI/product limitations. This is a documented scope change, not a claim that AI supplied qualified safety approval. Any future safety-critical content requires classification, authoritative sourcing and qualified review before activation.

The eleven explicit changes are:

| Stable key | Previous copy | Current copy |
| --- | --- | --- |
| `planning_progress_is_not_a_guarantee_of_safety_f_26ae34` | Planning progress is not a guarantee of safety. Follow local authorities during an emergency. | PrepTrove organises your records. It does not provide emergency alerts or assess whether you are safe. Use official guidance for your location. |
| `saved_guidance_united_kingdom_8a9326` | Saved guidance • United Kingdom | Official guidance links |
| `general_planning_information_not_live_alerts_for_127afe` | General planning information, not live alerts. For other countries, consult your local emergency-management authority. | These links open external websites and need internet access. Choose the authority for your location. Saved planner records remain available offline. |
| `source_uk_government_prepare_https_prepare_campa_17bb7e` | Source: UK Government Prepare<br>https://prepare.campaign.gov.uk/get-prepared-for-emergencies/<br>Reviewed: 28 October 2026 · Content version 1<br>Source update date: not supplied. This summary is stored offline. The source link needs internet access. | This directory links to external publishers. PrepTrove does not store, translate or verify their advice. |
| `msg_6079021c3755` | Record a meeting point and how you’ll check in. | Keep your household’s chosen meeting place and check-in details together. |
| `msg_64807d95473b` | Consider a torch, phone power bank, drinking water and food suited to your household. Keep supplies easy to find and review their condition. | Open the UK Government’s Prepare website for its published information. The website is external and needs internet access. |
| `msg_54c27d3dfa89` | Source: UK Government Prepare | Open UK Government Prepare |
| `msg_1a856e66963c` | Talk through your plan | Your household plan |
| `msg_78d63721f142` | Discuss a meeting point, communication arrangements and any support your household may need. | Record the arrangements you have chosen. Notes can be edited at any time. |
| `msg_9894105732a6` | Review official local guidance | Official guidance links |
| `msg_4db74eab28d5` | Use notes for meeting points, communication, pets and accessibility arrangements. Sensitive details are optional. | Use notes for your own household arrangements. Sensitive details are optional. |

Four guidance labels plus a missing-browser error explain country/region selection, opening official websites and internet requirements. The voice-help disclosure now identifies the selected app language and installed-service processing limits. No private user records are sent through the translation workflow.

## Current implementation and measured coverage

All 22 production language packs are generated and selectable. The source contains **256 ordinary string keys** plus three plural groups. Queue totals count each required plural quantity separately. All **5,803 current UI/plural entries** are translated and linguistically reviewed, with zero missing entries. Reviewer/provenance fields explicitly identify Codex AI translation and parallel AI cross-review; they do not represent human, professional, speech-device or specialist certification.

| Locale | Complete reviewed entries |
| --- | ---: |
| en-GB | 262 |
| en-US | 262 |
| en-AU | 262 |
| es-ES | 265 |
| es-419 | 265 |
| fr-FR | 265 |
| fr-CA | 265 |
| de-DE | 262 |
| it-IT | 265 |
| pt-BR | 265 |
| pt-PT | 265 |
| nl-NL | 262 |
| pl-PL | 268 |
| sv-SE | 262 |
| ja-JP | 262 |
| hi-IN | 262 |
| ar | 274 |
| tr-TR | 262 |
| id-ID | 262 |
| ko-KR | 262 |
| zh-Hans | 262 |
| zh-Hant | 262 |

There are **308 translated store fields** across the 22 content locales and **235 command templates** for eight bounded actions. Store descriptions now describe the worldwide interface scope, service-dependent speech languages, official-source links and disabled Premium checkout. Device language/manual selection, fallback, RTL and plural resources are implemented. This implementation has not yet received post-scope native runtime acceptance.

## Build and static/unit evidence for this scope

Debug, unsigned release, screenshot and isolated localisation-QA builds succeeded after the scope change. Reported checks also passed: **18 JVM tests**, **Android lint** and **30 tooling tests**. Tooling checks include resource escaping/extraction, translation placeholders, review/content policy and generation workflows. Unit tests cover deterministic command parsing, private-slot preservation, ambiguity and case-sensitive units. Build success and unit/static checks do not establish native user journeys, audible speech or complete accessibility.

The physical device is disconnected. No post-scope native QA run, primary device regression run or current store-capture set has completed. The current generated resource expectations must not be treated as native results.

## Historical evidence, separate from this scope

Commit **`fb0bdb1`**, before the official-links-only change, had 34 passing primary native tests, 5,522 string comparisons (251 keys × 22 locales), plural/typed-command checks and 88 isolated QA images. It also had 24 English store captures. All those results and images belong to the earlier product/content snapshot. They cannot prove the new 256-key scope or serve as its final submission assets.

Those earlier runs uncovered and verified repairs for Indonesian resource qualifiers, AAPT whitespace preservation, large-font top-bar subtitle clipping and long date-field help. Sample AI visual review checked some error controls, glyphs and expanded text; it did not certify every screen or a screen-reader journey. The repaired code still requires current-scope native regression.

## Prepared capture pipeline and console state

The NativeCanvas capture pipeline is prepared to render eight genuine app views for each of the 22 locales, followed by eight localized headline panels per locale: **176 raw captures plus 176 panels**, planned. All 22 synthetic fixtures have AI cross-review, localized headlines and example record labels. These assets have not yet been produced for the new scope. Review must verify the actual locale UI, clipping, glyphs, RTL, headline readability and correspondence to implemented behavior before image upload.

The separate QA application uses `.localizationqa`, an explicit QA marker/banner and isolated sample records. `tools/capture_localization_qa.py` binds capture evidence to APK/expectation hashes and exercises every locale, plural quantities, validation screens, a 200% Settings sample and typed commands. QA-banner images are unsuitable as marketing assets. Current-scope capture execution remains pending.

Amazon's observed listing choices are mapped in `release/amazon-locales.json`: de-DE, en-AU, en-GB, en-IN, en-US, es-ES, es-MX, fr-FR, it-IT, ja-JP, nl-NL, pl-PL, pt-BR, pt-PT and sv-SE. English India maps to en-GB content; Spanish Mexico maps to es-419. These fifteen listing choices are distinct from 22 interface locales or distribution-country eligibility. Descriptions were saved in the console and showed Complete after reload. Image uploads remain pending. The new APK has not been uploaded; the refreshed unsigned package is blocked until all 22 screenshot sets are captured and checked. No final Amazon submission or approval is claimed.

## Remaining acceptance and release work

- Reconnect a representative device, install the current builds and rerun primary and 22-locale native QA. Verify the official links, country selector, missing-browser fallback and offline saved records.
- Produce and inspect all current raw store views and headline panels, then upload the appropriate assets for the observed Amazon listing locales.
- Verify microphone permissions, actual recognition, TTS languages/audible output, interruptions and fallbacks on intended engines/devices. Typed parsing does not establish live speech support.
- Complete VoiceView/screen-reader journeys, representative hardware/OS coverage and final privacy/data declarations; document limits honestly.
- Refresh the unsigned package, resolve remaining console requirements, upload the current APK and complete submission. Keep purchases disabled until separate catalogue and purchase/restore validation is available.

## Reproduction entry points

```powershell
python -m unittest discover -s tools -p 'test_*.py'
python tools/capture_localization_qa.py
```

These commands are workflow entry points, not a statement that the disconnected-device run succeeded. Keep current source/build hashes, test results and reviewed asset manifests together before treating a package as submission-ready.
