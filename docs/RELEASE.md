# Release gates and implementation status

Last assessed: 2026-10-30. Package com.preptrove.app, version 0.1.0.

## Amazon draft and latest implementation

The Amazon app draft, English (U.S.) description, content-rating answers and privacy questionnaire are saved for review. Public support is banksmi@mail.com. The privacy and data-deletion links point to the public GitHub documents; these documents still require publisher/legal approval. The original 512px and 114px tablet icons and eight actual en-US tablet screenshots were accepted; Amazon reports the English image section Complete. No APK has been uploaded and no final submission has been made. See release/amazon-draft.json.

New implementation includes individually selectable export fields, native PDF preview, draft recovery after activity recreation, English reminder drafts, exact-name supply inspection commands and quantity answers that preserve units. The optional credential lock still requires manual credential/background/file-picker verification. Passwords are held in memory, excluded from saved instance state and cleared on cancellation/use. Export reads a fresh database snapshot.

The app-specific Amazon public key has been downloaded and installed. The SKU remains unconfigured and checkout remains disabled, as requested. This does not establish purchase or restore functionality in a real store environment.

The isolated screenshot build uses com.preptrove.app.storecapture and labelled example records. It never seeds or clears the primary installation. Raw captures do not constitute approved marketing compositions or human translation review.

## Implemented development scope

- Persisted local records and kit assignment; safe kit deletion preserves supplies.
- Supply quantities, units, optional expiry, notes, categories, storage and inspection date.
- Editable family-plan notes and manual contacts; no automatic sharing or calling.
- Grouped checklist tasks and user-triggered starter groups.
- Persisted reminders: completion pauses, date editing snoozes; periodic best-effort notification.
- Push-to-talk recognizer availability check, runtime microphone permission, transcript preview, typed English commands, optional TTS. No raw audio storage; recognition provider may transfer audio.
- User-selected SAF export destination, PDF/CSV/JSON export and sensitive-category exclusion by default.
- Local UK guidance summary with provenance; no live alerts or emergency-number assumptions.

## Blocking full product release

- Amazon SDK 3.0.9 client integration, catalogue pricing, purchase callbacks, paginated restore, cancellation handling and local account-bound entitlement persistence are implemented. Owner SKU, authorised App Tester runs, RVS verification decision and production premium limits remain pending. Checkout has an explicit build switch defaulting OFF; no real purchase was attempted. See docs/AMAZON-BILLING.md.
- Human-reviewed translations in all 19 product locales plus Korean, Simplified Chinese and Traditional Chinese from the standing requirements. Native screen and dynamic-message resources, plural notifications, manual language selection, device-language fallback, locale-aware quantities and date display are implemented. All 22 locales have review queues and separate voice/metadata queues. Three source English resource variants are generated. The other 19 need provider translations and human review; qualified safety review and multilingual voice dictionaries remain pending. No worldwide localisation completion is claimed.
- Qualified regional safety-content review and content packs. Only the UK summary exists. Country setting does not imply country-specific reference coverage.
- Structured plan/contact fields, purchase dates, bounded private photo import, reusable templates (including kit supplies), recurring reminder intervals, snooze/pause and completion history are implemented. Complete device journeys and backup/media/history coverage still require acceptance review.
- Optional device-credential app lock is implemented; actual credential entry/cancel/background flows need manual device verification. Individual export-field selection is implemented and covered by redaction tests. PDFs now include structured fields with measured, paginated native text layout; complex-script typography needs visual review. Backup encryption and copy-based restore are implemented; end-to-end file-picker and interoperability verification remain pending. PDF reports passed a long multipage render test but should not yet be advertised as globally reviewed.
- English checked-item, next-month reminder and unit-aware quantity commands are implemented. Multilingual command dictionaries remain pending; destruction by voice is unsupported.
- Feature-module decomposition, broader lifecycle/file-picker testing, accessibility audit and talkback, RTL, rotation and large-font tests.
- Original full asset suite, splash and adaptive/monochrome icons, onboarding illustrations, all eight marketing compositions, phone/tablet and approved-language screenshots. Adaptive and monochrome launcher icons and an editable SVG master exist. Illustrations, promotional compositions and approved localised captures remain pending.
- Marketing website, legal review, trademark clearance, support-channel verification, SEO/AI search content, store descriptions across approved languages, signed release APK (unsigned QA release builds are available), device compatibility and store certification.

## Store references checked

- https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html (2026-10-28): integration entry point; receipt and catalogue handling required before selling.
- https://www.developer.amazon.com/docs/app-submission/distribute-your-app-internationally.html (2026-10-28): UI language, device languages, listing localisation and distribution are distinct. Listing choices are not identical to the requested 19 UI locales. Country availability must be confirmed in the actual console.
- Console inspected 2026-10-30: tablet screenshots require at least three and at most ten PNG/JPG images. Accepted landscape sizes include 800x480, 1024x600, 1280x720, 1280x800, 1920x1080, 1920x1200 and 2560x1600 (portrait counterparts accepted). Tablet icons are 512x512 and 114x114 PNG. Optional promotional image is 1024x500. The saved ASCII title is 42 characters; no claim is made about the maximum title length. Distribution currently includes the available countries and requires owner review. Amazon now targets Fire devices; the manifest requires touchscreen support to exclude the unimplemented Fire TV experience.

## Submission procedure

1. Close the functional and localisation gates above; run tests on representative Fire and Android devices.
2. Obtain owner approval for final commercial SKU/price, legal texts and distribution countries.
3. Configure Amazon catalogue and validate purchases using official test tooling; never ship a mock store.
4. Keep signing keys outside the repository. Build signed release, record SHA-256 and retain secure key backup.
5. Capture implemented, translated screens with labelled deterministic demonstration data; generate manifest and review crops and typography for each approved listing locale.
6. Owner reviews final submission package and authorises publication.
