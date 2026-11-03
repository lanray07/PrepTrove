# PrepTrove release status

Assessed 3 November 2026. Package `com.preptrove.app`, version 0.1.0/code 1, minimum API 26, target API 36. Purchases remain disabled. No Amazon submission has occurred.

## Current launch scope

The owner approved worldwide household planning with neutral prompts and external official-source links. Saved records work offline. The app does not cache emergency recommendations, issue alerts, assess safety or translate linked advice. Synthetic screenshot records are examples. All 22 UI packs are implemented with explicitly recorded AI linguistic review; this is not human, specialist safety or store approval. Future safety-critical content still requires qualified review.

Implemented flows include local kits and supplies, optional photos, household-plan records, manual contacts, editable checklists, recurring review reminders, completion history, templates, typed supported commands, optional installed speech services, device-credential app locking, SAF exports, native PDF preview and copy-based JSON/PTB restore. Photos are excluded from backups. PTB backups use password-based encryption; app locking does not encrypt the Room database. See README, BACKUP.md, VOICE.md and NOTIFICATIONS.md for behavior and limits.

This release scope does not assert that every later ambition in the original master brief is implemented. There is no cloud account, publisher analytics, emergency monitoring, automatic contact sharing or Fire TV experience.

## Validation evidence

- Local release/debug builds, 18 JVM cases and 31 Python tooling cases pass. Lint has no Error/Fatal issues; remaining warnings are documented in its report.
- Physical KFTRWI/API 28 primary acceptance on source 5383c1d passed 35 cases with five assumption/variant skips and zero failures. Its isolated 22-language run compared 5,632 packaged strings and captured 110 QA screens. QA assets are never store assets.
- That Fire run also produced 176 raw store screens and 176 marketing panels. AI visual review resolved earlier layout/loading/count defects and identified an Arabic PDF page-counter ambiguity. Explicit Arabic current-page/total wording in 8816674 now passes hosted capture and scoped AI visual review. The final current-source Fire run is pending.
- Current hosted API 36 acceptance exercises document export/restore, credential lifecycle and microphone-denial fallback on an isolated emulator. Run 41561192222 passed 35 primary cases with 4 skips and1 separate system-file case. See GitHub Actions and release/native-api36-validation.json for exact evidence.
- The Fire has no recognizer. Typed controls and unavailable-service handling passed without requesting microphone permission. Its installed offline English TTS voice initialized; audible output and VoiceView still need manual verification. Other engines/locales require their own service checks.

Evidence is bound to production fingerprints, APK hashes and individual image hashes. Earlier snapshots are historical evidence and cannot prove a later source revision. Do not remove a pending gate merely because the build compiles.

## Amazon draft

All 15 description locales exposed by the console are saved Complete. Thirteen available app-language checkboxes are selected. Eleven media locale sections are enabled, but replacing their inherited English images awaits action-time confirmation of Amazon's delete dialog. Reviewed matching uploads are ready. All 22 UI locale asset sets are produced locally; the console does not expose matching media choices for every UI/description locale. See release/amazon-locales.json.

The corrected unsigned APK was accepted into the draft on 3 November. The stored download matches SHA-256 `aa3c8f1741ac21174ea13a319f5499103c51849e364b3ab2dc9812883752ad79`. Amazon parses the expected package/version/SDK values. Replacement preserves existing device selections: twelve Fire tablets and zero TV/automotive devices. Six newer disabled variants are not represented as runtime-tested. The primary category is Productivity > Notes & Bookmarking.

Amazon-managed signing permits the unsigned submission route used here; do not upload a debug or QA APK or invent a production keystore. No SKU is configured. Billing code is dormant and the unfinished purchase card is hidden. Real catalogue, purchase, restore and receipt-verification testing is mandatory before a future paid release, but is deferred for this disabled-purchase release.

## Remaining gates

1. Upload the eleven matching media sets after the old-image deletion confirmation. Current hosted native/capture checks and scoped AI visual reviews passed; final current-source Fire acceptance remains separate.
2. Complete manual audible-output/accessibility checks and document installed-service limitations accurately.
3. Publisher O.Bankole, United Kingdom must approve the final privacy/deletion notice and actual support handling at banksmi@mail.com. The concrete review proposal is in PUBLISHER-REVIEW.md. No email was sent or mailbox operation verified.
4. Review console audience, privacy/content declarations and territories against the final build. Paste release/amazon-testing-instructions.txt at final review.
5. Obtain action-time confirmation for Amazon's binding import/export/encryption-law certification before accepting it and submitting. It remains unchecked.

## Reproducible unsigned review package

Run the documented Gradle checks, capture scripts and asset review, then `python tools/submission_bundle.py`. The allowlisted ZIP contains the unsigned release APK, all locale assets, metadata, policies, source-bound evidence and pending gates. It validates identity, non-debuggable production variant, API values, touchscreen, permissions, ZIP alignment, absent test/QA resources, unit/lint results and disabled checkout. It remains `unsigned_review_only` with `productionReady:false`; producing it does not publish or certify compliance.

Use `python tools/release_check.py` for production preflight. Its nonzero result while a gate remains pending is intentional. Keep keys/passwords outside Git and retain owner-controlled signing material securely if another distribution route requires it.
