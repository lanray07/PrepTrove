# Localisation delivery and review

Status assessed 2 November 2026. The source catalogue covers screen labels, onboarding/help, confirmation and error messages, dynamic placeholders, accessibility action labels and generated checklist defaults. Native plurals cover reminder and dashboard summaries. Private record titles, notes and custom units are not translated. Catalogue coverage measures these resources, not every possible screen, speech engine or user-entered field.

## Current delivery

All non-safety UI entries in the 22 locale queues have translations and Codex AI linguistic review, with parallel AI cross-review. The current queues contain 5,693 UI/plural entries in total, including 4,713 translated and reviewed entries across the 19 non-English locales. These counts include plural quantities separately. There are no missing non-safety translations. AI review is not independent human or specialist approval.

Targets: en-GB, en-US, en-AU, fr-FR, fr-CA, es-ES, es-419, de-DE, it-IT, pt-BR, pt-PT, nl-NL, pl-PL, ar, hi-IN, ja-JP, ko-KR, zh-Hans, zh-Hant, tr-TR, id-ID and sv-SE.

Each non-English queue still has 11 safety-tagged entries with null translations and no review approval: 209 outstanding safety translations altogether. Every queue retains `qualifiedSafetyReview: false`. Only the three source English variants are generated for the production language selector; their final editorial and safety sign-off also remains outstanding. Worldwide production localisation is not release-ready.

All 22 store queues contain the full 14-field draft: title, short description, long description, ten feature bullets and keywords (308 fields). They preserve the current release disclosures, including the three English interface variants and disabled purchases. These drafts require listing eligibility, safety/disclosure review, localized store assets and final listing acceptance. A translated store listing does not enable its language in the app.

The voice queues contain 235 AI-reviewed command templates across all 22 targets, compiled into the deterministic parser. Parser availability and speech-engine compatibility are separate from production UI approval; see [VOICE.md](VOICE.md).

Counts are snapshots of the current files. Recalculate coverage from `localization/queues/*.json`, store `translation` objects and voice `translations` arrays after any source change. `localization/report.json` records resource/review status; refreshed generated reports must be kept consistent with the queues.

## Reproducible workflow

1. Edit canonical Android strings and plurals, then run `python tools/extract_resources.py` to refresh catalogues.
2. Run `python tools/localize.py` to refresh all 22 queues. It retains translations only for unchanged source text. No translation API provider is configured. Optional local `DEEPL_AUTH_KEY` enables provider drafts with `--translate`; the key is never embedded in the APK, and safety-tagged strings are excluded from automatic translation.
3. Fill translations in `localization/queues/LOCALE.json`, validate placeholders and plural categories, record the actual review identity and set per-entry review only for reviewed text. Qualified safety approval is a separate requirement and must not be inferred from AI linguistic review.
4. Use `--approve LOCALE` only after the complete review requirements are met. Only complete approved packs generate production Android resources. Invalidated packs are removed to prevent stale translations remaining in builds.
5. Review real screens, RTL layout, expanded text, large fonts and accessibility in representative configurations. Complete the separate voice, store metadata and localized screenshot reviews. None of those queues independently grants production language approval.

Run `python tools/generate_voice.py` after editing voice queues. It validates the eight-intent set and exact private-slot placeholders before compiling templates. It does not approve UI languages or speech devices.

## Isolated layout evidence

`python tools/localization_qa.py` generates preview resources for the separate localisation QA application, without changing production-approved languages. Its safety strings remain source English and the app is visibly marked as QA-only. `python tools/capture_localization_qa.py` builds/runs the native resource and screen checks; generated captures are review evidence, not approved store screenshots.

The previous native run completed all 22 isolated locale checks: 250 ordinary string resources per locale (5,500 checks), four captures each (88 images), typed-command checks and a 200% font-scale settings sample. Sample visual review found date-field label overflow and top-bar tagline clipping at large fonts. Shorter date labels, wrapping help text and removal of the decorative tagline at large font scales have since been implemented. The final rerun passed all 251 ordinary keys per locale (5,522 comparisons), plural and typed-command checks in 113.686 seconds, and refreshed 88 captures. Sample visual reinspection confirms the date help and large-font header fixes. Four sampled screens do not certify every screen or accessibility path.

## Offline review packets and locale behavior

`python tools/review_packets.py` exports 22 UTF-8 CSV files plus complete store and voice queues to `artifacts/localization-review/` and `artifacts/localization-review.zip`. Source changes invalidate corresponding translations/review. Import with `python tools/review_packets.py --import-csv FILE --locale fr-FR`. Unknown or duplicate keys, wrong locales, changed source and invalid placeholders fail before any queue is changed. A translation edit clears its review and the overall reviewer/safety sign-off; importing never approves a language. Private records are not input to the translation workflow.

Plural queues include Arabic zero/two/few/many, Polish few/many, and French/Spanish/Italian/Portuguese many. Resource generation and preflight require the complete locale-specific set. See [Android quantity strings](https://developer.android.com/topic/architecture/views/resources/string-resource-views) and [Unicode CLDR rules](https://unicode.org/cldr/charts/49/supplemental/language_plural_rules.html).

Device language resolution selects an available complete pack in preference order and falls back to en-GB. It preserves Simplified/Traditional Chinese distinctions and applies the resolved language to resources and layout direction. A withdrawn manual selection falls back safely. Numbers and displayed dates use locale-aware formatting; editable ISO date values remain explicitly labelled. User-entered units and private fields retain their original text.

Publisher/controller O.Bankole, United Kingdom and the GitHub privacy/data-deletion URLs are recorded. Regional safety review, latest device/layout acceptance, localized store assets, final privacy declarations and Amazon console acceptance remain release dependencies. Purchases remain disabled; Amazon catalogue setup is deferred.
