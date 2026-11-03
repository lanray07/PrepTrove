# Localisation delivery and review

Status: 2 November 2026. The owner approved a worldwide household planner with neutral record prompts and links to official sources. PrepTrove no longer caches emergency recommendations. External guidance opens in a browser, requires internet access and is neither downloaded nor translated by the app. Saved private planner records remain available offline.

## Current coverage and scope

All 22 production resource packs are generated. The canonical catalogue has 256 ordinary string keys and three plural groups; locale-specific plural quantities bring the queues to 5,803 entries. Every current entry is populated and marked linguistically reviewed. Review provenance identifies Codex AI translation and cross-review, not human, professional or specialist approval. Private record titles, notes, names and custom units retain their original text.

The packs are en-GB, en-US, en-AU, fr-FR, fr-CA, es-ES, es-419, de-DE, it-IT, pt-BR, pt-PT, nl-NL, pl-PL, ar, hi-IN, ja-JP, ko-KR, zh-Hans, zh-Hant, tr-TR, id-ID and sv-SE. Device-language resolution selects an available pack in preference order, preserving Simplified/Traditional Chinese distinctions, with en-GB fallback. Manual language selection applies resources and layout direction. Generated resources are not evidence that the revised app has passed runtime or accessibility acceptance on all devices.

All `qualifiedSafetyReview` flags remain false. The explicit owner-approved scope change and the eleven before/after replacements are recorded in `localization/content-policy.json` and [WORLDWIDE-VALIDATION.md](WORLDWIDE-VALIDATION.md). Qualification is required when actual safety-critical keys exist; the current authoritative critical-key list is empty because recommendations were removed, not approved. Future safety-critical content requires classification, authoritative sourcing and qualified review before activation.

All 22 store queues contain fourteen translated fields each (308 fields): title, short description, long description, ten feature bullets and keywords. Current copy describes worldwide interface languages, installed-service limits for speech, official-source links requiring internet and disabled purchases. There are 235 AI-reviewed voice templates across the same 22 targets. Typed command dictionaries do not establish recognition or TTS compatibility; see [VOICE.md](VOICE.md).

## Workflow

1. Edit canonical Android strings/plurals and run `python tools/extract_resources.py`.
2. Run `python tools/localize.py`. Unchanged source retains translations; changed source invalidates its review. Provider drafts can optionally use a locally configured `DEEPL_AUTH_KEY`; no provider or key is required for the current reviewed queues, and no key is embedded in the APK.
3. Translate and review entries, preserving placeholders and required plural categories. Record the actual reviewer and provenance. Do not translate private records or infer qualified approval from AI linguistic review.
4. Run the locale approval/resource workflow only when its current content/review requirements pass. Refresh reports and remove invalidated resources rather than retaining stale packs.
5. Run `python tools/generate_voice.py` after voice queue edits. It validates the eight-intent set and exact private-slot placeholders, then compiles deterministic templates.
6. Build and test current production and isolated QA variants. Check RTL, expanded text, 200% font scale, error/confirmation states, language switching and accessibility on representative devices. Resource generation alone completes none of these runtime checks.

Arabic, Polish and other locales carry their required additional plural forms. See [Android quantity strings](https://developer.android.com/topic/architecture/views/resources/string-resource-views) and [Unicode CLDR rules](https://unicode.org/cldr/charts/49/supplemental/language_plural_rules.html). Numbers and displayed dates use locale formatting; editable ISO dates have explicit labels/help. User-defined units are preserved.

`python tools/review_packets.py` exports 22 UTF-8 CSV files and full store/voice queues under `artifacts/localization-review/`, plus the review ZIP. Import with `--import-csv FILE --locale fr-FR`; unknown/duplicate keys, locale/source mismatches and invalid placeholders fail before mutation. Import does not approve a language. Private records are excluded from this process.

## Validation and assets still required

The neutral-planner scope passes hosted run 41561192222 on source 8816674:18 JVM/31 tooling checks,35 primary native passes with 4 skips,1 separate system-file pass,5632 packaged-string comparisons/110 QA captures and176 raw/176 marketing captures. All 176 marketing panels have scoped AI visual review on current hashes. The final current-source Fire run passed 35 cases with 5 skips, all 22 language checks/110 QA captures and176 raw/176 panel captures. The owner reported working audio and VoiceView controls; this is a one-device smoke check, not universal engine/accessibility certification. See [WORLDWIDE-VALIDATION.md](WORLDWIDE-VALIDATION.md).

The NativeCanvas store-capture pipeline and synthetic fixtures are prepared for eight raw screenshots and eight headline panels per locale: 176 raw captures and 176 panels across 22 locales, still to be produced and reviewed. Every fixture has AI cross-review and explicit synthetic/example identity. No advice is implied by example inventories; no translated headline is intended to cover an English UI image.

Amazon's console exposes fifteen listing locales, separately mapped to content locales in `release/amazon-locales.json`. Their descriptions were saved and showed Complete after reload. All 11 available Amazon media locales now have eight matching saved images; all 88 downloaded originals match reviewed hashes and order. The corrected unsigned APK is accepted into the draft and its stored download hash matches the local release. All 22 current-source screenshot sets are captured and checked; the unsigned review package records remaining gates. Purchases remain disabled. Production device acceptance, voice engines, accessibility, final declarations and the console's remaining submission requirements remain open.
