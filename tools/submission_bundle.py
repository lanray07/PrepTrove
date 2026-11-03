"""Assemble an unsigned Amazon review bundle. Never signs, uploads or publishes.

Run after Gradle validation: python tools/submission_bundle.py
Only explicitly allowlisted public assets/documents enter artifacts/amazon-review.zip.
"""
from pathlib import Path
from asset_provenance import production,capture
import datetime
import hashlib
import json
import os
import re
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def validate_release_manifest(text):
    if 'QaDocumentProvider' in text or '.test.documents' in text:
        raise ValueError('Test document provider must never be included in the release APK')


def validate_release_resources(text):
    if 'localization_qa_marker' in text:
        raise ValueError('Draft localisation QA resources must never be included in the release APK')


def validate_badging(text):
    """Fail closed if a wrong variant, debug APK or unexpected capability is bundled."""
    required = ["package: name='com.preptrove.app'", "sdkVersion:'26'",
                "launchable-activity: name='com.preptrove.app.MainActivity'",
                "uses-feature: name='android.hardware.touchscreen'"]
    if not all(value in text for value in required):
        raise ValueError('Release APK package, SDK, launcher or touchscreen requirement differs')
    if 'application-debuggable' in text:
        raise ValueError('Debuggable APK cannot be included in a submission review package')
    forbidden = ['android.permission.ACCESS_FINE_LOCATION', 'android.permission.READ_CONTACTS',
                 'android.permission.READ_EXTERNAL_STORAGE', 'android.permission.MANAGE_EXTERNAL_STORAGE']
    if any(value in text for value in forbidden):
        raise ValueError('APK contains an unexpected sensitive permission; review disclosures')
    permissions = re.findall(r"uses-permission: name='([^']+)'", text)
    allowed = {'android.permission.RECORD_AUDIO', 'android.permission.POST_NOTIFICATIONS',
               'android.permission.WAKE_LOCK', 'android.permission.ACCESS_NETWORK_STATE',
               'android.permission.RECEIVE_BOOT_COMPLETED', 'android.permission.FOREGROUND_SERVICE',
               'com.preptrove.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'}
    if set(permissions) - allowed:
        raise ValueError('APK permissions changed; review the allowlist and privacy declarations')
    target = re.search(r"targetSdkVersion:'(\d+)'", text)
    if not target or int(target[1]) < 36:
        raise ValueError('Unexpected target SDK')
    return {'applicationId': 'com.preptrove.app', 'minSdk': 26,
            'targetSdk': int(target[1]), 'debuggable': False,
            'permissions': permissions}


def validate_screenshots(root, manifest, locales=None, directory="assets/store/screenshots"):
    locales=locales or ["en-US","en-GB","en-AU"]
    paths = []
    for screen in manifest['screens']:
        path = root / screen['file']
        if not path.resolve().is_relative_to((root / directory).resolve()):
            raise ValueError('Screenshot escapes the allowlisted directory')
        if path.suffix != '.png' or digest(path) != screen['sha256']:
            raise ValueError('Screenshot missing or modified since capture: ' + screen['file'])
        paths.append(path)
    for locale in locales:
        if len([s for s in manifest['screens'] if s['locale'] == locale]) != 8:
            raise ValueError('Expected eight images per approved resource locale')
    if {s['locale'] for s in manifest['screens']}!=set(locales) or len(set(paths)) != 8*len(locales) or len(paths) != 8*len(locales):
        raise ValueError('Duplicate or unexpected screenshot inventory')
    return paths


def test_summary(folder):
    files = sorted(folder.rglob('TEST-*.xml'))
    if not files:
        return {'status': 'not_run', 'tests': 0}
    totals = {key: 0 for key in ['tests', 'failures', 'errors', 'skipped']}
    for path in files:
        suite = ET.parse(path).getroot()
        for key in totals:
            totals[key] += int(suite.get(key, 0))
    if totals['tests'] == 0 or totals['failures'] or totals['errors']:
        raise ValueError('Test evidence is empty or failing: ' + str(folder))
    return {'status': 'recorded_pass', **totals,
            'latestResultUtc': datetime.datetime.fromtimestamp(
                max(p.stat().st_mtime for p in files), datetime.timezone.utc).isoformat()}


