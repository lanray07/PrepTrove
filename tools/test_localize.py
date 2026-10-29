import unittest
from localize import placeholders,validate,render
import xml.etree.ElementTree as ET
class TranslationValidation(unittest.TestCase):
 def test_placeholders_preserve_repeated_arguments(self):
  validate("%1$s: %1$s", "%1$s : %1$s")
  with self.assertRaises(ValueError):validate("%1$s: %1$s", "%1$s")
 def test_missing_translation_rejected(self):
  with self.assertRaises(ValueError):validate("Hello", "")
 def test_render_escapes_xml_and_protects_android_quotes(self):
  root=ET.fromstring(render({"example":"Don't lose A & B"},{"count":{"one":"%d item","other":"%d items"}}))
  self.assertEqual(2,len(root));self.assertIn("&",root.find('string').text)
if __name__=='__main__':unittest.main()
