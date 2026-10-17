#!/usr/bin/env python3
"""Static site generator for the CleanKoach website.
Sources: pages/*.body.html (static pages), content/<lang>/ui.json and content/<lang>/<slug>.json (guides).
Output: ../website/ (served as-is). Only languages/articles that have content are built; hreflang and
sitemaps cover exactly those. Never edit generated HTML by hand."""
import json, html, re, shutil
from pathlib import Path
import pages_tool

ROOT = Path(__file__).parent
OUT = ROOT.parent / "website"
SITE = json.load(open(ROOT / "site.json", encoding="utf8"))
BASE = SITE["base_url"]
PLAY = SITE["play_url"]
PUB = SITE["published"]
import hashlib
def _v(name): return hashlib.md5((Path(__file__).parent.parent / "website" / name).read_bytes()).hexdigest()[:8]
CSS_V, JS_V = _v("style.css"), _v("menu.js")
LANGS = {l["code"]: l for l in SITE["languages"]}
e = html.escape
SITE_OG = f"{BASE}/assets/og/site.png"
SITE_OG_ALT = "CleanKoach: free up phone storage on Android"

def load(lang):
    d = ROOT / "content" / lang
    if not (d / "ui.json").exists():
        d = ROOT / "content" / LANGS[lang]["folder"]
    if not (d / "ui.json").exists():
        return None
    ui = json.load(open(d / "ui.json", encoding="utf8"))
    arts = {}
    for f in sorted(d.glob("*.json")):
        if f.name != "ui.json":
            a = json.load(open(f, encoding="utf8")); arts[a["slug"]] = a
    return ui, arts

DATA = {c: v for c in LANGS if (v := load(c))}
EN_UI = DATA["en"][0]

def prefix(lang): return "" if lang == "en" else "/" + LANGS[lang]["folder"]

def load_pages(lang):
    f = ROOT / "pages_tr" / f"{lang}.json"
    return json.load(open(f, encoding="utf8")) if f.exists() else None
PAGES_TR = {c: v for c in LANGS if (v := load_pages(c))}
def sprefix(lang):
    """Prefix for static pages (home, about, contact, legal). Falls back to English until a language has its pages translated."""
    return prefix(lang) if lang in PAGES_TR else ""
AUTHOR = {"@type": "Person", "name": "Ak", "url": BASE + "/about/"}
def art_path(lang, slug): return f"{prefix(lang)}/guides/{slug}/"
def hub_path(lang): return f"{prefix(lang)}/guides/"
def url(path): return BASE + path

def alternates(slug):
    return [(c, LANGS[c]["hreflang"], url(art_path(c, slug))) for c in DATA if slug in DATA[c][1]]

def hreflang_tags(items):
    out = "".join(f'<link rel="alternate" hreflang="{h}" href="{u}">\n    ' for h, u in items)
    return out + f'<link rel="alternate" hreflang="x-default" href="{items[0][1] if False else dict((h, u) for h, u in items).get("en", items[0][1])}">'

# ---------- shared chrome ----------
def store_button(ui):
    g = f'{ui["get_it_on"]} {ui["google_play"]}'
    return (f'<div class="stores">'
            f'<a class="store" href="{PLAY}" aria-label="{e(g)}"><img src="/assets/badge-google-play.png" alt="{e(g)}" width="180" height="61"></a>'
            f'<span class="store store-soon" role="img" aria-label="App Store, {e(ui["coming_soon"])}"><img src="/assets/badge-app-store.png" alt="" width="180" height="61"><em>{e(ui["coming_soon"])}</em></span>'
            f'</div>')

