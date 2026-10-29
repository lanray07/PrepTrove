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
