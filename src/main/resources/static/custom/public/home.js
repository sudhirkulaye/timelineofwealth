/*
 * Home: Markets today.
 *   /public/api/home/markets     indices, sectors, commodities, currencies (Yahoo, delayed; cached 2 min on the server)
 *   /public/api/home/history     a year of daily closes for the 1M to 1Y returns and charts (cached 1 h)
 *   /public/api/home/movers      NIFTY 50 breadth, gainers and losers
 *   /public/api/home/valuation   NIFTY 50 P/E, P/B and dividend yield (this site's data)
 *   /public/api/home/longterm    1Y to 20Y index returns (this site's data)
 *   /public/api/home/research    economy and sector teasers (this site's research notes)
 *   /public/api/home/news        newspaper headlines (RSS, cached 10 min)
 */
(function () {
    'use strict';
    var REFRESH_MS = 2 * 60 * 1000, NEWS_MS = 10 * 60 * 1000, HISTORY_MS = 30 * 60 * 1000, STALE_S = 30 * 60;
    /** {key, label, words}; 1D is today's move, the others run from the close on or before the same date n months ago */
    var RANGES = [['1d', '1D', 'today'], ['1m', '1M', 'one month'], ['3m', '3M', 'three months'], ['6m', '6M', 'six months'], ['1y', '1Y', 'one year']];
    var K = { '1m': 0, '3m': 1, '6m': 2, '1y': 3 };                           // position in history b[] and i[]
    var HEAT = { '1d': 0.025, '1m': 0.08, '3m': 0.15, '6m': 0.2, '1y': 0.3 };  // move that gets the strongest colour
    var GROUP = {
        global: ['Global indices', 'Local currency'],
        commodities: ['Commodities', 'Futures, US$'],
        currencies: ['Currencies & rates', 'Rupees per unit unless shown']
    };
    var $ = function (id) { return document.getElementById(id); };
    var S = { m: null, h: null, movers: null, news: null, research: null, region: 'india', range: '1d', lastOk: 0 };
    try { var saved = localStorage.getItem('hm-range'); if (/^(1d|1m|3m|6m|1y)$/.test(saved || '')) S.range = saved; } catch (e) { /* private mode */ }

    function getJSON(url, cache) {
        return fetch(url, { cache: cache || 'no-store' }).then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); });
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
    function dec(v) { var a = Math.abs(v); return a >= 10 ? 2 : (a >= 1 ? 3 : 4); }
    function sign(v) { return v > 0 ? '+' : (v < 0 ? '−' : ''); }
    function pct(r, d) { return r == null || !isFinite(r) ? '–' : sign(r) + Math.abs(r * 100).toFixed(d == null ? 2 : d) + '%'; }
    function pctAbs(r, d) { return Math.abs(r * 100).toFixed(d == null ? 2 : d) + '%'; }
    function cls(r) { return r == null || !isFinite(r) ? '' : (r >= 0 ? 'tw-pos' : 'tw-neg'); }
    function css(name) { return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || '#888'; }
    function ago(ms) {
        var m = Math.round((Date.now() - ms) / 60000);
        if (m < 1) return 'just now';
        if (m < 60) return m + ' min ago';
        var h = Math.round(m / 60);
        if (h < 24) return h + ' h ago';
        return Math.round(h / 24) + ' d ago';
    }
    function time(epochS) { return new Date(epochS * 1000).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }); }
    function closed(q) { return !q.t || Date.now() / 1000 - q.t > STALE_S; }
    var MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    /** "19-Nov-26", "19-Nov-2026" or "2026-11-19" -> "19 Nov 2026" */
    function day(s) {
        if (!s) return '';
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s);
        if (m) return (+m[3]) + ' ' + MON[+m[2] - 1] + ' ' + m[1];
        m = /^(\d{1,2})-([A-Za-z]{3})-(\d{2,4})$/.exec(s);
        if (m) return (+m[1]) + ' ' + m[2] + ' ' + (m[3].length === 2 ? '20' + m[3] : m[3]);
        return s;
    }
    function clip(s, n) {
        s = String(s || '').trim();
        if (s.length <= n) return s;
        return s.slice(0, s.lastIndexOf(' ', n) > n * 0.6 ? s.lastIndexOf(' ', n) : n).replace(/[\s,;:—–-]+$/, '') + '…';
    }

    // ------------------------------------------------------------------ returns and charts
    function hist(q) { return S.h && S.h.items ? S.h.items[q.s] : null; }

    /** change over the range: a ratio (0.012 = +1.2%), or percentage points for a yield */
    function ret(q, range) {
        if (q.p == null) return null;
        if (range === '1d') return q.u === '%' ? q.p - q.pc : q.r;
        var h = hist(q), b = h && h.b ? h.b[K[range]] : null;
        if (b == null || !(b > 0)) return null;
        return q.u === '%' ? q.p - b : q.p / b - 1;
    }
    function fmtRet(q, v, d) {
        if (v == null || !isFinite(v)) return '–';
        if (q.u === '%') { var bp = Math.round(v * 100); return sign(bp) + Math.abs(bp) + ' bp'; }
        return pct(v, d);
    }

    /** daily closes from the start of the range, ending with today's live price */
    function series(q, range) {
        var h = hist(q);
        if (!h || !h.c || !h.i) return null;
        var i = h.i[K[range]];
        if (i == null || i < 0) return null;
        var c = h.c.slice(i);
        if (q.p != null && c.length) {
            if (q.d && h.d === q.d) c[c.length - 1] = q.p;      // today's bar: use the live price
            else if (!q.d || q.d > h.d) c.push(q.p);
        }
        return c.length > 1 ? c : null;
    }

    /** 1D: intraday line with the previous close dashed; longer: daily closes with the starting level dashed */
    function chart(q, w, h, range) {
        var v, base, up;
        if (range === '1d') { v = q.spark || []; base = q.pc; up = q.r >= 0; }
        else { v = series(q, range) || []; base = v[0]; up = v.length > 1 && v[v.length - 1] >= v[0]; }
        var open = '<svg class="hm-spark" viewBox="0 0 ' + w + ' ' + h + '" preserveAspectRatio="none" aria-hidden="true">';
        if (v.length < 2 || !base) return open + '</svg>';
        var lo = Math.min.apply(null, v.concat([base])), hi = Math.max.apply(null, v.concat([base]));
        if (hi === lo) { hi += 1; lo -= 1; }
        var x = function (i) { return (i / (v.length - 1)) * (w - 2) + 1; };
        var y = function (p) { return h - 2 - (p - lo) / (hi - lo) * (h - 4); };
        var pts = v.map(function (p, i) { return x(i).toFixed(1) + ',' + y(p).toFixed(1); }).join(' ');
        var col = up ? css('--pos') : css('--neg');
        var b = y(base).toFixed(1);
        return open +
            '<polygon points="' + x(0).toFixed(1) + ',' + b + ' ' + pts + ' ' + x(v.length - 1).toFixed(1) + ',' + b + '" fill="' + col + '" fill-opacity=".08"/>' +
            '<line x1="0" x2="' + w + '" y1="' + b + '" y2="' + b + '" stroke="' + css('--axis') + '" stroke-dasharray="2 3" stroke-width="1" vector-effect="non-scaling-stroke"/>' +
            '<polyline fill="none" stroke="' + col + '" stroke-width="1.5" stroke-linejoin="round" stroke-linecap="round" vector-effect="non-scaling-stroke" points="' + pts + '"/></svg>';
    }
    function rangeWords(r) { for (var i = 0; i < RANGES.length; i++) if (RANGES[i][0] === r) return RANGES[i]; return RANGES[0]; }
    function liveDot(q) { return q.p != null && !closed(q) ? '<span class="hm-live" title="Trading now"></span>' : ''; }

    // ------------------------------------------------------------------ Indian indices
    function renderTiles(list) {
        $('hm-india').innerHTML = list.map(function (q) {
            var has = q.p != null;
            var strip = RANGES.slice(1).map(function (rg) {
                var v = ret(q, rg[0]);
                return '<span' + (S.range === rg[0] ? ' class="on"' : '') + ' title="Change over ' + rg[2] + '"><i>' + rg[1] + '</i><b class="' + cls(v) + '">' + fmtRet(q, v, 1) + '</b></span>';
            }).join('');
            var tip = q.n + (has ? ': previous close ' + fmt(q.pc, 2, true) + (q.t ? ', quote ' + time(q.t) : '') : '');
            var h = hist(q);
            if (h && h.hi > h.lo) tip += '; 52-week range ' + fmt(h.lo, 2, true) + ' – ' + fmt(h.hi, 2, true);
            return '<div class="hm-tile' + (has ? (q.r >= 0 ? ' up' : ' dn') : '') + '" title="' + esc(tip) + '">' +
                '<div class="nm">' + liveDot(q) + esc(q.n) + '</div>' +
                '<div class="px">' + (has ? fmt(q.p, 2, true) : '–') + '</div>' +
                '<div class="ch ' + cls(has ? q.r : null) + '">' + (has ? (q.r >= 0 ? '▲ ' : '▼ ') + fmt(Math.abs(q.p - q.pc), 2, true) + ' (' + pct(q.r) + ')' : 'no quote') + '</div>' +
                chart(q, 160, 36, S.range) + '<div class="rets">' + strip + '</div></div>';
        }).join('') || '<div class="hm-empty">No data right now.</div>';
    }

    // ------------------------------------------------------------------ world markets
    function worldRow(q) {
        var has = q.p != null;
        var val = has ? (q.u === '$' || q.u === '₹' ? q.u : '') + fmt(q.p, dec(q.p), q.u === '₹') + (q.u === '%' ? '%' : '') : '–';
        var cells = RANGES.map(function (rg) {
            var v = ret(q, rg[0]);
            return '<td class="rt ' + cls(v) + (S.range === rg[0] ? ' sel' : '') + '">' + fmtRet(q, v, rg[0] === '1d' ? 2 : 1) + '</td>';
        }).join('');
        var h = hist(q), wk = '';
        if (has && h && h.hi > h.lo) {
            var at = Math.max(0, Math.min(1, (q.p - h.lo) / (h.hi - h.lo)));
            var off = q.p / h.hi - 1;
            wk = '<div class="hm-52" role="img" aria-label="52-week range ' + fmt(h.lo, dec(h.lo)) + ' to ' + fmt(h.hi, dec(h.hi)) + '" title="52-week range ' +
                fmt(h.lo, dec(h.lo)) + ' – ' + fmt(h.hi, dec(h.hi)) + (off < -0.0005 ? '; ' + pctAbs(off, 1) + ' below the high' : '; at the high') + '">' +
                '<i style="left:' + (at * 100).toFixed(1) + '%"></i></div>';
        }
        var tip = q.n + (q.u && q.u.length > 2 ? ' (' + q.u + ')' : '') + (has ? ', previous close ' + fmt(q.pc, dec(q.pc)) + (q.t ? ', quote ' + time(q.t) : '') : '');
        return '<tr><th scope="row" class="nm" title="' + esc(tip) + '">' + liveDot(q) + esc(q.n) + (q.u && q.u.length > 2 ? '<small>' + esc(q.u) + '</small>' : '') + '</th>' +
            '<td class="px">' + val + '</td>' + cells + '<td class="hm-c-chart">' + chart(q, 96, 28, S.range) + '</td><td class="hm-c-52">' + wk + '</td></tr>';
    }
    function renderWorld() {
        Array.prototype.forEach.call(document.querySelectorAll('#hm-world tbody'), function (tb) {
            var g = tb.getAttribute('data-g'), list = (S.m && S.m[g]) || [];
            tb.innerHTML = '<tr class="hm-g"><th colspan="9">' + esc(GROUP[g][0]) + '<small>' + esc(GROUP[g][1]) + '</small></th></tr>' +
                (list.map(worldRow).join('') || '<tr><td colspan="9" class="hm-empty">No data right now.</td></tr>');
        });
        Array.prototype.forEach.call(document.querySelectorAll('#hm-world thead th[data-r]'), function (th) {
            th.classList.toggle('sel', th.getAttribute('data-r') === S.range);
        });
        $('hm-chart-h').textContent = rangeWords(S.range)[1] + ' chart';
    }

    // ------------------------------------------------------------------ sectors
    function renderSectors() {
        var rows = ((S.m && S.m.sectors) || []).filter(function (q) { return q.p != null; })
            .map(function (q) { return { q: q, v: ret(q, S.range) }; });
        rows.sort(function (a, b) { return (b.v == null ? -1e9 : b.v) - (a.v == null ? -1e9 : a.v); });
        var scale = HEAT[S.range];
        $('hm-sectors').innerHTML = rows.map(function (x) {
            var v = x.v;
            var bg = v == null ? 'var(--chip)' : 'color-mix(in srgb, var(' + (v >= 0 ? '--pos' : '--neg') + ') ' +
                Math.round(6 + 34 * Math.min(1, Math.abs(v) / scale)) + '%, var(--surface))';
            return '<a class="hm-ht" href="/public/sectors" style="background:' + bg + '" title="' + esc(x.q.n) + ' index ' + fmt(x.q.p, 2, true) +
                '; ' + rangeWords(S.range)[2] + ' ' + pct(v) + '"><span>' + esc(x.q.n) + '</span><b>' + pct(v, 1) + '</b></a>';
        }).join('') || '<div class="hm-empty">Sector data unavailable right now.</div>';
        $('hm-sec-note').textContent = 'NSE sector indices, best to worst over ' + rangeWords(S.range)[2] + '.';
    }

    // ------------------------------------------------------------------ the day in one paragraph
    function find(group, sym) {
        var l = (S.m && S.m[group]) || [];
        for (var i = 0; i < l.length; i++) if (l[i].s === sym) return l[i];
        return null;
    }
    function moved(r, d) { return Math.abs(r) < 0.0005 ? 'flat' : (r > 0 ? 'up ' : 'down ') + pctAbs(r, d); }
    function renderPulse() {
        var n = find('india', '^NSEI');
        if (!n || n.p == null) return;
        var open = !closed(n);
        var when = new Date(n.t * 1000).toLocaleDateString('en-IN', { weekday: 'long', timeZone: 'Asia/Kolkata' });
        var s = 'The NIFTY 50 ' + (open ? 'is ' : 'closed ') + moved(n.r) + ' at ' + fmt(n.p, 2, true) + (open || !n.t ? '' : ' on ' + when);
        var mo = S.movers;
        if (mo && (mo.up || mo.down)) s += ', with ' + mo.up + (mo.up === 1 ? ' stock ' : ' stocks ') + (open ? 'rising and ' : 'ending higher and ') + mo.down + (open ? ' falling' : ' lower');
        s += '.';
        var m1 = ret(n, '1m'), y1 = ret(n, '1y');
        if (m1 != null && y1 != null) s += ' The index is ' + moved(m1, 1) + ' over the past month and ' + moved(y1, 1) + ' over the year.';
        var sp = find('global', '^GSPC');
        if (sp && sp.p != null) s += ' In New York, the S&P 500 ' + (closed(sp) ? 'closed ' : 'is ') + moved(sp.r) + '.';
        var bits = [], br = find('commodities', 'BZ=F'), au = find('commodities', 'GC=F'), fx = find('currencies', 'INR=X');
        if (br && br.p != null) bits.push('Brent crude is at $' + fmt(br.p, 2) + ' a barrel');
        if (au && au.p != null) bits.push('gold at $' + fmt(au.p, 0) + ' an ounce');
        if (fx && fx.p != null) bits.push('the rupee at ' + fmt(fx.p, 2) + ' per dollar');
        if (bits.length) {
            var t = bits.length > 1 ? bits.slice(0, -1).join(', ') + ' and ' + bits[bits.length - 1] : bits[0];
            s += ' ' + t.charAt(0).toUpperCase() + t.slice(1) + '.';
        }
        $('hm-pulse').textContent = s;
    }

    function renderMarkets() {
        if (!S.m) return;
        renderTiles(S.m.india || []);
        renderWorld();
        renderSectors();
        renderPulse();
    }

    function loadMarkets() {
        return getJSON('/public/api/home/markets').then(function (d) {
            S.m = d;
            renderMarkets();
            var nifty = (d.india || [])[0] || {};
            var live = nifty.t && !closed(nifty);
            $('hm-dot').className = 'hm-dot' + (live ? ' live' : '');
            $('hm-status').textContent = (live ? 'Indian market open' : 'Indian market closed') +
                ' · updated ' + new Date(d.asOf || Date.now()).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
            S.lastOk = Date.now();
        }).catch(function () {
            $('hm-status').textContent = 'Live prices unavailable right now';
            if (!S.m) $('hm-india').innerHTML = '<div class="hm-empty">Live prices could not be loaded. Try again in a few minutes.</div>';
        });
    }
    function loadHistory() {
        return getJSON('/public/api/home/history', 'default').then(function (d) { S.h = d; renderMarkets(); }).catch(function () { /* returns stay blank */ });
    }

    // ------------------------------------------------------------------ period selectors (all in step)
    function setRange(r) {
        if (!r || r === S.range) return;
        S.range = r;
        try { localStorage.setItem('hm-range', r); } catch (e) { /* private mode */ }
        Array.prototype.forEach.call(document.querySelectorAll('.hm-range button'), function (b) {
            var on = b.getAttribute('data-v') === r;
            b.classList.toggle('on', on);
            b.setAttribute('aria-pressed', on ? 'true' : 'false');
        });
        renderMarkets();
    }
    Array.prototype.forEach.call(document.querySelectorAll('.hm-range'), function (el) {
        el.innerHTML = RANGES.map(function (rg) {
            var on = rg[0] === S.range;
            return '<button type="button" data-v="' + rg[0] + '"' + (on ? ' class="on"' : '') + ' aria-pressed="' + on + '" title="' + rg[2].charAt(0).toUpperCase() + rg[2].slice(1) + '">' + rg[1] + '</button>';
        }).join('');
        el.addEventListener('click', function (e) { var b = e.target.closest('button'); if (b) setRange(b.getAttribute('data-v')); });
    });
    document.querySelector('#hm-world thead').addEventListener('click', function (e) {
        var th = e.target.closest('th[data-r]');
        if (th) setRange(th.getAttribute('data-r'));
    });

    // ------------------------------------------------------------------ NIFTY 50 movers
    function moverRows(list) {
        return (list || []).map(function (m) {
            return '<div class="hm-mv"><a href="/public/stockanalysis/' + encodeURIComponent(m.t) + '" title="' + esc(m.n) + ', ₹' + fmt(m.p, 2, true) + '">' +
                esc(m.n || m.t) + '</a><b class="' + cls(m.r) + '">' + pct(m.r) + '</b></div>';
        }).join('') || '<div class="hm-empty">–</div>';
    }
    function renderMovers() {
        var d = S.movers;
        if (!d) return;
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
    }
    function loadMovers() {
        return getJSON('/public/api/home/movers').then(function (d) { S.movers = d; renderMovers(); renderPulse(); })
            .catch(function () { if (!S.movers) $('hm-breadth').innerHTML = '<div class="hm-empty">Movers unavailable right now.</div>'; });
    }

    // ------------------------------------------------------------------ headlines, by paper in order of preference
    function renderNews() {
        var papers = (S.news && S.news[S.region]) || [];
        $('hm-news').innerHTML = papers.map(function (p) {
            var items = p.items || [];
            return '<div class="hm-paper"><h3><a href="' + esc(p.home) + '" target="_blank" rel="noopener noreferrer">' + esc(p.name) + '</a>' +
                (items.length ? '<span class="ed">' + (p.edition === 'print' ? 'Print edition' : 'Latest online') + '</span>' : '') +
                (p.epaper ? '<a class="ep" href="' + esc(p.epaper) + '" target="_blank" rel="noopener noreferrer" title="Read the ' + esc(p.name) + ' e-paper (may need a subscription)">e-paper &rsaquo;</a>' : '') +
                '</h3>' + (items.length ? '<ul class="hm-news">' +
                items.map(function (n) {
                    return '<li><a href="' + esc(n.link) + '" target="_blank" rel="noopener noreferrer nofollow">' + esc(n.title) + '</a>' +
                        '<span class="meta">' + ago(n.at) + '</span></li>';
                }).join('') + '</ul>' : '<p class="hm-empty">No headline feed from this paper; read today\'s edition in its e-paper.</p>') + '</div>';
        }).join('') || '<p class="hm-empty">' + (S.news ? 'No headlines right now.' : 'Loading…') + '</p>';
    }
    function loadNews() {
        return getJSON('/public/api/home/news').then(function (d) { S.news = d; renderNews(); })
            .catch(function () { if (!S.news) $('hm-news').innerHTML = '<p class="hm-empty">Headlines unavailable right now.</p>'; });
    }
    $('hm-news-tab').addEventListener('click', function (e) {
        var b = e.target.closest('button');
        if (!b) return;
        S.region = b.getAttribute('data-v');
        Array.prototype.forEach.call(this.querySelectorAll('button'), function (x) { x.classList.toggle('on', x === b); });
        renderNews();
    });

    // ------------------------------------------------------------------ long-term returns (own data)
    function loadReturns() {
        getJSON('/public/api/home/longterm').then(function (d) {
            var asOf = '';
            var rows = (d.rows || []).map(function (x) {
                if (x.asOf > asOf) asOf = x.asOf;
                return '<tr><td title="' + esc(x.name) + ' close ' + fmt(x.close, 2, true) + ' on ' + day(x.asOf) + '">' + esc(x.name) + '</td>' +
                    (x.r || []).map(function (r) { return r == null ? '<td class="tw-muted">–</td>' : '<td class="' + cls(r) + '">' + pct(r, 1) + '</td>'; }).join('') + '</tr>';
            }).join('');
            $('hm-ret').querySelector('tbody').innerHTML = rows || '<tr><td colspan="6" class="hm-empty">No index history available.</td></tr>';
            if (asOf) $('hm-ret-foot').textContent = 'Price returns (dividends excluded) from end-of-day closes up to ' + day(asOf) +
                ' in this site\'s database; 3Y and longer are annualised.';
        }).catch(function () {
            $('hm-ret').querySelector('tbody').innerHTML = '<tr><td colspan="6" class="hm-empty">Long-term returns unavailable right now.</td></tr>';
        });
    }

    // ------------------------------------------------------------------ research desk teasers (own data)
    function renderResearch() {
        var d = S.research || {}, e = d.economy, sc = d.sectors;
        if (e) {
            var card = $('hm-f-econ');
            if ((e.top || []).length) {
                card.querySelector('[data-f="list"]').innerHTML = e.top.map(function (t) { return '<li>' + esc(clip(t, 70)) + '</li>'; }).join('');
                card.querySelector('[data-f="dek"]').hidden = true;
            }
            var facts = [];
            if (e.themes) facts.push(e.themes + ' themes ranked by market impact');
            if (e.critical) facts.push(e.critical + ' critical points to track');
            if (e.asOf) facts.push('updated ' + day(e.asOf));
            card.querySelector('[data-f="facts"]').textContent = facts.join(' · ');
        }
        if (sc && sc.total) {
            var box = $('hm-f-sec'), count = sc.counts || {};
            var kinds = [['Improving', '--pos'], ['Stable', '--muted'], ['Mixed', '--s4'], ['Weakening', '--neg']].filter(function (k) { return count[k[0]]; });
            box.querySelector('[data-f="trend"]').innerHTML =
                '<div class="hm-trend" role="img" aria-label="' + kinds.map(function (k) { return count[k[0]] + ' ' + k[0].toLowerCase(); }).join(', ') + '">' +
                kinds.map(function (k) { return '<span style="width:' + (count[k[0]] / sc.total * 100).toFixed(2) + '%;background:' + css(k[1]) + '"></span>'; }).join('') + '</div>' +
                '<div class="hm-legend">' + kinds.map(function (k) {
                    return '<span><i style="background:' + css(k[1]) + '"></i>' + k[0] + ' <b>' + count[k[0]] + '</b></span>';
                }).join('') + '</div>';
            var short = function (name) { return String(name || '').replace(/\s*\([^)]*\)/g, '').split(/,| — | - /)[0].trim(); };
            var line = function (label, names) {
                names = (names || []).map(short);
                return names.length ? '<b>' + label + ':</b> ' + esc(names.slice(0, 3).join(', ')) + (names.length > 3 ? ' and ' + (names.length - 3) + ' more' : '') + '.' : '';
            };
            var dek = [line('Improving', sc.improving), line('Weakening', sc.weakening)].filter(Boolean).join(' ');
            if (dek) box.querySelector('[data-f="dek"]').innerHTML = dek;
            box.querySelector('[data-f="cta"]').innerHTML = 'See all ' + sc.total + ' sectors <i class="fa fa-angle-right" aria-hidden="true"></i>';
        }
    }
    function loadResearch() {
        getJSON('/public/api/home/research').then(function (d) { S.research = d; renderResearch(); })
            .catch(function () { /* the static descriptions stay */ });
    }
    function loadValuation() {
        getJSON('/public/api/home/valuation').then(function (v) {
            if (!v || !v.pe) return;
            var card = $('hm-f-val'), lo = v.min, hi = v.max;
            var at = function (x) { return (Math.max(0, Math.min(1, (x - lo) / (hi - lo))) * 100).toFixed(1) + '%'; };
            var since = day(v.from).replace(/^\d+ /, '');
            card.querySelector('[data-f="val"]').innerHTML =
                '<div class="hm-val-big">' + v.pe.toFixed(1) + '×<small>trailing P/E, ' + esc(day(v.asOf)) + '</small></div>' +
                (hi > lo ? '<div class="hm-gauge" role="img" aria-label="P/E range since ' + esc(since) + ': ' + lo.toFixed(1) + ' to ' + hi.toFixed(1) + ', median ' + v.median.toFixed(1) + '">' +
                    '<span class="ml" style="left:' + at(v.median) + '">median ' + v.median.toFixed(1) + '×</span>' +
                    '<span class="med" style="left:' + at(v.median) + '"></span><span class="now" style="left:' + at(v.pe) + '"></span>' +
                    '<span class="lo">' + lo.toFixed(1) + '×</span><span class="hi">' + hi.toFixed(1) + '×</span></div>' : '');
            var parts = [];
            if (v.below != null) parts.push('The P/E has been lower on ' + Math.round(v.below * 100) + '% of trading days since ' + since + '.');
            var more = [];
            if (v.pb) more.push('P/B ' + v.pb.toFixed(2) + '×');
            if (v.dy) more.push('dividend yield ' + v.dy.toFixed(2) + '%');
            if (more.length) parts.push(more.join(', ').replace(/^./, function (c) { return c.toUpperCase(); }) + '.');
            card.querySelector('[data-f="dek"]').textContent = parts.join(' ');
        }).catch(function () { /* the static description stays */ });
    }

    // ------------------------------------------------------------------ refresh loop
    function refresh() {
        var btn = $('hm-refresh');
        btn.classList.add('spin');
        Promise.all([loadMarkets(), loadMovers()]).then(function () { btn.classList.remove('spin'); });
    }
    $('hm-refresh').addEventListener('click', function () { refresh(); loadNews(); });
    $('hm-dateline').textContent = new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric', timeZone: 'Asia/Kolkata' });

    refresh();
    loadHistory();
    loadNews();
    loadReturns();
    loadResearch();
    loadValuation();
    setInterval(function () { if (!document.hidden) refresh(); }, REFRESH_MS);
    setInterval(function () { if (!document.hidden) loadNews(); }, NEWS_MS);
    setInterval(function () { if (!document.hidden) loadHistory(); }, HISTORY_MS);
    setInterval(function () { if (S.news) renderNews(); }, 60 * 1000);       // keep "x min ago" current
    document.addEventListener('visibilitychange', function () {
        if (!document.hidden && Date.now() - S.lastOk > REFRESH_MS) refresh();
    });
    // redraw the charts in the new colours when the theme changes
    function recolour() { renderMarkets(); renderMovers(); renderResearch(); }
    new MutationObserver(recolour).observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
    if (window.matchMedia) {
        var mq = window.matchMedia('(prefers-color-scheme: dark)');
        if (mq.addEventListener) mq.addEventListener('change', recolour);
    }
})();