def header(lang, ui, current=None):
    sp = sprefix(lang)
    items = [(ui["nav_features"], sp + "/#features", None), (ui["nav_languages"], sp + "/#languages", None),
             (ui["nav_guides"], hub_path(lang), "guides")]
    links = "".join(f'<a href="{h}"' + (' aria-current="page"' if k and k == current else "") + f">{e(t)}</a>" for t, h, k in items)
    return f'''<a class="skip" href="#main">{e(ui["skip"])}</a>
        <header class="site-header">
            <a href="{sprefix(lang)}/" class="wordmark"><img src="/assets/favicon-192.png" alt="" width="32" height="32" class="logo"><span><span class="clean">Clean</span><span class="koach">Koach</span></span></a>
            <div class="header-tools">
                <nav id="site-nav" class="site-nav" aria-label="{e(ui["menu"])}">{links}<div class="menu-cta">{store_button(ui)}</div></nav>
                <button class="theme-toggle" type="button" data-labels="{e(ui["theme_auto"])}|{e(ui["theme_light"])}|{e(ui["theme_dark"])}" data-prefix="{e(ui["theme"])}" aria-label="{e(ui["theme"])}: {e(ui["theme_auto"])}"><svg class="i-auto" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 3a9 9 0 0 1 0 18z" fill="currentColor"/></svg><svg class="i-light" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg><svg class="i-dark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round" aria-hidden="true"><path d="M20 14.5A8.5 8.5 0 0 1 9.5 4 8.5 8.5 0 1 0 20 14.5z"/></svg></button>
                <button class="menu-toggle" type="button" aria-expanded="false" aria-controls="site-nav" aria-label="{e(ui["menu"])}"><span class="bars" aria-hidden="true"></span></button>
            </div>
        </header>'''

def footer(lang, ui, switcher=""):
    return f'''<footer>
            {switcher}
            <a href="{sprefix(lang)}/about/">{e(ui["footer_about"])}</a>
            <a href="{sprefix(lang)}/contact/">{e(ui["footer_contact"])}</a>
            <a href="{sprefix(lang)}/privacy.html">{e(ui["footer_privacy"])}</a>
            <a href="{sprefix(lang)}/terms.html">{e(ui["footer_terms"])}</a>
        </footer>'''

def switcher(lang, ui, alts):
    lis = "".join(f'<li><a href="{u}" hreflang="{h}" lang="{h}"' + (' aria-current="true"' if c == lang else "") + f'>{e(LANGS[c]["name"])}</a></li>' for c, h, u in alts)
    return f'<details class="lang-switch"><summary>{e(ui["language"])}: {e(LANGS[lang]["name"])}</summary><ul>{lis}</ul></details>'

def jsonld(objs):
    return "".join('<script type="application/ld+json">' + json.dumps(o, ensure_ascii=False, separators=(",", ":")).replace("</", "<\\/") + "</script>\n    " for o in objs)

ORG = {"@context": "https://schema.org", "@type": "Organization", "name": "CleanKoach", "url": BASE + "/",
       "logo": {"@type": "ImageObject", "url": BASE + "/assets/favicon-512.png"},
       "contactPoint": {"@type": "ContactPoint", "contactType": "customer support", "email": "stmakarios@gmail.com", "url": BASE + "/contact/"}}
APP = {"@context": "https://schema.org", "@type": "SoftwareApplication", "name": "CleanKoach", "operatingSystem": "Android 9+",
       "applicationCategory": "UtilitiesApplication", "url": PLAY, "image": BASE + "/assets/favicon-512.png",
       "offers": {"@type": "Offer", "price": "0", "priceCurrency": "USD"}}

def crumbs(items):
    return {"@context": "https://schema.org", "@type": "BreadcrumbList", "itemListElement": [
        {"@type": "ListItem", "position": i + 1, "name": n, "item": u} for i, (n, u) in enumerate(items)]}

