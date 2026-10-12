#!/usr/bin/env python3
"""Static SEO page generator for the CleanKoach website.
Reads content/<lang>/ui.json and content/<lang>/<slug>.json, writes into ../website/.
Only languages/articles that have content are built; hreflang and sitemaps cover exactly those."""
import json, os, html, sys, shutil
from pathlib import Path

ROOT = Path(__file__).parent
OUT = ROOT.parent / "website"
SITE = json.load(open(ROOT / "site.json", encoding="utf8"))
BASE = SITE["base_url"]
LANGS = {l["code"]: l for l in SITE["languages"]}
e = html.escape

def load(lang):
    d = ROOT / "content" / lang
    if not (d / "ui.json").exists():
        return None
    ui = json.load(open(d / "ui.json", encoding="utf8"))
    arts = {}
    for f in sorted(d.glob("*.json")):
        if f.name == "ui.json":
            continue
        a = json.load(open(f, encoding="utf8"))
        arts[a["slug"]] = a
    return ui, arts

DATA = {c: load(c) for c in LANGS}
DATA = {c: v for c, v in DATA.items() if v}
SLUGS = []
for c in DATA:
    for s in DATA[c][1]:
        if s not in SLUGS:
            SLUGS.append(s)

def prefix(lang):
    return "" if lang == "en" else "/" + LANGS[lang]["folder"]

def art_path(lang, slug): return f"{prefix(lang)}/guides/{slug}/"
def hub_path(lang): return f"{prefix(lang)}/guides/"
def url(path): return BASE + path

def alternates(slug):
    return [(LANGS[c]["hreflang"], url(art_path(c, slug))) for c in DATA if slug in DATA[c][1]]

def alt_tags(slug):
    alts = alternates(slug)
    out = "".join(f'<link rel="alternate" hreflang="{h}" href="{u}">\n    ' for h, u in alts)
    out += f'<link rel="alternate" hreflang="x-default" href="{url(art_path("en", slug))}">'
    return out

def nav(lang, ui, current=None):
    items = [(ui["nav_features"], "/#features", None), (ui["nav_languages"], "/#languages", None),
             (ui["nav_guides"], hub_path(lang), "guides"), (ui["nav_privacy"], "/privacy.html", None),
             (ui["nav_terms"], "/terms.html", None)]
    return '<nav aria-label="Main">' + "".join(
        f'<a href="{h}"' + (' aria-current="page"' if k and k == current else "") + f">{e(t)}</a>" for t, h, k in items) + "</nav>"

def store_button(ui):
    return (f'<a class="store" href="{SITE["play_url"]}" aria-label="{e(ui["get_it_on"])} {e(ui["google_play"])}">'
            f'<img class="store-icon" src="/assets/favicon-192.png" alt="" width="40" height="40">'
            f'<span class="store-text"><small>{e(ui["get_it_on"])}</small><b>{e(ui["google_play"])}</b></span></a>')

def page(lang, ui, *, title, desc, canonical_path, body, head_extra="", og_image, og_type, current=None):
    L = LANGS[lang]
    canon = url(canonical_path)
    return f'''<!DOCTYPE html>
<html lang="{L["hreflang"]}" dir="{L["dir"]}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{e(title)}</title>
    <meta name="description" content="{e(desc)}">
    <meta name="robots" content="index,follow,max-image-preview:large">
    <link rel="canonical" href="{canon}">
    {head_extra}
    <link rel="icon" type="image/svg+xml" href="/assets/favicon.svg">
    <link rel="icon" type="image/png" sizes="32x32" href="/assets/favicon-32.png">
    <link rel="apple-touch-icon" href="/assets/apple-touch-icon.png">
    <meta name="theme-color" content="#FFFFFF">
    <meta property="og:site_name" content="CleanKoach">
    <meta property="og:type" content="{og_type}">
    <meta property="og:title" content="{e(title)}">
    <meta property="og:description" content="{e(desc)}">
    <meta property="og:url" content="{canon}">
    <meta property="og:locale" content="{L["hreflang"].replace("-", "_")}">
    <meta property="og:image" content="{og_image}">
    <meta property="og:image:width" content="1200">
    <meta property="og:image:height" content="630">
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="{e(title)}">
    <meta name="twitter:description" content="{e(desc)}">
    <meta name="twitter:image" content="{og_image}">
    <link rel="stylesheet" href="/style.css">
</head>
<body>
    <div class="container">
        <header>
            <a href="/" class="wordmark"><img src="/assets/favicon-192.png" alt="" width="32" height="32" class="logo"><span><span class="clean">Clean</span><span class="koach">Koach</span></span></a>
            {nav(lang, ui, current)}
        </header>
{body}
        <footer>
            <a href="/privacy.html">{e(ui["footer_privacy"])}</a>
            <a href="/terms.html">{e(ui["footer_terms"])}</a>
            <a href="mailto:stmakarios@gmail.com">{e(ui["footer_support"])}</a>
        </footer>
    </div>
</body>
</html>
'''

