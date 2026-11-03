"""Catch Windows code-page corruption before native selectors or release JSON fail."""
import json
from pathlib import Path
import unittest

class SourceEncodingTest(unittest.TestCase):
    def test_sources_and_release_metadata_are_utf8(self):
        root = Path(__file__).resolve().parents[1]
        extensions = {'.kt', '.json', '.md', '.py', '.sh', '.yml', '.xml'}
        for directory in ['app/src', 'tools', 'docs', 'release', 'localization']:
            for path in (root / directory).rglob('*'):
                if path.is_file() and path.suffix in extensions:
                    with self.subTest(file=path.relative_to(root).as_posix()):
                        content = path.read_text(encoding='utf-8')
                        if path.suffix == '.json':
                            json.loads(content)
