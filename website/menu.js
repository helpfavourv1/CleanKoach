(function () {
  var root = document.documentElement;
  var header = document.querySelector('.site-header');
  if (!header) return;

  var button = header.querySelector('.menu-toggle');
  function set(open) {
    header.classList.toggle('open', open);
    if (button) button.setAttribute('aria-expanded', open ? 'true' : 'false');
  }
  if (button) button.addEventListener('click', function () { set(!header.classList.contains('open')); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') set(false); });
  Array.prototype.forEach.call(header.querySelectorAll('.site-nav a'), function (a) {
    a.addEventListener('click', function () { set(false); });
  });

  var theme = header.querySelector('.theme-toggle');
  if (theme) {
    var order = ['auto', 'light', 'dark'];
    var labels = (theme.getAttribute('data-labels') || 'Auto|Light|Dark').split('|');
    var prefix = theme.getAttribute('data-prefix') || 'Theme';
    function current() {
      var t = root.getAttribute('data-theme');
      return t === 'light' || t === 'dark' ? t : 'auto';
    }
    function label() { theme.setAttribute('aria-label', prefix + ': ' + labels[order.indexOf(current())]); }
    label();
    theme.addEventListener('click', function () {
      var next = order[(order.indexOf(current()) + 1) % order.length];
      if (next === 'auto') root.removeAttribute('data-theme'); else root.setAttribute('data-theme', next);
      try { if (next === 'auto') localStorage.removeItem('ck-theme'); else localStorage.setItem('ck-theme', next); } catch (e) {}
      label();
    });
  }
})();