def jsonld(objs):
    return "".join('<script type="application/ld+json">' + json.dumps(o, ensure_ascii=False, separators=(",", ":")).replace("</", "<\\/") + "</script>\n    " for o in objs)

def strip(s):
    import re
    return html.unescape(re.sub(r"<[^>]+>", "", s)).strip()

def build_article(lang, slug):
    ui, arts = DATA[lang]
    a = arts[slug]
    path = art_path(lang, slug)
    img = f"{BASE}/assets/og/{slug}.png"
    pub = SITE["published"]
    breadcrumbs = {"@context": "https://schema.org", "@type": "BreadcrumbList", "itemListElement": [
        {"@type": "ListItem", "position": 1, "name": ui["breadcrumb_home"], "item": BASE + "/"},
        {"@type": "ListItem", "position": 2, "name": ui["breadcrumb_guides"], "item": url(hub_path(lang))},
        {"@type": "ListItem", "position": 3, "name": a["h1"], "item": url(path)}]}
    article = {"@context": "https://schema.org", "@type": "Article", "headline": a["title"], "description": a["meta_description"],
               "image": [img], "datePublished": pub, "dateModified": pub, "inLanguage": LANGS[lang]["hreflang"],
               "mainEntityOfPage": url(path), "keywords": ", ".join(a["keywords"]),
               "author": {"@type": "Organization", "name": "CleanKoach", "url": BASE + "/"},
               "publisher": {"@type": "Organization", "name": "CleanKoach", "url": BASE + "/",
                             "logo": {"@type": "ImageObject", "url": BASE + "/assets/favicon-512.png"}}}
    app = {"@context": "https://schema.org", "@type": "SoftwareApplication", "name": "CleanKoach",
           "operatingSystem": "Android 9+", "applicationCategory": "UtilitiesApplication", "url": SITE["play_url"],
           "offers": {"@type": "Offer", "price": "0", "priceCurrency": "USD"}}
    faq = {"@context": "https://schema.org", "@type": "FAQPage", "mainEntity": [
        {"@type": "Question", "name": f["q"], "acceptedAnswer": {"@type": "Answer", "text": f["a"]}} for f in a["faqs"]]}
    head = alt_tags(slug) + "\n    " + jsonld([article, breadcrumbs, faq, app])

    toc = "".join(f'<li><a href="#{s["id"]}">{e(s["h2"])}</a></li>' for s in a["sections"]) + f'<li><a href="#faq">{e(ui["faq"])}</a></li>'
    secs = ""
    for s in a["sections"]:
        secs += f'<section id="{s["id"]}"><h2>{e(s["h2"])}</h2>'
        if "html" in s: secs += s["html"]
        if "table" in s:
            t = s["table"]
            secs += '<div class="table-wrap"><table>' + f'<caption>{e(t["caption"])}</caption><thead><tr>' + "".join(f"<th scope=\"col\">{e(h)}</th>" for h in t["headers"]) + "</tr></thead><tbody>"
            for r in t["rows"]:
                secs += "<tr><th scope=\"row\">" + e(r[0]) + "</th>" + "".join(f"<td>{e(c)}</td>" for c in r[1:]) + "</tr>"
            secs += "</tbody></table></div>"
        secs += "</section>\n"
    faqs = "".join(f'<details><summary>{e(f["q"])}</summary><p>{e(f["a"])}</p></details>' for f in a["faqs"])
    cites = "".join(f'<li><a href="{c["url"]}" rel="noopener" target="_blank">{e(c["title"])}</a>. {e(c["note"])}</li>' for c in a["citations"])
    rel = [s for s in a.get("related", []) if s in arts] or [s for s in arts if s != slug]
    related = ""
    if rel:
        related = f'<section><h2>{e(ui["related"])}</h2><ul class="related">' + "".join(f'<li><a href="{art_path(lang, s)}">{e(arts[s]["h1"])}</a></li>' for s in rel) + "</ul></section>"
    body = f'''        <article class="guide">
            <nav class="crumbs" aria-label="Breadcrumb"><a href="/">{e(ui["breadcrumb_home"])}</a> / <a href="{hub_path(lang)}">{e(ui["breadcrumb_guides"])}</a></nav>
            <h1>{e(a["h1"])}</h1>
            <p class="updated">{e(ui["updated"])}: <time datetime="{pub}">{pub}</time></p>
            <aside class="quick" aria-label="{e(ui["quick_answer"])}"><h2>{e(ui["quick_answer"])}</h2>{a["quick_answer"]}</aside>
            {a["intro"]}
            {store_button(ui)}
            <nav class="toc" aria-label="{e(ui["on_this_page"])}"><h2>{e(ui["on_this_page"])}</h2><ol>{toc}</ol></nav>
{secs}
            <section id="faq"><h2>{e(ui["faq"])}</h2>{faqs}</section>
            <section class="cta"><h2>{e(ui["cta_title"])}</h2><p>{e(ui["cta_text"])}</p>{store_button(ui)}</section>
            <section id="sources"><h2>{e(ui["sources"])}</h2><ol class="sources">{cites}</ol></section>
            {related}
        </article>'''
    write(path, page(lang, ui, title=a["title"] + " | CleanKoach", desc=a["meta_description"], canonical_path=path,
                     body=body, head_extra=head, og_image=img, og_type="article", current="guides"))

