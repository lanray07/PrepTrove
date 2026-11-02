"""Capture real localised app screens using an isolated, disposable Android package.
Usage: python tools/capture_store.py [--skip-build]
Never seeds or clears the primary com.preptrove.app installation.
"""
from pathlib import Path
import argparse,subprocess,os,json,hashlib,shutil,uuid,datetime,re
from PIL import Image
from asset_provenance import production,capture
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--skip-build',action='store_true');args=p.parse_args()
def run(command,**kwargs):return subprocess.run([str(v) for v in command],cwd=ROOT,check=True,**kwargs)
sdk=Path(os.environ.get('ANDROID_HOME') or str(Path.home()/'AppData/Local/Android/Sdk'));adb=sdk/'platform-tools'/('adb.exe' if os.name=='nt' else 'adb')
if not args.skip_build:run([ROOT/('gradlew.bat' if os.name=='nt' else 'gradlew'),':app:assembleScreenshots',':app:assembleScreenshotsAndroidTest','-PstoreScreenshots=true','--console=plain'])
def one_apk(folder):
 matches=list((ROOT/folder).glob('*.apk'));assert len(matches)==1,'Expected one screenshot APK';return matches[0]
app_apk=one_apk('app/build/outputs/apk/screenshots');test_apk=one_apk('app/build/outputs/apk/androidTest/screenshots')
provenance={'productionFingerprint':production(ROOT),'captureFingerprint':capture(ROOT),'appApkSha256':hashlib.sha256(app_apk.read_bytes()).hexdigest(),'testApkSha256':hashlib.sha256(test_apk.read_bytes()).hexdigest()}
stamp=ROOT/'artifacts/store-build-fingerprint.json';stamp.parent.mkdir(exist_ok=True)
if args.skip_build:
 assert stamp.exists() and json.loads(stamp.read_text())==provenance,'Stale or unbound screenshot APK; rebuild without --skip-build'
else:stamp.write_text(json.dumps(provenance,indent=2),encoding='utf-8')
package='com.preptrove.app.storecapture'
run([adb,'install','-r',one_apk('app/build/outputs/apk/screenshots')]);run([adb,'install','-r',one_apk('app/build/outputs/apk/androidTest/screenshots')])
result=run([adb,'shell','am','instrument','-w','-e','class','com.preptrove.app.StoreScreenshotsTest',package+'.test/androidx.test.runner.AndroidJUnitRunner'],capture_output=True,text=True,encoding='utf-8',errors='replace')
print(result.stdout)
assert 'OK (1 test)' in result.stdout and 'FAILURES!!!' not in result.stdout,'Store screenshot instrumentation did not pass'
folder=ROOT/'artifacts'/('store-capture-'+uuid.uuid4().hex);folder.mkdir(parents=True)
run([adb,'pull','/sdcard/Android/data/'+package+'/files/store',folder]);run([adb,'pull','/sdcard/Android/data/'+package+'/files/marketing',folder])
locales=re.findall(r'"([^"]+)"',(ROOT/'app/src/main/java/com/preptrove/app/ApprovedLanguages.kt').read_text(encoding='utf-8'))
files=list((folder/'store').rglob('*.png'));assert len(files)==8*len(locales),'Expected eight screens for every approved resource locale'
assert {f.parent.name for f in files}==set(locales),'Captured locale set differs from compiled resources'
manifest=[]
valid_sizes={(800,480),(1024,600),(1280,720),(1280,800),(1920,1080),(1920,1200),(2560,1600)}
for file in sorted(files):
 locale=file.parent.name;target=ROOT/'assets/store/screenshots'/locale/file.name;target.parent.mkdir(parents=True,exist_ok=True)
 with Image.open(file) as image:width,height=image.size
 assert (width,height) in valid_sizes or (height,width) in valid_sizes,'Unsupported Amazon screenshot dimensions'
 shutil.copy2(file,target)
 manifest.append({'file':str(target.relative_to(ROOT)).replace('\\','/'),'locale':locale,'width':width,'height':height,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'source':'actual isolated app with labelled example records','marketingComposition':False,'reviewApproved':False})
panels=[]
for file in sorted((folder/'marketing').rglob('*.png')):
 locale=file.parent.name;target=ROOT/'assets/store/marketing'/locale/file.name;target.parent.mkdir(parents=True,exist_ok=True)
 with Image.open(file) as frame:assert frame.size==(1920,1200),'Marketing panel dimensions differ'
 shutil.copy2(file,target)
 panels.append({'file':target.relative_to(ROOT).as_posix(),'locale':locale,'width':1920,'height':1200,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'source':'native typography around actual fully localized sample UI','reviewApproved':False})
assert len(panels)==len(files),'Marketing panel set incomplete'
(ROOT/'assets/store/marketing/manifest.json').write_text(json.dumps({'capturedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),**provenance,'screens':panels},indent=2),encoding='utf-8')
(ROOT/'assets/store/screenshots/manifest.json').write_text(json.dumps({'capturedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),**provenance,'screens':manifest},indent=2),encoding='utf-8')
run([adb,'uninstall',package+'.test']);run([adb,'uninstall',package])
print('Captured and validated',len(files),'real app screens and',len(panels),'localized marketing panels. Visual review remains required.')
