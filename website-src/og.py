#!/usr/bin/env python3
"""Renders 1200x630 Open Graph images for each article from its English title."""
import json, sys
from pathlib import Path
from playwright.sync_api import sync_playwright
ROOT = Path(__file__).parent; OUT = ROOT.parent / "website"
font = (OUT / "assets/fonts/PlusJakartaSans.ttf").resolve().as_uri()
icon = (OUT / "assets/favicon-512.png").resolve().as_uri()
def card(title, tagline):
    return f'''<html><head><style>
@font-face{{font-family:J;src:url("{font}");font-weight:200 800}}
body{{margin:0;width:1200px;height:630px;font-family:J,sans-serif;color:#fff;background:linear-gradient(135deg,#10151B 0%,#0A0E13 100%);position:relative;overflow:hidden}}
.g1{{position:absolute;left:-180px;bottom:-220px;width:700px;height:700px;border-radius:50%;background:radial-gradient(circle,rgba(232,127,88,.45),transparent 65%)}}
.g2{{position:absolute;right:-200px;top:-240px;width:760px;height:760px;border-radius:50%;background:radial-gradient(circle,rgba(111,163,255,.40),transparent 65%)}}
.w{{position:absolute;left:80px;top:70px;display:flex;align-items:center;gap:18px;font-size:42px;font-weight:800;letter-spacing:-1.5px}}
.w img{{width:64px;height:64px;border-radius:16px}} .c{{color:#E87F58}} .k{{color:#6FA3FF}}
h1{{position:absolute;left:80px;right:80px;top:210px;margin:0;font-size:84px;line-height:1.04;font-weight:800;letter-spacing:-3px}}
p{{position:absolute;left:80px;bottom:70px;margin:0;font-size:34px;font-weight:500;color:rgba(255,255,255,.72)}}
</style></head><body><div class="g1"></div><div class="g2"></div>
<div class="w"><img src="{icon}"><span><span class="c">Clean</span><span class="k">Koach</span></span></div>
<h1>{title}</h1><p>{tagline}</p></body></html>'''
with sync_playwright() as p:
    b = p.chromium.launch(); pg = b.new_page(viewport={"width":1200,"height":630})
    tmp = ROOT/"_og_tmp.html"; tmp.write_text(card("Free up phone storage on Android", "Large files, duplicates, similar photos. 42 languages."), encoding="utf8"); pg.goto(tmp.resolve().as_uri()); pg.wait_for_timeout(400); tmp.unlink()
    pg.screenshot(path=str(OUT/"assets/og"/"site.png")); print("og site")
    for f in sorted((ROOT/"content/en").glob("*.json")):
        if f.name == "ui.json": continue
        a = json.load(open(f, encoding="utf8"))
        tmp = ROOT/"_og_tmp.html"; tmp.write_text(card(a["h1"], a["tagline"]), encoding="utf8"); pg.goto(tmp.resolve().as_uri()); pg.wait_for_timeout(400); tmp.unlink()
        pg.screenshot(path=str(OUT/"assets/og"/f"{a['slug']}.png"))
        print("og", a["slug"])
