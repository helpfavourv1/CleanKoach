# Website generator
`python3 website-src/build.py` rebuilds every page in `website/` from `pages/*.body.html` (static pages) and `content/<lang>/` (guides). `python3 website-src/og.py` redraws the social images. `python3 website-src/check.py` validates titles, descriptions, canonicals, JSON-LD, links and hreflang. Never edit generated HTML by hand.