def page(lang, ui, *, title, desc, canonical_path, body, head_extra="", og_image, og_alt, og_type, current=None,
         noindex=False, sw="", preload=""):
    L = LANGS[lang]
    canon = url(canonical_path)
    robots = "noindex,follow" if noindex else "index,follow,max-image-preview:large"
    canon_tag = "" if noindex else f'<link rel="canonical" href="{canon}">'
    return f'''<!DOCTYPE html>
<html lang="{L["hreflang"]}" dir="{L["dir"]}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{e(title)}</title>
    <meta name="description" content="{e(desc)}">
    <meta name="robots" content="{robots}">
    {canon_tag}
    {head_extra}{preload}
    <link rel="preload" href="/assets/fonts/PlusJakartaSans.ttf" as="font" type="font/ttf" crossorigin>
    <link rel="icon" type="image/svg+xml" href="/assets/favicon.svg">
    <link rel="icon" type="image/png" sizes="32x32" href="/assets/favicon-32.png">
    <link rel="apple-touch-icon" href="/assets/apple-touch-icon.png">
    <meta name="theme-color" content="#FAFAF8" media="(prefers-color-scheme: light)">
    <meta name="theme-color" content="#0F1419" media="(prefers-color-scheme: dark)">
    <meta property="og:site_name" content="CleanKoach">
    <meta property="og:type" content="{og_type}">
    <meta property="og:title" content="{e(title)}">
    <meta property="og:description" content="{e(desc)}">
    <meta property="og:url" content="{canon}">
    <meta property="og:locale" content="{L["hreflang"].replace("-", "_")}">
    <meta property="og:image" content="{og_image}">
    <meta property="og:image:width" content="1200">
    <meta property="og:image:height" content="630">
    <meta property="og:image:alt" content="{e(og_alt)}">
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="{e(title)}">
    <meta name="twitter:description" content="{e(desc)}">
    <meta name="twitter:image" content="{og_image}">
    <meta name="twitter:image:alt" content="{e(og_alt)}">
    <link rel="stylesheet" href="/style.css?v={CSS_V}">
    <noscript><style>.menu-toggle{{display:none}}.site-nav{{display:flex!important;position:static!important}}</style></noscript>
    <script>try{{var t=localStorage.getItem("ck-theme");if(t==="light"||t==="dark")document.documentElement.setAttribute("data-theme",t)}}catch(e){{}}</script>
    <script src="/menu.js?v={JS_V}" defer></script>
</head>
<body>
    <div class="container">
        {header(lang, ui, current)}
        <main id="main">
{body}
        </main>
        {footer(lang, ui, sw)}
    </div>
</body>
</html>
'''

def write(rel, content):
    p = OUT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding="utf8")

def write_dir(path, content): write(path.lstrip("/") + "index.html", content)

def crumb_html(ui, lang, trail):
    """trail: list of (label, href or None). Home is prepended."""
    parts = [f'<a href="{sprefix(lang) + "/"}">{e(ui["breadcrumb_home"])}</a>'] + [f'<a href="{h}">{e(t)}</a>' if h else f'<span aria-current="page">{e(t)}</span>' for t, h in trail]
    return f'<nav class="crumbs" aria-label="Breadcrumb">' + " / ".join(parts) + "</nav>"

# ---------- guides ----------
def linkify_see(lang, arts, see):
    items = [(s, t) for s, t in see if s in arts]
    if not items: return ""
    return '<p class="see">' + " · ".join(f'<a href="{art_path(lang, s)}">{e(t)}</a>' for s, t in items) + "</p>"

