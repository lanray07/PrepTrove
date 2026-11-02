import unittest
from android_resource_text import decode
from localize import xml_text
import xml.etree.ElementTree as ET

class ResourceTextTest(unittest.TestCase):
    def test_generated_roundtrip_preserves_quotes_whitespace_and_backslashes(self):
        for value in ['PrepTrove', '  leading and  repeated  ', 'He said "hello"', "Don't", "line\nnext", r'C:\new\tab', 'العربية', '%1$s: YYYY-MM-DD']:
            node=ET.fromstring('<string>'+xml_text(value)+'</string>')
            self.assertEqual(value,decode(node.text))
    def test_unquoted_source_and_unicode(self):
        self.assertEqual("Don't",decode("Don\\'t"))
        self.assertEqual('A',decode(r'\u0041'))
