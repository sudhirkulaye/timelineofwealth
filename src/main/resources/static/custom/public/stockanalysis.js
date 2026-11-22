/*
 * /public/stockanalysis/{ticker} — chart-first stock page (Apache ECharts 5).
 *   Price:        /public/api/getstockohlc/{t}     (nse_price_history)
 *   Details:      /public/api/getstockdetails/{t}  (sector, market cap, ratios)
 *   Fundamentals: /public/api/getstockpnl/{t}, /public/api/getstockquarter/{t}
 *   Consensus:    /public/api/getconsensus/{t}     (yahoo_consensus)
 */
(function () {
    'use strict';
    var root = document.getElementById('sa');
    if (!root || !window.echarts) return;
    var TICKER = root.getAttribute('data-ticker');

    // ---------- helpers ----------
    var $ = function (id) { return document.getElementById(id); };
    var MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    function css(name) { return getComputedStyle(document.documentElement).getPropertyValue(name).trim(); }
    function pal() {
        return {
            ink: css('--ink'), ink2: css('--ink2'), muted: css('--muted'), grid: css('--grid'), axis: css('--axis'),
            surface: css('--surface'), page: css('--page'), s1: css('--s1'), s2: css('--s2'), s3: css('--s3'), s4: css('--s4'),
            pos: css('--pos'), neg: css('--neg'), font: css('--font') || 'Inter, system-ui, sans-serif'
        };
    }
    function parseD(s) { var p = s.split('-'); return new Date(+p[0], +p[1] - 1, +p[2]); }
    function fmtD(s) { var d = parseD(s); return d.getDate() + ' ' + MON[d.getMonth()] + ' ' + d.getFullYear(); }
    function fmtShort(s) { var d = parseD(s); return MON[d.getMonth()] + ' ' + String(d.getFullYear()).slice(2); }
    function num(v, dp) {
        if (v == null || isNaN(v)) return '–';
        return Number(v).toLocaleString('en-IN', { minimumFractionDigits: dp || 0, maximumFractionDigits: dp || 0 });
    }
    function px(v) { return v == null ? '–' : '₹' + num(v, v < 100 ? 2 : (v < 1000 ? 1 : 0)); }
    function pct(v, dp) { if (v == null || !isFinite(v)) return '–'; return (v > 0 ? '+' : '') + (v * 100).toFixed(dp == null ? 1 : dp) + '%'; }
    function cls(v) { return v > 0 ? 'tw-pos' : (v < 0 ? 'tw-neg' : ''); }
    function cr(v) {
        if (v == null || isNaN(v)) return '–';
        var a = Math.abs(v);
        if (a >= 100000) return '₹' + num(v / 100000, 2) + ' L Cr';
        return '₹' + num(v, 0) + ' Cr';
    }
    function compact(v) {
        var a = Math.abs(v);
        if (a >= 1e7) return (v / 1e7).toFixed(a >= 1e8 ? 0 : 1) + ' Cr';
        if (a >= 1e5) return (v / 1e5).toFixed(a >= 1e6 ? 0 : 1) + ' L';
        if (a >= 1e3) return (v / 1e3).toFixed(0) + 'K';
        return String(Math.round(v));
    }
    function axisNum(v) {
        var a = Math.abs(v);
        if (a >= 100000) return num(v / 1000, 0) + 'K';
        return num(v, a < 10 && a !== 0 ? 1 : 0);
    }
    function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
    function getJSON(url) {
        return fetch(url).then(function (r) { if (!r.ok) throw new Error(r.status + ' ' + url); return r.json(); });
    }
    function tile(label, value, sub, valueCls) {
        return '<div class="tw-tile"><div class="tl">' + esc(label) + '</div><div class="tv ' + (valueCls || '') + '">' + value +
            '</div>' + (sub ? '<div class="ts">' + sub + '</div>' : '') + '</div>';
    }
    function empty(el, msg) { el.innerHTML = '<div class="sa-empty">' + esc(msg) + '</div>'; }
    function baseText(P) { return { color: P.muted, fontFamily: P.font, fontSize: 11 }; }
    function tooltipBase(P) {
        return {
            backgroundColor: P.surface, borderColor: P.grid, borderWidth: 1, padding: [8, 10],
            textStyle: { color: P.ink, fontFamily: P.font, fontSize: 12 },
            extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,.14);border-radius:10px;'
        };
    }
    function alpha(hex, a) {
        var h = hex.replace('#', '');
        if (h.length === 3) h = h.replace(/./g, '$&$&');
        var n = parseInt(h, 16);
        return 'rgba(' + (n >> 16 & 255) + ',' + (n >> 8 & 255) + ',' + (n & 255) + ',' + a + ')';
    }

    var charts = {};
    function chart(id) {
        var el = $(id);
        if (!charts[id] || charts[id].isDisposed()) charts[id] = echarts.init(el, null, { renderer: 'canvas' });
        return charts[id];
    }
    window.addEventListener('resize', function () { Object.keys(charts).forEach(function (k) { charts[k].resize(); }); });

    // ---------- state ----------
    var S = { ohlc: null, details: null, pnl: null, qtr: null, cons: null, range: '1Y', kind: 'line', period: 'A',
              sel: null, pending: null, drag: null };

    // =====================================================================
    // Price chart
    // =====================================================================
    function sliceRange() {
        var d = S.ohlc.d, n = d.length;
        if (!n) return { from: 0, to: -1 };
        if (S.range === 'MAX') return { from: 0, to: n - 1 };
        var last = parseD(d[n - 1]);
        var months = { '1M': 1, '6M': 6, '1Y': 12, '3Y': 36, '5Y': 60 }[S.range];
        var start = new Date(last.getFullYear(), last.getMonth() - months, last.getDate());
        var i = 0;
        while (i < n - 1 && parseD(d[i]) < start) i++;
        return { from: i, to: n - 1 };
    }

    function viewData() {
        var r = sliceRange(), o = S.ohlc;
        return {
            from: r.from,
            d: o.d.slice(r.from, r.to + 1), o: o.o.slice(r.from, r.to + 1), h: o.h.slice(r.from, r.to + 1),
            l: o.l.slice(r.from, r.to + 1), c: o.c.slice(r.from, r.to + 1), v: o.v.slice(r.from, r.to + 1)
        };
    }

    function renderPrice() {
        var el = $('sa-price-chart');
        if (!S.ohlc || !S.ohlc.d.length) { empty(el, 'No NSE price history for ' + TICKER + '.'); $('sa-readout').innerHTML = '&#160;'; return; }
        var P = pal(), V = viewData(), n = V.d.length;
        S.view = V;
        var first = V.c[0], last = V.c[n - 1], up = last >= first;
        var lineCol = up ? P.pos : P.neg;
        var sel = S.sel;
        var selUp = sel ? V.c[sel[1]] >= V.c[sel[0]] : up;
        var selCol = selUp ? P.pos : P.neg;

        var volColors = V.c.map(function (c, i) { return (i === 0 ? c >= V.o[i] : c >= V.c[i - 1]) ? alpha(P.pos, .45) : alpha(P.neg, .45); });
        var mainSeries;
        if (S.kind === 'candle') {
            mainSeries = {
                name: 'Price', type: 'candlestick', xAxisIndex: 0, yAxisIndex: 0,
                data: V.d.map(function (_, i) { return [V.o[i], V.c[i], V.l[i], V.h[i]]; }),
                itemStyle: { color: P.pos, color0: P.neg, borderColor: P.pos, borderColor0: P.neg },
                large: n > 600
            };
        } else {
            mainSeries = {
                name: 'Price', type: 'line', xAxisIndex: 0, yAxisIndex: 0, data: V.c, showSymbol: false, symbolSize: 7,
                lineStyle: { width: 2, color: lineCol }, itemStyle: { color: lineCol },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: alpha(lineCol, .22) }, { offset: 1, color: alpha(lineCol, 0) }]) },
                emphasis: { disabled: true }
            };
        }
        // selection overlay
        var a = sel ? sel : (S.pending != null ? [S.pending, S.pending] : null);
        if (a) {
            mainSeries.markArea = sel ? {
                silent: true, itemStyle: { color: alpha(selCol, .10) },
                data: [[{ xAxis: V.d[a[0]] }, { xAxis: V.d[a[1]] }]]
            } : undefined;
            mainSeries.markLine = {
                silent: true, symbol: 'none', label: { show: false },
                lineStyle: { color: sel ? selCol : P.ink2, type: 'solid', width: 1 },
                data: (sel ? [a[0], a[1]] : [a[0]]).map(function (i) { return { xAxis: V.d[i] }; })
            };
            mainSeries.markPoint = {
                silent: true, symbol: 'circle', symbolSize: 9,
                itemStyle: { color: sel ? selCol : P.ink2, borderColor: P.surface, borderWidth: 2 }, label: { show: false },
                data: (sel ? [a[0], a[1]] : [a[0]]).map(function (i) { return { coord: [V.d[i], V.c[i]] }; })
            };
        }

        var c = chart('sa-price-chart');
        c.setOption({
            animation: false,
            textStyle: { fontFamily: P.font },
            axisPointer: { link: [{ xAxisIndex: 'all' }], lineStyle: { color: P.axis }, label: { backgroundColor: P.ink2 } },
            tooltip: Object.assign(tooltipBase(P), {
                trigger: 'axis', axisPointer: { type: 'line', lineStyle: { color: P.axis } },
                formatter: function (ps) {
                    var i = ps[0].dataIndex, chg = i > 0 ? V.c[i] / V.c[i - 1] - 1 : null;
                    var s = '<div style="font-weight:600;margin-bottom:4px">' + fmtD(V.d[i]) + '</div>';
                    if (S.kind === 'candle') {
                        s += 'O ' + px(V.o[i]) + ' &nbsp;H ' + px(V.h[i]) + '<br/>L ' + px(V.l[i]) + ' &nbsp;C <b>' + px(V.c[i]) + '</b>';
                    } else {
                        s += 'Close <b>' + px(V.c[i]) + '</b>';
                    }
                    if (chg != null) s += ' <span style="color:' + (chg >= 0 ? P.pos : P.neg) + '">' + pct(chg, 2) + '</span>';
                    s += '<br/><span style="color:' + P.muted + '">Volume ' + compact(V.v[i]) + '</span>';
                    return s;
                }
            }),
            grid: [{ left: 8, right: 64, top: 10, height: '70%' }, { left: 8, right: 64, top: '80%', bottom: 26 }],
            xAxis: [
                { type: 'category', data: V.d, boundaryGap: S.kind === 'candle', axisLine: { lineStyle: { color: P.axis } },
                  axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
                { type: 'category', gridIndex: 1, data: V.d, boundaryGap: S.kind === 'candle', axisLine: { lineStyle: { color: P.axis } },
                  axisTick: { show: false }, splitLine: { show: false },
                  axisLabel: Object.assign(baseText(P), { hideOverlap: true,
                      formatter: function (s) { return n <= 40 ? fmtD(s).replace(/ \d{4}$/, '') : fmtShort(s); } }) }
            ],
            yAxis: [
                { scale: true, position: 'right', splitNumber: 5, axisLabel: Object.assign(baseText(P), { formatter: axisNum }),
                  splitLine: { lineStyle: { color: P.grid } }, axisLine: { show: false } },
                { gridIndex: 1, position: 'right', splitNumber: 2, axisLabel: Object.assign(baseText(P), { formatter: compact }),
                  splitLine: { show: false }, axisLine: { show: false } }
            ],
            series: [
                mainSeries,
                { name: 'Volume', type: 'bar', xAxisIndex: 1, yAxisIndex: 1, barCategoryGap: '20%', large: n > 600,
                  data: V.v.map(function (v, i) { return { value: v, itemStyle: { color: volColors[i] } }; }) }
            ]
        }, true);
        renderReadout();
    }

    function renderReadout() {
        var V = S.view, el = $('sa-readout');
        if (!V || !V.d.length) return;
        var n = V.d.length;
        function span(i, j, label) {
            var r = V.c[j] / V.c[i] - 1, abs = V.c[j] - V.c[i];
            var days = Math.round((parseD(V.d[j]) - parseD(V.d[i])) / 864e5);
            var cagr = days > 400 ? Math.pow(V.c[j] / V.c[i], 365 / days) - 1 : null;
            return (label ? '<span>' + label + '</span>' : '') +
                '<span class="big ' + cls(r) + '">' + pct(r, 2) + '</span>' +
                '<span class="' + cls(abs) + '">' + (abs >= 0 ? '+' : '−') + px(Math.abs(abs)) + '</span>' +
                '<span><b>' + fmtD(V.d[i]) + '</b> ' + px(V.c[i]) + ' → <b>' + fmtD(V.d[j]) + '</b> ' + px(V.c[j]) + '</span>' +
                '<span>' + num(days) + ' days' + (cagr != null ? ' · CAGR <b class="' + cls(cagr) + '">' + pct(cagr, 1) + '</b>' : '') + '</span>';
        }
        if (S.sel) {
            var i = Math.min(S.sel[0], S.sel[1]), j = Math.max(S.sel[0], S.sel[1]);
            el.innerHTML = span(i, j, '<i class="fa fa-arrows-h"></i> Selected') +
                '<button class="x" type="button" id="sa-clear" title="Clear selection (Esc)">✕ Clear</button>';
            $('sa-clear').onclick = clearSel;
        } else if (S.pending != null) {
            el.innerHTML = '<span><i class="fa fa-crosshairs"></i> Start: <b>' + fmtD(V.d[S.pending]) + '</b> ' + px(V.c[S.pending]) +
                '</span><span class="tw-muted">Click a second point to measure the return.</span>' +
                '<button class="x" type="button" id="sa-clear">✕ Cancel</button>';
            $('sa-clear').onclick = clearSel;
        } else {
            el.innerHTML = span(0, n - 1, '<b>' + (S.range === 'MAX' ? 'Max' : S.range) + '</b>');
        }
    }

    function clearSel() { S.sel = null; S.pending = null; S.drag = null; renderPrice(); }

    function idxAt(c, x) {
        var V = S.view; if (!V) return null;
        var v = c.convertFromPixel({ xAxisIndex: 0 }, x);
        if (v == null || isNaN(v)) return null;
        return Math.max(0, Math.min(V.d.length - 1, Math.round(v)));
    }
    function inPlot(c, e) { return c.containPixel({ gridIndex: 0 }, [e.offsetX, e.offsetY]) || c.containPixel({ gridIndex: 1 }, [e.offsetX, e.offsetY]); }

    function bindMeasure() {
        var c = chart('sa-price-chart'), zr = c.getZr();
        zr.on('mousedown', function (e) {
            if (!S.view || !inPlot(c, e)) return;
            var i = idxAt(c, e.offsetX); if (i == null) return;
            S.drag = { start: i, x: e.offsetX, moved: false };
        });
        zr.on('mousemove', function (e) {
            if (!S.drag) return;
            if (Math.abs(e.offsetX - S.drag.x) < 6 && !S.drag.moved) return;
            var i = idxAt(c, e.offsetX); if (i == null) return;
            S.drag.moved = true; S.pending = null;
            var next = [Math.min(S.drag.start, i), Math.max(S.drag.start, i)];
            if (next[0] === next[1]) return;
            if (!S.sel || S.sel[0] !== next[0] || S.sel[1] !== next[1]) { S.sel = next; renderPrice(); }
        });
        function up(e) {
            if (!S.drag) return;
            var d = S.drag; S.drag = null;
            if (d.moved) return;                         // drag finished; selection already shown
            var i = d.start;                             // a click: two clicks make a selection
            if (S.pending == null || S.sel) { S.sel = null; S.pending = i; }
            else if (i !== S.pending) { S.sel = [Math.min(S.pending, i), Math.max(S.pending, i)]; S.pending = null; }
            renderPrice();
        }
        zr.on('mouseup', up);
        zr.on('globalout', function () { if (S.drag && S.drag.moved) S.drag = null; });
        document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && (S.sel || S.pending != null)) clearSel(); });
    }

    function bindSeg(id, attr, key, cb) {
        $(id).addEventListener('click', function (e) {
            var b = e.target.closest('button'); if (!b) return;
            Array.prototype.forEach.call(this.querySelectorAll('button'), function (x) { x.classList.toggle('on', x === b); });
            S[key] = b.getAttribute(attr); cb();
        });
    }

    // =====================================================================
    // Header + tiles
    // =====================================================================
    function renderHeader() {
        var D = S.details, O = S.ohlc;
        if (D) {
            if (D.name) $('sa-name').textContent = D.name;
            var crumbs = [D.sectorNameDisplay, D.industryNameDisplay, D.subIndustryNameDisplay].filter(Boolean);
            $('sa-crumbs').textContent = crumbs.join('  ›  ') || ' ';
            var idx = D.includedIndexName || (D.isNifty50 ? 'NIFTY 50' : '');
            if (idx) { $('sa-index').hidden = false; $('sa-index').textContent = idx; }
            document.title = (D.shortName || D.name || TICKER) + ' · Stock analysis';
        }
        if (O && O.d.length) {
            var n = O.d.length, last = O.c[n - 1], prev = n > 1 ? O.c[n - 2] : null;
            $('sa-price').textContent = px(last);
            if (prev) {
                var ch = last - prev, r = last / prev - 1;
                $('sa-chg').className = 'sa-chg ' + cls(ch);
                $('sa-chg').textContent = (ch >= 0 ? '+' : '−') + px(Math.abs(ch)).replace('₹', '₹') + '  (' + pct(r, 2) + ')  1D';
            }
            $('sa-asof').textContent = 'NSE close · ' + fmtD(O.d[n - 1]);
        }
        renderTiles();
    }

    function renderTiles() {
        var D = S.details, O = S.ohlc, html = '';
        var dd = D && D.dailyDataS;
        if (dd) {
            html += tile('Market cap', cr(dd.marketCap), D.marketcapRank ? 'Rank ' + D.marketcapRank : '');
            html += tile('P/E (TTM)', dd.peTtm ? num(dd.peTtm, 1) + '×' : '–', dd.historicalPe3years ? '3Y avg ' + num(dd.historicalPe3years, 1) + '×' : '');
            html += tile('P/B', dd.pbTtm ? num(dd.pbTtm, 1) + '×' : '–', '');
            html += tile('ROCE', dd.roce != null ? num(dd.roce, 1) + '%' : '–', dd.avgRoce3years != null ? '3Y avg ' + num(dd.avgRoce3years, 1) + '%' : '');
            html += tile('ROE', dd.roe != null ? num(dd.roe, 1) + '%' : '–', dd.avgRoe3years != null ? '3Y avg ' + num(dd.avgRoe3years, 1) + '%' : '');
        }
        if (O && O.d.length) {
            var n = O.d.length, lastD = parseD(O.d[n - 1]);
            var yAgo = new Date(lastD.getFullYear() - 1, lastD.getMonth(), lastD.getDate());
            var hi = -Infinity, lo = Infinity;
            for (var i = n - 1; i >= 0 && parseD(O.d[i]) >= yAgo; i--) { hi = Math.max(hi, O.h[i]); lo = Math.min(lo, O.l[i]); }
            var last = O.c[n - 1];
            html += tile('52-week range', px(lo) + ' – ' + px(hi), pct(last / hi - 1, 1) + ' from high');
        }
        var C = S.cons;
        if (C && C.dates && C.dates.length) {
            var k = C.dates.length - 1, tp = C.tpAvg[k], price = O && O.c.length ? O.c[O.c.length - 1] : C.price[k];
            if (tp && price) html += tile('Consensus TP', px(tp), '<span class="' + cls(tp / price - 1) + '">' + pct(tp / price - 1, 1) + '</span> upside · ' + (C.analysts[k] || '–') + ' analysts');
        }
        $('sa-tiles').innerHTML = html;
    }

    // =====================================================================
    // Fundamentals
    // =====================================================================
    function fyLabel(dateStr) { var d = parseD(dateStr); var y = d.getMonth() >= 3 ? d.getFullYear() + 1 : d.getFullYear(); return 'FY' + String(y).slice(2); }
    function qLabel(dateStr) { var d = parseD(dateStr); return MON[d.getMonth()] + ' ' + String(d.getFullYear()).slice(2); }
    function preferConsolidated(rows) {
        var hasC = rows.some(function (r) { return r.key.consStandalone === 'C'; });
        return rows.filter(function (r) { return r.key.consStandalone === (hasC ? 'C' : 'S'); })
            .sort(function (a, b) { return a.key.date < b.key.date ? -1 : 1; });
    }
    function isFinancial() {
        var D = S.details || {};
        return /financ|bank/i.test((D.sectorNameDisplay || '') + ' ' + (D.industryNameDisplay || ''));
    }
    // share count (crore) for split-consistent EPS: today's market cap / today's price (both post any split or bonus),
    // else the latest quarter's market cap / price, else latest annual NP / EPS
    function sharesCr(pnl, qtr) {
        var dd = S.details && S.details.dailyDataS, O = S.ohlc;
        if (dd && dd.marketCap > 0 && O && O.c.length) return dd.marketCap / O.c[O.c.length - 1];
        for (var i = qtr.length - 1; i >= 0; i--) if (qtr[i].mcap > 0 && qtr[i].price > 0) return qtr[i].mcap / qtr[i].price;
        for (var j = pnl.length - 1; j >= 0; j--) if (pnl[j].eps > 0 && pnl[j].netProfit) return pnl[j].netProfit / pnl[j].eps;
        return null;
    }

    function renderFundamentals() {
        var pnl = preferConsolidated(S.pnl || []), qtr = preferConsolidated(S.qtr || []);
        var fin = isFinancial(), sh = sharesCr(pnl, qtr), annual = S.period === 'A';
        var rows = annual ? pnl.slice(-10) : qtr.slice(-12);
        var labels = rows.map(function (r) { return annual ? fyLabel(r.key.date) : qLabel(r.key.date); });
        var rev = rows.map(function (r) { return r.sales; });
        var ebitda = rows.map(function (r) { return r.operatingProfit; });
        var eps = rows.map(function (r) { return sh ? r.netProfit / sh : null; });
        var nAct = rows.length;

        // append consensus estimates (annual view only)
        var C = S.cons, estRev = [], estEps = [];
        if (annual && C && C.dates && C.dates.length && nAct) {
            var lastFy = +labels[nAct - 1].slice(2), k = C.dates.length - 1;
            Object.keys(C.fy).sort().forEach(function (fy) {
                var y = +fy.replace(/\D/g, ''); if (!(y > lastFy)) return;
                labels.push(fy + 'E');
                var f = C.fy[fy];
                rev.push(f.revAvg[k]); ebitda.push(null); eps.push(f.epsAvg[k]);
            });
        }

        var last = rows.length ? rows[rows.length - 1] : null;
        $('sa-fin-asof').textContent = last ? 'Data as of ' + (annual ? labels[nAct - 1] + ' (' + fmtD(last.key.date) + ')' : fmtD(last.key.date)) : 'No data';
        $('sa-fin-sub').textContent = annual ? 'Last 10 fiscal years' + (labels.length > nAct ? ', plus consensus estimates (E)' : '') + '. Hover for YoY growth.'
            : 'Last 12 quarters. Hover for YoY growth.';
        var notes = [];
        if (sh) notes.push('EPS = net profit ÷ current share count, so it is comparable across splits and bonuses.');
        if (fin) notes.push('Financial company: EBITDA is not meaningful for lenders; a dedicated banking view is planned.');
        if (last && (Date.now() - parseD(last.key.date)) / 864e5 > (annual ? 550 : 200)) notes.push('Source financials have not been refreshed recently.');
        $('sa-fin-note').textContent = notes.join(' ');

        if (!rows.length) {
            ['sa-rev', 'sa-ebitda', 'sa-eps'].forEach(function (id) { if (charts[id]) charts[id].dispose(); empty($(id), 'No financials available.'); });
            return;
        }
        var lag = annual ? 1 : 4;
        bars('sa-rev', labels, rev, nAct, lag, 's1');
        if (fin) { if (charts['sa-ebitda']) charts['sa-ebitda'].dispose(); empty($('sa-ebitda'), 'Not shown for financial companies.'); }
        else bars('sa-ebitda', labels.slice(0, nAct), ebitda.slice(0, nAct), nAct, lag, 's3');
        if (sh) bars('sa-eps', labels, eps, nAct, lag, 's4', 1);
        else { if (charts['sa-eps']) charts['sa-eps'].dispose(); empty($('sa-eps'), 'EPS not available.'); }
    }

    function bars(id, labels, vals, nAct, lag, colorVar, dp) {
        var P = pal(), col = P[colorVar];
        var el = $(id);
        if (el.querySelector('.sa-empty')) { el.innerHTML = ''; if (charts[id]) charts[id].dispose(); }
        chart(id).setOption({
            animationDuration: 300,
            textStyle: { fontFamily: P.font },
            grid: { left: 4, right: 6, top: 18, bottom: 4, containLabel: true },
            tooltip: Object.assign(tooltipBase(P), {
                trigger: 'axis', axisPointer: { type: 'shadow', shadowStyle: { color: alpha(P.ink, .04) } },
                formatter: function (ps) {
                    var i = ps[0].dataIndex, v = vals[i], prev = i - lag >= 0 ? vals[i - lag] : null;
                    var g = (v != null && prev) ? v / prev - 1 : null;
                    return '<b>' + labels[i] + '</b>' + (i >= nAct ? ' <span style="color:' + P.muted + '">consensus</span>' : '') +
                        '<br/>' + num(v, dp || 0) + (g != null && prev > 0 ? ' <span style="color:' + (g >= 0 ? P.pos : P.neg) + '">' + pct(g, 1) + ' YoY</span>' : '');
                }
            }),
            xAxis: { type: 'category', data: labels, axisTick: { show: false }, axisLine: { lineStyle: { color: P.axis } },
                     axisLabel: Object.assign(baseText(P), { hideOverlap: true, interval: 'auto' }) },
            yAxis: { type: 'value', splitNumber: 3, axisLabel: Object.assign(baseText(P), { formatter: axisNum }),
                     splitLine: { lineStyle: { color: P.grid } } },
            series: [{
                type: 'bar', barMaxWidth: 26,
                data: vals.map(function (v, i) {
                    var est = i >= nAct;
                    return { value: v, itemStyle: {
                        color: est ? alpha(col, .18) : (v < 0 ? P.neg : col),
                        borderColor: est ? col : 'transparent', borderWidth: est ? 1 : 0, borderType: 'dashed',
                        borderRadius: v < 0 ? [0, 0, 3, 3] : [3, 3, 0, 0] } };
                })
            }]
        }, true);
    }

    // =====================================================================
    // Consensus
    // =====================================================================
    function renderConsensus() {
        var C = S.cons, card = $('sa-cons-card');
        if (!C || !C.dates || !C.dates.length) { card.hidden = true; return; }
        card.hidden = false;
        var P = pal(), d = C.dates, k = d.length - 1;
        $('sa-cons-asof').textContent = 'As of ' + fmtD(d[k]) + ' · ' + d.length + ' weeks';

        // tiles
        var price = C.price[k], html = '';
        function chg(arr, weeks) { var j = k - weeks; return j >= 0 && arr[j] ? arr[k] / arr[j] - 1 : null; }
        if (C.tpAvg[k]) {
            html += tile('Target price (avg)', px(C.tpAvg[k]), price ? '<span class="' + cls(C.tpAvg[k] / price - 1) + '">' + pct(C.tpAvg[k] / price - 1, 1) + '</span> vs ' + px(price) : '');
            html += tile('Target range', px(C.tpMin[k]) + ' – ' + px(C.tpMax[k]), C.tpAvg[k] ? 'spread ' + pct((C.tpMax[k] - C.tpMin[k]) / C.tpAvg[k], 0).replace('+', '') : '');
            var t13 = chg(C.tpAvg, 13);
            html += tile('TP change, 13 weeks', '<span class="' + cls(t13) + '">' + pct(t13, 1) + '</span>', '4 weeks: ' + pct(chg(C.tpAvg, 4), 1));
        }
        html += tile('Analysts', C.analysts[k] != null ? String(C.analysts[k]) : '–', k >= 13 && C.analysts[k - 13] != null ? (C.analysts[k] - C.analysts[k - 13] >= 0 ? '+' : '') + (C.analysts[k] - C.analysts[k - 13]) + ' in 13 weeks' : '');
        var fys = Object.keys(C.fy).sort().filter(function (fy) { return C.fy[fy].epsAvg[k] != null; });
        fys.slice(0, 2).forEach(function (fy) {
            var e = C.fy[fy].epsAvg, c13 = chg(e, 13);
            html += tile(fy + ' EPS (avg)', '₹' + num(e[k], 1), '13W revision <span class="' + cls(c13) + '">' + pct(c13, 1) + '</span>');
        });
        $('sa-cons-tiles').innerHTML = html;

        // price vs target: band = tpMin + (tpMax - tpMin) stacked
        var labels = d;
        chart('sa-cons-tp').setOption({
            animationDuration: 300, textStyle: { fontFamily: P.font },
            grid: { left: 4, right: 58, top: 12, bottom: 4, containLabel: true },
            tooltip: Object.assign(tooltipBase(P), {
                trigger: 'axis', axisPointer: { lineStyle: { color: P.axis } },
                formatter: function (ps) {
                    var i = ps[0].dataIndex;
                    return '<b>' + fmtD(d[i]) + '</b><br/>Price ' + px(C.price[i]) + '<br/>TP avg <b>' + px(C.tpAvg[i]) + '</b>' +
                        (C.price[i] && C.tpAvg[i] ? ' <span style="color:' + (C.tpAvg[i] >= C.price[i] ? P.pos : P.neg) + '">' + pct(C.tpAvg[i] / C.price[i] - 1, 1) + '</span>' : '') +
                        '<br/><span style="color:' + P.muted + '">Range ' + px(C.tpMin[i]) + ' – ' + px(C.tpMax[i]) + ' · ' + (C.analysts[i] || '–') + ' analysts</span>';
                }
            }),
            xAxis: { type: 'category', data: labels, boundaryGap: false, axisTick: { show: false }, axisLine: { lineStyle: { color: P.axis } },
                     axisLabel: Object.assign(baseText(P), { hideOverlap: true, formatter: fmtShort }) },
            yAxis: { type: 'value', scale: true, splitNumber: 4, axisLabel: Object.assign(baseText(P), { formatter: axisNum }), splitLine: { lineStyle: { color: P.grid } } },
            series: [
                { name: 'min', type: 'line', stack: 'band', data: C.tpMin, symbol: 'none', lineStyle: { opacity: 0 }, silent: true },
                { name: 'range', type: 'line', stack: 'band', symbol: 'none', lineStyle: { opacity: 0 }, silent: true,
                  areaStyle: { color: alpha(P.s2, .14) },
                  data: C.tpMax.map(function (v, i) { return v != null && C.tpMin[i] != null ? v - C.tpMin[i] : null; }) },
                { name: 'TP avg', type: 'line', data: C.tpAvg, symbol: 'none', lineStyle: { width: 2, color: P.s2 }, itemStyle: { color: P.s2 },
                  endLabel: { show: true, formatter: function () { return px(C.tpAvg[k]); }, color: P.s2, fontSize: 11, fontWeight: 600 } },
                { name: 'Price', type: 'line', data: C.price, symbol: 'none', lineStyle: { width: 2, color: P.s1 }, itemStyle: { color: P.s1 },
                  endLabel: { show: true, formatter: function () { return px(C.price[k]); }, color: P.s1, fontSize: 11, fontWeight: 600 } }
            ]
        }, true);

        // EPS by FY: show the fiscal years that still have a current estimate (max 3)
        var cols = [P.s1, P.s2, P.s3];
        var show = fys.slice(0, 3);
        $('sa-cons-eps-legend').innerHTML = show.map(function (fy, i) { return '<span><i style="background:' + cols[i] + '"></i>' + esc(fy) + ' EPS (avg)</span>'; }).join('');
        chart('sa-cons-eps').setOption({
            animationDuration: 300, textStyle: { fontFamily: P.font },
            grid: { left: 4, right: 44, top: 12, bottom: 4, containLabel: true },
            tooltip: Object.assign(tooltipBase(P), {
                trigger: 'axis', axisPointer: { lineStyle: { color: P.axis } },
                formatter: function (ps) {
                    var i = ps[0].dataIndex, s = '<b>' + fmtD(d[i]) + '</b>';
                    show.forEach(function (fy, j) {
                        var f = C.fy[fy]; if (f.epsAvg[i] == null) return;
                        s += '<br/><span style="color:' + cols[j] + '">●</span> ' + fy + ' <b>₹' + num(f.epsAvg[i], 1) + '</b>' +
                            ' <span style="color:' + P.muted + '">(' + num(f.epsMin[i], 1) + '–' + num(f.epsMax[i], 1) + ')</span>';
                    });
                    return s;
                }
            }),
            xAxis: { type: 'category', data: labels, boundaryGap: false, axisTick: { show: false }, axisLine: { lineStyle: { color: P.axis } },
                     axisLabel: Object.assign(baseText(P), { hideOverlap: true, formatter: fmtShort }) },
            yAxis: { type: 'value', scale: true, splitNumber: 4, axisLabel: Object.assign(baseText(P), { formatter: function (v) { return num(v, v < 10 ? 1 : 0); } }), splitLine: { lineStyle: { color: P.grid } } },
            series: show.map(function (fy, i) {
                return { name: fy, type: 'line', data: C.fy[fy].epsAvg, symbol: 'none', connectNulls: false,
                         lineStyle: { width: 2, color: cols[i] }, itemStyle: { color: cols[i] },
                         endLabel: { show: true, formatter: fy, color: cols[i], fontSize: 11, fontWeight: 600 } };
            })
        }, true);
    }

    // =====================================================================
    // boot
    // =====================================================================
    function renderAll() {
        renderHeader();
        if (S.ohlc) renderPrice();
        if (S.pnl || S.qtr) renderFundamentals();
        renderConsensus();
    }

    bindSeg('sa-range', 'data-r', 'range', function () { S.sel = null; S.pending = null; renderPrice(); });
    bindSeg('sa-kind', 'data-k', 'kind', renderPrice);
    bindSeg('sa-period', 'data-p', 'period', renderFundamentals);
    document.addEventListener('tw-themechange', renderAll);
    if (window.matchMedia) {
        var mq = window.matchMedia('(prefers-color-scheme: dark)');
        (mq.addEventListener ? mq.addEventListener.bind(mq, 'change') : mq.addListener.bind(mq))(renderAll);
    }

    chart('sa-price-chart').showLoading('default', { text: '', color: pal().s1, maskColor: 'transparent' });
    bindMeasure();

    var t = encodeURIComponent(TICKER);
    var pOhlc = getJSON('/public/api/getstockohlc/' + t).then(function (r) { S.ohlc = r; }).catch(function () { S.ohlc = { d: [] }; })
        .then(function () { chart('sa-price-chart').hideLoading(); renderHeader(); renderPrice(); });
    var pDet = getJSON('/public/api/getstockdetails/' + t).then(function (r) { S.details = r; renderHeader(); }).catch(function () {});
    var pCons = getJSON('/public/api/getconsensus/' + t).then(function (r) { S.cons = r; }).catch(function () { S.cons = null; });
    var pFin = Promise.all([
        getJSON('/public/api/getstockpnl/' + t).catch(function () { return []; }),
        getJSON('/public/api/getstockquarter/' + t).catch(function () { return []; })
    ]).then(function (r) { S.pnl = r[0]; S.qtr = r[1]; });
    Promise.all([pDet, pCons, pFin, pOhlc]).then(function () { renderTiles(); renderFundamentals(); renderConsensus(); });
})();
