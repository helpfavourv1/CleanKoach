(function () {
  var header = document.querySelector('.site-header');
  var button = header && header.querySelector('.menu-toggle');
  if (!button) return;
  function set(open) {
    header.classList.toggle('open', open);
    button.setAttribute('aria-expanded', open ? 'true' : 'false');
  }
  button.addEventListener('click', function () { set(!header.classList.contains('open')); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') set(false); });
  Array.prototype.forEach.call(header.querySelectorAll('.site-nav a'), function (a) {
    a.addEventListener('click', function () { set(false); });
  });
})();
