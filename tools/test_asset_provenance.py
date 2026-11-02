import tempfile,unittest
from pathlib import Path
from asset_provenance import fingerprint

class AssetProvenanceTest(unittest.TestCase):
 def test_source_fingerprint_ignores_checkout_line_endings_but_detects_changes(self):
  with tempfile.TemporaryDirectory() as directory:
   root=Path(directory);file=root/'Screen.kt';file.write_bytes(b'first\r\nsecond\r\n');before=fingerprint(root,[file])
   file.write_bytes(b'first\nsecond\n');self.assertEqual(before,fingerprint(root,[file]))
   file.write_bytes(b'changed\nsecond\n');self.assertNotEqual(before,fingerprint(root,[file]))
