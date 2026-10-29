"""Check Android resources against the source catalogue and report untranslated UI literals."""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
source=json.loads((root/'localization/source.json').read_text(encoding='utf-8'))
resources={e.attrib['name']:e.text for e in ET.parse(root/'app/src/main/res/values/strings.xml').getroot()}
assert set(source)==set(resources),'Source catalogue and Android resource keys differ'
files=list((root/'app/src/main/java').rglob('*.kt'))
missing=[]
literals=[]
for f in files:
 text=f.read_text(encoding='utf-8')
 missing.extend({'file':str(f.relative_to(root)),'key':k} for k in re.findall(r'R\.string\.(\w+)',text) if k not in resources)
 literals.extend({'file':str(f.relative_to(root)),'line':text[:m.start()].count('\n')+1,'text':m.group(1)} for m in re.finditer(r'Text\("([^"\n]+)"',text))
assert not missing,missing
report={'resourceKeys':len(source),'missingKeys':missing,'remainingInlineTextCalls':literals,'releaseReady':False}
(root/'localization/coverage.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'{len(source)} resource keys checked; {len(literals)} inline Text calls require review. Full UI coverage remains pending.')