def build_article(lang, slug):
    ui, arts = DATA[lang]
    a = arts[slug]
    path = art_path(lang, slug)
    img = f"{BASE}/assets/og/{slug}.png" if lang == "en" else f"{BASE}/assets/og/{LANGS[lang]['folder']}/{slug}.png"
    alts = alternates(slug)
    head = hreflang_tags([(h, u) for _, h, u in alts]) + "\n    "
    article = {"@context": "https://schema.org", "@type": "Article", "headline": a["title"], "description": a["meta_description"],
               "image": [img], "datePublished": PUB, "dateModified": PUB, "inLanguage": LANGS[lang]["hreflang"],
               "mainEntityOfPage": url(path), "keywords": ", ".join(a["keywords"]),
               "author": AUTHOR,
               "publisher": {"@type": "Organization", "name": "CleanKoach", "url": BASE + "/",
                             "logo": {"@type": "ImageObject", "url": BASE + "/assets/favicon-512.png"}}}
    faq = {"@context": "https://schema.org", "@type": "FAQPage", "mainEntity": [
        {"@type": "Question", "name": f["q"], "acceptedAnswer": {"@type": "Answer", "text": f["a"]}} for f in a["faqs"]]}
    bc = crumbs([(ui["breadcrumb_home"], BASE + "/"), (ui["breadcrumb_guides"], url(hub_path(lang))), (a["h1"], url(path))])
    head += jsonld([article, bc, faq, APP, ORG])

    hero = ""; preload = ""
    if a.get("hero"):
        h = a["hero"]
        preload = f'\n    <link rel="preload" as="image" href="{h["src"]}" fetchpriority="high">'
        hero = (f'<figure class="hero-img"><img src="{h["src"]}" alt="{e(h["alt"])}" width="{h.get("w", 1024)}" height="{h.get("h", 500)}" '
                f'fetchpriority="high" decoding="async"><figcaption>{e(h["caption"])}</figcaption></figure>')
    toc = "".join(f'<li><a href="#{s["id"]}">{e(s["h2"])}</a></li>' for s in a["sections"]) + f'<li><a href="#faq">{e(ui["faq"])}</a></li>'
    secs = ""
    for s in a["sections"]:
        secs += f'<section id="{s["id"]}"><h2>{e(s["h2"])}</h2>'
        if "image" in s:
            im = s["image"]
            secs += (f'<figure><img src="{im["src"]}" alt="{e(im["alt"])}" width="{im.get("w", 640)}" height="{im.get("h", 360)}" '
                     f'loading="lazy" decoding="async"><figcaption>{e(im["caption"])}</figcaption></figure>')
        if "html" in s: secs += s["html"]
        for t in ([s["table"]] if "table" in s else []) + s.get("tables", []):
            secs += '<div class="table-wrap"><table>' + f'<caption>{e(t["caption"])}</caption><thead><tr>' + "".join(f'<th scope="col">{e(h)}</th>' for h in t["headers"]) + "</tr></thead><tbody>"
            for r in t["rows"]:
                secs += '<tr><th scope="row">' + e(r[0]) + "</th>" + "".join(f'<td data-label="{e(t["headers"][i + 1])}">{e(c)}</td>' for i, c in enumerate(r[1:])) + "</tr>"
            secs += "</tbody></table></div>"
        secs += linkify_see(lang, arts, s.get("see", []))
        secs += "</section>\n"
    faqs = "".join(f'<details><summary>{e(f["q"])}</summary><p>{e(f["a"])}</p></details>' for f in a["faqs"])
    cites = "".join(f'<li><a href="{c["url"]}" rel="noopener" target="_blank">{e(c["title"])}</a>. {e(c["note"])}</li>' for c in a["citations"])
    rel = [s for s in a.get("related", []) if s in arts] or [s for s in arts if s != slug]
    related = ""
    if rel:
        related = f'<section><h2>{e(ui["related"])}</h2><ul class="related">' + "".join(f'<li><a href="{art_path(lang, s)}">{e(arts[s]["h1"])}</a></li>' for s in rel) + "</ul></section>"
    body = f'''        <article class="guide">
            {crumb_html(ui, lang, [(ui["breadcrumb_guides"], hub_path(lang)), (a["h1"], None)])}
            <h1>{e(a["h1"])}</h1>
            <p class="updated">{e(ui["updated"])}: <time datetime="{PUB}">{PUB}</time> · <a href="{sprefix(lang)}/about/">{e(ui["by_author"])}</a></p>
            <aside class="quick" aria-label="{e(ui["quick_answer"])}"><h2>{e(ui["quick_answer"])}</h2>{a["quick_answer"]}</aside>
            {a["intro"]}
            {hero}
            {store_button(ui)}
            <nav class="toc" aria-label="{e(ui["on_this_page"])}"><h2>{e(ui["on_this_page"])}</h2><ol>{toc}</ol></nav>
{secs}
            <section id="faq"><h2>{e(ui["faq"])}</h2>{faqs}</section>
            <section class="cta"><h2>{e(ui["cta_title"])}</h2><p>{e(ui["cta_text"])}</p>{store_button(ui)}</section>
            <section id="sources"><h2>{e(ui["sources"])}</h2><ol class="sources">{cites}</ol></section>
            {related}
        </article>'''
    write_dir(path, page(lang, ui, title=a["title"] + " | CleanKoach", desc=a["meta_description"], canonical_path=path, body=body,
                         head_extra=head, og_image=img, og_alt=a["h1"] + " | CleanKoach", og_type="article", current="guides",
                         sw=switcher(lang, ui, alternates(slug)), preload=preload))

