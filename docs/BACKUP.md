# Backup and restore

Settings supports plaintext JSON and password-protected PTB exports. PDF and CSV are reports, not restorable backups. Existing records are never overwritten by restore: each record gets a fresh ID, and relationships to kits in the backup are remapped. Repeating an import creates duplicates.

## PTB format version 1

- 4-byte ASCII magic: PTB1.
- 16-byte random salt, 12-byte random GCM nonce.
- Remaining bytes: AES-256-GCM ciphertext and 128-bit authentication tag.
- Key derivation: PBKDF2-HMAC-SHA256, 210,000 iterations, 256-bit output.
- The header magic is authenticated as additional data. Key derivation parameters are fixed by format version.
- Minimum 12-character export password. Passwords cannot be recovered. Keep a separate secure copy.
- This protects exported PTB files; the local Room database is not additionally encrypted by this feature.

Input files are bounded to 5 MiB and 5,000 records. Unsupported versions, duplicate IDs, invalid quantities and invalid dates are rejected before database mutation. Records are imported in one Room transaction after explicit confirmation. Missing kit references become unassigned.

Contacts and plans require inclusion confirmation when exporting. Other notes may also contain sensitive information. Plaintext JSON, CSV and PDFs remain unencrypted. User-selected document providers may sync the selected files according to their own settings.

The encryption uses standard Android/JVM cryptographic providers. A security audit, full Storage Access Framework journey tests and backup interoperability checks remain release requirements. No end-to-end encryption claim is made.

## Database version 2 fields

JSON/PTB records now preserve structured plan/contact data, purchase dates, recurring intervals and template flags. Older backups load those additions with safe defaults. Kit links continue to be remapped on copy-based restore. Photo pixels are not included; photo references may be unavailable on another device. Completion history can be explicitly selected for JSON/PTB backups. This is disclosed in the export confirmation.


## Optional completion history (JSON schema 2)

The export dialog offers an unchecked history option for JSON/PTB only. History is filtered to records actually included after the contact/plan and field selections. Historical event titles may contain earlier personal names even if a record has since been renamed; include history only when appropriate for the destination. PDF/CSV do not include history.

Backups without history retain schema 1 for compatibility. A backup with history uses schema 2 and a `reviews` array of ID, entry ID, title and ISO completion date. The reader accepts both schemas. All record, kit and event links are remapped to new IDs, so repeated imports remain independent copies. Malformed/orphaned/duplicate review IDs and invalid dates are rejected before any database mutation. Records and history import atomically; a failure rolls back both.

The size bound remains 5 MiB for the complete encrypted container. JSON export enforces the same plaintext budget (minus container overhead) so a newly created backup fits the restore limit. Limits are 5,000 records and 50,000 history events within that shared byte budget. Large histories may need a smaller selection; the app reports export failure instead of claiming success. The encrypted PTB1 envelope and cryptographic parameters are unchanged. Cross-device provider and encrypted interoperability acceptance still requires manual review.
