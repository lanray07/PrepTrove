"""Production preflight. Reports incomplete gates and exits nonzero; does not publish."""
from pathlib import Path
import json,re,sys
ROOT=Path(__file__).resolve().parents[1]
def gate_failures(gates,purchases_disabled):
 return [key+': '+item['evidence'] for key,item in gates.items()
         if item.get('status')!='passed' and not (
          key=='amazonCatalogueAndVerifiedReceipts' and
          item.get('status')=='deferred_while_disabled' and purchases_disabled)]

def disabled_purchase_build():
 config=ROOT/'app/build/generated/source/buildConfig/release/com/preptrove/app/BuildConfig.java'
 return config.exists() and bool(re.search(r'AMAZON_PURCHASES_ENABLED\s*=\s*false\s*;',config.read_text(encoding='utf-8')))

def check():
 gates=json.loads((ROOT/'release/gates.json').read_text(encoding='utf-8'))
 failures=gate_failures(gates,disabled_purchase_build())
 source=json.loads((ROOT/'localization/source.json').read_text(encoding='utf-8'))
 plurals=json.loads((ROOT/'localization/plurals.json').read_text(encoding='utf-8'))
 from localize import LOCALES,validate,catalogue
 queue_files=list((ROOT/'localization/queues').glob('*.json'))
 if {p.stem for p in queue_files}!=set(LOCALES):failures.append('Target locale queue set is incomplete')
 for queue_file in sorted((ROOT/'localization/queues').glob('*.json')):
  queue=json.loads(queue_file.read_text(encoding='utf-8'));items=queue['strings']
  expected=catalogue(source,plurals,queue['locale'])
  if set(items)!=set(expected) or any(items.get(k,{}).get('source')!=v or items.get(k,{}).get('reviewed') is not True or not items.get(k,{}).get('translation') for k,v in expected.items()):failures.append('Translation review incomplete: '+queue['locale'])
  else:
   try:
    for key,value in expected.items():validate(value,items[key]['translation'])
   except (ValueError,KeyError):failures.append('Invalid translation placeholders: '+queue['locale'])
  if not isinstance(queue.get('reviewer'),str) or not queue['reviewer'].strip() or queue.get('qualifiedSafetyReview') is not True:failures.append('Release reviewer and safety sign-off missing: '+queue['locale'])
 return failures
if __name__=='__main__':
 failures=check();report={'productionReady':not failures,'remaining':failures}
 (ROOT/'release/readiness.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
 print(json.dumps(report,indent=2));sys.exit(1 if failures else 0)
