"""Development-only DeepL translation queue. Never translates private app records.
Usage: python tools/localize.py [--translate] [--approve LOCALE]
Approval is a human review assertion, never inferred from API success.
"""
import argparse,json,os,re,urllib.request
from pathlib import Path
from xml.sax.saxutils import escape
ROOT=Path(__file__).resolve().parents[1]
LOCALES=['en-GB','en-US','en-AU','es-ES','es-419','fr-FR','fr-CA','de-DE','it-IT','pt-BR','pt-PT','nl-NL','pl-PL','sv-SE','ja-JP','hi-IN','ar','tr-TR','id-ID','ko-KR','zh-Hans','zh-Hant']
TARGET={'es-ES':'ES','es-419':'ES','fr-FR':'FR','fr-CA':'FR','de-DE':'DE','it-IT':'IT','pt-BR':'PT-BR','pt-PT':'PT-PT','nl-NL':'NL','pl-PL':'PL','sv-SE':'SV','ja-JP':'JA','hi-IN':'HI','ar':'AR','tr-TR':'TR','id-ID':'ID'}
p=argparse.ArgumentParser();p.add_argument('--translate',action='store_true');p.add_argument('--approve',choices=LOCALES);args=p.parse_args()
source=json.loads((ROOT/'localization/source.json').read_text(encoding='utf-8'))
cache_dir=ROOT/'localization/cache';cache_dir.mkdir(exist_ok=True)
report=[]
for locale in LOCALES:
 file=cache_dir/(locale+'.json'); cache=json.loads(file.read_text(encoding='utf-8')) if file.exists() else {}
 if args.translate and locale in TARGET:
  key=os.environ.get('DEEPL_AUTH_KEY');assert key,'Set DEEPL_AUTH_KEY in the development environment only'
  for name,text in source.items():
   if cache.get(name,{}).get('source')==text:continue
   placeholders=re.findall(r'%\d*\$?[sdf]|\{[^}]+\}',text)
   protected=text
   for i,token in enumerate(placeholders):protected=protected.replace(token,f'<x id="{i}"/>',1)
   req=urllib.request.Request(os.environ.get('DEEPL_ENDPOINT','https://api-free.deepl.com/v2/translate'),data=json.dumps({'text':[protected],'target_lang':TARGET[locale],'source_lang':'EN','tag_handling':'xml','ignore_tags':['x']}).encode(),headers={'Authorization':'DeepL-Auth-Key '+key,'Content-Type':'application/json'})
   translated=json.load(urllib.request.urlopen(req,timeout=30))['translations'][0]['text']
   for i,token in enumerate(placeholders):translated=re.sub(r'<x id="'+str(i)+r'"\s*/>',lambda _:token,translated)
   assert sorted(re.findall(r'%\d*\$?[sdf]|\{[^}]+\}',translated))==sorted(placeholders),'Placeholder mismatch'
   cache[name]={'source':text,'translation':translated,'reviewed':False}
   file.write_text(json.dumps(cache,ensure_ascii=False,indent=2),encoding='utf-8')
 if args.approve==locale:
  assert all(cache.get(k,{}).get('source')==v for k,v in source.items()),'Missing or stale translations'
  for item in cache.values():item['reviewed']=True
  file.write_text(json.dumps(cache,ensure_ascii=False,indent=2),encoding='utf-8')
 complete=all(cache.get(k,{}).get('source')==v and cache[k].get('reviewed') for k,v in source.items())
 if complete:
  qualifier='b+'+locale.replace('-','+')
  out=ROOT/'app/src/main/res'/('values-'+qualifier);out.mkdir(exist_ok=True)
  (out/'strings.xml').write_text('<resources>\n'+''.join('<string name="'+k+'">'+escape(cache[k]['translation']).replace("'","\\'")+'</string>\n' for k in source)+'</resources>',encoding='utf-8')
 report.append({'locale':locale,'staticStrings':len(source),'reviewed':sum(1 for k,v in source.items() if cache.get(k,{}).get('source')==v and cache[k].get('reviewed')),'resourceStatus':'generated' if complete else 'pending','releaseReady':False,'remaining':'Dynamic messages, plurals, voice dictionary, marketing and safety review require completion'})
(ROOT/'localization/report.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print('Wrote localization/report.json. Pending languages are not advertised as supported.')
