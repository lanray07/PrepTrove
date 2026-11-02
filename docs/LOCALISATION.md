# Localisation delivery and review

The Android source catalogue includes screen labels, confirmation/error text, dynamic placeholders, accessibility action labels and generated checklist defaults. Native plurals cover reminders and the dashboard expiry summary. The remaining raw interpolated supply row combines formatted numbers with private user fields and deliberately does not translate those fields. This audit does not establish total product translation coverage.

The app offers three source English variants: en-GB, en-US and en-AU. Other target locales are queued and cannot be selected until resource packs pass review. Source-English release copy also requires final editorial and safety sign-off.

## Reproducible workflow

1. Add or edit canonical Android strings and plurals. Run `python tools/extract_resources.py` to refresh catalogues.
2. Run `python tools/localize.py` to refresh all 22 queues while retaining translations only for unchanged source text.
3. Optionally configure DEEPL_AUTH_KEY locally and run `--translate`. No key is embedded in the APK. Provider output remains unreviewed, placeholders are validated, and safety-tagged strings are never sent for automatic translation.
4. A reviewer fills missing text in localization/queues/LOCALE.json, verifies placeholders and plurals, sets each reviewed field, records reviewer identity and obtains qualifiedSafetyReview sign-off.
5. `--approve LOCALE` validates the review. Only complete approved packs generate Android resources. Invalidated packs are removed so stale files cannot silently remain in a build.
6. Generate app screens in the selected locale on representative devices, review RTL/expanded text/large fonts/TalkBack, and complete separate voice and store metadata queues. Those queues are not runtime language-support declarations.

## Voice and store differences

Recognition and TTS availability depend on installed services and are not guaranteed by a resource translation. Intent parsing currently supports the documented English subset. Voice queue files contain source examples and slots for linguistic/device review; unimplemented dictionaries are not advertised.

Store-listing locale availability must be validated separately in Amazon's console. A target UI locale does not imply an eligible listing or distribution country. No store metadata queue is approved. No translated headline is placed over an English app screen.

## Current external dependencies

No translation API provider is configured. On 2 November 2026 the owner assigned linguistic review to Codex. The queues explicitly identify AI translation and review; no human or specialist safety review is implied. There are now 1,478 AI-translated/reviewed non-English entries: 246 of 257 each for fr-FR/fr-CA (all non-safety copy), and 58 each for the other 17 targets. The latter cover core navigation, record actions, export selections, photo controls and locking. The 11 safety-tagged French entries remain untranslated pending qualified review. Other copy and actual device typography remain pending; these partial packs cannot be selected in the release app.

Publisher/controller O.Bankole, United Kingdom and the GitHub support policy URLs are recorded. Regional safety review, full translated copy, device/layout acceptance, localized store assets and final privacy declarations remain open. Amazon catalogue setup is deferred while purchases remain disabled.

## Offline review packets

Run `python tools/review_packets.py` after refreshing `localize.py`. This exports all 22 UTF-8 CSV files plus separate full store and voice queues to `artifacts/localization-review/` and the allowlisted `artifacts/localization-review.zip`. The store source now includes the actual current title, short and long descriptions, ten feature bullets and keywords, replacing the earlier three-field brief. Source changes invalidate the corresponding store translations and reviewer.

Import a completed CSV with `python tools/review_packets.py --import-csv FILE --locale fr-FR`. Unknown/duplicate keys, wrong locales, changed source and invalid formatting placeholders fail before any queue is changed. Updating a translation clears its review and the overall reviewer/safety sign-off. Importing a file never approves a language. Private records are never input to this process.

Review queues now include additional plural categories for Arabic (zero/two/few/many), Polish (few/many), and French/Spanish/Italian/Portuguese (many). The English other text provides context for these forms; translators must supply the target grammar. Resource generation and preflight require the complete locale-specific category set. See [Android quantity strings](https://developer.android.com/topic/architecture/views/resources/string-resource-views) and [Unicode CLDR rules](https://unicode.org/cldr/charts/49/supplemental/language_plural_rules.html).

Device language selection now picks a complete available pack in preference order, falls back to en-GB, and preserves the Simplified/Traditional Chinese distinction. It applies the resolved language to both resources and layout direction, so an unavailable Arabic pack cannot leave English voice tools requesting Arabic speech recognition. Manual selections whose packs were withdrawn also fall back safely.
