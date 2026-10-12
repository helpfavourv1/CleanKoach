#!/usr/bin/env python3
"""check_lang.py <lang>: sanity checks on a translated language against English and the app's own terms."""
import json, re, sys, unicodedata
from pathlib import Path
ROOT = Path(__file__).parent
sys.path.insert(0, str(ROOT))
import tr_tool
lang = sys.argv[1]
en = json.load(open(ROOT / "content/en/best-phone-cleaner-app-android.json", encoding="utf8"))
tr = json.load(open(ROOT / "content" / lang / "best-phone-cleaner-app-android.json", encoding="utf8"))
fe, ft = tr_tool.flatten(en), tr_tool.flatten(tr)
text = " ".join(re.sub(r"<[^>]+>", " ", v) for v in ft.values())
bad = []
BRAND = {"cleankoach", "android", "google", "play", "wi-fi", "pro"}
for k, v in ft.items():
    plain = re.sub(r"<[^>]+>", "", v).strip()
    if lang != "en" and plain.lower() == re.sub(r"<[^>]+>", "", fe[k]).strip().lower() and len(plain.split()) > 3: bad.append(("identical to English", k))
if "42" not in ft["quick_answer"]: bad.append(("42 missing in quick answer", ""))
if "9" not in " ".join(v for k, v in ft.items() if k.startswith("faq.3.")): bad.append(("Android 9 missing in faq 3", ""))
low = unicodedata.normalize("NFC", text.lower())
for k, term in tr_tool.gloss(lang if lang != "en" else "en").items():
    t = unicodedata.normalize("NFC", re.sub(r"[^\w\s]", "", term).lower().strip())
    if t and t not in unicodedata.normalize("NFC", re.sub(r"[^\w\s]", "", low)): bad.append(("app term not used", f"{k}={term}"))
if not (60 <= len(tr["meta_description"]) <= 170 or lang in ("ja", "ko", "zh-CN", "zh-TW", "th")): bad.append(("meta description length", len(tr["meta_description"])))
if len(tr["title"]) > 70: bad.append(("title length", len(tr["title"])))
latin_words = re.findall(r"[A-Za-z]{4,}", text)
nonbrand = [w for w in latin_words if w.lower() not in {"cleankoach", "android", "google", "play", "photo", "optimizer", "video", "clean", "large", "files", "similar", "photos", "wifi", "pro", "settings", "usage", "access", "https", "href", "strong", "sources", "help", "storage", "user", "data", "policy", "console", "your", "device", "free", "storage"}]
from collections import Counter
nonlatin = lang in ("ar","fa","ur","he","th","hi","mr","bn","ta","te","gu","ml","ja","ko","zh-CN","zh-TW","ru","uk","bg","el")
if nonlatin and len(nonbrand) > 35: bad.append(("many Latin words left in a non-Latin script", Counter(nonbrand).most_common(8)))
print(lang, "OK" if not bad else bad)
sys.exit(1 if bad else 0)
