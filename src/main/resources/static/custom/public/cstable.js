/*
 * CsTable: small sortable tables with green / red heat cells for the consensus pages.
 *
 *   CsTable.render(el, rows, cols, {sort:'key', desc:true, group:'key'})
 *   cols: [{key, label, kind:'pct'|'num'|'int'|'text'|'html', dp, heat:cap (fraction), left:bool,
 *           html:function(row) (cell markup), value:function(row) (sort value), tip}]
 */
(function () {
    'use strict';
    var MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
    function isNum(v) { return v !== null && v !== undefined && v !== '' && isFinite(v); }
    function pct(v, dp) {
        if (!isNum(v)) return '';
        var s = (v * 100).toFixed(dp == null ? 1 : dp);
        if (Number(s) === 0) s = s.replace('-', '');          // no "-0.0%"
        return (Number(s) > 0 ? '+' : '') + s + '%';
    }
    function num(v, dp) {
        if (!isNum(v)) return '';
        return Number(v).toLocaleString('en-IN', { minimumFractionDigits: dp || 0, maximumFractionDigits: dp || 0 });
    }
    function day(s) {
        var m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s || '');
        return m ? (+m[3]) + ' ' + MON[+m[2] - 1] + ' ' + m[1] : (s || '');
    }
    /** background for a value (fraction), darkest at |v| >= cap */
    function heat(v, cap) {
        if (!isNum(v) || !cap) return '';
        var a = Math.min(Math.abs(v) / cap, 1) * 0.42 + 0.04;
        return 'background-color:' + (v >= 0 ? 'rgba(27,175,122,' : 'rgba(208,59,59,') + a.toFixed(2) + ')';
    }
    function cell(c, r) {
        var v = r[c.key];
        if (c.html) return c.html(r);
        if (c.kind === 'pct') return c.color && isNum(v) ? '<span class="' + (v > 0 ? 'tw-pos' : v < 0 ? 'tw-neg' : '') + '">' + pct(v, c.dp) + '</span>' : pct(v, c.dp);
        if (c.kind === 'num') return num(v, c.dp);
        if (c.kind === 'int') return isNum(v) ? String(Math.round(v)) : '';
        return esc(v);
    }
    function sortVal(c, r) {
        var v = c.value ? c.value(r) : r[c.key];
        return v === undefined ? null : v;
    }

    function render(el, rows, cols, opt) {
        if (typeof el === 'string') el = document.getElementById(el);
        if (!el) return;
        opt = opt || {};
        var state = { key: opt.sort || null, desc: opt.desc !== false };
        function draw() {
            var list = rows.slice();
            var col = cols.filter(function (c) { return c.key === state.key; })[0];
            if (col) {
                list.sort(function (a, b) {
                    var x = sortVal(col, a), y = sortVal(col, b);
                    if (x === null && y === null) return 0;
                    if (x === null) return 1;
                    if (y === null) return -1;
                    var r = typeof x === 'string' ? x.localeCompare(y) : x - y;
                    return state.desc ? -r : r;
                });
            }
            var h = '<table class="cs-table"><thead><tr>' + cols.map(function (c) {
                var ar = c.key === state.key ? '<span class="ar">' + (state.desc ? '▼' : '▲') + '</span>' : '';
                return '<th class="' + (c.left ? 'l' : '') + '" data-k="' + esc(c.key) + '"' + (c.tip ? ' title="' + esc(c.tip) + '"' : '') + '>' + esc(c.label) + ar + '</th>';
            }).join('') + '</tr></thead><tbody>';
            var lastGroup = null;
            list.forEach(function (r) {
                if (opt.group && !col && r[opt.group] !== lastGroup) {
                    lastGroup = r[opt.group];
                    h += '<tr class="grp"><td colspan="' + cols.length + '">' + esc(lastGroup) + '</td></tr>';
                }
                h += '<tr>' + cols.map(function (c) {
                    var st = c.heat ? heat(r[c.key], c.heat) : '';
                    return '<td class="' + (c.left ? 'l' : '') + '"' + (st ? ' style="' + st + '"' : '') + '>' + cell(c, r) + '</td>';
                }).join('') + '</tr>';
            });
            if (!list.length) h += '<tr><td class="l" colspan="' + cols.length + '"><span class="sub">Nothing this week.</span></td></tr>';
            el.innerHTML = h + '</tbody></table>';
            Array.prototype.forEach.call(el.querySelectorAll('th'), function (th) {
                th.addEventListener('click', function () {
                    var k = th.getAttribute('data-k');
                    if (state.key === k) state.desc = !state.desc;
                    else { state.key = k; state.desc = true; }
                    draw();
                });
            });
        }
        draw();
    }

    function stock(r) {
        return '<a href="/public/stockanalysis/' + encodeURIComponent(r.ticker) + '" target="_blank">' + esc(r.name || r.ticker) + '</a>' +
            (r.name && r.name !== r.ticker ? ' <span class="sub">' + esc(r.ticker) + '</span>' : '');
    }

    /** ticker in bold with the company name below and an F&O badge, as on the weekly dashboard */
    function stock2(r) {
        return '<a class="cs-tk" href="/public/stockanalysis/' + encodeURIComponent(r.ticker) + '" target="_blank">' + esc(r.ticker) + '</a>' +
            (r.fno ? ' <span class="cs-badge" title="Available for F&amp;O trading on NSE">F&amp;O</span>' : '') +
            (r.name && r.name !== r.ticker ? '<div class="sub">' + esc(r.name) + '</div>' : '');
    }
    /** score percentile as a small meter plus the number */
    function meter(v) {
        if (!isNum(v)) return '<span class="sub">–</span>';
        return '<span class="cs-meter"><i style="width:' + Math.max(2, Math.min(100, v)).toFixed(0) + '%"></i></span>' + Math.round(v);
    }
    /** market cap in crore: 22,440 Cr or 1.36 L Cr */
    function cr(v) {
        if (!isNum(v)) return '';
        return v >= 100000 ? (v / 100000).toFixed(2) + ' L Cr' : num(v) + ' Cr';
    }

    window.CsTable = { render: render, pct: pct, num: num, day: day, esc: esc, heat: heat, stock: stock, stock2: stock2,
                       meter: meter, cr: cr, isNum: isNum };
})();
