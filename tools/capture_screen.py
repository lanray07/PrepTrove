"""Save a raw development screenshot only when PrepTrove is the foreground app."""
from pathlib import Path
import argparse,subprocess,os,json,datetime
p=argparse.ArgumentParser();p.add_argument('--name',default='development-screen');args=p.parse_args()
assert args.name.replace('-','').replace('_','').isalnum(),'Use a plain asset name'
adb=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'AppData/Local/Android/Sdk')))/'platform-tools/adb.exe'
state=subprocess.check_output([str(adb),'shell','dumpsys','activity','activities'],text=True,errors='replace')
assert any('com.preptrove.app/' in line and ('mResumedActivity' in line or 'topResumedActivity' in line) for line in state.splitlines()),'Bring PrepTrove to the foreground before capture'
folder=Path(__file__).resolve().parents[1]/'artifacts/screenshots';folder.mkdir(parents=True,exist_ok=True)
output=folder/(args.name+'.png');output.write_bytes(subprocess.check_output([str(adb),'exec-out','screencap','-p']))
manifest=folder/'manifest.json';items=json.loads(manifest.read_text()) if manifest.exists() else []
items.append({'file':output.name,'capturedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'type':'raw development capture','storeApproved':False,'locale':'not asserted; inspect device settings'})
manifest.write_text(json.dumps(items,indent=2));print(output)
