/*
 * /adviser/focusfiveconsensus: Focus Five weights, named broker views and daily performance against the indices,
 * from /adviser/api/getfocusfiveconsensus (adviser login only).
 */
(function () {
    'use strict';
    var $ = function (id) { return document.getElementById(id); };
    var T = window.CsTable, pct = T.pct, esc = T.esc;
    var D = null, months = 12;

    function tile(label, value, sub) {
        return '<div class="tw-tile"><div class="tl">' + esc(label) + '</div><div class="tv">' + value + '</div>' +
            (sub ? '<div class="ts">' + sub + '</div>' : '') + '</div>';
    }
    function signed(v) {
        if (!T.isNum(v)) return '–';
        return '<span class="' + (v >= 0 ? 'tw-pos' : 'tw-neg') + '">' + pct(v) + '</span>';
    }

    function tiles() {
        var perf = D.performance || {}, rows = perf.returns || [];
        var ff = rows[0] || {}, nifty = rows.filter(function (r) { return r.name === 'NIFTY'; })[0] || {};
        function vs(w) { return T.isNum(ff[w]) && T.isNum(nifty[w]) ? 'NIFTY ' + pct(nifty[w]) + ' · gap ' + pct(ff[w] - nifty[w]) : ''; }
        var stocks = (D.holdings || []).filter(function (h) { return !h.cash; }).length;
        var cash = (D.holdings || []).filter(function (h) { return h.cash; }).reduce(function (a, h) { return a + (h.weight || 0); }, 0);
        $('ff-tiles').innerHTML =
            tile('Portfolio value', '₹' + T.num(D.portfolioValue), stocks + ' stocks · cash ' + Math.round(cash * 100) + '%') +
            tile('Today (1D)', signed(ff['1D']), vs('1D')) +
            tile('1 month', signed(ff['1M']), vs('1M')) +
            tile('Year to date', signed(ff.YTD), vs('YTD')) +
            tile('1 year', signed(ff['1Y']), vs('1Y')) +
            tile('Since inception (p.a.)', signed(ff.Inception), vs('Inception'));
    }

    function returnsTable() {
        var perf = D.performance || {}, rows = (perf.returns || []).slice(), w = perf.windows || [];
        var ff = rows[0], nifty = rows.filter(function (r) { return r.name === 'NIFTY'; })[0];
        if (ff && nifty) {
            var diff = { name: 'Focus Five minus NIFTY' };
            w.forEach(function (k) { diff[k] = T.isNum(ff[k]) && T.isNum(nifty[k]) ? ff[k] - nifty[k] : null; });
            rows.push(diff);
        }
        // green when the row beat NIFTY over that window, red when it lagged (the NIFTY row itself stays plain)
        function vsNifty(r, k) {
            var v = r[k];
            if (!T.isNum(v)) return '';
            var cls = '';
            if (/minus/.test(r.name)) cls = v > 0 ? 'tw-pos' : v < 0 ? 'tw-neg' : '';
            else if (r.name !== 'NIFTY' && nifty && T.isNum(nifty[k])) cls = v > nifty[k] ? 'tw-pos' : v < nifty[k] ? 'tw-neg' : '';
            var txt = pct(v);
            return cls ? '<span class="' + cls + '" style="font-weight:600">' + txt + '</span>' : txt;
        }
        T.render('ff-returns', rows, [{ key: 'name', label: '', left: true, html: function (r) { return r.name === 'Focus Five' || /minus/.test(r.name) ? '<b>' + esc(r.name) + '</b>' : esc(r.name); } }]
            .concat(w.map(function (k) { return { key: k, label: k, html: function (r) { return vsNifty(r, k); }, value: function (r) { return r[k]; } }; })), { sort: null });
    }

    function chart() {
        var perf = D.performance || {}, dates = perf.dates || [], series = perf.series || {};
        if (!dates.length) return;
        var end = new Date(dates[dates.length - 1]), start = new Date(end);
        start.setMonth(start.getMonth() - months);
        var from = start.toISOString().slice(0, 10), i0 = 0;
        while (i0 < dates.length - 1 && dates[i0] < from) i0++;
        var names = Object.keys(series);
        TwChart.line('ff-chart', {
            labels: dates.slice(i0), height: 320, measure: 'return',
            yFmt: function (v) { return v.toFixed(0); },
            series: names.map(function (n, i) {
                var s = series[n].slice(i0), base = null;
                for (var j = 0; j < s.length && base == null; j++) if (T.isNum(s[j])) base = s[j];
                return { name: n, index: n === 'NIFTY', color: i,
                         data: s.map(function (v) { return T.isNum(v) && base ? v / base * 100 : null; }) };
            })
        });
    }
    Array.prototype.forEach.call(document.querySelectorAll('#ff-range button'), function (b) {
        b.addEventListener('click', function () {
            months = +b.getAttribute('data-v');
            Array.prototype.forEach.call(document.querySelectorAll('#ff-range button'), function (x) { x.classList.toggle('on', x === b); });
            if (D) chart();
        });
    });

    function broker(name) {
        return {
            key: 'b' + name, label: name, left: true,
            value: function (r) { var b = r.brokers && r.brokers[name]; return b ? b.upside : null; },
            html: function (r) {
                var b = r.brokers && r.brokers[name];
                if (!b) return '';
                return esc(b.reco || '') + ' ' + (b.tp ? T.num(b.tp) : '') +
                    (T.isNum(b.upside) ? ' <span class="' + (b.upside >= 0 ? 'tw-pos' : 'tw-neg') + '">(' + pct(b.upside) + ')</span>' : '');
            }
        };
    }
    function holdings() {
        var rows = (D.holdings || []).map(function (h) {
            var c = h.consensus || {};
            return Object.assign({}, h, {
                analysts: c.analysts, tpAvg: c.tpAvg, tpMin: c.tpMin, tpMax: c.tpMax, upside: c.upside,
                eps1: c.eps1Rev4w, tp4w: c.tpRev4w, rank: c.scoreRank
            });
        });
        T.render('ff-holdings', rows, [
            { key: 'name', label: 'Stock', left: true, html: function (r) { return r.cash ? esc(r.name) : T.stock(r); }, value: function (r) { return r.name; } },
            { key: 'weight', label: 'Weight', html: function (r) { return T.isNum(r.weight) ? (r.weight * 100).toFixed(1) + '%' : ''; }, value: function (r) { return r.weight; } },
            { key: 'value', label: 'Value', kind: 'num' },
            { key: 'gain', label: 'Gain', kind: 'pct', heat: 0.3, tip: 'Market value vs cost' },
            { key: 'price', label: 'Price', kind: 'num', dp: 1 },
            { key: 'r1D', label: '1D', kind: 'pct', heat: 0.03 },
            { key: 'r1W', label: '1W', kind: 'pct', heat: 0.06 },
            { key: 'r1M', label: '1M', kind: 'pct', heat: 0.1 },
            { key: 'rYTD', label: 'YTD', kind: 'pct', heat: 0.4 },
            { key: 'analysts', label: 'Analysts', kind: 'int' },
            { key: 'tpAvg', label: 'Target Low / Avg / High', html: function (r) { return r.tpAvg ? T.num(r.tpMin) + ' / <b>' + T.num(r.tpAvg) + '</b> / ' + T.num(r.tpMax) : ''; } },
            { key: 'upside', label: 'Upside', kind: 'pct', heat: 0.3 },
            { key: 'eps1', label: 'EPS FY1 4W', kind: 'pct', heat: 0.08 },
            { key: 'tp4w', label: 'Target 4W', kind: 'pct', heat: 0.08 },
            { key: 'rank', label: 'Score rank', kind: 'int' }
        ].concat(['AMBIT', 'MOSL', 'AXIS', 'PL'].filter(function (n) {
            return n === 'AMBIT' || n === 'MOSL' || rows.some(function (r) { return r.brokers && r.brokers[n]; });
        }).map(broker)), { sort: 'weight', desc: true });
    }

    fetch('/adviser/api/getfocusfiveconsensus', { credentials: 'same-origin' })
        .then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
        .then(function (data) {
            D = data;
            $('ff-asof').textContent = 'Consensus as of ' + T.day(D.asOf);
            $('ff-pricedate').textContent = 'Prices as of ' + T.day((D.performance || {}).end || D.priceDate);
            tiles(); returnsTable(); chart(); holdings();
        })
        .catch(function () {
            $('ff-tiles').innerHTML = '<p class="cs-note">Could not load the Focus Five data (are you signed in as the adviser?).</p>';
        });
})();
