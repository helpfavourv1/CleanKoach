#!/usr/bin/env python3
"""Translation workflow.
  tr_tool.py dump            -> translations/_en.json (flat, keyed template of every translatable string)
  tr_tool.py apply <lang>    -> reads translations/<lang>.json, writes content/<lang>/ (ui.json + every article), runs checks
  tr_tool.py glossary <lang> -> prints the app's own terms for that language (values-xx/strings.xml)
Keys are readable paths (title, sec.<id>.html, faq.3.a, ui.nav_features ...) so a translation can never drift out of alignment."""
import json, re, sys, copy
from pathlib import Path
ROOT = Path(__file__).parent
EN = ROOT / "content" / "en"
SITE = json.load(open(ROOT / "site.json", encoding="utf8"))
APP_RES = ROOT.parent / "app/src/main/res"
GLOSS = ["tool_large_files_title", "tool_duplicates_title", "tool_similar_title", "tool_screenshots_title", "tool_photo_optimizer_title",
         "tool_video_optimizer_title", "tool_activity_monitor_title", "tool_wifi_security_title", "tool_network_speed_title",
         "home_button_cleanup", "home_ring_caption"]

def flatten(slug_json):
    """Return ordered dict key -> string for one article."""
    a = slug_json; out = {}
    for k in ("title", "h1", "meta_description", "tagline", "quick_answer", "intro"): out[k] = a[k]
    for i, k in enumerate(a["keywords"]): out[f"keywords.{i}"] = k
    if "hero" in a:
        out["hero.alt"] = a["hero"]["alt"]; out["hero.caption"] = a["hero"]["caption"]
    for s in a["sections"]:
        p = f"sec.{s['id']}"
        out[p + ".h2"] = s["h2"]
        if "html" in s: out[p + ".html"] = s["html"]
        if "image" in s:
            out[p + ".image.alt"] = s["image"]["alt"]; out[p + ".image.caption"] = s["image"]["caption"]
        tabs = ([s["table"]] if "table" in s else []) + s.get("tables", [])
        for ti, t in enumerate(tabs):
            q = f"{p}.table{ti}"
            out[q + ".caption"] = t["caption"]
            for i, h in enumerate(t["headers"]): out[f"{q}.h.{i}"] = h
            for r, row in enumerate(t["rows"]):
                for c, cell in enumerate(row): out[f"{q}.r.{r}.{c}"] = cell
        for i, (slug, text) in enumerate(s.get("see", [])): out[f"{p}.see.{i}"] = text
    for i, f in enumerate(a["faqs"]):
        out[f"faq.{i}.q"] = f["q"]; out[f"faq.{i}.a"] = f["a"]
    for i, c in enumerate(a["citations"]): out[f"cite.{i}.note"] = c["note"]
    return out

def rebuild(en, tr):
    a = copy.deepcopy(en)
    g = lambda k: tr[k]
    for k in ("title", "h1", "meta_description", "tagline", "quick_answer", "intro"): a[k] = g(k)
    a["keywords"] = [g(f"keywords.{i}") for i in range(len(en["keywords"]))]
    if "hero" in a: a["hero"]["alt"] = g("hero.alt"); a["hero"]["caption"] = g("hero.caption")
    for s in a["sections"]:
        p = f"sec.{s['id']}"; s["h2"] = g(p + ".h2")
        if "html" in s: s["html"] = g(p + ".html")
        if "image" in s: s["image"]["alt"] = g(p + ".image.alt"); s["image"]["caption"] = g(p + ".image.caption")
        tabs = ([s["table"]] if "table" in s else []) + s.get("tables", [])
        for ti, t in enumerate(tabs):
            q = f"{p}.table{ti}"; t["caption"] = g(q + ".caption")
            t["headers"] = [g(f"{q}.h.{i}") for i in range(len(t["headers"]))]
            t["rows"] = [[g(f"{q}.r.{r}.{c}") for c in range(len(row))] for r, row in enumerate(t["rows"])]
        if "see" in s: s["see"] = [[slug, g(f"{p}.see.{i}")] for i, (slug, _) in enumerate(s["see"])]
    for i, f in enumerate(a["faqs"]): f["q"] = g(f"faq.{i}.q"); f["a"] = g(f"faq.{i}.a")
    for i, c in enumerate(a["citations"]): c["note"] = g(f"cite.{i}.note")
    return a

def skeleton(s): return re.findall(r"<[^>]+>", s)

def dump():
    out = {}
    ui = json.load(open(EN / "ui.json", encoding="utf8"))
    for k, v in ui.items(): out["ui." + k] = v
    for f in sorted(EN.glob("*.json")):
        if f.name == "ui.json": continue
        a = json.load(open(f, encoding="utf8"))
        for k, v in flatten(a).items(): out[f"{a['slug']}::{k}"] = v
    (ROOT / "translations/_en.json").write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding="utf8")
    print(len(out), "strings")

def gloss(lang):
    folder = {"en": "values", "id": "values-in", "in": "values-in", "he": "values-iw", "pt-BR": "values-pt-rBR"}.get(lang, f"values-{lang.replace('-CN', '-rCN').replace('-TW', '-rTW')}")
    p = APP_RES / folder / "strings.xml"
    if not p.exists(): p = APP_RES / "values" / "strings.xml"
    s = p.read_text(encoding="utf8")
    return {k: (re.search(rf'<string name="{k}">(.*?)</string>', s, re.S) or [0, ""])[1] for k in GLOSS}

def apply(lang):
    tr = json.load(open(ROOT / "translations" / f"{lang}.json", encoding="utf8"))
    en_all = json.load(open(ROOT / "translations/_en.json", encoding="utf8"))
    problems = []
    for k, v in en_all.items():
        if k not in tr: problems.append(f"missing {k}")
    for k in tr:
        if k not in en_all: problems.append(f"extra {k}")
    for k, v in tr.items():
        if k in en_all and skeleton(en_all[k]) != skeleton(v): problems.append(f"markup differs {k}")
    if problems:
        print("\n".join(problems[:40])); sys.exit(1)
    out = ROOT / "content" / lang; out.mkdir(parents=True, exist_ok=True)
    ui = {k[3:]: v for k, v in tr.items() if k.startswith("ui.")}
    (out / "ui.json").write_text(json.dumps(ui, ensure_ascii=False, indent=1), encoding="utf8")
    for f in sorted(EN.glob("*.json")):
        if f.name == "ui.json": continue
        en = json.load(open(f, encoding="utf8"))
        sub = {k.split("::", 1)[1]: v for k, v in tr.items() if k.startswith(en["slug"] + "::")}
        (out / f.name).write_text(json.dumps(rebuild(en, sub), ensure_ascii=False, indent=1), encoding="utf8")
    print("wrote content/" + lang)

if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "dump": dump()
    elif cmd == "apply": apply(sys.argv[2])
    elif cmd == "glossary": print(json.dumps(gloss(sys.argv[2]), ensure_ascii=False, indent=1))
