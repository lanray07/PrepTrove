# PrepTrove release status

Assessed 3 November 2026. Package `com.preptrove.app`, version 0.1.0/code 1, minimum API 26, target API 36. Purchases remain disabled. Amazon status **SUBMITTED** was verified at 07:11 UTC. Store approval/live availability remains pending.

## Implemented launch scope

The owner approved worldwide household planning with neutral prompts and external official-source links. Saved records work offline. The app does not cache emergency recommendations, issue alerts, assess safety or translate linked advice. Screenshot inventories are labelled examples. All 22 UI packs and 308 store fields have explicitly recorded AI linguistic review; this is not human, specialist safety or store approval. Future safety-critical content still requires qualified review.

Implemented flows include local kits/supplies, optional private photos, household plans, manual contacts, checklists, recurring review reminders, completion history, templates, supported typed commands, optional installed speech services, device-credential app locking, SAF exports, PDF preview and copy-based JSON/PTB restore. Photos are excluded from backups. PTB files use password-based encryption; app locking does not encrypt the Room database. The app has no publisher cloud account, advertising, publisher analytics or Fire TV interface. See README, BACKUP.md, VOICE.md and NOTIFICATIONS.md for setup, architecture and limits.

This release does not assert that every later ambition in the original master brief is implemented.

## Completed validation

| Check | Recorded result |
| --- | --- |
| Local release/debug builds | Passed |
| JVM / Python tooling | 18 / 31 passed |
| Android lint | No Error/Fatal; 84 warnings and one hint remain |
| Hosted API 36 primary | 35 passed, four condition/variant skips, zero failures |
| Separate real system-file case | Passed; JSON export/copy restore and original preservation |
| Current physical Fire/API 28 primary | 35 passed, five environment/variant skips, zero failures; 43.481 seconds |
| Physical 22-language QA | 5,632 string comparisons, 110 captures; 143.477 seconds |
| Physical store capture | 176 raw screens and 176 panels; 257.985 seconds |
| Current marketing review | 176 exact panels pass scoped AI visual/linguistic review |
| Audible response / VoiceView | Owner reported both working in requested one-device smoke check |

Hosted runs [41561192222](https://github.com/lanray07/PrepTrove/actions/runs/41561192222) and [41564100971](https://github.com/lanray07/PrepTrove/actions/runs/41564100971) passed both jobs. Exact source fingerprints, APK hashes, converted physical-runner XML and hosted XML are in `release/fire-device-validation.json`, `release/native-api36-validation.json` and their referenced evidence files. Owner manual evidence is separately attributed in `release/manual-accessibility-validation.json`.

The Fire has no recognizer; its typed/unavailable-service fallback passes without requesting microphone access. The installed offline English TTS voice initialized and the owner heard output. This does not certify every engine, locale, audio route or accessibility criterion. All raw companions are capture/hash validated; not every raw image was individually inspected full size. A minor retained Overview focus-style background in some emulator screenshots is documented in the mixed-locale report; the actual section has gold text.

## Amazon draft

All 15 exposed description locales are saved Complete. All 11 available media locales have eight matching reviewed PNGs: 88 saved images. Each saved set was reloaded; original PNG downloads match the reviewed hashes and 01–08 order. See `release/amazon-media-validation.json`. The console has no matching media choice for four description locales; all 22 app-locale asset sets are still delivered in the repository and review ZIP. Thirteen available app-language checkboxes are selected; absent language options are documented in `release/amazon-locales.json`.

The corrected unsigned APK is accepted into the draft. Its stored download matches local SHA-256 `aa3c8f1741ac21174ea13a319f5499103c51849e364b3ab2dc9812883752ad79`. Final Review shows the expected package/version, free price, primary Productivity > Notes & Bookmarking category, 18+ audience, all available countries, twelve Fire tablets and zero TV/automotive devices. Six newer disabled variants are not claimed as runtime-tested. Amazon-managed signing is the unsigned route used here; no production keystore was invented.

The 39 privacy data-type answers are completed and saved for publisher review. Expected user-requested exports/disclosed voice flows use Amazon's transfer exceptions; this does not imply that these actions never leave the device. Optional support correspondence, user-entered supply purchase dates and accessibility notes are disclosed for app functionality. Platform-service boundaries and the exact draft answers are recorded in `release/privacy-disclosure-review.json`. No independent inspection or certification of Amazon wrapper telemetry is claimed.

The 2,364-character tester instructions were present at submission. After the publisher explicitly certified the displayed import/export/encryption declaration, its checkbox was checked and Submit App clicked. Amazon returned SUBMITTED and App Submission Successful. Evidence and the console history timezone are recorded in `release/amazon-submission.json`; the status screenshot is in `release/evidence`.

## Publisher approvals and next store step

On 3 November 2026 the owner approved the exact public notices and all 39 privacy declarations, confirmed monitored support and its stated process/provider assessment, and explicitly certified the Amazon export declaration. The approved privacy/deletion text was published in commit39d8425 and fetched publicly to verify it matches after Git newline normalization. Owner attribution is in `release/publisher-approval.json`; this is not independent legal certification.

Amazon estimates 7 November 2026,11:30 PM PST (8 November07:30 UTC) for Appstore availability, subject to review. No store approval or live availability is claimed. The remaining external step is Amazon's review; no additional publisher action was displayed at submission.

Real SKU/catalogue, App Tester, purchase/restore and receipt verification remain deferred prerequisites for a future paid release. They are not asserted as working; checkout and its unfinished card remain disabled.

## Reproducible unsigned review package

Run the documented Gradle checks, capture scripts and asset review, then `python tools/submission_bundle.py`. The ZIP allowlists the unsigned production APK, all locale assets, metadata, proposed/current notices and source-bound evidence. It checks identity, non-debuggable variant, API values, permissions, ZIP alignment, absent test/QA resources, unit/lint results and disabled checkout. It remains `unsigned_review_only` with `productionReady:false`; packaging does not publish or certify compliance.

`python tools/release_check.py` now passes the submitted scope; this does not predict Amazon approval. Keep signing material/passwords outside Git. Other distribution routes may need owner-controlled signing keys.
