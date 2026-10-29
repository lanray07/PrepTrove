"""Build an unsigned QA release or sign an approved release. Never uploads or publishes.
Production signing secrets are read by apksigner from environment variables.
"""
from pathlib import Path
import argparse,os,subprocess,hashlib,json
from release_check import ROOT,check
p=argparse.ArgumentParser();p.add_argument('--unsigned',action='store_true');args=p.parse_args()
if not args.unsigned:
 failures=check()
 if failures:raise SystemExit('Production packaging blocked. Run python tools/release_check.py for remaining gates.')
 for name in ['PREPTROVE_KEYSTORE_PATH','PREPTROVE_KEYSTORE_PASSWORD','PREPTROVE_KEY_ALIAS','PREPTROVE_KEY_PASSWORD']:
  if not os.environ.get(name):raise SystemExit('Missing signing configuration: '+name)
wrapper=ROOT/('gradlew.bat' if os.name=='nt' else 'gradlew')
subprocess.run([str(wrapper),':app:assembleRelease','--console=plain'],cwd=ROOT,check=True)
apk=ROOT/'app/build/outputs/apk/release/app-release-unsigned.apk'
if not args.unsigned:
 sdk=Path(os.environ['ANDROID_HOME']);candidates=sorted((sdk/'build-tools').glob('*/apksigner'+('.bat' if os.name=='nt' else '')))
 if not candidates:raise SystemExit('apksigner not found in ANDROID_HOME')
 signed=apk.with_name('PrepTrove-release.apk')
 subprocess.run([str(candidates[-1]),'sign','--ks',os.environ['PREPTROVE_KEYSTORE_PATH'],'--ks-key-alias',os.environ['PREPTROVE_KEY_ALIAS'],'--ks-pass','env:PREPTROVE_KEYSTORE_PASSWORD','--key-pass','env:PREPTROVE_KEY_PASSWORD','--out',str(signed),str(apk)],check=True)
 subprocess.run([str(candidates[-1]),'verify','--verbose',str(signed)],check=True);apk=signed
manifest={'file':apk.name,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'signed':not args.unsigned,'published':False}
apk.with_suffix('.manifest.json').write_text(json.dumps(manifest,indent=2));print(json.dumps(manifest,indent=2))
