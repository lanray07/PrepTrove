# Voice capability and acceptance report

Assessed 2 November 2026. Typed command parsing, installed speech services and production language approval are separate capabilities.

## Implemented interaction

PrepTrove checks Android speech-recognition availability before requesting microphone access. With a recognizer installed, push-to-talk requests permission when needed and presents the transcript for explicit review. Typed input is available independently. The app does not save microphone recordings. Recognition and optional text-to-speech use installed services, which may process audio or spoken text over a network under their own terms.

The deterministic parser now compiles 235 AI-reviewed templates across 22 locale targets from `localization/voice-queues/`. The eight intents are expiry review, contacts, checklists, saved plans, add-supply drafts, next-calendar-month reminder drafts, exact-name supply inspection and quantity queries. These are bounded command forms, not unrestricted natural-language understanding. All 22 generated UI locale packs are now selectable under the owner-approved neutral planner scope. The dictionaries do not establish multilingual microphone support; installed services and actual device availability remain separate.

The parser resolves the requested locale against available dictionaries and also accepts the en-GB fallback. Templates match the whole command, tolerate whitespace and straight/curly apostrophe differences, ignore command case and allow limited trailing punctuation. It rejects multiline or overlong input. `tools/generate_voice.py` validates all intent groups and exact slot placeholders before producing `VoicePatterns.kt`.

Private slot values are preserved with their original case, Unicode and internal spacing; outer whitespace is trimmed. No slot translation or external language service is used. Reminder and supply creation opens an editable draft requiring Save. Reminder dates use one calendar month, including end-of-month adjustment. Destructive commands, calls, purchases and emergency-service actions are unsupported.

Inspection collects matches from every applicable inspection template. It excludes reusable templates and compares saved supply names using the matched command locale. A record is marked checked only when exactly one record matches; duplicate names or overlapping grammar that identifies different records produce the exact-name clarification instead of an automatic choice.

Quantity queries use locale-aware case-insensitive exact saved names for non-English templates. English queries additionally support token matching and a small documented set of English singular/regional aliases, such as torches/flashlights. This is not general multilingual inflection. Quantities are combined only when trimmed unit strings are identical and the sum is finite. Unit case is significant: `mL` and `ML` remain different; the parser performs no unit conversion. Quantity answers use locale number formatting and keep private names and units intact.

Spoken output is user initiated; the read-response button is disabled for an empty response. When TTS is missing, lacks the requested language or rejects a request, the response stays visible with a text alternative. Recognition is cancelled and speech stopped when the activity backgrounds. A successful TTS request does not prove audible output.

## Evidence and limits

JVM tests cover every generated template's intended action and private-slot preservation, anchored/rejected input, reminder calendar behavior, exact-name ambiguity, overlapping Polish inspection grammar, excluded reusable templates and case-sensitive mixed-unit handling. The previous isolated native localisation run exercised typed commands in all 22 locale configurations. These tests validate command behavior rather than speech recognition accuracy, audio output or language quality on every device. The physical Fire neutral-planner QA run on source 5383c1d compared 5,632 packaged strings across 22 locales and captured 110 QA screens. The subsequent Arabic PDF counter wording correction passed current hosted validation in 41561192222; voice command resources did not change. The final current-source Fire run now passes; the owner also reported working audio and VoiceView controls.

The observed physical KFTRWI reported Fire OS 7.3.3.1 / Android API 28 with no recognition service. IVONA and Pico TTS services were discoverable. On 3 November, TTS initialization succeeded and its default IVONA engine reported one installed offline en-GB Amy voice. This metadata does not establish audible output or voice quality. The owner has now reported working audio and VoiceView navigation/controls; see the separately attributed manual smoke report. This does not establish every locale/engine or full accessibility compliance. The no-recognizer instrumentation test checks the fallback message, unchanged microphone permission and continued typed-command access when that condition is present. A conditional skip on another device is not a successful microphone test.

An earlier API 36 emulator attempt was blocked by a missing host hypervisor driver; that attempt provides no speech compatibility evidence. Later native layout or typed-command execution also cannot establish real recognizer or TTS engine support.

## Remaining acceptance work

- Test microphone grant, denial, retry and actual transcription with each intended recognizer and locale.
- Verify installed language/voice selection, audible output, interruption, audio routing and offline/network behavior on intended TTS engines.
- Review TalkBack, hearing/speech accessibility and localized error/output behavior on representative hardware.
- Hosted native/22-language layout checks passed in 41561192222. Complete current-source Fire and real installed-service/manual accessibility acceptance before making further device-specific voice claims.

AI linguistic review is recorded explicitly in the queues. It is not human, speech-device or specialist safety approval. Purchases remain disabled.

Zero-match quantity answers use the active UI locale’s default unit. Existing saved unit labels retain their original case and wording. The empty-result fallback is covered by the parser unit test.


## Owner-reported Fire smoke check — 3 November 2026

The owner reported audible output and usable VoiceView navigation/controls after the requested typed expiry command, Review command and Read response aloud flow. This is one-device owner evidence, not automated audio detection or an independent accessibility audit. The voice implementation was unchanged from the preceding physical build; the intervening production change concerned Arabic PDF pagination. The exact manually tested APK digest was not supplied. Current-source physical native and 22-language capture runs also pass. Actual speech recognition is unavailable on this Fire, and no human transcription accuracy is claimed. See release/manual-accessibility-validation.json and release/fire-device-validation.json.
