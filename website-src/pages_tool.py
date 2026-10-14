#!/usr/bin/env python3
"""Static-page translation units.
  pages_tool.py dump  -> prints unit counts per page and writes pages_tr/_en.json
A page's translatable units are the inner HTML of every h1/h2/h3/p/li in pages/<name>.body.html, in order, plus its <title> and meta description.
pages_tr/<lang>.json = {"<page>": {"title": ..., "desc": ..., "units": [...]}}."""
import re, json, sys, html
from pathlib import Path
ROOT = Path(__file__).parent
PAGES = ["index", "about", "contact", "privacy", "terms"]
UNIT = re.compile(r"<(h1|h2|h3|p|li)\b([^>]*)>(.*?)</\1>", re.S)

def units(html): return [m.group(3) for m in UNIT.finditer(html)]

def inject(html, tr_units):
    it = iter(tr_units)
    return UNIT.sub(lambda m: f"<{m.group(1)}{m.group(2)}>{next(it)}</{m.group(1)}>", html)

def skeleton(s): return re.findall(r"<[^>]+>", s)

def dump():
    out = {}
    for n in PAGES:
        out[n] = {"units": units((ROOT / "pages" / f"{n}.body.html").read_text(encoding="utf8"))}
    (ROOT / "pages_tr").mkdir(exist_ok=True)
    (ROOT / "pages_tr/_en.json").write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding="utf8")
    for n in PAGES: print(n, len(out[n]["units"]))

def validate(lang):
    en = json.load(open(ROOT / "pages_tr/_en.json", encoding="utf8"))
    tr = json.load(open(ROOT / "pages_tr" / f"{lang}.json", encoding="utf8"))
    bad = []
    for n in PAGES:
        a, b = en[n]["units"], tr[n]["units"]
        if len(a) != len(b): bad.append((n, "unit count", len(a), len(b))); continue
        for i, (x, y) in enumerate(zip(a, b)):
            if skeleton(x) != skeleton(y): bad.append((n, i, "markup differs"))
            if x.strip() and not y.strip(): bad.append((n, i, "empty"))
        for k in ("title", "desc"):
            if not tr[n].get(k): bad.append((n, k, "missing"))
    D = str.maketrans("٠١٢٣٤٥٦٧٨٩۰۱۲۳۴۵۶۷۸۹", "01234567890123456789")
    u = lambda n, i: tr[n]["units"][i].translate(D)
    if "42" not in u("index", 20) or "42" not in u("about", 12): bad.append(("42 missing",))
    if "2026" not in u("privacy", 1) or "2026" not in u("terms", 1): bad.append(("date year missing",))
    if "stmakarios@gmail.com" not in u("contact", 1) or "stmakarios@gmail.com" not in u("privacy", 52): bad.append(("email missing",))
    for n in PAGES:
        for k, lim in (("title", 62), ("desc", 165)):
            if len(html.escape(tr[n][k])) > lim: bad.append((n, k, "too long", len(tr[n][k])))
        if len(html.escape(tr[n]["desc"])) < 70: bad.append((n, "desc short", len(tr[n]["desc"])))
        if "CleanKoach" not in tr[n]["title"]: bad.append((n, "brand missing in title"))
    return bad

if __name__ == "__main__":
    if sys.argv[1] == "dump": dump()
    elif sys.argv[1] == "validate":
        b = validate(sys.argv[2]); print("OK" if not b else b); sys.exit(1 if b else 0)
