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

No translation provider was configured for this session. The owner requested translation queues. Non-English translations, qualified regional safety review, localised screenshot review, publisher identity/support URLs and Amazon purchase configuration remain necessary before worldwide release.
