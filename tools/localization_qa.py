"""Generate isolated QA resources. Never changes release-approved languages.

Every non-safety entry must have an AI/human linguistic review. Safety-tagged
entries remain source English; screenshots carry a QA-only banner, never marketing.
"""
import json
from pathlib import Path
from localize import ROOT, LOCALES, catalogue, render, validate, resource_folder


def preview_pack(queue, source, plurals, safety_keys=None):
    expected = catalogue(source, plurals, queue['locale'])
    items = queue['strings']
    if set(items) != set(expected):
        raise ValueError('Queue/source key mismatch: ' + queue['locale'])
    values = {}
    safety = []
    for key, text in expected.items():
        item = items[key]
        if item['source'] != text:
            raise ValueError('Stale source: ' + key)
        canonical_safety = (key in safety_keys or key.rsplit('.',1)[0] in safety_keys) if safety_keys is not None else item['safetyReviewRequired']
        if safety_keys is not None and item.get('safetyReviewRequired') is not canonical_safety:
            raise ValueError('Queue safety flag differs from authoritative catalogue: ' + key)
        if canonical_safety:
            values[key] = text
            safety.append(key)
        else:
            if item.get('reviewed') is not True:
                raise ValueError('Non-safety review missing: ' + key)
            validate(text, item.get('translation'))
            values[key] = item['translation']
    strings = {key: values[key] for key in source}
    quantities = {name: {key[len(name)+1:]: value for key, value in values.items()
                         if key.startswith(name + '.')} for name in plurals}
    return strings, quantities, safety


def main():
    source = json.loads((ROOT / 'localization/source.json').read_text(encoding='utf-8'))
    plurals = json.loads((ROOT / 'localization/plurals.json').read_text(encoding='utf-8'))
    safety_keys = set(json.loads((ROOT / 'localization/safety-keys.json').read_text(encoding='utf-8')))
    prepared = {}
    # Validate all packs before writing any build inputs.
    for locale in LOCALES:
        queue = json.loads((ROOT / 'localization/queues' / (locale + '.json')).read_text(encoding='utf-8'))
        prepared[locale] = preview_pack(queue, source, plurals, safety_keys)
    qa_root = ROOT / 'app/src/localizationQa'
    expectations = {}
    for locale, (strings, quantities, safety) in prepared.items():
        folder = qa_root / 'res' / resource_folder(locale)
        legacy = qa_root / 'res' / ('values-b+' + locale.replace('-', '+')) / 'strings.xml'
        if legacy.parent != folder and legacy.exists():
            legacy.unlink()
        folder.mkdir(parents=True, exist_ok=True)
        (folder / 'strings.xml').write_text(render(strings, quantities), encoding='utf-8')
        voice=json.loads((ROOT/'localization/voice-queues'/(locale+'.json')).read_text(encoding='utf-8'))
        expectations[locale] = {'strings': strings, 'plurals': quantities,
                                'typedExpiryCommand': voice['translations']['expiry'][0],
                                'sourceEnglishSafetyKeys': safety}
    marker = qa_root / 'res/values/qa_marker.xml'
    marker.parent.mkdir(parents=True, exist_ok=True)
    marker.write_text('<resources><string name="localization_qa_marker">qa_only_not_for_release</string></resources>', encoding='utf-8')
    assets = qa_root / 'assets'
    assets.mkdir(parents=True, exist_ok=True)
    (assets / 'qa-expectations.json').write_text(json.dumps(expectations, ensure_ascii=False), encoding='utf-8')
    report = {'kind': 'qa_only_not_store', 'releaseApproved': False,
              'safetyContent': 'source English only; no specialist sign-off granted',
              'locales': {locale: {'strings': len(data[0]), 'pluralGroups': len(data[1]),
                                    'sourceEnglishSafetyKeys': data[2]} for locale, data in prepared.items()}}
    output = ROOT / 'artifacts'
    output.mkdir(exist_ok=True)
    (output / 'localization-qa-report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print('Prepared', len(prepared), 'isolated QA packs; release languages unchanged.')


if __name__ == '__main__':
    main()