def build_hub(lang):
    ui, arts = DATA[lang]
    path = hub_path(lang)
    alts = [(c, LANGS[c]["hreflang"], url(hub_path(c))) for c in DATA]
    head = hreflang_tags([(h, u) for _, h, u in alts]) + "\n    "
    coll = {"@context": "https://schema.org", "@type": "CollectionPage", "name": ui["guides_title"], "description": ui["guides_intro"],
            "url": url(path), "inLanguage": LANGS[lang]["hreflang"],
            "mainEntity": {"@type": "ItemList", "itemListElement": [
                {"@type": "ListItem", "position": i + 1, "url": url(art_path(lang, s)), "name": a["h1"]} for i, (s, a) in enumerate(arts.items())]}}
    head += jsonld([coll, crumbs([(ui["breadcrumb_home"], BASE + "/"), (ui["breadcrumb_guides"], url(path))]), ORG])
    items = "".join(f'<a class="guide-card" href="{art_path(lang, s)}"><h2>{e(a["h1"])}</h2><p>{e(a["tagline"])}</p></a>' for s, a in arts.items())
    body = f'''        {crumb_html(ui, lang, [(ui["breadcrumb_guides"], None)])}
        <section class="hero"><h1>{e(ui["guides_title"])}</h1><p>{e(ui["guides_intro"])}</p></section>
        <div class="guide-list">{items}</div>'''
    first = list(arts.values())[0]
    hub_desc = ui["guides_intro"] + " " + ("Large files, duplicates, storage and safety." if lang == "en" else first["tagline"].rstrip(".") + ".")
    if len(hub_desc) < 70: hub_desc += " " + first["h1"]
    write_dir(path, page(lang, ui, title=ui["guides_title"] + " | CleanKoach", desc=hub_desc,
                         canonical_path=path, body=body, head_extra=head, og_image=SITE_OG, og_alt=SITE_OG_ALT, og_type="website",
                         current="guides", sw=switcher(lang, ui, alts)))

# ---------- static pages ----------
STATIC = [
    dict(src="index", out="index.html", path="/", title="CleanKoach: Android Phone Cleaner and Storage Cleanup",
         desc="Free Android phone cleaner. Find large files, duplicates, similar photos and screenshots, and compress photos and videos. On-device, in 44 languages.",
         kind="home"),
    dict(src="privacy", out="privacy.html", path="/privacy.html", title="Privacy Policy | CleanKoach",
         desc="What the CleanKoach app and website access, what they do with it, and what they never do. Nothing is uploaded.", kind="web"),
    dict(src="terms", out="terms.html", path="/terms.html", title="Terms of Service | CleanKoach",
         desc="The terms for using the CleanKoach Android storage cleaner app and this website, including ads, Pro and your responsibilities.", kind="web"),
    dict(src="about", out="about/index.html", path="/about/", title="About CleanKoach | Android Storage Cleaner",
         desc="CleanKoach is a free Android storage cleaner that finds large files, duplicates and similar photos on your phone. Nothing is uploaded.", kind="about"),
    dict(src="contact", out="contact/index.html", path="/contact/", title="Contact CleanKoach | Support and Feedback",
         desc="Contact CleanKoach for app support, bug reports, feedback and press questions.", kind="contact"),
]

