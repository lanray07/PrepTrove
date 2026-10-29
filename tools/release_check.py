"""Production preflight. Reports incomplete gates and exits nonzero; does not publish."""
from pathlib import Path
import json,sys
ROOT=Path(__file__).resolve().parents[1]
def check():
 gates=json.loads((ROOT/'release/gates.json').read_text(encoding='utf-8'))
 failures=[key+': '+item['evidence'] for key,item in gates.items() if item.get('status')!='passed']
 source=json.loads((ROOT/'localization/source.json').read_text(encoding='utf-8'))
 plurals=json.loads((ROOT/'localization/plurals.json').read_text(encoding='utf-8'))
 source.update({k+'.'+q:v for k,forms in plurals.items() for q,v in forms.items()})
 from localize import LOCALES,validate
 queue_files=list((ROOT/'localization/queues').glob('*.json'))
 if {p.stem for p in queue_files}!=set(LOCALES):failures.append('Target locale queue set is incomplete')
 for queue_file in sorted((ROOT/'localization/queues').glob('*.json')):
  queue=json.loads(queue_file.read_text(encoding='utf-8'));items=queue['strings']
  if any(items.get(k,{}).get('source')!=v or not items.get(k,{}).get('reviewed') or not items.get(k,{}).get('translation') for k,v in source.items()):failures.append('Translation review incomplete: '+queue['locale'])
  else:
   try:
    for key,value in source.items():validate(value,items[key]['translation'])
   except (ValueError,KeyError):failures.append('Invalid translation placeholders: '+queue['locale'])
  if not queue.get('reviewer') or not queue.get('qualifiedSafetyReview'):failures.append('Release reviewer and safety sign-off missing: '+queue['locale'])
 return failures
if __name__=='__main__':
 failures=check();report={'productionReady':not failures,'remaining':failures}
 (ROOT/'release/readiness.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
 print(json.dumps(report,indent=2));sys.exit(1 if failures else 0)
