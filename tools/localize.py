"""Development translation queue and resource generation. No credentials go into the APK.
--translate uses DeepL for non-safety strings. --approve LOCALE requires completed
review fields in localization/queues/LOCALE.json, including a reviewer identity.
"""
import argparse,collections,json,os,re,urllib.request
from pathlib import Path
from xml.sax.saxutils import escape
ROOT=Path(__file__).resolve().parents[1]
LOCALES=['en-GB','en-US','en-AU','es-ES','es-419','fr-FR','fr-CA','de-DE','it-IT','pt-BR','pt-PT','nl-NL','pl-PL','sv-SE','ja-JP','hi-IN','ar','tr-TR','id-ID','ko-KR','zh-Hans','zh-Hant']
TARGET={'es-ES':'ES','es-419':'ES','fr-FR':'FR','fr-CA':'FR','de-DE':'DE','it-IT':'IT','pt-BR':'PT-BR','pt-PT':'PT-PT','nl-NL':'NL','pl-PL':'PL','sv-SE':'SV','ja-JP':'JA','hi-IN':'HI','ar':'AR','tr-TR':'TR','id-ID':'ID','ko-KR':'KO','zh-Hans':'ZH-HANS','zh-Hant':'ZH-HANT'}
def placeholders(text):return collections.Counter(re.findall(r'%(?:\d+\$)?[sdf]',text))
def xml_text(text):return escape(text).replace("'","\\'").replace('"','\\"').replace('\n','\\n')
def render(strings,plurals):
 return '<resources>\n'+''.join(f'<string name="{k}">{xml_text(v)}</string>\n' for k,v in strings.items())+''.join('<plurals name="'+k+'">'+''.join('<item quantity="'+q+'">'+xml_text(v)+'</item>' for q,v in forms.items())+'</plurals>\n' for k,forms in plurals.items())+'</resources>\n'
def validate(source,translation):
 if not isinstance(translation,str) or not translation.strip():raise ValueError('Missing translation')
 if placeholders(source)!=placeholders(translation):raise ValueError('Placeholder mismatch')
 if 'PrepTrove' in source and 'PrepTrove' not in translation:raise ValueError('Brand terminology must be preserved')
 if re.search(r'%(?!%|(?:\d+\$)?[sdf])',translation):raise ValueError('Unescaped formatting token')
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--translate',action='store_true');parser.add_argument('--approve',choices=LOCALES);args=parser.parse_args()
 source=json.loads((ROOT/'localization/source.json').read_text(encoding='utf-8'))
 plurals=json.loads((ROOT/'localization/plurals.json').read_text(encoding='utf-8'))
 safety=set(json.loads((ROOT/'localization/safety-keys.json').read_text(encoding='utf-8')))
 combined={**source,**{k+'.'+q:v for k,forms in plurals.items() for q,v in forms.items()}}
 queue_dir=ROOT/'localization/queues';queue_dir.mkdir(exist_ok=True);report=[];available=[]
 for locale in LOCALES:
  file=queue_dir/(locale+'.json');previous=json.loads(file.read_text(encoding='utf-8')) if file.exists() else {}
  english=locale.startswith('en-');items={}
  for k,v in combined.items():
   old=previous.get('strings',{}).get(k,{})
   if old.get('source')!=v:old={}
   items[k]={'source':v,'translation':v if english else old.get('translation'),'reviewed':english or old.get('reviewed',False),'safetyReviewRequired':k in safety}
   if locale in ['en-US','en-AU']:
    if locale=='en-US':items[k]['translation']=v.replace('Organise','Organize').replace('organise','organize').replace('organised','organized').replace('localised','localized').replace('litres','liters')
  queue={'locale':locale,'reviewer':previous.get('reviewer',''),'qualifiedSafetyReview':previous.get('qualifiedSafetyReview',False),'strings':items}
  if args.translate and not english:
   token=os.environ.get('DEEPL_AUTH_KEY');assert token,'DEEPL_AUTH_KEY must be configured locally'
   for k,item in items.items():
    if item['translation'] or item['safetyReviewRequired']:continue
    text=item['source'];tokens=list(placeholders(text).elements())
    for n,token_value in enumerate(tokens):text=text.replace(token_value,f'<x id="{n}"/>',1)
    endpoint=os.environ.get('DEEPL_ENDPOINT','https://api-free.deepl.com/v2/translate')
    assert endpoint in ['https://api-free.deepl.com/v2/translate','https://api.deepl.com/v2/translate'],'Use an official DeepL HTTPS endpoint'
    request=urllib.request.Request(endpoint,data=json.dumps({'text':[text],'source_lang':'EN','target_lang':TARGET[locale],'tag_handling':'xml','ignore_tags':['x']}).encode(),headers={'Authorization':'DeepL-Auth-Key '+token,'Content-Type':'application/json'})
    result=json.load(urllib.request.urlopen(request,timeout=30))['translations'][0]['text']
    for n,token_value in enumerate(tokens):result=re.sub(r'<x\s+id="'+str(n)+r'"\s*/>',lambda _:token_value,result)
    validate(item['source'],result);item['translation']=result;item['reviewed']=False
    file.write_text(json.dumps(queue,ensure_ascii=False,indent=2),encoding='utf-8')
  if args.approve==locale:
   assert queue['reviewer'].strip(),'Record the human reviewer in the queue file'
   assert all(item['reviewed'] for item in items.values()),'All strings require review'
   assert queue['qualifiedSafetyReview'] or not safety,'Qualified safety review is required'
   for item in items.values():validate(item['source'],item['translation'])
  complete=english or (bool(queue['reviewer']) and queue['qualifiedSafetyReview'] and all(i['reviewed'] and i['translation'] for i in items.values()))
  out=ROOT/'app/src/main/res'/('values-b+'+locale.replace('-','+'))/'strings.xml'
  if complete:
   for item in items.values():validate(item['source'],item['translation'])
   out.parent.mkdir(exist_ok=True)
   translated={k:items[k]['translation'] for k in source};translated_plurals={k:{q:items[k+'.'+q]['translation'] for q in forms} for k,forms in plurals.items()}
   out.write_text(render(translated,translated_plurals),encoding='utf-8');available.append(locale)
  elif out.exists():out.unlink() # stale or unreviewed packs must not remain shippable
  file.write_text(json.dumps(queue,ensure_ascii=False,indent=2),encoding='utf-8')
  report.append({'locale':locale,'strings':len(items),'translated':sum(bool(i['translation']) for i in items.values()),'reviewed':sum(bool(i['reviewed']) for i in items.values()),'resourceStatus':'generated' if complete else 'pending','releaseReady':False,'reviewType':'source English variant' if english else 'human review required'})
 (ROOT/'localization/report.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
 (ROOT/'app/src/main/java/com/preptrove/app/ApprovedLanguages.kt').write_text('package com.preptrove.app\ninternal val approvedResourceLocales=listOf('+','.join('"'+s+'"' for s in available)+')\n',encoding='utf-8')
 print('Prepared',len(LOCALES),'locale queues;',len(available),'resource packs available. Store release review remains required.')
if __name__=='__main__':main()
