/*
 * Home: Markets today.
 *   /public/api/home/markets   indices, commodities, currencies (Yahoo, delayed; cached 2 min on the server)
 *   /public/api/home/movers    NIFTY 50 breadth, gainers and losers
 *   /public/api/home/news      headlines (RSS, cached 10 min)
 *   /public/api/getindexhistory/{index}, /public/api/geteconomy   this site's own data
 */
(function () {
    'use strict';
    var REFRESH_MS = 2 * 60 * 1000, NEWS_MS = 10 * 60 * 1000, STALE_S = 30 * 60;
    var $ = function (id) { return document.getElementById(id); };
    var S = { news: null, region: 'india', lastOk: 0 };

    function getJSON(url) {
        return fetch(url, { cache: 'no-store' }).then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); });
    }
    function esc(s) {
        return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }
    function fmt(v, d, inr) {
        if (v == null || !isFinite(v)) return '–';
        return Number(v).toLocaleString(inr ? 'en-IN' : 'en-US', { minimumFractionDigits: d, maximumFractionDigits: d });
    }
    function dec(v) { var a = Math.abs(v); return a >= 1000 ? 2 : (a >= 10 ? 2 : (a >= 1 ? 3 : 4)); }
    function pct(r, d) { return r == null || !isFinite(r) ? '–' : (r >= 0 ? '+' : '') + (r * 100).toFixed(d == null ? 2 : d) + '%'; }
    function cls(r) { return r == null ? '' : (r >= 0 ? 'tw-pos' : 'tw-neg'); }
    function css(name) { return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || '#888'; }
    function ago(ms) {
        var m = Math.round((Date.now() - ms) / 60000);
        if (m < 1) return 'just now';
        if (m < 60) return m + ' min ago';
        var h = Math.round(m / 60);
        if (h < 24) return h + ' h ago';
        return Math.round(h / 24) + ' d ago';
    }
    function time(epochS) {
        return new Date(epochS * 1000).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
    }
    function closed(q) { return !q.t || Date.now() / 1000 - q.t > STALE_S; }

    /** intraday line with the previous close dashed; green above it, red below */
    function spark(q, w, h) {
        var v = q.spark || [];
        if (v.length < 2 || !q.pc) return '<svg viewBox="0 0 ' + w + ' ' + h + '"></svg>';
        var lo = Math.min.apply(null, v.concat([q.pc])), hi = Math.max.apply(null, v.concat([q.pc]));
        if (hi === lo) { hi += 1; lo -= 1; }
        var x = function (i) { return (i / (v.length - 1)) * (w - 2) + 1; };
        var y = function (p) { return h - 2 - (p - lo) / (hi - lo) * (h - 4); };
        var pts = v.map(function (p, i) { return x(i).toFixed(1) + ',' + y(p).toFixed(1); }).join(' ');
        var col = q.r >= 0 ? css('--pos') : css('--neg');
        var base = y(q.pc).toFixed(1);
        return '<svg viewBox="0 0 ' + w + ' ' + h + '" preserveAspectRatio="none" aria-hidden="true">' +
            '<line x1="0" x2="' + w + '" y1="' + base + '" y2="' + base + '" stroke="' + css('--axis') + '" stroke-dasharray="2 3" stroke-width="1"/>' +
            '<polyline fill="none" stroke="' + col + '" stroke-width="1.6" stroke-linejoin="round" stroke-linecap="round" points="' + pts + '"/></svg>';
    }

    // ------------------------------------------------------------------ markets
    function renderTiles(list) {
        $('hm-india').innerHTML = list.map(function (q, i) {
            var has = q.p != null;
            return '<div class="hm-tile' + (i < 2 ? ' big' : '') + (has ? (q.r >= 0 ? ' up' : ' dn') : '') + '"' +
                ' title="' + esc(q.n) + (has ? ': previous close ' + fmt(q.pc, 2, true) + (q.t ? ', quote ' + time(q.t) : '') : '') + '">' +
                '<div class="nm">' + esc(q.n) + '</div>' +
                '<div class="px">' + (has ? fmt(q.p, 2, true) : '–') + '</div>' +
                '<div class="ch ' + cls(has ? q.r : null) + '">' + (has ? (q.r >= 0 ? '▲ ' : '▼ ') + fmt(Math.abs(q.p - q.pc), 2, true) + ' (' + pct(q.r) + ')' : 'no quote') + '</div>' +
                spark(q, 160, 34) + '</div>';
        }).join('');
    }

    function renderRows(id, list) {
        $(id).innerHTML = list.map(function (q) {
            var has = q.p != null;
            var unit = q.u === '%' ? '%' : '';
            var val = has ? (q.u === '$' || q.u === '₹' ? q.u : '') + fmt(q.p, dec(q.p), q.u === '₹') + unit : '–';
            var tip = q.n + (q.u && q.u !== '%' && q.u !== '$' && q.u !== '₹' ? ' (' + q.u + ')' : '') +
                (has ? ', previous close ' + fmt(q.pc, dec(q.pc)) + (q.t ? ', quote ' + time(q.t) : '') : '');
            return '<div class="hm-row" title="' + esc(tip) + '"><span class="nm">' + esc(q.n) +
                (q.u && q.u.length > 2 ? '<small>' + esc(q.u) + '</small>' : '') + '</span>' +
                spark(q, 64, 22) + '<span class="px">' + val + '</span>' +
                '<span class="ch ' + cls(has ? q.r : null) + '">' + (has ? pct(q.r) : '') +
                (has && closed(q) ? '<span class="closed">closed</span>' : '') + '</span></div>';
        }).join('') || '<div class="hm-empty">No data right now.</div>';
    }

    function loadMarkets() {
        return getJSON('/public/api/home/markets').then(function (d) {
            renderTiles(d.india || []);
            renderRows('hm-global', d.global || []);
            renderRows('hm-commodities', d.commodities || []);
            renderRows('hm-currencies', d.currencies || []);
            var nifty = (d.india || [])[0] || {};
            var live = nifty.t && !closed(nifty);
            $('hm-dot').className = 'hm-dot' + (live ? ' live' : '');
            $('hm-status').textContent = (live ? 'Indian market open' : 'Indian market closed') +
                ' · updated ' + new Date(d.asOf || Date.now()).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
            S.lastOk = Date.now();
        }).catch(function () {
            $('hm-status').textContent = 'Live prices unavailable right now';
            if (!$('hm-india').innerHTML) $('hm-india').innerHTML = '<div class="hm-empty">Live prices could not be loaded. Try again in a few minutes.</div>';
        });
    }

    // ------------------------------------------------------------------ movers
    function moverRows(list) {
        return (list || []).map(function (m) {
            return '<div class="hm-mv"><a href="/public/stockanalysis/' + encodeURIComponent(m.t) + '" title="' + esc(m.n) + ', ₹' + fmt(m.p, 2, true) + '">' +
                esc(m.n || m.t) + '</a><b class="' + cls(m.r) + '">' + pct(m.r) + '</b></div>';
        }).join('') || '<div class="hm-empty">–</div>';
    }
    function loadMovers() {
        return getJSON('/public/api/home/movers').then(function (d) {
            var n = (d.up || 0) + (d.down || 0) + (d.flat || 0);
            if (!n) {
                $('hm-breadth').innerHTML = '<div class="hm-empty">Live stock prices unavailable right now.</div>';
            } else {
                $('hm-breadth').innerHTML = '<div class="bar" role="img" aria-label="' + d.up + ' up, ' + d.down + ' down">' +
                    '<span style="width:' + (d.up / n * 100) + '%;background:' + css('--pos') + '"></span>' +
                    '<span style="width:' + (d.flat / n * 100) + '%;background:' + css('--axis') + '"></span>' +
                    '<span style="width:' + (d.down / n * 100) + '%;background:' + css('--neg') + '"></span></div>' +
                    '<div class="lbl"><span class="tw-pos">' + d.up + ' advancing</span><span>' + (d.flat ? d.flat + ' unchanged' : '') + '</span><span class="tw-neg">' + d.down + ' declining</span></div>';
            }
            $('hm-gainers').innerHTML = moverRows(d.gainers);
            $('hm-losers').innerHTML = moverRows(d.losers);
        }).catch(function () { $('hm-breadth').innerHTML = '<div class="hm-empty">Movers unavailable right now.</div>'; });
    }

    // ------------------------------------------------------------------ news
    function renderNews() {
        var list = (S.news && S.news[S.region]) || [];
        $('hm-news').innerHTML = list.map(function (n) {
            return '<li><a href="' + esc(n.link) + '" target="_blank" rel="noopener noreferrer nofollow">' + esc(n.title) + '</a>' +
                '<div class="meta">' + esc(n.source) + ' · ' + ago(n.at) + '</div></li>';
        }).join('') || '<li class="hm-empty">' + (S.news ? 'No headlines right now.' : 'Loading…') + '</li>';
    }
    function loadNews() {
        return getJSON('/public/api/home/news').then(function (d) { S.news = d; renderNews(); })
            .catch(function () { if (!S.news) $('hm-news').innerHTML = '<li class="hm-empty">Headlines unavailable right now.</li>'; });
    }
    $('hm-news-tab').addEventListener('click', function (e) {
        var b = e.target.closest('button');
        if (!b) return;
        S.region = b.getAttribute('data-v');
        Array.prototype.forEach.call(this.querySelectorAll('button'), function (x) { x.classList.toggle('on', x === b); });
        renderNews();
    });

    // ------------------------------------------------------------------ index returns (own data)
    var INDICES = [['NIFTY', 'NIFTY 50'], ['NIFTY200', 'NIFTY 200'], ['BSEMidCap', 'BSE Midcap'], ['BSESmallCap', 'BSE Smallcap']];
    function asof(d, v, target) {          // last value on or before the target date
        var lo = 0, hi = d.length - 1, k = -1;
        while (lo <= hi) { var m = (lo + hi) >> 1; if (d[m] <= target) { k = m; lo = m + 1; } else hi = m - 1; }
        return k < 0 ? null : v[k];
    }
    function back(last, months) {
        var t = new Date(last + 'T00:00:00');
        t.setMonth(t.getMonth() - months);
        return t.toISOString().slice(0, 10);
    }
    function loadReturns() {
        Promise.all(INDICES.map(function (ix) {
            return getJSON('/public/api/getindexhistory/' + ix[0]).catch(function () { return null; });
        })).then(function (res) {
            var asOf = '';
            var rows = res.map(function (h, i) {
                if (!h || !h.d || h.d.length < 2) return '';
                var d = h.d, v = h.v, n = d.length, last = d[n - 1], now = v[n - 1];
                if (last > asOf) asOf = last;
                var cells = [[1, false], [3, false], [12, false], [36, true], [60, true]].map(function (p) {
                    var then = asof(d, v, back(last, p[0]));
                    if (!then || d[0] > back(last, p[0])) return '<td class="tw-muted">–</td>';
                    var r = p[1] ? Math.pow(now / then, 12 / p[0]) - 1 : now / then - 1;
                    return '<td class="' + cls(r) + '">' + pct(r, 1) + '</td>';
                }).join('');
                return '<tr><td title="' + esc(INDICES[i][1]) + ' close ' + fmt(now, 2, true) + ' on ' + last + '">' + esc(INDICES[i][1]) + '</td>' + cells + '</tr>';
            }).join('');
            $('hm-ret').querySelector('tbody').innerHTML = rows || '<tr><td colspan="6" class="hm-empty">No index history available.</td></tr>';
            if (asOf) $('hm-ret').parentNode.nextElementSibling.textContent = 'End-of-day closes up to ' + asOf + ' from this site\'s database; 3Y and 5Y are annualised.';
        });
    }

    // ------------------------------------------------------------------ economy (own research)
    function loadEconomy() {
        getJSON('/public/api/geteconomy').then(function (d) {
            $('hm-bottom').textContent = d.bottomLine || 'No summary available.';
            $('hm-nut').innerHTML = (d.nutshell || []).slice(0, 4).map(function (n) {
                var t = typeof n === 'string' ? n : (n.headline || n.text || '');
                return t ? '<li title="' + esc(n.text || '') + '">' + esc(t) + '</li>' : '';
            }).join('');
        }).catch(function () { $('hm-bottom').textContent = 'Economy summary unavailable right now.'; });
    }

    // ------------------------------------------------------------------ refresh loop
    function refresh() {
        var btn = $('hm-refresh');
        btn.classList.add('spin');
        Promise.all([loadMarkets(), loadMovers()]).then(function () { btn.classList.remove('spin'); });
    }
    $('hm-refresh').addEventListener('click', function () { refresh(); loadNews(); });
    $('hm-date').textContent = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) +
        ' · Indian and global markets, commodities, currencies and the day\'s headlines.';

    refresh();
    loadNews();
    loadReturns();
    loadEconomy();
    setInterval(function () { if (!document.hidden) refresh(); }, REFRESH_MS);
    setInterval(function () { if (!document.hidden) { loadNews(); } }, NEWS_MS);
    setInterval(function () { if (S.news) renderNews(); }, 60 * 1000);       // keep "x min ago" current
    document.addEventListener('visibilitychange', function () {
        if (!document.hidden && Date.now() - S.lastOk > REFRESH_MS) refresh();
    });
    // redraw sparklines in the new colours when the theme is switched
    new MutationObserver(function () { refresh(); }).observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
})();
