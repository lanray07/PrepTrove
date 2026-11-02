"""Prepare a review bundle only with current complete assets; never infer release readiness from CI."""
import json,re
from localize import ROOT
from asset_provenance import production,capture
from submission_bundle import validate_screenshots,main

locales=re.findall(r'"([^"]+)"',(ROOT/'app/src/main/java/com/preptrove/app/ApprovedLanguages.kt').read_text(encoding='utf-8'))
missing=[]
for directory in ['assets/store/screenshots','assets/store/marketing']:
 path=ROOT/directory/'manifest.json'
 if not path.exists():missing.append(directory+': capture manifest missing');continue
 data=json.loads(path.read_text(encoding='utf-8'));provided={x['locale'] for x in data['screens']}
 if not provided.issubset(set(locales)):raise ValueError('Unexpected asset locales: '+directory)
 validate_screenshots(ROOT,data,sorted(provided),directory)
 if provided!=set(locales):missing.append(directory+': missing '+','.join(sorted(set(locales)-provided)))
 if data.get('productionFingerprint')!=production(ROOT) or data.get('captureFingerprint')!=capture(ROOT):missing.append(directory+': source-bound recapture required')
status={'reviewPackagePrepared':not missing,'productionReady':False,'pendingAssetRequirements':missing,'note':'CI build success does not approve a release. Physical-device and owner acceptance remain separate.'}
output=ROOT/'artifacts';output.mkdir(exist_ok=True);(output/'package-status.json').write_text(json.dumps(status,indent=2),encoding='utf-8')
if missing:print(json.dumps(status,indent=2))
else:main()
