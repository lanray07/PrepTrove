"""Create indexed contact sheets from real captures for visual review; grants no approval."""
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]

def main():
    manifest = json.loads((ROOT / 'assets/store/marketing/manifest.json').read_text(encoding='utf-8'))
    output = ROOT / 'artifacts/store-visual-review'
    output.mkdir(parents=True, exist_ok=True)
    locales = sorted({screen['locale'] for screen in manifest['screens']})
    index = []
    for locale in locales:
        screens = sorted((s for s in manifest['screens'] if s['locale'] == locale), key=lambda s: s['file'])
        if len(screens) != 8:
            raise ValueError('Incomplete review inventory: ' + locale)
        sheet = Image.new('RGB', (1920, 2560), '#edf0f2')
        draw = ImageDraw.Draw(sheet)
        for position, screen in enumerate(screens):
            path = ROOT / screen['file']
            if not path.resolve().is_relative_to((ROOT / 'assets/store/marketing').resolve()):
                raise ValueError('Asset escapes marketing directory')
            if hashlib.sha256(path.read_bytes()).hexdigest() != screen['sha256']:
                raise ValueError('Modified capture: ' + screen['file'])
            with Image.open(path) as image:
                if image.size != (1920, 1200):
                    raise ValueError('Unexpected panel dimensions')
                tile = image.convert('RGB').resize((960, 600), Image.Resampling.LANCZOS)
                left, top = (position % 2) * 960, (position // 2) * 640
                draw.text((left + 12, top + 12), locale + ' / ' + path.stem, fill='#162a38')
                sheet.paste(tile, (left, top + 40))
        target = output / (locale + '.jpg')
        sheet.save(target, quality=94)
        index.append({'locale': locale, 'contactSheet': target.relative_to(ROOT).as_posix(),
                      'sourceFiles': [s['file'] for s in screens], 'reviewApproved': False})
    (output / 'index.json').write_text(json.dumps({'kind': 'visual_review_aid_only', 'locales': index}, indent=2), encoding='utf-8')
    print('Prepared', len(index), 'contact sheets. Inspect original images for detailed findings; no approval granted.')

if __name__ == '__main__':
    main()
