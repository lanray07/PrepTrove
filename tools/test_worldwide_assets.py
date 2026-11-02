import json,unittest
from pathlib import Path
from urllib.parse import urlsplit
from localize import ROOT,LOCALES

class WorldwideAssetsTest(unittest.TestCase):
 def test_directory_contains_only_https_links_and_no_advice_cache(self):
  data=json.loads((ROOT/'app/src/main/assets/official-sources.json').read_text(encoding='utf-8'))
  self.assertEqual('external_links_only',data['scope']);self.assertIs(False,data['adviceCached'])
  countries=[]
  for item in data['sources']:
   self.assertEqual({'country','publisher','url'},set(item));countries.append(item['country'])
   self.assertRegex(item['country'],r'^[A-Z]{2}$');self.assertTrue(item['publisher'].strip())
   url=urlsplit(item['url']);self.assertEqual('https',url.scheme);self.assertTrue(url.hostname);self.assertIsNone(url.username)
  self.assertEqual(len(countries),len(set(countries)))
 def test_all_localised_fixtures_have_matching_shape_and_nonempty_content(self):
  folder=ROOT/'app/src/androidTest/assets/store-fixtures'
  self.assertEqual(set(LOCALES),{p.stem for p in folder.glob('*.json')})
  source=json.loads((folder/'en-GB.json').read_text(encoding='utf-8'))
  def check(expected,actual):
   if isinstance(expected,dict):
    self.assertEqual(set(expected),set(actual))
    for key,value in expected.items():check(value,actual[key])
   elif isinstance(expected,list):
    self.assertEqual(len(expected),len(actual))
    for left,right in zip(expected,actual):check(left,right)
   else:self.assertIsInstance(actual,str);self.assertTrue(actual.strip())
  for locale in LOCALES:
   value=json.loads((folder/(locale+'.json')).read_text(encoding='utf-8'));check(source,value);self.assertEqual(locale,value['locale'])
