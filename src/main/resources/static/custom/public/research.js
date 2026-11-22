/*
 * /public/economy and /public/sectors: macro and sector summaries from the research workbook
 * (Economy and Sector sheets), served by /public/api/geteconomy and /public/api/getsectors.
 * Broker names are already anonymised on the server.
 */
(function () {
    'use strict';
    var $ = function (id) { return document.getElementById(id); };
    var MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
    /** "19-Nov-26", "19-Nov-2026" or "2026-11-19" -> "19 Nov 2026" */
    function day(s) {
        if (!s) return '';
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s);
        if (m) return (+m[3]) + ' ' + MON[+m[2] - 1] + ' ' + m[1];
        m = /^(\d{1,2})-([A-Za-z]{3})-(\d{2,4})$/.exec(s);
        if (m) return (+m[1]) + ' ' + m[2] + ' ' + (m[3].length === 2 ? '20' + m[3] : m[3]);
        m = /^([A-Za-z]{3})-(\d{2})$/.exec(s);
        if (m) return m[1] + ' 20' + m[2];
        return s;
    }
    /** chip for a signal / view: Positive, Negative, Mixed, Cautiously ..., Improving, Weakening ... */
    function tone(v) {
        var s = String(v || '').toLowerCase();
        if (/^(improving|positive)/.test(s)) return 'pos';
        if (/^(weakening|negative)/.test(s)) return 'neg';
        if (/^cautiously positive/.test(s)) return 'pos';
        if (/^cautiously negative/.test(s)) return 'neg';
        if (/^(mixed|neutral)/.test(s)) return 'mix';
        return '';
    }
    function chip(v, cls) { return v ? '<span class="gd-chip ' + (cls || tone(v)) + '">' + esc(v) + '</span>' : ''; }
    function multiline(t) {   // "1) ... 2) ..." blocks keep their line breaks
        return esc(t).replace(/\n/g, '<br>');
    }
    function details(list) {
        if (!list || !list.length) return '';
        return '<dl class="ec-det">' + list.map(function (d) {
            return '<dt>' + esc(d.label) + '</dt><dd>' + multiline(d.text) + '</dd>';
        }).join('') + '</dl>';
    }
    function seg(el, onPick) {
        el.addEventListener('click', function (e) {
            var b = e.target.closest('button'); if (!b) return;
            [].forEach.call(el.children, function (x) { x.classList.toggle('on', x === b); });
            onPick(b.getAttribute('data-v'));
        });
    }
    function fail(el, what) { el.innerHTML = '<p class="tw-muted">The ' + what + ' summary is not available right now.</p>'; }

    // ===================== Economy =====================
    function economy() {
        fetch('/public/api/geteconomy').then(function (r) { return r.json(); }).then(function (d) {
            if (!d || !d.points || !d.points.length) { fail($('ec-root'), 'economy'); return; }
            $('ec-asof').textContent = 'As of ' + day(d.asOf);
            $('ec-bottom').textContent = d.bottomLine || '';
            $('ec-nut').innerHTML = (d.nutshell || []).map(function (n) {
                return '<li><span class="n">' + n.rank + '</span><b>' + esc(n.headline) + '</b><span>' + esc(n.text) + '</span></li>';
            }).join('');

            var imp = { critical: 'crit', high: 'high' };
            var drawPoints = function (filter) {
                var pts = d.points.filter(function (p) { return !filter || String(p.importance).toLowerCase() === filter; });
                $('ec-points').innerHTML = pts.map(function (p) {
                    return '<details class="ec-pt"><summary><span class="rk">' + p.rank + '</span><span><span class="th">' + esc(p.theme) + '</span>' +
                        '<div class="pt">' + esc(p.point) + '</div></span><span class="chips">' +
                        chip(p.importance, imp[String(p.importance).toLowerCase()] || '') + chip(p.impact) +
                        (p.status && p.status !== 'Active' ? '<span class="gd-chip">' + esc(p.status) + '</span>' : '') +
                        '</span></summary>' + details(p.details) +
                        (p.updated ? '<p class="tw-muted tw-small" style="margin:-6px 0 12px 42px">Updated ' + esc(day(p.updated)) + '</p>' : '') +
                        '</details>';
                }).join('') || '<p class="tw-muted">No points at this level.</p>';
            };
            drawPoints('');
            seg($('ec-imp'), function (v) { drawPoints(v); });

            var g = null, rows = '';
            (d.indicators || []).forEach(function (i) {
                if (i.group !== g) { g = i.group; rows += '<tr class="grp"><td colspan="4">' + esc(g || '') + '</td></tr>'; }
                rows += '<tr><td>' + esc(i.indicator) + '</td><td>' + esc(i.latest) + '</td><td>' + chip(i.signal) + '</td><td class="tw-muted" style="white-space:nowrap">' + esc(day(i.asOf)) + '</td></tr>';
            });
            $('ec-ind').innerHTML = rows;
        }).catch(function () { fail($('ec-root'), 'economy'); });
    }

    // ===================== Sectors =====================
    function sectors() {
        fetch('/public/api/getsectors').then(function (r) { return r.json(); }).then(function (d) {
            var list = (d && d.sectors) || [];
            if (!list.length) { fail($('sc-root'), 'sector'); return; }
            $('sc-asof').textContent = 'As of ' + day(d.asOf);
            var counts = {};
            list.forEach(function (s) { counts[s.trend] = (counts[s.trend] || 0) + 1; });
            $('sc-score').innerHTML = ['Improving', 'Stable', 'Mixed', 'Weakening'].filter(function (t) { return counts[t]; }).map(function (t) {
                return '<span class="gd-chip ' + tone(t) + '">' + t + ' · ' + counts[t] + '</span>';
            }).join('');
            var state = { trend: '', q: '' };
            var draw = function () {
                var q = state.q.toLowerCase(), g = null, html = '', n = 0;
                list.forEach(function (s) {
                    if (state.trend && s.trend !== state.trend) return;
                    if (q && (s.sector + ' ' + s.view + ' ' + s.details.map(function (x) { return x.text; }).join(' ')).toLowerCase().indexOf(q) < 0) return;
                    n++;
                    if (s.group !== g) { g = s.group; html += '<div class="sc-group">' + esc(g || '') + '</div>'; }
                    html += '<details class="ec-pt"><summary><span class="rk">' + s.rank + '</span><span>' +
                        '<div class="pt">' + esc(s.sector) + '</div><div class="vw">' + esc(s.view) + '</div>' +
                        '<div class="sc-brokers"><span>Broker A: <b>' + esc(s.brokerA || '—') + '</b></span><span>Broker B: <b>' + esc(s.brokerB || '—') + '</b></span>' +
                        (s.updated ? '<span>Last note ' + esc(day(s.updated)) + '</span>' : '') + '</div></span>' +
                        '<span class="chips">' + chip(s.trend) + '</span></summary>' + details(s.details) + '</details>';
                });
                $('sc-list').innerHTML = html || '<p class="tw-muted">No sectors match.</p>';
                $('sc-count').textContent = n + ' of ' + list.length + ' sectors';
            };
            draw();
            seg($('sc-trend'), function (v) { state.trend = v; draw(); });
            $('sc-q').addEventListener('input', function (e) { state.q = e.target.value.trim(); draw(); });
        }).catch(function () { fail($('sc-root'), 'sector'); });
    }

    if ($('ec-root')) economy();
    if ($('sc-root')) sectors();
})();
