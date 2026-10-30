"""Capture real localised app screens using an isolated, disposable Android package.
Usage: python tools/capture_store.py [--skip-build]
Never seeds or clears the primary com.preptrove.app installation.
"""
from pathlib import Path
import argparse,subprocess,os,json,hashlib,shutil,uuid,datetime
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--skip-build',action='store_true');args=p.parse_args()
def run(command,**kwargs):return subprocess.run([str(v) for v in command],cwd=ROOT,check=True,**kwargs)
sdk=Path(os.environ.get('ANDROID_HOME') or str(Path.home()/'AppData/Local/Android/Sdk'));adb=sdk/'platform-tools'/('adb.exe' if os.name=='nt' else 'adb')
if not args.skip_build:run([ROOT/('gradlew.bat' if os.name=='nt' else 'gradlew'),':app:assembleScreenshots',':app:assembleScreenshotsAndroidTest','-PstoreScreenshots=true','--console=plain'])
def one_apk(folder):
 matches=list((ROOT/folder).glob('*.apk'));assert len(matches)==1,'Expected one screenshot APK';return matches[0]
package='com.preptrove.app.storecapture'
run([adb,'install','-r',one_apk('app/build/outputs/apk/screenshots')]);run([adb,'install','-r',one_apk('app/build/outputs/apk/androidTest/screenshots')])
result=run([adb,'shell','am','instrument','-w','-e','class','com.preptrove.app.StoreScreenshotsTest',package+'.test/androidx.test.runner.AndroidJUnitRunner'],capture_output=True,text=True,encoding='utf-8',errors='replace')
print(result.stdout)
assert 'OK (1 test)' in result.stdout and 'FAILURES!!!' not in result.stdout,'Store screenshot instrumentation did not pass'
folder=ROOT/'artifacts'/('store-capture-'+uuid.uuid4().hex);folder.mkdir(parents=True)
run([adb,'pull','/sdcard/Android/data/'+package+'/files/store',folder])
files=list((folder/'store').rglob('*.png'));assert len(files)==24,'Expected eight screens for each of three English resource variants'
manifest=[]
valid_sizes={(800,480),(1024,600),(1280,720),(1280,800),(1920,1080),(1920,1200),(2560,1600)}
for file in sorted(files):
 locale=file.parent.name;target=ROOT/'assets/store/screenshots'/locale/file.name;target.parent.mkdir(parents=True,exist_ok=True)
 with Image.open(file) as image:width,height=image.size
 assert (width,height) in valid_sizes or (height,width) in valid_sizes,'Unsupported Amazon screenshot dimensions'
 shutil.copy2(file,target)
 manifest.append({'file':str(target.relative_to(ROOT)).replace('\\','/'),'locale':locale,'width':width,'height':height,'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'source':'actual isolated app with labelled example records','marketingComposition':False,'reviewApproved':False})
(ROOT/'assets/store/screenshots/manifest.json').write_text(json.dumps({'capturedUtc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'screens':manifest},indent=2),encoding='utf-8')
run([adb,'uninstall',package+'.test']);run([adb,'uninstall',package])
print('Captured and validated 24 real app screens. Human marketing review remains required.')