LANGS_UL = (ROOT / "pages" / "languages.fragment.html").read_text(encoding="utf8")
LINK = re.compile(r'href="(/(?!assets/)[^"]*)"')

def static_path(lang, p): return sprefix(lang) + p["path"]
def static_out(lang, p): return p["out"] if not sprefix(lang) else sprefix(lang).lstrip("/") + "/" + p["out"]
def static_langs(): return ["en"] + [c for c in DATA if c != "en" and c in PAGES_TR]

def build_static_page(lang, p):
    ui, arts = DATA[lang]
    pre = sprefix(lang)
    raw = (ROOT / "pages" / f'{p["src"]}.body.html').read_text(encoding="utf8")
    if lang != "en":
        raw = pages_tool.inject(raw, PAGES_TR[lang][p["src"]]["units"])
        title, desc = PAGES_TR[lang][p["src"]]["title"], PAGES_TR[lang][p["src"]]["desc"]
    else:
        title, desc = p["title"], p["desc"]
    raw = LINK.sub(lambda m: f'href="{pre}{m.group(1)}"', raw)
    guides_cards = "".join(f'<a class="guide-card" href="{art_path(lang, s)}"><h2>{e(a["h1"])}</h2><p>{e(a["tagline"])}</p></a>' for s, a in arts.items())
    body = raw.replace("<!--guides-->", guides_cards).replace("<!--store-->", store_button(ui)).replace("<!--langs-->", LANGS_UL)
    name = {"home": "CleanKoach"}.get(p["kind"]) or re.search(r"<h1>(.*?)</h1>", raw, re.S).group(1)
    body = "        " + (body.strip() if p["kind"] == "home" else crumb_html(ui, lang, [(name, None)]) + "\n        " + body.strip())
    lh = LANGS[lang]["hreflang"]
    if p["kind"] == "home":
        objs = [ORG, APP, {"@context": "https://schema.org", "@type": "WebSite", "name": "CleanKoach", "url": BASE + "/", "inLanguage": lh}]
    else:
        t = {"about": "AboutPage", "contact": "ContactPage"}.get(p["kind"], "WebPage")
        main = {"@type": t, "name": title, "description": desc, "url": url(static_path(lang, p)), "inLanguage": lh,
                "isPartOf": {"@type": "WebSite", "name": "CleanKoach", "url": BASE + "/"}}
        if p["kind"] == "about": main["mainEntity"] = dict(AUTHOR, worksFor={"@type": "Organization", "name": "CleanKoach", "url": BASE + "/"})
        objs = [dict({"@context": "https://schema.org"}, **main), crumbs([(ui["breadcrumb_home"], url(pre + "/")), (name, url(static_path(lang, p)))]), ORG]
    alts = [(c, LANGS[c]["hreflang"], url(static_path(c, p))) for c in static_langs()]
    head = hreflang_tags([(h, u) for _, h, u in alts]) + "\n    " + jsonld(objs)
    write(static_out(lang, p), page(lang, ui, title=title, desc=desc, canonical_path=static_path(lang, p), body=body, head_extra=head,
                                    og_image=SITE_OG, og_alt=SITE_OG_ALT, og_type="website", sw=switcher(lang, ui, alts)))

def build_static():
    for lang in static_langs():
        for p in STATIC: build_static_page(lang, p)
    ui = EN_UI
    nf = (ROOT / "pages" / "notfound.body.html").read_text(encoding="utf8")
    write("404.html", page("en", ui, title="Page not found | CleanKoach", desc="This page does not exist.", canonical_path="/404.html",
                           body="        " + nf.strip(), og_image=SITE_OG, og_alt=SITE_OG_ALT, og_type="website", noindex=True))

