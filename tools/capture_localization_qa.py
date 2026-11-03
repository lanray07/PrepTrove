"""Build and exercise all draft locales in an isolated app. Never captures store assets."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import zipfile
from PIL import Image
from localization_qa import main as prepare
from localize import ROOT, LOCALES


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--skip-build',action='store_true')
    args=parser.parse_args()
    prepare()
    sdk=Path(os.environ.get('ANDROID_HOME') or str(Path.home()/'AppData/Local/Android/Sdk'))
    adb=sdk/'platform-tools'/('adb.exe' if os.name=='nt' else 'adb')
    def run(command,**kwargs):return subprocess.run([str(x) for x in command],cwd=ROOT,check=True,**kwargs)
    if not args.skip_build:
        run([ROOT/('gradlew.bat' if os.name=='nt' else 'gradlew'),':app:assembleLocalizationQa',':app:assembleLocalizationQaAndroidTest','-PlocalizationQa=true','--no-parallel','--console=plain'])
    def apk(folder):
        files=list((ROOT/folder).glob('*.apk'))
        if len(files)!=1:raise ValueError('Expected one isolated QA APK: '+folder)
        return files[0]
    package='com.preptrove.app.localizationqa'
    app_apk=apk('app/build/outputs/apk/localizationQa')
    test_apk=apk('app/build/outputs/apk/androidTest/localizationQa')
    expected=ROOT/'app/src/localizationQa/assets/qa-expectations.json'
    with zipfile.ZipFile(app_apk) as archive:
        if archive.read('assets/qa-expectations.json')!=expected.read_bytes():
            raise SystemExit('QA APK contains stale translation expectations. Rebuild without --skip-build.')
    trust=run([adb,'shell','dumpsys','trust'],capture_output=True,text=True).stdout
    if 'deviceLocked=1' in trust.split('(current):',1)[-1].split('User ',1)[0]:
        raise SystemExit('Current tablet is locked. Ask the owner to unlock; no device lock is bypassed.')
    run([adb,'install','-r',app_apk])
    run([adb,'install','-r',test_apk])
    result=run([adb,'shell','am','instrument','-w','-e','class','com.preptrove.app.LocalizationQaTest',package+'.test/androidx.test.runner.AndroidJUnitRunner'],capture_output=True,text=True,encoding='utf-8',errors='replace')
    print(result.stdout)
    if 'OK (1 test)' not in result.stdout or 'FAILURES!!!' in result.stdout:
        raise SystemExit('Localisation instrumentation did not pass; app is retained for investigation.')
    output=ROOT/'artifacts/localization-qa-captures'
    output.mkdir(parents=True,exist_ok=True)
    run([adb,'pull','/sdcard/Android/data/'+package+'/files/localization-qa',output])
    folder=output/'localization-qa'
    results=json.loads((folder/'results.json').read_text(encoding='utf-8'))
    if set(results['locales'])!=set(LOCALES):raise ValueError('Locale result set differs')
    screens=[]
    for locale in LOCALES:
        for name in ['01-overview','02-editor-error','03-settings-200percent','04-typed-voice','05-official-links']:
            path=folder/locale/(name+'.png')
            with Image.open(path) as frame:width,height=frame.size
            screens.append({'locale':locale,'file':path.relative_to(ROOT).as_posix(),
                            'width':width,'height':height,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
    manifest={'kind':'qa_only_not_store','releaseApproved':False,'safetyContent':'neutral planner and external official links only; no cached safety recommendations',
              'appApkSha256':hashlib.sha256(app_apk.read_bytes()).hexdigest(),
              'testApkSha256':hashlib.sha256(test_apk.read_bytes()).hexdigest(),
              'expectationsSha256':hashlib.sha256(expected.read_bytes()).hexdigest(),'screens':screens,'results':results}
    (ROOT/'artifacts/localization-qa-capture-manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
    run([adb,'uninstall',package+'.test']);run([adb,'uninstall',package])
    print('Validated',len(screens),'QA captures; primary installation untouched; no store asset approval granted.')


if __name__=='__main__':main()
