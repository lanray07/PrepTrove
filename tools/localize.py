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
FORMAT_TOKEN=re.compile(r'%%|%(?:\d+\$)?[sdf]')

def placeholders(text):return collections.Counter(token for token in FORMAT_TOKEN.findall(text) if token!='%%')

def catalogue(source,plurals,locale):
 """Keep source forms and add locale-specific cardinal forms for linguistic review.

 Android/CLDR categories, including million-count many forms, are not English
 singular/plural. Extra forms use the English other source as translation context.
 """
 language=locale.split('-')[0]
 extra={'ar':['zero','two','few','many'],'pl':['few','many'],
        'fr':['many'],'es':['many'],'it':['many'],'pt':['many']}.get(language,[])
 return {**source,**{key+'.'+quantity:forms.get(quantity,forms['other'])
         for key,forms in plurals.items() for quantity in dict.fromkeys([*forms,*extra])}}

def queue_complete(queue):
 return (bool(str(queue.get('reviewer') or '').strip()) and queue.get('qualifiedSafetyReview') is True
         and all(item.get('reviewed') is True and item.get('translation') for item in queue['strings'].values()))
def xml_text(text):return '"'+escape(text).replace('\\','\\\\').replace("'","\\'").replace('"','\\"').replace('\n','\\n')+'"'
def resource_folder(locale):return 'values-in-rID' if locale=='id-ID' else 'values-b+'+locale.replace('-','+')
def render(strings,plurals):
 return '<resources>\n'+''.join(f'<string name="{k}">{xml_text(v)}</string>\n' for k,v in strings.items())+''.join('<plurals name="'+k+'">'+''.join('<item quantity="'+q+'">'+xml_text(v)+'</item>' for q,v in forms.items())+'</plurals>\n' for k,forms in plurals.items())+'</resources>\n'
def validate(source,translation):
 if not isinstance(translation,str) or not translation.strip():raise ValueError('Missing translation')
 if placeholders(source)!=placeholders(translation):raise ValueError('Placeholder mismatch')
 if 'PrepTrove' in source and 'PrepTrove' not in translation:raise ValueError('Brand terminology must be preserved')
 if '%' in FORMAT_TOKEN.sub('',translation):raise ValueError('Unescaped formatting token')
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--translate',action='store_true');parser.add_argument('--approve',choices=LOCALES);args=parser.parse_args()
 source=json.loads((ROOT/'localization/source.json').read_text(encoding='utf-8'))
 plurals=json.loads((ROOT/'localization/plurals.json').read_text(encoding='utf-8'))
 safety=set(json.loads((ROOT/'localization/safety-keys.json').read_text(encoding='utf-8')))
 queue_dir=ROOT/'localization/queues';queue_dir.mkdir(exist_ok=True);report=[];available=[]
 for locale in LOCALES:
  combined=catalogue(source,plurals,locale)
  file=queue_dir/(locale+'.json');previous=json.loads(file.read_text(encoding='utf-8')) if file.exists() else {}
  english=locale.startswith('en-');items={}
  for k,v in combined.items():
   old=previous.get('strings',{}).get(k,{})
   if old.get('source')!=v:old={}
   items[k]={'source':v,'translation':v if english else old.get('translation'),'reviewed':english or old.get('reviewed',False),'safetyReviewRequired':k in safety or k.rsplit('.',1)[0] in safety}
   if old.get('origin'):items[k]['origin']=old['origin']
   if locale in ['en-US','en-AU']:
    if locale=='en-US':items[k]['translation']=v.replace('Organise','Organize').replace('organise','organize').replace('organised','organized').replace('localised','localized').replace('litres','liters')
  queue={'locale':locale,'reviewer':previous.get('reviewer',''),'qualifiedSafetyReview':previous.get('qualifiedSafetyReview',False),'strings':items}
  for key,item in items.items():
   if item['translation'] is not None:
    try:validate(item['source'],item['translation'])
    except ValueError as error:raise ValueError(locale+': '+key+': '+str(error)) from error
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
    validate(item['source'],result);item['translation']=result;item['reviewed']=False;item['origin']='DeepL machine translation; linguistic review pending'
    file.write_text(json.dumps(queue,ensure_ascii=False,indent=2),encoding='utf-8')
  if args.approve==locale:
   assert queue['reviewer'].strip(),'Record the reviewer identity and review type in the queue file'
   assert all(item['reviewed'] for item in items.values()),'All strings require review'
   assert queue['qualifiedSafetyReview'] or not safety,'Qualified safety review is required'
   for item in items.values():validate(item['source'],item['translation'])
  complete=english or queue_complete(queue)
  out=ROOT/'app/src/main/res'/resource_folder(locale)/'strings.xml'
  legacy=ROOT/'app/src/main/res'/('values-b+'+locale.replace('-','+'))/'strings.xml'
  if legacy!=out and legacy.exists():legacy.unlink()
  if complete:
   for item in items.values():validate(item['source'],item['translation'])
   out.parent.mkdir(exist_ok=True)
   translated={k:items[k]['translation'] for k in source};translated_plurals={k:{key[len(k)+1:]:item['translation'] for key,item in items.items() if key.startswith(k+'.')} for k in plurals}
   out.write_text(render(translated,translated_plurals),encoding='utf-8');available.append(locale)
  elif out.exists():out.unlink() # stale or unreviewed packs must not remain shippable
  file.write_text(json.dumps(queue,ensure_ascii=False,indent=2),encoding='utf-8')
  review_type='source English variant' if english else ('AI-assisted partial linguistic review' if any('Codex AI' in i.get('origin','') for i in items.values()) else 'linguistic review pending')
  report.append({'locale':locale,'strings':len(items),'translated':sum(bool(i['translation']) for i in items.values()),'reviewed':sum(i['reviewed'] is True for i in items.values()),'resourceStatus':'generated' if complete else 'pending','releaseReady':False,'reviewType':review_type,'reviewer':queue['reviewer'] or None,'qualifiedSafetyReview':queue['qualifiedSafetyReview'] is True})
 (ROOT/'localization/report.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
 (ROOT/'app/src/main/java/com/preptrove/app/ApprovedLanguages.kt').write_text('package com.preptrove.app\ninternal val approvedResourceLocales=listOf('+','.join('"'+s+'"' for s in available)+')\n',encoding='utf-8')
 print('Prepared',len(LOCALES),'locale queues;',len(available),'resource packs available. Store release review remains required.')
if __name__=='__main__':main()
