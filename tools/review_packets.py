"""Offline translator handoff; never translates private records or approves a language.

Export: python tools/review_packets.py
Import: python tools/review_packets.py --import-csv FILE --locale fr-FR
Imports validate every row before writing and always clear review on changed text.
"""
import argparse
import csv
import io
import json
import zipfile
from pathlib import Path
from localize import ROOT, LOCALES, validate


def import_translations(queue, text):
    reader = csv.DictReader(io.StringIO(text))
    if reader.fieldnames != ['locale', 'key', 'source', 'translation', 'safetyReviewRequired']:
        raise ValueError('Unexpected review CSV columns')
    changes = {}
    for row in reader:
        key = row['key']
        if row['locale'] != queue['locale'] or key not in queue['strings'] or key in changes:
            raise ValueError('Wrong locale, unknown or duplicate key: ' + key)
        item = queue['strings'][key]
        if row['source'] != item['source']:
            raise ValueError('Stale source: ' + key)
        translated = row['translation']
        if translated is None or len(translated) > 40000:
            raise ValueError('Missing or oversized CSV field: ' + key)
        if translated.strip():
            validate(item['source'], translated)
        changes[key] = translated or None
    # Work on a copy: failure above cannot partially change the caller's queue.
    result = json.loads(json.dumps(queue))
    for key, translated in changes.items():
        item = result['strings'][key]
        if translated != item.get('translation'):
            item.update(translation=translated, reviewed=False, origin='review-file-import')
            result['reviewer'] = ''
            result['qualifiedSafetyReview'] = False
    return result


def listing_source(root):
    listing = json.loads((root / 'release/listing-en-US.json').read_text(encoding='utf-8'))
    result = {key: listing[key] for key in ['displayTitle', 'shortDescription', 'longDescription']}
    for index, bullet in enumerate(listing['featureBullets'], 1):
        result['featureBullet.' + str(index)] = bullet
    result['keywords'] = listing['keywords']
    return result


def refresh_store_queues(root):
    source = listing_source(root)
    for locale in LOCALES:
        path = root / 'localization/store-queues' / (locale + '.json')
        previous = json.loads(path.read_text(encoding='utf-8'))
        # The original minimal title/description/disclaimer brief was not a full
        # listing. Changed source invalidates its translations and review identity.
        same = previous.get('source') == source
        queue = {**previous, 'source': source,
                 'translation': previous.get('translation') if same else None,
                 'reviewer': previous.get('reviewer') if same else None,
                 'status': 'pending listing-locale eligibility and human review'}
        path.write_text(json.dumps(queue, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def export_packets(root, output):
    output.mkdir(parents=True, exist_ok=True)
    summary = []
    for locale in LOCALES:
        queue = json.loads((root / 'localization/queues' / (locale + '.json')).read_text(encoding='utf-8'))
        with (output / (locale + '.csv')).open('w', encoding='utf-8-sig', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['locale', 'key', 'source', 'translation', 'safetyReviewRequired'])
            for key, item in queue['strings'].items():
                writer.writerow([locale, key, item['source'], item.get('translation') or '', item['safetyReviewRequired']])
        for area in ['store-queues', 'voice-queues']:
            source = root / 'localization' / area / (locale + '.json')
            (output / (locale + '.' + area + '.json')).write_bytes(source.read_bytes())
        items = list(queue['strings'].values())
        summary.append({'locale': locale, 'requiredForms': len(items),
                        'translated': sum(bool(item.get('translation')) for item in items),
                        'reviewed': sum(item.get('reviewed') is True for item in items),
                        'safetyReviewRequired': sum(item['safetyReviewRequired'] for item in items),
                        'reviewer': queue.get('reviewer') or None,
                        'qualifiedSafetyReview': queue.get('qualifiedSafetyReview') is True})
    (output / 'manifest.json').write_text(json.dumps({'kind': 'review_material_only', 'locales': summary}, indent=2), encoding='utf-8')
    (output / 'README.txt').write_text(
        'PrepTrove localisation review material\n\n'
        'These packets contain public product copy only. They are not installable language packs.\n'
        'CSV files use UTF-8 with BOM. Keep locale, key and source fields unchanged.\n'
        'Fill the translation column; do not change Android printf placeholders.\n'
        'Some plural categories deliberately share English context but need distinct target grammar.\n'
        'Import using python tools/review_packets.py --import-csv FILE --locale TAG.\n'
        'Changed text clears earlier review. Import does not approve a language.\n'
        'Store and voice JSON files have separate source, translation and review fields.\n'
        'AI linguistic review is identified in the source queues; it is not human or specialist approval.\n'
        'Safety-tagged text needs qualified review before release.\n'
        'Real device, speech-engine, RTL, typography and listing eligibility checks remain required.\n', encoding='utf-8')
    # Fixed allowlist avoids accidentally including a reviewer's unrelated files.
    names = ['manifest.json', 'README.txt'] + [locale + suffix for locale in LOCALES
            for suffix in ['.csv', '.store-queues.json', '.voice-queues.json']]
    with zipfile.ZipFile(output.parent / 'localization-review.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for name in names:
            archive.write(output / name, name)
    return summary


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--import-csv', type=Path)
    parser.add_argument('--locale', choices=LOCALES)
    args = parser.parse_args()
    if args.import_csv:
        if not args.locale:
            parser.error('--locale is required for import')
        if args.import_csv.stat().st_size > 5 * 1024 * 1024:
            parser.error('CSV must be at most 5 MiB')
        path = ROOT / 'localization/queues' / (args.locale + '.json')
        queue = json.loads(path.read_text(encoding='utf-8'))
        updated = import_translations(queue, args.import_csv.read_text(encoding='utf-8-sig'))
        path.write_text(json.dumps(updated, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    refresh_store_queues(ROOT)
    result = export_packets(ROOT, ROOT / 'artifacts/localization-review')
    print('Prepared', len(result), 'locale packets; no language or safety approval granted.')


if __name__ == '__main__':
    main()
