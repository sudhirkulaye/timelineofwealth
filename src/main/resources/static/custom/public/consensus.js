/*
 * /public/consensus: weekly Yahoo consensus revisions with daily prices, from /public/api/getconsensusdashboard.
 * No broker names and no portfolio weights are served to this page.
 */
(function () {
    'use strict';
    var $ = function (id) { return document.getElementById(id); };
    var T = window.CsTable, pct = T.pct, esc = T.esc;
    var D = null, drawn = {};

    function tile(label, value, sub) {
        return '<div class="tw-tile"><div class="tl">' + esc(label) + '</div><div class="tv">' + value + '</div>' +
            (sub ? '<div class="ts">' + sub + '</div>' : '') + '</div>';
    }
    function signed(v, dp) {
        if (!T.isNum(v)) return '–';
        return '<span class="' + (v >= 0 ? 'tw-pos' : 'tw-neg') + '">' + pct(v, dp) + '</span>';
    }

    // ------------------------------------------------------------------ tabs
    function show(tab) {
        Array.prototype.forEach.call(document.querySelectorAll('#cs-tabs button'), function (b) {
            b.classList.toggle('on', b.getAttribute('data-v') === tab);
        });
        Array.prototype.forEach.call(document.querySelectorAll('.cs-pane'), function (p) {
            p.classList.toggle('on', p.id === 'pane-' + tab);
        });
        if (D && !drawn[tab]) { drawn[tab] = true; DRAW[tab](); }
        if (history.replaceState) history.replaceState(null, '', '#' + tab);
        window.dispatchEvent(new Event('resize'));   // charts drawn while hidden
    }
    Array.prototype.forEach.call(document.querySelectorAll('#cs-tabs button'), function (b) {
        b.addEventListener('click', function () { show(b.getAttribute('data-v')); });
    });

    // ------------------------------------------------------------------ market
    function market() {
        var m = D.market || {}, sb = D.scoreboard || [];
        var last = sb.length ? sb[sb.length - 1] : {}, prev = sb.length > 1 ? sb[sb.length - 2] : {};
        var net = T.isNum(last.up) && T.isNum(last.down) ? last.up - last.down : null;
        var netPrev = T.isNum(prev.up) && T.isNum(prev.down) ? prev.up - prev.down : null;
        var nifty = (m.indices || []).filter(function (x) { return x.name === 'NIFTY'; })[0] || {};
        $('mk-tiles').innerHTML =
            tile('Stocks covered', T.num(m.covered), T.num(m.scored) + ' with 5+ analysts (scored)') +
            tile('EPS upgrades (4W)', pct(last.up, 0).replace('+', ''), 'of the ' + T.num(m.scored) + ' scored stocks') +
            tile('EPS downgrades (4W)', pct(last.down, 0).replace('+', ''), 'of the ' + T.num(m.scored) + ' scored stocks') +
            tile('Net revisions', signed(net, 0), 'last week ' + (T.isNum(netPrev) ? pct(netPrev, 0) : '–')) +
            tile('NIFTY 1M', signed(nifty['1M']), 'YTD ' + (T.isNum(nifty.YTD) ? pct(nifty.YTD) : '–'));
        var labels = sb.map(function (r) { return r.d; });
        TwChart.line('mk-breadth', {
            labels: labels, zero: true, height: 280,
            yFmt: function (v) { return Math.round(v * 100) + '%'; },
            series: [
                { name: 'Upgrades', data: sb.map(function (r) { return r.up; }), color: 2 },
                { name: 'Downgrades', data: sb.map(function (r) { return r.down; }), color: 1 },
                { name: 'Net', type: 'bar', data: sb.map(function (r) { return T.isNum(r.up) && T.isNum(r.down) ? r.up - r.down : null; }), color: 0 }
            ]
        });
        var ic = m.indexChart || {};
        if (ic.dates && ic.dates.length) {
            $('mk-index-note').textContent = 'Cumulative return since ' + T.day(ic.start) + ', daily closes.';
            TwChart.line('mk-index-chart', {
                labels: ic.dates, zero: true, height: 300,
                yFmt: function (v) { return Math.round(v * 100) + '%'; },
                tipFmt: function (v) { return pct(v); },
                series: Object.keys(ic.series || {}).map(function (n, i) { return { name: n, data: ic.series[n], index: n === 'NIFTY', color: i }; })
            });
        }
        T.render('mk-indices', m.indices || [], [
            { key: 'name', label: 'Index', left: true },
            { key: '1D', label: '1D', kind: 'pct', heat: 0.02 },
            { key: '1W', label: '1W', kind: 'pct', heat: 0.04 },
            { key: '1M', label: '1M', kind: 'pct', heat: 0.08 },
            { key: '3M', label: '3M', kind: 'pct', heat: 0.15 },
            { key: 'YTD', label: 'YTD', kind: 'pct', heat: 0.25 },
            { key: '1Y', label: '1Y', kind: 'pct', heat: 0.3 }
        ]);
    }

    // ------------------------------------------------------------------ industries
    var indView = 'sector', indSector = '';
    function indCols(first) {
        return [first,
            { key: 'n', label: 'Stocks', kind: 'int', tip: 'Scored stocks (5+ analysts and a price)' },
            { key: 'net', label: 'Net EPS upgrades', kind: 'pct', heat: 0.6,
              tip: 'Share of scored stocks whose consensus EPS rose more than 0.5% in 4 weeks, minus the share whose EPS fell more than 0.5%' },
            { key: 'eps4w', label: 'EPS 4W', kind: 'pct', heat: 0.05, tip: 'Median 4-week change in consensus average EPS (nearer fiscal year)' },
            { key: 'eps13w', label: 'EPS 13W', kind: 'pct', heat: 0.1, tip: 'Median 13-week change in consensus average EPS' },
            { key: 'tp4w', label: 'TP 4W', kind: 'pct', heat: 0.05, tip: 'Median 4-week change in consensus average target price' },
            { key: 'rel1m', label: '1M vs NIFTY', kind: 'pct', heat: 0.08, tip: 'Median of (stock 1-month return minus NIFTY 1-month return), latest trading day' },
            { key: 'rel3m', label: '3M vs NIFTY', kind: 'pct', heat: 0.15, tip: 'Median of (stock 3-month return minus NIFTY 3-month return), latest trading day' }
        ];
    }
    function industries() {
        var sectors = D.sectors || [], groups = D.groups || [];
        var sel = $('ind-sector');
        if (!sel.options.length) {
            sel.innerHTML = '<option value="">All sectors</option>' + sectors.map(function (x) { return '<option>' + esc(x.name) + '</option>'; }).join('');
            sel.addEventListener('change', function () { indSector = sel.value; industries(); });
        }
        sel.value = indSector;
        T.render('ind-sectors', sectors, indCols({ key: 'order', label: 'Sector', left: true,
            html: function (r) { return '<a href="#industries" class="cs-pick" data-s="' + esc(r.name) + '"><b>' + esc(r.name) + '</b></a> <span class="sub">' + r.groups + (r.groups === 1 ? ' group' : ' groups') + '</span>'; },
            value: function (r) { return -r.order; } }), { sort: null });
        Array.prototype.forEach.call(document.querySelectorAll('#ind-sectors .cs-pick'), function (a) {
            a.addEventListener('click', function (e) {
                e.preventDefault();
                indSector = a.getAttribute('data-s');
                industries();
                $('ind-table-card').scrollIntoView({ behavior: 'smooth', block: 'start' });
            });
        });
        var rows = groups.filter(function (g) { return !indSector || g.sector === indSector; });
        var first = { key: 'name', label: 'Group', left: true,
            html: function (r) { return esc(r.name) + (indView === 'all' && r.sector !== r.name ? ' <span class="sub">' + esc(r.sector) + '</span>' : ''); } };
        if (indView === 'sector') T.render('ind-table', rows, indCols(first), { sort: null, group: 'sector' });
        else T.render('ind-table', rows, indCols(first), { sort: 'net', desc: true });
    }
    Array.prototype.forEach.call(document.querySelectorAll('#ind-view button'), function (b) {
        b.addEventListener('click', function () {
            indView = b.getAttribute('data-v');
            Array.prototype.forEach.call(document.querySelectorAll('#ind-view button'), function (x) { x.classList.toggle('on', x === b); });
            if (D) industries();
        });
    });

    // ------------------------------------------------------------------ idea lists
    // Same columns as the weekly dashboard's idea tables.
    var STOCK = { key: 'ticker', label: 'Stock', left: true, html: T.stock2, value: function (r) { return r.ticker; } };
    var SCORE = { key: 'pct', label: 'Score', html: function (r) { return T.meter(r.pct); },
                  tip: 'Composite score as a percentile among this week\'s scored stocks: 100 is the best' };
    function ideaCols(shortSide) {
        return [
            { key: 'pos', label: '#', kind: 'int', tip: 'Position in the list' },
            STOCK,
            { key: 'industry', label: 'Industry', left: true, html: function (r) { return '<span class="cs-ind">' + esc(r.industry) + '</span>'; } },
            { key: 'mcap', label: 'Mkt cap', html: function (r) { return T.cr(r.mcap); }, value: function (r) { return r.mcap; } },
            SCORE,
            { key: 'eps4', label: 'EPS 4W', kind: 'pct', color: true, tip: '4-week change in consensus EPS (the two fiscal years Yahoo shows, averaged)' },
            { key: 'tp4', label: 'TP 4W', kind: 'pct', color: true, tip: '4-week change in the consensus target price' },
            { key: 'mom', label: 'Price 4W', kind: 'pct', color: true, tip: 'Price return over the 4 weeks to the consensus date' },
            { key: 'mom12', label: '12-1M', kind: 'pct', dp: 0, color: true, tip: 'Price return from 12 months ago to 1 month ago (12-1 month momentum)' },
            { key: 'rel1m', label: '1M vs NIFTY', kind: 'pct', color: true, tip: '1-month return minus NIFTY, latest trading day' },
            { key: 'upside', label: 'TP upside', kind: 'pct', dp: 0, color: true, tip: 'Consensus average target vs the latest price' },
            { key: 'analysts', label: 'Analysts', kind: 'int' },
            { key: 'brokers', label: 'Brokers', tip: 'Positive / neutral / negative ratings among the brokers tracked (latest report each)',
              html: function (r) { var b = r.brokers; return b ? b.pos + '/' + b.neu + '/' + b.neg : '<span class="sub">none</span>'; },
              value: function (r) { return r.brokers ? (r.brokers.pos - r.brokers.neg) / r.brokers.n : null; } },
            { key: 'age', label: 'Age', tip: 'Weeks since the Yahoo consensus last changed',
              html: function (r) { return T.isNum(r.age) ? '<span class="' + (r.age < 8 ? 'sub' : '') + '">' + Math.round(r.age) + 'w</span>' : '<span class="sub">–</span>'; } },
            { key: shortSide ? 'driversShort' : 'drivers', label: 'Main drivers', left: true,
              html: function (r) { var d = shortSide ? r.driversShort : r.drivers; return d ? '<span class="cs-drv">' + esc(d) + '</span>' : '<span class="sub">–</span>'; } }
        ];
    }
    /** table with an "F&O stocks only" box; shows the first `limit` rows of the (filtered) list */
    function ideaTable(id, rows, shortSide, limit, countEl) {
        var box = $(id + '-fno');
        function draw() {
            var list = rows.map(function (r, i) { return Object.assign({ pos: i + 1 }, r); })
                .filter(function (r) { return !box || !box.checked || r.fno; });
            var shown = limit ? list.slice(0, limit) : list;
            if (countEl) $(countEl).textContent = 'Top ' + shown.length + ' of ' + list.length + ' ranked';
            T.render(id, shown, ideaCols(shortSide), { sort: null });
        }
        if (box && !box.dataset.on) { box.dataset.on = '1'; box.addEventListener('change', draw); }
        draw();
    }
    function long() {
        $('long-n').textContent = T.num(D.nScored);
        ideaTable('long-table', D.long || [], false, D.topN || 15, 'long-count');
    }
    function short() { ideaTable('short-table', D.short || [], true, null); }

    var divView = 'cut';
    function divergence() {
        $('div-cut-box').hidden = divView !== 'cut';
        $('div-raised-box').hidden = divView !== 'raised';
        if (divView === 'cut') ideaTable('div-cut', D.divCut || [], true, 10);
        else ideaTable('div-raised', D.divRaised || [], false, 10);
    }
    Array.prototype.forEach.call(document.querySelectorAll('#div-view button'), function (b) {
        b.addEventListener('click', function () {
            divView = b.getAttribute('data-v');
            Array.prototype.forEach.call(document.querySelectorAll('#div-view button'), function (x) { x.classList.toggle('on', x === b); });
            if (D) divergence();
        });
    });

    function emerging() {
        var E = D.emerging || {}, STOCK_S = { key: 'ticker', label: 'Stock', left: true, html: T.stock2 };
        $('em-n').textContent = T.num(D.nScored);
        var move = [STOCK_S, SCORE,
            { key: 'pct4w', label: '4 wks ago', html: function (r) { return T.isNum(r.pct4w) ? String(Math.round(r.pct4w)) : '<span class="sub">–</span>'; }, value: function (r) { return r.pct4w; } },
            { key: 'eps4', label: 'EPS 4W', kind: 'pct', color: true }];
        T.render('em-top', E.newTop || [], move, { sort: null });
        T.render('em-bottom', E.newBottom || [], move, { sort: null });
        T.render('em-coverage', E.coverage || [], [STOCK_S,
            { key: 'analysts', label: 'Analysts', kind: 'int' },
            { key: 'countChg13', label: '13W chg', html: function (r) { return T.isNum(r.countChg13) ? (r.countChg13 > 0 ? '+' : '') + Math.round(r.countChg13) : ''; }, value: function (r) { return r.countChg13; } },
            { key: 'eps13', label: 'EPS 13W', kind: 'pct', color: true }], { sort: null });
        T.render('em-steady', E.persistent || [], [STOCK_S,
            { key: 'persist', label: 'Net up weeks', tip: 'Upgrade weeks minus downgrade weeks, last 13', html: function (r) { return T.isNum(r.persist) ? (r.persist > 0 ? '+' : '') + r.persist : ''; }, value: function (r) { return r.persist; } },
            { key: 'eps13', label: 'EPS 13W', kind: 'pct', color: true },
            SCORE], { sort: null });
    }

    // ------------------------------------------------------------------ scoreboard
    function scoreboard() {
        var s = D.scoreboardSummary || {}, rows = (D.scoreboard || []).filter(function (r) { return T.isNum(r.ic); });
        $('sb-tiles').innerHTML =
            tile('Weeks measured', T.num(s.weeks), 'a week counts once its next 4 weeks are known') +
            tile('Average IC', T.isNum(s.avgIc) ? s.avgIc.toFixed(3) : '–', T.isNum(s.positiveIc) ? Math.round(s.positiveIc * 100) + '% of weeks above zero' : '') +
            tile('Long minus short', signed(s.avgSpread), 'average next-4-week gap') +
            tile('Hit rate', T.isNum(s.avgHit) ? Math.round(s.avgHit * 100) + '%' : '–', 'picks that beat (long) or lagged (short) the average');
        TwChart.line('sb-chart', {
            labels: rows.map(function (r) { return r.d; }), zero: true, height: 280,
            yFmt: function (v) { return (v * 100).toFixed(0) + '%'; },
            tipFmt: function (v) { return pct(v); },
            y2Fmt: function (v) { return v.toFixed(2); }, y2Name: 'IC',
            series: [
                { name: 'Long minus short', type: 'bar', data: rows.map(function (r) { return r.spread; }), color: 0 },
                { name: 'IC', data: rows.map(function (r) { return r.ic; }), axis: 1, color: 1 }
            ]
        });
        T.render('sb-table', rows.slice().reverse(), [
            { key: 'd', label: 'Score week', left: true, html: function (r) { return T.day(r.d); } },
            { key: 'ic', label: 'IC', kind: 'num', dp: 3 },
            { key: 'long', label: 'Long vs avg', kind: 'pct', heat: 0.05 },
            { key: 'short', label: 'Short vs avg', kind: 'pct', heat: 0.05 },
            { key: 'spread', label: 'Long minus short', kind: 'pct', heat: 0.06 },
            { key: 'hit', label: 'Hit rate', html: function (r) { return T.isNum(r.hit) ? Math.round(r.hit * 100) + '%' : ''; }, value: function (r) { return r.hit; } }
        ], { sort: 'd', desc: true });
    }

    window.addEventListener('hashchange', function () {
        var tab = (location.hash || '').replace('#', '');
        if (DRAW[tab]) show(tab);
    });

    var DRAW = { market: market, industries: industries, long: long, short: short, divergence: divergence, emerging: emerging, scoreboard: scoreboard };

    fetch('/public/api/getconsensusdashboard', { credentials: 'same-origin' })
        .then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
        .then(function (data) {
            D = data;
            $('cs-asof').textContent = 'Consensus as of ' + T.day(D.asOf);
            $('cs-pricedate').textContent = 'Prices as of ' + T.day(D.priceDate);
            var tab = (location.hash || '').replace('#', '');
            show(DRAW[tab] ? tab : 'market');
        })
        .catch(function () {
            document.querySelector('.cs-pane.on .tw-card').insertAdjacentHTML('beforeend', '<p class="cs-note">Could not load the consensus data.</p>');
        });
})();