def build_hub(lang):
    ui, arts = DATA[lang]
    path = hub_path(lang)
    alts = [(LANGS[c]["hreflang"], url(hub_path(c))) for c in DATA]
    head = "".join(f'<link rel="alternate" hreflang="{h}" href="{u}">\n    ' for h, u in alts) + f'<link rel="alternate" hreflang="x-default" href="{url(hub_path("en"))}">'
    items = "".join(f'<a class="guide-card" href="{art_path(lang, s)}"><h2>{e(a["h1"])}</h2><p>{e(a["tagline"])}</p></a>' for s, a in arts.items())
    body = f'''        <section class="hero"><h1>{e(ui["guides_title"])}</h1><p>{e(ui["guides_intro"])}</p></section>
        <div class="guide-list">{items}</div>'''
    write(path, page(lang, ui, title=ui["guides_title"] + " | CleanKoach", desc=ui["guides_intro"], canonical_path=path, body=body,
                     head_extra=head, og_image=BASE + "/assets/og-image.png", og_type="website", current="guides"))

def write(path, content):
    p = OUT / path.lstrip("/") / "index.html"
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding="utf8")

def sitemaps():
    d = OUT / "sitemaps"
    if d.exists(): shutil.rmtree(d)
    d.mkdir()
    ns = 'xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml"'
    pub = SITE["published"]
    index = []
    site_urls = [BASE + "/", BASE + "/privacy.html", BASE + "/terms.html"]
    x = f'<?xml version="1.0" encoding="UTF-8"?>\n<urlset {ns}>\n' + "".join(f"  <url><loc>{u}</loc><lastmod>{pub}</lastmod></url>\n" for u in site_urls) + "</urlset>\n"
    (d / "sitemap-site.xml").write_text(x); index.append("sitemap-site.xml")
    for c in DATA:
        f = f"sitemap-{LANGS[c]['folder']}.xml"
        ents = ""
        hub_alts = "".join(f'    <xhtml:link rel="alternate" hreflang="{LANGS[k]["hreflang"]}" href="{url(hub_path(k))}"/>\n' for k in DATA) + f'    <xhtml:link rel="alternate" hreflang="x-default" href="{url(hub_path("en"))}"/>\n'
        ents += f"  <url>\n    <loc>{url(hub_path(c))}</loc>\n    <lastmod>{pub}</lastmod>\n{hub_alts}  </url>\n"
        for s in DATA[c][1]:
            al = "".join(f'    <xhtml:link rel="alternate" hreflang="{h}" href="{u}"/>\n' for h, u in alternates(s)) + f'    <xhtml:link rel="alternate" hreflang="x-default" href="{url(art_path("en", s))}"/>\n'
            ents += f"  <url>\n    <loc>{url(art_path(c, s))}</loc>\n    <lastmod>{pub}</lastmod>\n{al}  </url>\n"
        (d / f).write_text(f'<?xml version="1.0" encoding="UTF-8"?>\n<urlset {ns}>\n{ents}</urlset>\n'); index.append(f)
    (OUT / "sitemap.xml").write_text('<?xml version="1.0" encoding="UTF-8"?>\n<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n' + "".join(f"  <sitemap><loc>{BASE}/sitemaps/{f}</loc><lastmod>{pub}</lastmod></sitemap>\n" for f in index) + "</sitemapindex>\n")
    (OUT / "robots.txt").write_text(f"User-agent: *\nAllow: /\n\nSitemap: {BASE}/sitemap.xml\n")

def main():
    for c in DATA:
        build_hub(c)
        for s in DATA[c][1]:
            build_article(c, s)
    sitemaps()
    print("built", {c: len(DATA[c][1]) for c in DATA})

if __name__ == "__main__":
    main()
