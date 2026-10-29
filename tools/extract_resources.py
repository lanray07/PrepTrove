"""Extract Android resources into review catalogues, or check for catalogue drift."""
from pathlib import Path
import argparse,json,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args()
root=ET.parse(ROOT/'app/src/main/res/values/strings.xml').getroot()
def decode(text):return (text or '').replace("\\'", "'").replace('\\"','"').replace('\\n','\n')
strings={e.attrib['name']:decode(e.text) for e in root if e.tag=='string'}
plurals={e.attrib['name']:{item.attrib['quantity']:decode(item.text) for item in e} for e in root if e.tag=='plurals'}
for file,value in [('source.json',strings),('plurals.json',plurals)]:
 path=ROOT/'localization'/file
 if args.check:assert json.loads(path.read_text(encoding='utf-8'))==value,'Catalogue drift: '+file
 else:path.write_text(json.dumps(value,ensure_ascii=False,indent=2),encoding='utf-8')
print('Source catalogues match Android resources.' if args.check else 'Source catalogues extracted.')