# ---------- sitemaps, robots, headers ----------
def sitemaps():
    d = OUT / "sitemaps"
    if d.exists(): shutil.rmtree(d)
    d.mkdir()
    ns = 'xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml"'
    index = []
    for c in DATA:
        f = f"sitemap-{LANGS[c]['folder']}.xml"
        hub_alts = "".join(f'    <xhtml:link rel="alternate" hreflang="{LANGS[k]["hreflang"]}" href="{url(hub_path(k))}"/>\n' for k in DATA) + f'    <xhtml:link rel="alternate" hreflang="x-default" href="{url(hub_path("en"))}"/>\n'
        ents = ""
        if c in static_langs():
            for p in STATIC:
                al = "".join(f'    <xhtml:link rel="alternate" hreflang="{LANGS[k]["hreflang"]}" href="{url(static_path(k, p))}"/>\n' for k in static_langs()) + f'    <xhtml:link rel="alternate" hreflang="x-default" href="{url(static_path("en", p))}"/>\n'
                ents += f"  <url>\n    <loc>{url(static_path(c, p))}</loc>\n    <lastmod>{PUB}</lastmod>\n{al}  </url>\n"
        ents += f"  <url>\n    <loc>{url(hub_path(c))}</loc>\n    <lastmod>{PUB}</lastmod>\n{hub_alts}  </url>\n"
        for s in DATA[c][1]:
            al = "".join(f'    <xhtml:link rel="alternate" hreflang="{h}" href="{u}"/>\n' for _, h, u in alternates(s)) + f'    <xhtml:link rel="alternate" hreflang="x-default" href="{url(art_path("en", s))}"/>\n'
            ents += f"  <url>\n    <loc>{url(art_path(c, s))}</loc>\n    <lastmod>{PUB}</lastmod>\n{al}  </url>\n"
        (d / f).write_text(f'<?xml version="1.0" encoding="UTF-8"?>\n<urlset {ns}>\n{ents}</urlset>\n'); index.append(f)
    (OUT / "sitemap.xml").write_text('<?xml version="1.0" encoding="UTF-8"?>\n<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n' + "".join(f"  <sitemap><loc>{BASE}/sitemaps/{f}</loc><lastmod>{PUB}</lastmod></sitemap>\n" for f in index) + "</sitemapindex>\n")
    (OUT / "robots.txt").write_text(f"User-agent: *\nAllow: /\n\nSitemap: {BASE}/sitemap.xml\n")
    (OUT / "_headers").write_text("""/*
  X-Content-Type-Options: nosniff
  Referrer-Policy: strict-origin-when-cross-origin
  X-Frame-Options: DENY
  Permissions-Policy: camera=(), microphone=(), geolocation=()

/assets/*
  Cache-Control: public, max-age=86400

/assets/fonts/*
  Cache-Control: public, max-age=31536000, immutable

/menu.js
  Cache-Control: public, max-age=31536000, immutable

/style.css
  Cache-Control: public, max-age=31536000, immutable
""")
    red = "/index.html / 301\n"
    for old, new in SITE.get("renamed_folders", {}).items():
        red += f"/{old}/guides/* /{new}/guides/:splat 301\n/{old} /{new}/ 301\n/{old}/* /{new}/ 301\n"
    for old in SITE.get("retired_folders", []):
        red += f"/{old}/guides/* /guides/:splat 301\n/{old} / 301\n/{old}/* / 301\n"
    (OUT / "_redirects").write_text(red)

def main():
    for f in list(SITE.get("renamed_folders", {})) + SITE.get("retired_folders", []):
        for d in (OUT / f, OUT / "assets" / "og" / f):
            if d.exists(): shutil.rmtree(d)
    build_static()
    for c in DATA:
        build_hub(c)
        for s in DATA[c][1]:
            build_article(c, s)
    sitemaps()
    print("built", {c: len(DATA[c][1]) for c in DATA})

if __name__ == "__main__":
    main()
