/* Site chrome behaviour: light/dark toggle and the top-bar stock search. No dependencies. */
(function () {
    'use strict';
    var root = document.documentElement;

    function currentTheme() {
        var t = root.getAttribute('data-theme');
        if (t) return t;
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    function setTheme(t) {
        root.setAttribute('data-theme', t);
        try { localStorage.setItem('tw-theme', t); } catch (e) { /* private mode */ }
        document.dispatchEvent(new CustomEvent('tw-themechange', { detail: t }));
    }
    window.twTheme = { current: currentTheme, set: setTheme };

    function initToggle() {
        var btn = document.getElementById('tw-theme-toggle');
        if (btn) btn.addEventListener('click', function () { setTheme(currentTheme() === 'dark' ? 'light' : 'dark'); });
    }

    function initSearch() {
        var input = document.getElementById('tw-search-input');
        var list = document.getElementById('tw-search-list');
        if (!input || !list) return;
        var timer = null, items = [], sel = -1, seq = 0;

        function go(t) { window.location.href = '/public/stockanalysis/' + encodeURIComponent(t); }
        function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
        function render() {
            list.innerHTML = items.map(function (it, i) {
                return '<li role="option"' + (i === sel ? ' class="on"' : '') + '><a href="/public/stockanalysis/' +
                    encodeURIComponent(it.t) + '"><b>' + esc(it.t) + '</b><span>' + esc(it.n) + '</span></a></li>';
            }).join('');
        }
        function search(q) {
            var my = ++seq;
            fetch('/public/api/searchstocks?q=' + encodeURIComponent(q))
                .then(function (r) { return r.ok ? r.json() : []; })
                .then(function (rows) { if (my !== seq) return; items = rows || []; sel = items.length ? 0 : -1; render(); })
                .catch(function () { /* ignore */ });
        }
        input.addEventListener('input', function () {
            var q = input.value.trim();
            clearTimeout(timer);
            if (!q) { items = []; render(); return; }
            timer = setTimeout(function () { search(q); }, 160);
        });
        input.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowDown') { sel = Math.min(items.length - 1, sel + 1); render(); e.preventDefault(); }
            else if (e.key === 'ArrowUp') { sel = Math.max(0, sel - 1); render(); e.preventDefault(); }
            else if (e.key === 'Enter') {
                e.preventDefault();
                if (sel >= 0 && items[sel]) go(items[sel].t);
                else if (input.value.trim()) go(input.value.trim().toUpperCase());
            } else if (e.key === 'Escape') { items = []; render(); }
        });
        document.addEventListener('click', function (e) { if (!list.contains(e.target) && e.target !== input) { items = []; render(); } });
        document.addEventListener('keydown', function (e) {
            if (e.key === '/' && document.activeElement && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) {
                e.preventDefault(); input.focus();
            }
        });
    }

    function init() { initToggle(); initSearch(); }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
