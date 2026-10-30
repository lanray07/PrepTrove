import hashlib
import tempfile
import unittest
from pathlib import Path
from submission_bundle import validate_badging, validate_screenshots, test_summary, validate_release_manifest


class SubmissionValidation(unittest.TestCase):
    def test_release_rejects_document_test_components(self):
        validate_release_manifest('Application MainActivity')
        for value in ['provider com.preptrove.app.QaDocumentProvider', 'authority com.preptrove.app.test.documents']:
            with self.subTest(value=value), self.assertRaises(ValueError):
                validate_release_manifest(value)
    BADGING = """package: name='com.preptrove.app' versionCode='1'
sdkVersion:'26'
targetSdkVersion:'35'
launchable-activity: name='com.preptrove.app.MainActivity'
uses-feature: name='android.hardware.touchscreen'
uses-permission: name='android.permission.RECORD_AUDIO'
"""

    def test_rejects_wrong_variant_debug_and_unreviewed_permissions(self):
        self.assertFalse(validate_badging(self.BADGING)['debuggable'])
        for invalid in [self.BADGING.replace("name='com.preptrove.app'", "name='com.preptrove.app.storecapture'"),
                        self.BADGING + '\napplication-debuggable',
                        self.BADGING.replace("targetSdkVersion:'35'", "targetSdkVersion:'28'"),
                        self.BADGING.replace("uses-feature: name='android.hardware.touchscreen'", ''),
                        self.BADGING + "\nuses-permission: name='android.permission.CAMERA'"]:
            with self.subTest(invalid=invalid), self.assertRaises(ValueError):
                validate_badging(invalid)

    def test_missing_failed_and_skipped_test_evidence_are_distinct(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            self.assertEqual('not_run', test_summary(root)['status'])
            file = root / 'TEST-suite.xml'
            file.write_text('<testsuite tests="3" failures="0" errors="0" skipped="1"/>')
            result = test_summary(root)
            self.assertEqual(3, result['tests'])
            self.assertEqual(1, result['skipped'])
            file.write_text('<testsuite tests="3" failures="1"/>')
            with self.assertRaises(ValueError):
                test_summary(root)

    def test_screenshot_tampering_and_path_escape_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            screens = []
            for locale in ['en-US', 'en-GB', 'en-AU']:
                for index in range(8):
                    path = root / 'assets/store/screenshots' / locale / (str(index) + '.png')
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_bytes(b'test fixture')
                    screens.append({'file': path.relative_to(root).as_posix(), 'locale': locale,
                                    'sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
            self.assertEqual(24, len(validate_screenshots(root, {'screens': screens})))
            (root / screens[0]['file']).write_bytes(b'changed')
            with self.assertRaises(ValueError):
                validate_screenshots(root, {'screens': screens})
            screens[0]['file'] = '../outside.png'
            with self.assertRaises(ValueError):
                validate_screenshots(root, {'screens': screens})


if __name__ == '__main__':
    unittest.main()
