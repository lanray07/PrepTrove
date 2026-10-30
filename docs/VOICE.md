# Voice capability and acceptance report

Assessed 30 October 2026. These are capability boundaries, not a claim that every speech engine or language works.

## Implemented

PrepTrove checks Android speech-recognition availability before requesting microphone access. On a device with a recognition service, push-to-talk requests permission when needed and presents the transcript for explicit review. The app does not retain microphone recordings. Recognition and optional text-to-speech use installed device services, which can process audio or spoken text over the network under their own terms. Typed input is available independently.

The command parser supports the documented English subset: expiry review, navigation to contacts/checklists, add-supply drafts, read saved plans, next-month reminder drafts, exact-name supply inspection and unit-aware quantity queries. Destructive commands are unsupported. Ambiguous exact-name inspection does not change records. Quantity answers do not combine different units. Multilingual dictionaries remain queued; English UI variants do not establish support for other command languages.

Spoken output is only user initiated. The read-response button is disabled for an empty response. If the TTS service/language is unavailable or rejects a request, the screen keeps the response text and explains how to use the text alternative or check the installed service. Recognition is cancelled and speech stopped when the activity backgrounds. A successful TTS request is not proof that audio was heard.

## Observed physical device

The connected KFTRWI reports Fire OS 7.3.3.1, Android API 28. Android service discovery reported no recognition service. TTS discovery reported IVONA and Pico services. Installed TTS services alone do not establish language availability, audible output or voice quality.

The no-recognition fallback is covered by an instrumentation test which is conditional on the device having no recognizer. It checks the unavailable-service message, unchanged microphone permission and continued typed-command access. A conditional skip on another device is not a successful recognition test. Parser behavior is covered independently by JVM tests.

## Still required

- Actual microphone grant/deny/retry and transcription with each intended supported recognizer.
- Installed language/voice selection, audible output, interruption, audio routing and offline/network behavior for the intended TTS engines.
- TalkBack interaction, hearing/speech accessibility review and localized error/output review.
- Additional Fire hardware/OS versions and approved multilingual dictionaries.

API 36 emulator boot was previously attempted but blocked by the missing host hypervisor driver. That attempt provides no speech compatibility evidence. No account, purchase, phone call or emergency-service action is initiated by the voice parser.
