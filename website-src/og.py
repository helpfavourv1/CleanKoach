#!/usr/bin/env python3
"""Renders 1200x630 Open Graph images: assets/og/<slug>.png (English) and assets/og/<lang>/<slug>.png.
Non-Latin scripts need Noto fonts from the @fontsource npm packages: set FONTS_DIR to a folder containing node_modules/@fontsource.
Usage: og.py [lang ...]  (default: every language that has content)"""
import json, os, sys, re
from pathlib import Path
from playwright.sync_api import sync_playwright
ROOT = Path(__file__).parent; OUT = ROOT.parent / "website"
SITE = json.load(open(ROOT / "site.json", encoding="utf8")); LANGS = {l["code"]: l for l in SITE["languages"]}; LANGS.update({l["folder"]: l for l in SITE["languages"]})
FONTS = Path(os.environ.get("FONTS_DIR", "/tmp/claude-0/-home-claude/31c370b5-549d-5474-baae-8a84eca4937b/scratchpad/fonts")) / "node_modules/@fontsource"
SCRIPT = {"ar": "arabic", "fa": "arabic", "ur": "arabic", "he": "hebrew", "th": "thai", "hi": "devanagari", "mr": "devanagari", "bn": "bengali",
          "ta": "tamil", "te": "telugu", "gu": "gujarati", "ml": "malayalam", "ja": "jp", "ko": "kr", "zh-CN": "sc", "zh-TW": "tc"}
font = (OUT / "assets/fonts/PlusJakartaSans.ttf").resolve().as_uri()
icon = (OUT / "assets/favicon-512.png").resolve().as_uri()

def script_css(lang):
    pkg = SCRIPT.get(lang)
    if not pkg: return "", "J"
    d = FONTS / f"noto-sans-{pkg}"
    css = (d / "700.css").read_text(encoding="utf8").replace("./files/", (d / "files").resolve().as_uri() + "/")
    fam = re.search(r"font-family:\s*'([^']+)'", css).group(1)
    return css, f"'{fam}'"

def card(lang, title, tagline):
    css, fam = script_css(lang); rtl = LANGS[lang]["dir"] == "rtl"
    side = "right" if rtl else "left"
    return f'''<html lang="{LANGS[lang]["hreflang"]}" dir="{LANGS[lang]["dir"]}"><head><style>
@font-face{{font-family:J;src:url("{font}");font-weight:200 800}}
{css}
body{{margin:0;width:1200px;height:630px;font-family:{fam},J,sans-serif;color:#fff;background:linear-gradient(135deg,#10151B 0%,#0A0E13 100%);position:relative;overflow:hidden}}
.g1{{position:absolute;left:-180px;bottom:-220px;width:700px;height:700px;border-radius:50%;background:radial-gradient(circle,rgba(232,127,88,.45),transparent 65%)}}
.g2{{position:absolute;right:-200px;top:-240px;width:760px;height:760px;border-radius:50%;background:radial-gradient(circle,rgba(111,163,255,.40),transparent 65%)}}
.w{{position:absolute;{side}:80px;top:70px;display:flex;align-items:center;gap:18px;font-family:J,sans-serif;font-size:42px;font-weight:800;letter-spacing:-1.5px;direction:ltr}}
.w img{{width:64px;height:64px;border-radius:16px}} .c{{color:#E87F58}} .k{{color:#6FA3FF}}
h1{{position:absolute;left:80px;right:80px;top:200px;margin:0;font-size:{74 if SCRIPT.get(lang) else 84}px;line-height:1.18;font-weight:700;letter-spacing:{0 if SCRIPT.get(lang) else -3}px}}
p{{position:absolute;left:80px;right:80px;bottom:70px;margin:0;font-size:32px;font-weight:500;color:rgba(255,255,255,.72)}}
</style></head><body><div class="g1"></div><div class="g2"></div>
<div class="w"><img src="{icon}"><span><span class="c">Clean</span><span class="k">Koach</span></span></div>
<h1>{title}</h1><p>{tagline}</p></body></html>'''

def main():
    want = sys.argv[1:] or [d.name for d in (ROOT / "content").iterdir() if d.is_dir()]
    with sync_playwright() as p:
        b = p.chromium.launch(); pg = b.new_page(viewport={"width": 1200, "height": 630})
        def shot(lang, title, tagline, path):
            tmp = ROOT / "_og_tmp.html"; tmp.write_text(card(lang, title, tagline), encoding="utf8")
            pg.goto(tmp.resolve().as_uri()); pg.wait_for_timeout(700); tmp.unlink()
            path.parent.mkdir(parents=True, exist_ok=True); pg.screenshot(path=str(path)); print("og", path.relative_to(OUT))
        if "en" in want:
            shot("en", "Free up phone storage on Android", "Large files, duplicates, similar photos. 42 languages.", OUT / "assets/og/site.png")
        for lang in want:
            for f in sorted((ROOT / "content" / lang).glob("*.json")):
                if f.name == "ui.json": continue
                a = json.load(open(f, encoding="utf8"))
                path = OUT / "assets/og" / (f"{a['slug']}.png" if lang == "en" else f"{LANGS[lang]['folder']}/{a['slug']}.png")
                shot(lang, a["h1"], a["tagline"], path)
if __name__ == "__main__": main()