def main():
    from release_check import check
    apk = ROOT / 'app/build/outputs/apk/release/app-release-unsigned.apk'
    sdk = Path(os.environ.get('ANDROID_HOME') or
               str(Path.home() / 'AppData/Local/Android/Sdk'))
    suffix = '.exe' if os.name == 'nt' else ''
    versions = sorted((sdk / 'build-tools').iterdir(), key=lambda p: tuple(int(n) for n in re.findall(r'\d+', p.name)))
    build_tools = versions[-1]
    def command(args, check_result=True):
        return subprocess.run([str(a) for a in args], check=check_result,
                              capture_output=True, text=True, encoding='utf-8', errors='replace')
    badging = command([build_tools / ('aapt' + suffix), 'dump', 'badging', apk]).stdout
    apk_report = validate_badging(badging)
    release_manifest = command([build_tools / ('aapt' + suffix), 'dump', 'xmltree', apk, 'AndroidManifest.xml']).stdout
    validate_release_manifest(release_manifest)
    resources = command([build_tools / ('aapt' + suffix), 'dump', 'resources', apk]).stdout
    validate_release_resources(resources)
    command([build_tools / ('zipalign' + suffix), '-c', '-P', '16', '4', apk])
    signer = build_tools / ('apksigner.bat' if os.name == 'nt' else 'apksigner')
    signature = command([signer, 'verify', apk], check_result=False)
    if signature.returncode == 0:
        raise ValueError('Expected an unsigned QA APK, but signature verification succeeded')
    # Distinguish an unsigned APK from a broken verifier/tool invocation.
    if 'Missing META-INF/MANIFEST.MF' not in signature.stderr:
        raise ValueError('Unable to establish unsigned APK status: ' + signature.stderr)
    manifest_path = ROOT / 'assets/store/screenshots/manifest.json'
    locales=re.findall(r'"([^"]+)"',(ROOT/'app/src/main/java/com/preptrove/app/ApprovedLanguages.kt').read_text(encoding='utf-8'))
    screenshots = validate_screenshots(ROOT, json.loads(manifest_path.read_text(encoding='utf-8')),locales)
    marketing_manifest=ROOT/'assets/store/marketing/manifest.json'
    panels=validate_screenshots(ROOT,json.loads(marketing_manifest.read_text(encoding='utf-8')),locales,'assets/store/marketing')
    for asset_manifest in [manifest_path,marketing_manifest]:
        data=json.loads(asset_manifest.read_text(encoding='utf-8'))
        if data.get('productionFingerprint')!=production(ROOT) or data.get('captureFingerprint')!=capture(ROOT):
            raise ValueError('Store assets are stale for current production/fixture source: '+asset_manifest.name)
    unit = test_summary(ROOT / 'app/build/test-results/testDebugUnitTest')
    if unit['status'] != 'recorded_pass':
        raise ValueError('Run JVM validation before assembling the review bundle')
    device = test_summary(ROOT / 'app/build/outputs/androidTest-results/connected/debug')
    config_path = ROOT / 'app/build/generated/source/buildConfig/release/com/preptrove/app/BuildConfig.java'
    config = config_path.read_text(encoding='utf-8')
    if not re.search(r'AMAZON_PURCHASES_ENABLED\s*=\s*false\s*;', config):
        raise ValueError('This review workflow requires checkout disabled, as requested by the owner')
    if not re.search(r'LOCALIZATION_QA\s*=\s*false\s*;', config):
        raise ValueError('Localisation QA must be disabled in a submission build')
    remaining = check()
    revision = command(['git', '-c', 'safe.directory=' + ROOT.as_posix(), 'rev-parse', 'HEAD']).stdout.strip()
    dirty = bool(command(['git', '-c', 'safe.directory=' + ROOT.as_posix(), 'status', '--porcelain']).stdout.strip())
    lint_path = ROOT / 'app/build/reports/lint-results-debug.xml'
    lint = {}
    for issue in ET.parse(lint_path).getroot():
        severity = issue.get('severity', 'Unknown')
        lint[severity] = lint.get(severity, 0) + 1
    if lint.get('Error', 0) or lint.get('Fatal', 0):
        raise ValueError('Lint contains release-blocking errors')
    files = [apk, manifest_path, *screenshots, marketing_manifest, *panels,
             ROOT / 'assets/store/icon-512.png', ROOT / 'assets/store/icon-114.png',
             *[ROOT / 'release' / name for name in ['amazon-draft.json', 'listing-en-US.json', 'gates.json', 'amazon-testing-instructions.txt', 'worldwide-validation.json', 'amazon-locales.json']],
             *[ROOT / 'docs' / name for name in ['RELEASE.md', 'QA.md', 'PRIVACY.md', 'DATA-DELETION.md', 'BACKUP.md', 'AMAZON-BILLING.md', 'AMAZON-SUBMISSION.md', 'VOICE.md', 'NOTIFICATIONS.md', 'LOCALISATION.md', 'WORLDWIDE-VALIDATION.md', 'SOURCE-DIRECTORY.md']],
             ROOT / 'README.md', ROOT / 'docs/PUBLISHER-REVIEW.md', *sorted((ROOT/'docs').glob('visual-review-*.json')),
             *[p for p in [ROOT/'release/fire-device-validation.json',ROOT/'release/evidence/TEST-fire-api28.xml'] if p.exists()],
             *sorted((ROOT/'localization/store-queues').glob('*.json')), ROOT / 'localization/report.json', ROOT / 'localization/content-policy.json', ROOT / 'app/src/main/assets/official-sources.json']
    report = {'kind': 'unsigned_review_only', 'productionReady': False, 'signed': False,
              'submitted': False, 'checkoutEnabled': False,
              'buildConfigSha256': digest(config_path),
              'sourceCommit': revision, 'workingTreeDirty': dirty,
              'apk': apk_report, 'apkSha256': digest(apk), 'zipAlignmentVerified': True,
              'testProviderAbsent': True, 'localizationQaResourcesAbsent': True,
              'unitTests': unit, 'deviceTests': device, 'systemUiTests': test_summary(ROOT/'artifacts/system-ui-results'), 'physicalFireTests': test_summary(ROOT/'release/evidence'), 'lint': lint,
              'remaining': remaining, 'files': {p.relative_to(ROOT).as_posix(): digest(p) for p in files}}
    output = ROOT / 'artifacts'
    output.mkdir(exist_ok=True)
    report_bytes = json.dumps(report, indent=2).encode('utf-8')
    (output / 'amazon-review-report.json').write_bytes(report_bytes)
    bundle = output / 'amazon-review.zip'
    with zipfile.ZipFile(bundle, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in files:
            archive.write(path, path.relative_to(ROOT).as_posix())
        archive.writestr('review-report.json', report_bytes)
    (output / 'amazon-review.zip.sha256').write_text(digest(bundle) + '  amazon-review.zip\n', encoding='utf-8')
    print(json.dumps({'bundle': str(bundle), 'sha256': digest(bundle),
                      'productionReady': False, 'remainingGates': len(remaining)}, indent=2))


if __name__ == '__main__':
    main()
