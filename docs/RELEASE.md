# Release gates and implementation status

Last assessed: 2026-10-29. Package com.preptrove.app, version 0.1.0.

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

- Amazon IAP SDK integration, configured entitlement SKU, localised store pricing, verified receipts, paginated restore, revocation, durable verified entitlement, offline policy, failure testing. No purchases or fake premium unlock exist in this build. The development preview does not enforce free-tier limits.
- Human-reviewed translations in all 19 product locales plus Korean, Simplified Chinese and Traditional Chinese from the standing requirements. Native screen and dynamic-message resources, plural notifications, manual language selection, device-language fallback, locale-aware quantities and date display are implemented. All 22 locales have review queues and separate voice/metadata queues. Three source English resource variants are generated. The other 19 need provider translations and human review; qualified safety review and multilingual voice dictionaries remain pending. No worldwide localisation completion is claimed.
- Qualified regional safety-content review and content packs. Only the UK summary exists. Country setting does not imply country-specific reference coverage.
- Optional photographs, secure attachment handling, purchase dates, custom reusable kit/checklist templates, structured multi-plan fields, custom reminder intervals and history.
- App lock, export selection at individual-field level, PDF complex-script shaping and word-boundary wrapping. Backup encryption and copy-based restore are implemented; end-to-end file-picker and interoperability verification remain pending. Current PDFs use Android canvas text and should not be advertised as worldwide-ready.
- Complete voice intents for checked items, reminder dates and locale-aware quantity nouns; destruction by voice remains unsupported.
- Feature-module decomposition, schema export/migration tests, lifecycle-restored draft state, accessibility audit and talkback, RTL, rotation and large-font tests.
- Original full asset suite, splash and adaptive/monochrome icons, onboarding illustrations, all eight marketing compositions, phone/tablet and approved-language screenshots. Adaptive and monochrome launcher icons and an editable SVG master exist. Illustrations, promotional compositions and approved localised captures remain pending.
- Marketing website, legal review, trademark clearance, support contact and privacy hosting, SEO/AI search content, store descriptions across approved languages, signed release APK (unsigned QA release builds are available), device compatibility and store certification.

## Store references checked

- https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html (2026-10-28): integration entry point; receipt and catalogue handling required before selling.
- https://www.developer.amazon.com/docs/app-submission/distribute-your-app-internationally.html (2026-10-28): UI language, device languages, listing localisation and distribution are distinct. Listing choices are not identical to the requested 19 UI locales. Country availability must be confirmed in the actual console.
- Exact screenshot dimensions, current title limits and distribution selection remain unverified release gates. Do not reuse Apple requirements for Amazon.

## Submission procedure

1. Close the functional and localisation gates above; run tests on representative Fire and Android devices.
2. Obtain owner approval for final commercial SKU/price, legal texts and distribution countries.
3. Configure Amazon catalogue and validate purchases using official test tooling; never ship a mock store.
4. Keep signing keys outside the repository. Build signed release, record SHA-256 and retain secure key backup.
5. Capture implemented, translated screens with labelled deterministic demonstration data; generate manifest and review crops and typography for each approved listing locale.
6. Owner reviews final submission package and authorises publication.
