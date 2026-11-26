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
    var S = { ohlc: null, details: null, pnl: null, qtr: null, cons: null, range: '1Y', kind: 'line', period: 'A', wbPeriod: 'Q',
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
        attachIndex(V);
        S.view = V;
        var cmp = !!V.ix;                                   // compare mode: both lines as % return from range start
        var yv = function (i) { return cmp ? V.c[i] / V.c[0] - 1 : V.c[i]; };
        var first = V.c[0], last = V.c[n - 1], up = last >= first;
        var lineCol = up ? P.pos : P.neg;
        var sel = S.sel;
        var selUp = sel ? V.c[sel[1]] >= V.c[sel[0]] : up;
        var selCol = selUp ? P.pos : P.neg;

        var volColors = V.c.map(function (c, i) { return (i === 0 ? c >= V.o[i] : c >= V.c[i - 1]) ? alpha(P.pos, .45) : alpha(P.neg, .45); });
        var mainSeries, extraSeries = [];
        if (cmp) {
            mainSeries = {
                name: TICKER, type: 'line', xAxisIndex: 0, yAxisIndex: 0, data: V.c.map(function (_, i) { return yv(i); }),
                showSymbol: false, lineStyle: { width: 2, color: lineCol }, itemStyle: { color: lineCol }, emphasis: { disabled: true }
            };
            extraSeries.push({
                name: V.ixName, type: 'line', xAxisIndex: 0, yAxisIndex: 0, showSymbol: false, emphasis: { disabled: true },
                data: V.ix.map(function (v) { return v == null ? null : v / V.ix0 - 1; }), connectNulls: true,
                lineStyle: { width: 1.6, color: P.ink2, type: [5, 3] }, itemStyle: { color: P.ink2 }, z: 1
            });
        } else if (S.kind === 'candle') {
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
                data: (sel ? [a[0], a[1]] : [a[0]]).map(function (i) { return { coord: [V.d[i], yv(i)] }; })
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
                    if (S.kind === 'candle' && !cmp) {
                        s += 'O ' + px(V.o[i]) + ' &nbsp;H ' + px(V.h[i]) + '<br/>L ' + px(V.l[i]) + ' &nbsp;C <b>' + px(V.c[i]) + '</b>';
                    } else {
                        s += 'Close <b>' + px(V.c[i]) + '</b>';
                    }
                    if (chg != null) s += ' <span style="color:' + (chg >= 0 ? P.pos : P.neg) + '">' + pct(chg, 2) + '</span>';
                    if (cmp) {
                        var rs = V.c[i] / V.c[0] - 1, ri = V.ix[i] != null ? V.ix[i] / V.ix0 - 1 : null;
                        s += '<br/>Since ' + fmtD(V.d[0]) + ': ' + TICKER + ' <b style="color:' + (rs >= 0 ? P.pos : P.neg) + '">' + pct(rs, 1) + '</b>' +
                            (ri != null ? ' · ' + V.ixName + ' <b>' + pct(ri, 1) + '</b>' : '');
                    }
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
                { scale: true, position: 'right', splitNumber: 5,
                  axisLabel: Object.assign(baseText(P), { formatter: cmp ? function (v) { return (v > 0 ? '+' : '') + Math.round(v * 100) + '%'; } : axisNum }),
                  splitLine: { lineStyle: { color: P.grid } }, axisLine: { show: false } },
                { gridIndex: 1, position: 'right', splitNumber: 2, axisLabel: Object.assign(baseText(P), { formatter: compact }),
                  splitLine: { show: false }, axisLine: { show: false } }
            ],
            series: extraSeries.concat([
                mainSeries,
                { name: 'Volume', type: 'bar', xAxisIndex: 1, yAxisIndex: 1, barCategoryGap: '20%', large: n > 600,
                  data: V.v.map(function (v, i) { return { value: v, itemStyle: { color: volColors[i] } }; }) }
            ])
        }, true);
        renderReadout();
    }

    // ---------- index comparison ----------
    var INDEX_NAMES = { NIFTY: 'NIFTY 50', BSEMidCap: 'BSE Midcap', BSESmallCap: 'BSE Smallcap', NIFTY200: 'NIFTY 200' };
    S.idx = {};
    /** Aligns the chosen index to the visible trading days (last level on or before each day). */
    function attachIndex(V) {
        var I = S.cmp && S.idx[S.cmp];
        if (!I || !I.d.length) return;
        var out = [], j = 0, lastV = null;
        for (var i = 0; i < V.d.length; i++) {
            while (j < I.d.length && I.d[j] <= V.d[i]) { lastV = I.v[j]; j++; }
            out.push(lastV);
        }
        var base = null;
        for (var k = 0; k < out.length; k++) if (out[k] != null) { base = out[k]; break; }
        if (base == null) return;
        for (var m = 0; m < out.length && out[m] == null; m++) out[m] = base;
        V.ix = out; V.ix0 = base; V.ixName = INDEX_NAMES[S.cmp] || S.cmp;
    }
    function onCompare() {
        var k = S.cmp;
        if (!k || S.idx[k]) { renderPrice(); return; }
        getJSON('/public/api/getindexhistory/' + encodeURIComponent(k))
            .then(function (r) { S.idx[k] = r; }).catch(function () { S.idx[k] = { d: [], v: [] }; })
            .then(renderPrice);
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
                '<span>' + num(days) + ' days' + (cagr != null ? ' · CAGR <b class="' + cls(cagr) + '">' + pct(cagr, 1) + '</b>' : '') + '</span>' +
                benchmark(i, j, r);
        }
        function benchmark(i, j, r) {
            if (!V.ix || V.ix[i] == null || V.ix[j] == null) return '';
            var ri = V.ix[j] / V.ix[i] - 1, diff = r - ri;
            return '<span>' + V.ixName + ' <b class="' + cls(ri) + '">' + pct(ri, 2) + '</b> · ' +
                (diff >= 0 ? 'outperformed' : 'underperformed') + ' by <b class="' + cls(diff) + '">' + Math.abs(diff * 100).toFixed(1) + ' pp</b></span>';
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
    // Indian fiscal quarters: Apr-Jun = Q1 ... Jan-Mar = Q4 of the FY ending that March
    function qName(dateStr) { var m = parseD(dateStr).getMonth(); return 'Q' + (m >= 3 ? Math.floor((m - 3) / 3) + 1 : 4) + ' ' + fyLabel(dateStr); }
    function qLabel(dateStr) { return qName(dateStr); }
    function isFinancial() {
        var D = S.details || {};
        return /financ|bank/i.test((D.sectorNameDisplay || '') + ' ' + (D.industryNameDisplay || ''));
    }
    // share count (crore) for split-consistent EPS: today's market cap / today's price (both post any split or bonus)
    function sharesCr() {
        var dd = S.details && S.details.dailyDataS, O = S.ohlc;
        if (dd && dd.marketCap > 0 && O && O.c.length) return dd.marketCap / O.c[O.c.length - 1];
        return null;
    }

    // ---------- lenders and other financials: ModelBFSI sheet ----------
    // ModelBFSI's profit / EPS rows are filled for every company, so it is used for financials (by sector) and for
    // any company whose ModelBFSI reports net interest income; everyone else gets the HistoricalData P&L
    function useBfsi() { return !!(S.fin && S.fin.bfsi && (S.fin.bfsiHasNii || isFinancial())); }
    function bfsiBlock(kind, period) {
        var B = (S.fin && S.fin.bfsi) || [];
        for (var i = 0; i < B.length; i++) if (B[i].period === period && B[i].block.indexOf(kind) >= 0) return B[i];
        return null;
    }
    function bfsiRow(blk, re) {
        if (!blk) return null;
        for (var i = 0; i < blk.rows.length; i++) if (re.test(blk.rows[i].label)) return blk.rows[i].values;
        return null;
    }
    function bfsiLabel(l) { var m = /^FY(\d{2})Q([1-4])$/.exec(l); return m ? 'Q' + m[2] + ' FY' + m[1] : l; }
    function setTitles(t1, t2, t3, title) {
        $('sa-t1').innerHTML = t1; $('sa-t2').innerHTML = t2; $('sa-t3').innerHTML = t3; $('sa-fin-title').textContent = title;
    }

    function renderBankFundamentals() {
        var annual = S.period === 'A', per = annual ? 'A' : 'Q';
        var inc = bfsiBlock('Income Expense', per), bank = bfsiBlock('Banking', per);
        var all = inc ? inc.labels : [], take = annual ? 10 : 12, from = Math.max(0, all.length - take);
        var labels = all.slice(from).map(bfsiLabel), nAct = labels.length;
        var sl = function (v) { return v ? v.slice(from) : labels.map(function () { return null; }); };
        var has = function (v) { return v && v.some(function (x) { return x != null && x !== 0; }); };
        var pat = bfsiRow(inc, /^Consolidated Profit/) || bfsiRow(inc, /^Standalone Profit/);
        // chart 1: net interest income (lenders); else total income; else revenue from HistoricalData (insurers, AMCs)
        var nii = bfsiRow(inc, /^Net Interest Income/), t1 = 'Net interest income <span class="tw-muted">&#8377; Cr &#183; line = NIM</span>', l1 = null;
        if (has(nii)) l1 = sl(bfsiRow(inc, /^Net Interest Margin/));
        else if (has(bfsiRow(inc, /^Total Income/))) { nii = bfsiRow(inc, /^Total Income/); t1 = 'Total income <span class="tw-muted">&#8377; Cr</span>'; }
        else {
            var H = (annual ? (S.fin.annual || {}) : (S.fin.quarterly || {})), hl = H.label || [];
            nii = all.map(function (l) { var i = hl.indexOf(l); return i >= 0 ? H.sales[i] : null; });
            t1 = 'Revenue <span class="tw-muted">&#8377; Cr</span>';
        }
        nii = sl(nii);
        // chart 2: pre-provision profit with cost to income (lenders); else net profit
        var ppop = bfsiRow(inc, /^Pre Provision Profit/), t2 = 'Pre-provision profit <span class="tw-muted">&#8377; Cr &#183; line = cost to income</span>', l2 = null;
        if (has(ppop)) l2 = sl(bfsiRow(bank, /^Cost to Core Income/));
        else { ppop = pat; t2 = 'Net profit <span class="tw-muted">&#8377; Cr</span>'; }
        ppop = sl(ppop);
        setTitles(t1, t2, 'EPS <span class="tw-muted">&#8377;</span>',
                  has(bfsiRow(inc, /^Net Interest Income/)) ? 'Net interest income, operating profit and EPS' : 'Income, profit and EPS');
        var nim = l1, cti = l2;
        var sh = sharesCr(), eps = sl(pat).map(function (p) { return sh && p != null ? p / sh : null; });

        var C = S.cons;
        if (annual && C && C.dates && C.dates.length && nAct) {
            var lastFy = +labels[nAct - 1].slice(2), k = C.dates.length - 1;
            Object.keys(C.fy).sort().forEach(function (fy) {
                if (!(+fy.replace(/\D/g, '') > lastFy)) return;
                labels.push(fy + 'E'); eps.push(C.fy[fy].epsAvg[k]);
            });
        }
        var lastEnd = inc && inc.ends.length ? inc.ends[inc.ends.length - 1] : null;
        $('sa-fin-asof').textContent = lastEnd ? 'Data as of ' + labels[nAct - 1] + ' (' + fmtD(lastEnd) + ')' : 'No data';
        $('sa-fin-sub').textContent = (annual ? 'Fiscal years (April to March)' + (labels.length > nAct ? ', plus consensus EPS (E)' : '') : 'Last 12 quarters') + '. Hover for YoY growth.';
        $('sa-fin-note').textContent = 'Source: ModelBFSI sheet of ' + S.fin.file + '.' +
            (sh ? ' EPS = consolidated profit ÷ current share count.' : '') +
            (bank && bank.rows.length ? ' Asset quality, deposits and loan mix are under Business details.' : '');
        if (!nAct) { ['sa-rev', 'sa-ebitda', 'sa-eps'].forEach(function (id) { if (charts[id]) charts[id].dispose(); empty($(id), 'No data in ModelBFSI.'); }); return; }
        var lag = annual ? 1 : 4;
        bars('sa-rev', labels.slice(0, nAct), nii, nAct, lag, 's1', 0, has(nim) ? nim : null);
        bars('sa-ebitda', labels.slice(0, nAct), ppop, nAct, lag, 's3', 0, has(cti) ? cti : null);
        if (sh) bars('sa-eps', labels, eps, nAct, lag, 's4', 1);
        else { if (charts['sa-eps']) charts['sa-eps'].dispose(); empty($('sa-eps'), 'EPS not available.'); }
    }

    /** Bank-specific charts for the Business details card, in the same shape as getstockdetails.excelCharts. */
    function bfsiCharts() {
        if (!useBfsi()) return [];
        var out = [];
        ['Q', 'A'].forEach(function (per) {
            var pre = per === 'A' ? 'Annual ' : 'Quarterly ';
            var bank = bfsiBlock('Banking', per), loans = bfsiBlock('Loan Distribution', per);
            function add(title, blk, specs, type, stacked) {
                if (!blk) return;
                var series = [];
                specs.forEach(function (sp) { var v = bfsiRow(blk, sp[1]); if (v && v.some(function (x) { return x != null; })) series.push({ label: sp[0], values: v, chartType: type }); });
                if (series.length) out.push({ title: pre + title, labels: blk.labels, series: series, chartType: type, stacked: !!stacked, bank: true });
            }
            add('Deposits and loans (Rs. Cr)', bank, [['Deposits', /^Deposits$/], ['Loans / AUM', /^Loans \/ AUM$/]], 'bar');
            add('Deposit and loan growth (%)', bank, [['Deposits', /^Deposits Growth/], ['Loans / AUM', /^Loan\/AUM Growth/]], 'line');
            add('Asset quality (%)', bank, [['GNPA', /^GNPA%/], ['NNPA', /^NNPA%/], ['Slippages', /^Slippages%/]], 'line');
            add('Provision coverage and CASA (%)', bank, [['PCR', /^PCR/], ['CASA', /^CASA/]], 'line');
            add('Return ratios (%)', bank, [['RoE', /^RoE%/], ['RoA', /^RoA%/]], 'line');
            add('Cost to income and capital (%)', bank, [['Cost to core income', /^Cost to Core Income/], ['CAR (Tier-1)', /^CAR/]], 'line');
            if (loans) {
                var leaf = loans.rows.filter(function (r) { return !/^(Total|Corporate Total|Contribution:|Growth:)/.test(r.label); });
                add('Loan book mix (Rs. Cr)', loans, leaf.map(function (r) { return [r.label, new RegExp('^' + r.label.replace(/[.*+?^${}()|[\]\\/]/g, '\\$&') + '$')]; }), 'bar', true);
                var contrib = loans.rows.filter(function (r) { return /^Contribution:/.test(r.label); });
                add('Loan book share (%)', loans, contrib.map(function (r) { return [r.label.replace('Contribution: ', ''), new RegExp('^' + r.label.replace(/[.*+?^${}()|[\]\\/]/g, '\\$&') + '$')]; }), 'line');
                var growth = loans.rows.filter(function (r) { return /^Growth:/.test(r.label); });
                add('Loan growth by segment (%)', loans, growth.map(function (r) { return [r.label.replace('Growth: ', ''), new RegExp('^' + r.label.replace(/[.*+?^${}()|[\]\\/]/g, '\\$&') + '$')]; }), 'line');
            }
            var dist = bank ? [['Branches', /^Branches/], ['Employees', /^Employee/]] : [];
            add('Branches and employees', bank, dist, 'line');
        });
        return out;
    }

    function renderFundamentals() {
        if (useBfsi()) { renderBankFundamentals(); return; }
        setTitles('Revenue <span class="tw-muted">&#8377; Cr</span>', 'EBITDA <span class="tw-muted">&#8377; Cr &#183; line = EBITDA margin</span>',
                  'EPS <span class="tw-muted">&#8377;</span>', 'Revenue, EBITDA and EPS');
        var F = S.fin || {}, annual = S.period === 'A';
        var src = (annual ? F.annual : F.quarterly) || { d: [] };
        var fin = isFinancial(), sh = sharesCr();
        var take = annual ? 10 : 12, from = Math.max(0, src.d.length - take);
        var dates = src.d.slice(from);
        var labels = dates.map(function (d) { return annual ? fyLabel(d) : qLabel(d); });
        var rev = src.sales.slice(from);
        var ebitda = src.ebitda.slice(from);
        // EBITDA margin and EPS come from the HistoricalData sheet; fall back to computing them
        var sheetMargin = (src.ebitdaMargin || []).slice(from);
        var margin = ebitda.map(function (e, i) {
            var m = sheetMargin[i];
            return m != null && isFinite(m) ? m : (e != null && rev[i] ? e / rev[i] : null);
        });
        var np = src.np.slice(from), patM = (src.patMargin || []).slice(from), repEps = (src.eps || []).slice(from);
        // charted EPS = net profit / current share count (comparable across splits and bonuses);
        // without a share count, the workbook's reported diluted EPS
        var eps = np.map(function (v, i) { return sh && v != null ? v / sh : (repEps[i] != null ? repEps[i] : null); });
        var epsTip = np.map(function (v, i) {
            var t = [];
            if (sh && repEps[i] != null) t.push('Reported diluted EPS <b>' + num(repEps[i], 1) + '</b>');
            if (v != null) t.push('Net profit <b>' + num(v, 0) + '</b> Cr' + (patM[i] != null && isFinite(patM[i]) ? ' · PAT margin <b>' + (patM[i] * 100).toFixed(1) + '%</b>' : ''));
            return t.join('<br/>');
        });
        var nAct = dates.length;
        var rows = dates;

        // append consensus estimates (annual view only)
        var C = S.cons;
        if (annual && C && C.dates && C.dates.length && nAct) {
            var lastFy = +labels[nAct - 1].slice(2), k = C.dates.length - 1;
            Object.keys(C.fy).sort().forEach(function (fy) {
                var y = +fy.replace(/\D/g, ''); if (!(y > lastFy)) return;
                labels.push(fy + 'E');
                var f = C.fy[fy];
                rev.push(f.revAvg[k]); ebitda.push(null); eps.push(f.epsAvg[k]);
            });
        }

        var last = nAct ? dates[nAct - 1] : null;
        $('sa-fin-asof').textContent = last ? 'Data as of ' + (annual ? labels[nAct - 1] + ' (' + fmtD(last) + ')' : qName(last) + ' (' + fmtD(last) + ')') : 'No data';
        $('sa-fin-sub').textContent = annual ? 'Fiscal years (April to March)' + (labels.length > nAct ? ', plus consensus estimates (E)' : '') + '. Hover for YoY growth.'
            : 'Last 12 quarters. Hover for YoY growth.';
        var notes = [];
        notes.push(F.source === 'excel' ? 'Source: HistoricalData sheet of the company result workbook (' + F.file + ').' : '');
        if (sh) notes.push('EPS = net profit ÷ current share count, so it is comparable across splits and bonuses; hover for the reported diluted EPS.');
        if (fin) notes.push('Financial company: EBITDA is not meaningful for lenders; a dedicated banking view is planned.');
        if (last && (Date.now() - parseD(last)) / 864e5 > (annual ? 550 : 200)) notes.push('These financials have not been refreshed recently.');
        $('sa-fin-note').textContent = notes.filter(Boolean).join(' ');

        if (!rows.length) {
            ['sa-rev', 'sa-ebitda', 'sa-eps'].forEach(function (id) { if (charts[id]) charts[id].dispose(); empty($(id), 'No result workbook for this stock yet.'); });
            return;
        }
        var lag = annual ? 1 : 4;
        bars('sa-rev', labels, rev, nAct, lag, 's1');
        if (fin) { if (charts['sa-ebitda']) charts['sa-ebitda'].dispose(); empty($('sa-ebitda'), 'Not shown for financial companies.'); }
        else bars('sa-ebitda', labels.slice(0, nAct), ebitda.slice(0, nAct), nAct, lag, 's3', 0, margin);
        if (eps.some(function (v) { return v != null; })) bars('sa-eps', labels, eps, nAct, lag, 's4', 1, null, epsTip);
        else { if (charts['sa-eps']) charts['sa-eps'].dispose(); empty($('sa-eps'), 'EPS not available.'); }
    }

    // bar chart; optional `margin` adds a % line on a right-hand axis (EBITDA margin);
    // optional `tip` adds an HTML line per bar to the tooltip
    function bars(id, labels, vals, nAct, lag, colorVar, dp, margin, tip) {
        var P = pal(), col = P[colorVar];
        var el = $(id);
        if (el.querySelector('.sa-empty')) { el.innerHTML = ''; if (charts[id]) charts[id].dispose(); }
        var yAxes = [{ type: 'value', splitNumber: 3, axisLabel: Object.assign(baseText(P), { formatter: axisNum }),
                       splitLine: { lineStyle: { color: P.grid } } }];
        var extra = [];
        if (margin) {
            var ms = margin.filter(function (m) { return m != null; });
            var lo = Math.min.apply(null, ms), hi = Math.max.apply(null, ms), pad = Math.max(0.02, (hi - lo) * 0.6);
            yAxes.push({ type: 'value', position: 'right', splitNumber: 3, splitLine: { show: false },
                         min: Math.floor((lo - pad) * 100) / 100, max: Math.ceil((hi + pad) * 100) / 100,
                         axisLabel: Object.assign(baseText(P), { formatter: function (v) { return Math.round(v * 100) + '%'; } }) });
            extra.push({ name: 'EBITDA margin', type: 'line', yAxisIndex: 1, data: margin, symbol: 'circle', symbolSize: 5,
                         lineStyle: { width: 2, color: P.ink2 }, itemStyle: { color: P.ink2, borderColor: P.surface, borderWidth: 1 }, z: 5 });
        }
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
                        '<br/>' + num(v, dp || 0) + (g != null && prev > 0 ? ' <span style="color:' + (g >= 0 ? P.pos : P.neg) + '">' + pct(g, 1) + ' YoY</span>' : '') +
                        (margin && margin[i] != null ? '<br/>Margin <b>' + (margin[i] * 100).toFixed(1) + '%</b>' +
                            (i - lag >= 0 && margin[i - lag] != null ? ' <span style="color:' + P.muted + '">(' + ((margin[i] - margin[i - lag]) * 10000 >= 0 ? '+' : '') +
                                Math.round((margin[i] - margin[i - lag]) * 10000) + ' bps YoY)</span>' : '') : '') +
                        (tip && tip[i] ? '<br/>' + tip[i] : '');
                }
            }),
            xAxis: { type: 'category', data: labels, axisTick: { show: false }, axisLine: { lineStyle: { color: P.axis } },
                     axisLabel: Object.assign(baseText(P), { hideOverlap: true, interval: 'auto' }) },
            yAxis: yAxes,
            series: extra.concat([{
                type: 'bar', barMaxWidth: 26,
                data: vals.map(function (v, i) {
                    var est = i >= nAct;
                    return { value: v, itemStyle: {
                        color: est ? alpha(col, .18) : (v < 0 ? P.neg : col),
                        borderColor: est ? col : 'transparent', borderWidth: est ? 1 : 0, borderType: 'dashed',
                        borderRadius: v < 0 ? [0, 0, 3, 3] : [3, 3, 0, 0] } };
                })
            }])
        }, true);
    }

    // =====================================================================
    // Business details: extra charts configured per ticker for the result workbook
    // (ChartForSheet&Row.property / TickerChartCombination.property), delivered in getstockdetails.excelCharts
    // =====================================================================
    // generic series (mostly repeat the Revenue / EBITDA / EPS card): shown only with "Show all".
    // Market cap is deliberately not here: it stays with the company-specific charts.
    // The generic balance-sheet / return / valuation charts of the default set (HistoricalData) are here too.
    var WB_CORE = /(noplat|revenue \(rs|ttm revenue|revenue growth|total revenue|\bopm\b|\bpat\b|net profit margin|roic|ev \/ ebit|invested capital|avg\. borrowings|total equity and liabilities|borrowings \(rs|property, plant|inventories \(rs|cash and cash|trade receivables)/i;

    function wbIsAnnual(c) { return /^(annual|annaul)\b/i.test(c.title); }
    function wbTitle(t) { return t.replace(/^annaul/i, 'Annual').replace(/^(Quarterly|Annual)\s+/i, ''); }
    function wbPct(c, series) {
        if (!/%/.test(c.title)) return false;
        if (c.bank) return true;          // ModelBFSI ratios are always fractions
        var mx = 0;
        series.forEach(function (s) { (s.values || []).forEach(function (v) { if (v != null && isFinite(v)) mx = Math.max(mx, Math.abs(v)); }); });
        return mx <= 3;   // stored as fractions
    }
    function wbFmt(v, isPct) {
        if (v == null || !isFinite(v)) return '–';
        if (isPct) return (v * 100).toFixed(1) + '%';
        var a = Math.abs(v);
        return num(v, a >= 100 ? 0 : (a >= 10 ? 1 : 2));
    }

    function renderWorkbook() {
        var card = $('sa-wb-card'), grid = $('sa-wb-grid');
        // bank metrics first, then the workbook's company-specific charts (segments, geography, capacity...),
        // then (with "Show all") the generic ones that repeat the P&L card (market cap, revenue, OPM, PAT)
        var xl = (S.details && S.details.excelCharts) || [];
        var bank = bfsiCharts();
        // for lenders the ModelBFSI charts already cover the workbook's generic bank series, so those go behind "Show all"
        var isCore = function (c) { return bank.length ? true : WB_CORE.test(c.title); };
        var all = bank
            .concat(xl.filter(function (c) { return !isCore(c); }))
            .concat(xl.filter(isCore));
        Object.keys(charts).forEach(function (k) { if (k.indexOf('sa-wb-') === 0) { charts[k].dispose(); delete charts[k]; } });
        if (!all.length) { card.hidden = true; return; }
        card.hidden = false;
        var showAll = $('sa-wb-all').checked, annual = S.wbPeriod === 'A';
        var list = all.filter(function (c) { return wbIsAnnual(c) === annual && (c.bank || showAll || !isCore(c)); });
        if (!list.length) {
            grid.innerHTML = '<div class="sa-empty" style="grid-column:1/-1;height:80px">No ' + (annual ? 'annual' : 'quarterly') +
                ' business charts' + (showAll ? '' : ' beyond the ones above; tick “Show all”') + '.</div>';
            return;
        }
        grid.innerHTML = list.map(function (c, i) {
            return '<div class="sa-wb-item"><h3 class="sa-mini">' + esc(wbTitle(c.title)) + '</h3><div class="sa-wb-latest" id="sa-wb-l' + i + '"></div>' +
                '<div class="sa-chart sa-chart-wb" id="sa-wb-' + i + '"></div></div>';
        }).join('');

        var P = pal(), palette = [P.s1, P.s2, P.s3, P.s4, P.ink2, P.muted, alpha(P.s1, .55), alpha(P.s2, .55)];
        list.forEach(function (c, i) {
            var series = (c.series && c.series.length) ? c.series
                : [{ label: c.fieldName || wbTitle(c.title), values: c.values, chartType: c.chartType || 'line' }];
            var n = Math.max.apply(null, series.map(function (s) { return (s.values || []).length; }).concat([(c.labels || []).length]));
            var labels = (c.labels || []).slice(-n);
            while (labels.length < n) labels.unshift('');
            labels = labels.map(function (l) {   // FY26Q2 -> Q2 FY26; 2026-09-30 -> Q2 FY27 (or FY27 for annual charts)
                var s = String(l), m = /^FY(\d{2})Q([1-4])$/.exec(s);
                if (m) return 'Q' + m[2] + ' FY' + m[1];
                if (/^\d{4}-\d{2}-\d{2}/.test(s)) return wbIsAnnual(c) ? fyLabel(s.slice(0, 10)) : qName(s.slice(0, 10));
                return s;
            });
            var isPct = wbPct(c, series), stacked = c.stacked === true;
            var firstType = (series[0].chartType || c.chartType) === 'bar' ? 'bar' : 'line';

            $('sa-wb-l' + i).innerHTML = series.slice(0, 6).map(function (s, j) {
                var v = null;
                for (var k = (s.values || []).length - 1; k >= 0; k--) if (s.values[k] != null && isFinite(s.values[k])) { v = s.values[k]; break; }
                return (series.length > 1 ? '<span style="color:' + palette[j % palette.length] + '">●</span> ' + esc(s.label) + ' ' : 'Latest ') +
                    '<b>' + wbFmt(v, isPct) + '</b>';
            }).join(' &nbsp; ') + (labels.length ? ' <span class="tw-muted">· ' + esc(labels[labels.length - 1]) + '</span>' : '');

            chart('sa-wb-' + i).setOption({
                animationDuration: 300, textStyle: { fontFamily: P.font }, color: palette,
                grid: { left: 4, right: 8, top: series.length > 1 ? 30 : 12, bottom: 4, containLabel: true },
                legend: series.length > 1 ? { top: 0, left: 0, type: 'scroll', icon: 'roundRect', itemWidth: 10, itemHeight: 8,
                                              textStyle: { color: P.ink2, fontSize: 11, fontFamily: P.font },
                                              pageTextStyle: { color: P.muted } } : { show: false },
                tooltip: Object.assign(tooltipBase(P), {
                    trigger: 'axis',
                    axisPointer: { type: firstType === 'bar' ? 'shadow' : 'line', shadowStyle: { color: alpha(P.ink, .04) }, lineStyle: { color: P.axis } },
                    valueFormatter: function (v) { return wbFmt(v, isPct); }
                }),
                xAxis: { type: 'category', data: labels, axisTick: { show: false }, axisLine: { lineStyle: { color: P.axis } },
                         axisLabel: Object.assign(baseText(P), { hideOverlap: true }) },
                yAxis: { type: 'value', scale: !stacked && firstType === 'line', splitNumber: 3,
                         axisLabel: Object.assign(baseText(P), { formatter: function (v) { return isPct ? Math.round(v * 100) + '%' : axisNum(v); } }),
                         splitLine: { lineStyle: { color: P.grid } } },
                series: series.map(function (s) {
                    var type = (s.chartType || c.chartType) === 'bar' ? 'bar' : 'line';
                    var vals = (s.values || []).slice(-n);
                    while (vals.length < n) vals.unshift(null);
                    return type === 'bar'
                        ? { name: s.label, type: 'bar', stack: stacked ? 'all' : undefined, data: vals, barMaxWidth: 22 }
                        : { name: s.label, type: 'line', data: vals, symbol: 'circle', symbolSize: 4, showSymbol: n <= 16,
                            connectNulls: true, lineStyle: { width: 2 } };
                })
            }, true);
        });
    }

    // =====================================================================
    // Critical factors (broker names are replaced with "Broker 1/2" on the server)
    // =====================================================================
    function ratingOf(theme) {
        var m = /^\s*(strong buy|buy|add|accumulate|outperform|hold|neutral|reduce|underperform|sell)\b/i.exec(theme || '');
        if (!m) return null;
        var r = m[1].toLowerCase();
        return { text: m[1].replace(/\b\w/g, function (c) { return c.toUpperCase(); }),
                 cls: /buy|add|accum|outperform/.test(r) ? 'buy' : (/sell|reduce|underperform/.test(r) ? 'sell' : 'hold') };
    }
    function factorList(text) {
        if (!text) return '';
        var parts = String(text).split(/\s*(?:^|;\s*|\s)(?=\d{1,2}\)\s)/).map(function (p) { return p.replace(/^\d{1,2}\)\s*/, '').trim(); })
            .filter(Boolean);
        if (parts.length <= 1) parts = String(text).split(/;\s+/).filter(Boolean);
        return '<ol>' + parts.map(function (p) { return '<li>' + esc(p) + '</li>'; }).join('') + '</ol>';
    }
    function noteHtml(n, open) {
        var r = ratingOf(n.theme);
        return '<div class="sa-note ' + (r ? r.cls : '') + '"><h4>' + esc(n.broker) + (r ? ' <span class="sa-rating ' + r.cls + '">' + esc(r.text) + '</span>' : '') +
            ' <span class="tw-muted" style="font-weight:400">' + (n.date ? fmtD(n.date) : '') + '</span></h4>' +
            (n.theme ? '<p class="theme">' + esc(n.theme) + '</p>' : '') +
            (n.factors ? (open ? factorList(n.factors) : '<details><summary>Critical factors</summary>' + factorList(n.factors) + '</details>') : '') + '</div>';
    }
    function renderViews() {
        var A = S.views, card = $('sa-views-card');
        if (!A || (!A.consolidated.length && !A.notes.length)) { card.hidden = true; return; }
        card.hidden = false;
        $('sa-views-cons').innerHTML = A.consolidated.map(function (c) {
            return '<div class="sa-view"><h4>' + esc(c.broker) + ' · consolidated view</h4><p>' + esc(c.view) + '</p></div>';
        }).join('');
        // newest note per broker in full, the rest under "Earlier notes"
        var seen = {}, latest = [], earlier = [];
        A.notes.forEach(function (n) { if (!seen[n.broker]) { seen[n.broker] = 1; latest.push(n); } else earlier.push(n); });
        $('sa-views-notes').innerHTML = latest.map(function (n) { return noteHtml(n, true); }).join('');
        if (earlier.length) {
            var host = $('sa-views-earlier');
            if (!host) { host = document.createElement('details'); host.id = 'sa-views-earlier'; host.className = 'sa-earlier'; $('sa-views-notes').after(host); }
            host.innerHTML = '<summary>Earlier notes (' + earlier.length + ')</summary><div class="sa-views-notes">' +
                earlier.map(function (n) { return noteHtml(n, false); }).join('') + '</div>';
        }
        var d = A.notes.length ? A.notes[0].date : null;
        $('sa-views-asof').textContent = d ? 'Latest note ' + fmtD(d) : '';
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
        fys.slice(0, 2).forEach(function (fy) {
            var f = C.fy[fy], lo = f.epsMin[k], hi = f.epsMax[k], avg = f.epsAvg[k];
            if (lo == null || hi == null) return;
            html += tile(fy + ' EPS range', '₹' + num(lo, 1) + ' – ' + num(hi, 1), avg ? 'spread ' + pct((hi - lo) / Math.abs(avg), 0).replace('+', '') : '');
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
        $('sa-cons-eps-legend').innerHTML = show.map(function (fy, i) { return '<span><i style="background:' + cols[i] + '"></i>' + esc(fy) + ' EPS (avg)</span>'; }).join('') +
            '<span style="opacity:.75">Shaded: analysts\' low – high estimate</span>';
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
            // low-high band per fiscal year (as on the target chart), then the average line
            series: show.slice(0, 2).reduce(function (acc, fy, i) {
                var f = C.fy[fy];
                acc.push({ name: fy + ' low', type: 'line', stack: 'eps' + i, data: f.epsMin, symbol: 'none', lineStyle: { opacity: 0 }, silent: true });
                acc.push({ name: fy + ' range', type: 'line', stack: 'eps' + i, symbol: 'none', lineStyle: { opacity: 0 }, silent: true,
                           areaStyle: { color: alpha(cols[i], .12) },
                           data: f.epsMax.map(function (v, j) { return v != null && f.epsMin[j] != null ? v - f.epsMin[j] : null; }) });
                return acc;
            }, []).concat(show.map(function (fy, i) {
                return { name: fy, type: 'line', data: C.fy[fy].epsAvg, symbol: 'none', connectNulls: false,
                         lineStyle: { width: 2, color: cols[i] }, itemStyle: { color: cols[i] },
                         endLabel: { show: true, formatter: fy, color: cols[i], fontSize: 11, fontWeight: 600 } };
            }))
        }, true);
    }

    // =====================================================================
    // boot
    // =====================================================================
    function renderAll() {
        renderHeader();
        if (S.ohlc) renderPrice();
        if (S.fin) renderFundamentals();
        if (S.details) renderWorkbook();
        renderConsensus();
    }

    bindSeg('sa-range', 'data-r', 'range', function () { S.sel = null; S.pending = null; renderPrice(); });
    bindSeg('sa-kind', 'data-k', 'kind', renderPrice);
    bindSeg('sa-cmp', 'data-c', 'cmp', onCompare);
    bindSeg('sa-period', 'data-p', 'period', renderFundamentals);
    bindSeg('sa-wb-period', 'data-p', 'wbPeriod', renderWorkbook);
    $('sa-wb-all').addEventListener('change', renderWorkbook);
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
    var pFin = getJSON('/public/api/getstockfinancials/' + t).then(function (r) { S.fin = r; }).catch(function () { S.fin = null; });
    getJSON('/public/api/getanalystviews/' + t).then(function (r) { S.views = r; renderViews(); }).catch(function () {});
    Promise.all([pDet, pCons, pFin, pOhlc]).then(function () { renderTiles(); renderFundamentals(); renderWorkbook(); renderConsensus(); });
})();
