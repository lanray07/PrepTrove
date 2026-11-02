"""Stable cross-platform fingerprints binding native store captures to their source inputs."""
import hashlib,json
from pathlib import Path

def fingerprint(root,files):
 values={}
 for file in sorted(files):
  data=file.read_bytes()
  if file.suffix in {'.kt','.kts','.xml','.json'}:data=data.replace(b'\r\n',b'\n')
  values[file.relative_to(root).as_posix()]=hashlib.sha256(data).hexdigest()
 return hashlib.sha256(json.dumps(values,sort_keys=True).encode()).hexdigest()

def production(root):
 return fingerprint(root,[p for p in (root/'app/src/main').rglob('*') if p.is_file()]+[root/'app/build.gradle.kts'])

def capture(root):
 base=root/'app/src/androidTest'
 return fingerprint(root,[p for p in (base/'assets/store-fixtures').glob('*.json')]+[base/'java/com/preptrove/app'/name for name in ['StorePanel.kt','StoreScreenshotsTest.kt']])
