import unittest
from localize import placeholders,validate,render,catalogue,queue_complete,resource_folder
import xml.etree.ElementTree as ET
class TranslationValidation(unittest.TestCase):
 def test_indonesian_uses_the_android_legacy_resource_language(self):
  self.assertEqual('values-in-rID',resource_folder('id-ID'))
  self.assertEqual('values-b+zh+Hant',resource_folder('zh-Hant'))
 def test_placeholders_preserve_repeated_arguments(self):
  validate("%1$s: %1$s", "%1$s : %1$s")
  with self.assertRaises(ValueError):validate("%1$s: %1$s", "%1$s")
 def test_missing_translation_rejected(self):
  with self.assertRaises(ValueError):validate("Hello", "")
 def test_literal_percent_is_not_a_placeholder(self):
  validate('Progress: %1$d%%','Avancement : %1$d%%')
  self.assertEqual({},dict(placeholders('%%s')))
  with self.assertRaises(ValueError):validate('%d','%d%')
 def test_english_forms_cannot_approve_arabic_or_polish(self):
  source={'title':'Hello'};plurals={'count':{'one':'%d item','other':'%d items'}}
  arabic=catalogue(source,plurals,'ar')
  self.assertEqual({'title','count.zero','count.one','count.two','count.few','count.many','count.other'},set(arabic))
  self.assertEqual('%d items',arabic['count.two'])
  self.assertIn('count.few',catalogue(source,plurals,'pl-PL'))
  self.assertIn('count.many',catalogue(source,plurals,'fr-FR'))
 def test_approval_requires_real_boolean_reviews_and_named_reviewer(self):
  queue={'reviewer':' ', 'qualifiedSafetyReview':True,'strings':{'x':{'translation':'bonjour','reviewed':True}}}
  self.assertFalse(queue_complete(queue))
  queue['reviewer']='Linguist'
  self.assertTrue(queue_complete(queue))
  queue['strings']['x']['reviewed']='false'
  self.assertFalse(queue_complete(queue))
 def test_render_escapes_xml_and_protects_android_quotes(self):
  root=ET.fromstring(render({"example":"Don't lose A & B"},{"count":{"one":"%d item","other":"%d items"}}))
  self.assertEqual(2,len(root));self.assertIn("&",root.find('string').text)
 def test_android_quotes_preserve_edge_and_repeated_whitespace(self):
  root=ET.fromstring(render({'example':' add  water '},{}))
  self.assertEqual('" add  water "',root.find('string').text)
if __name__=='__main__':unittest.main()
