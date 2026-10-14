#!/usr/bin/env python3
"""Site checks: titles, descriptions, canonical, single h1, JSON-LD, internal links, hreflang reciprocity, alt text."""
import re, json, sys
from pathlib import Path
OUT = Path(__file__).parent.parent / "website"
BASE = json.load(open(Path(__file__).parent / "site.json"))["base_url"]
bad = []
pages = [p for p in OUT.rglob("*.html")]
def resolve(href):
    href = href.split("#")[0].split("?")[0]
    if not href: return True
    if href.startswith(BASE): href = href[len(BASE):]
    if not href.startswith("/"): return True
    t = OUT / href.lstrip("/")
    return t.is_file() or (t / "index.html").is_file()
for p in pages:
    s = p.read_text(encoding="utf8"); rel = str(p.relative_to(OUT))
    title = re.search(r"<title>(.*?)</title>", s, re.S).group(1)
    desc = (re.search(r'<meta name="description" content="(.*?)">', s) or [None, ""])[1]
    noindex = 'content="noindex' in s
    if len(title) > 62: bad.append((rel, "title long", len(title)))
    if not noindex and not 70 <= len(desc) <= 165: bad.append((rel, "desc len", len(desc)))
    if s.count("<h1") != 1: bad.append((rel, "h1 count", s.count("<h1")))
    if not noindex and 'rel="canonical"' not in s: bad.append((rel, "no canonical"))
    if "<main" not in s: bad.append((rel, "no main"))
    for b in re.findall(r'<script type="application/ld\+json">(.*?)</script>', s):
        try: json.loads(b.replace("<\\/", "</"))
        except Exception as ex: bad.append((rel, "jsonld", str(ex)))
    for href in re.findall(r'(?:href|src)="([^"]+)"', s):
        if not resolve(href): bad.append((rel, "broken", href))
    for m in re.finditer(r"<img [^>]*>", s):
        if "alt=" not in m.group(0): bad.append((rel, "img no alt"))
    for h in re.findall(r'<link rel="alternate" hreflang="[^"]+" href="([^"]+)"', s):
        t = OUT / h[len(BASE):].lstrip("/")
        t = t if t.is_file() else t / "index.html"
        if not t.is_file(): bad.append((rel, "hreflang target missing", h)); continue
        if "guides" in h and (BASE + ("/" + str(p.parent.relative_to(OUT)) + "/" if str(p.parent.relative_to(OUT)) != "." else "/")) not in t.read_text(encoding="utf8"):
            bad.append((rel, "hreflang not reciprocal", h))
print(len(pages), "pages checked;", "OK" if not bad else bad)
sys.exit(1 if bad else 0)
