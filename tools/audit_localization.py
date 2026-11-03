"""Check Android resources against the source catalogue and report untranslated UI literals."""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
source=json.loads((root/'localization/source.json').read_text(encoding='utf-8'))
resources={e.attrib['name']:e.text for e in ET.parse(root/'app/src/main/res/values/strings.xml').getroot() if e.tag=='string'}
assert set(source)==set(resources),'Source catalogue and Android resource keys differ'
files=list((root/'app/src/main/java').rglob('*.kt'))
missing=[]
literals=[]
for f in files:
 text=f.read_text(encoding='utf-8')
 missing.extend({'file':str(f.relative_to(root)),'key':k} for k in re.findall(r'R\.string\.(\w+)',text) if k not in resources)
 literals.extend({'file':str(f.relative_to(root)),'line':text[:m.start()].count('\n')+1,'text':m.group(1)} for m in re.finditer(r'Text\("([^"\n]+)"',text))
assert not missing,missing
# Exact, reviewed non-translatable calls; new or changed literals remain visible.
reviewed={
 ('MainActivity.kt','${java.text.NumberFormat.getNumberInstance(resources.configuration.locales[0]).format(e.quantity)} ${e.unit} \u00b7 ${e.category}'):'Locale-formatted quantity plus original user-entered unit/category; private records are not translated.',
 ('PdfReports.kt','PrepTrove \u00b7 '):'Product name plus punctuation; PDF drawing helper, not untranslated interface prose.'
}
classified=[];unclassified=[]
for item in literals:
 reason=reviewed.get((Path(item['file']).name,item['text']))
 if reason:classified.append({**item,'reason':reason})
 else:unclassified.append(item)
report={'resourceKeys':len(source),'missingKeys':missing,'remainingInlineTextCalls':unclassified,'reviewedInlineTextCalls':classified,'staticInventoryComplete':not missing and not unclassified,'releaseReady':False,'limitation':'Static inventory does not establish rendered-language quality, accessibility or release approval.'}
(root/'localization/coverage.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'{len(source)} resource keys checked; {len(classified)} non-translatable calls reviewed; {len(unclassified)} inline calls remain unclassified. Runtime and release approval are separate.')
