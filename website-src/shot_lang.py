import sys,http.server,threading,functools
from playwright.sync_api import sync_playwright
lang=sys.argv[1]; folder=sys.argv[2] if len(sys.argv)>2 else lang
class Q(http.server.SimpleHTTPRequestHandler):
    def log_message(self,*a): pass
s=http.server.ThreadingHTTPServer(('127.0.0.1',0),functools.partial(Q,directory='/root/cleankoach/website')); port=s.server_address[1]; threading.Thread(target=s.serve_forever,daemon=True).start()
sc='/tmp/claude-0/-home-claude/31c370b5-549d-5474-baae-8a84eca4937b/scratchpad/'
with sync_playwright() as p:
    b=p.chromium.launch(); pg=b.new_page(viewport={'width':390,'height':800},device_scale_factor=1.5)
    pg.goto(f'http://127.0.0.1:{port}/{folder}/guides/best-phone-cleaner-app-android/'); pg.screenshot(path=sc+f'L_{lang}.png',clip={'x':0,'y':0,'width':390,'height':800})
    print('scrollW',pg.evaluate('document.documentElement.scrollWidth'),'dir',pg.evaluate('document.documentElement.dir'))
    pg.goto(f'http://127.0.0.1:{port}/{folder}/guides/'); 
