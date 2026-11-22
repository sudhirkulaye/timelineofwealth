/*
 * TwChart: themed Apache ECharts for the comparison / statistics pages, with the same look and
 * drag-to-measure behaviour as the stock page.
 *
 *   TwChart.line(el, {labels, series:[{name, data, axis:0|1, type:'line'|'bar', index:bool, color:int}],
 *                     yFmt, tipFmt, yName, y2Name, y2Fmt, zero, measure:'return'|'value'|false, height})
 *
 * el may be a container div (the chart is drawn inside it). Colours come from the theme tokens and follow
 * the light / dark toggle. With `measure`, dragging across the chart (or clicking two points) shows the
 * change of every series between the two dates in a readout strip above the chart.
 */
(function () {
    'use strict';
    var MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    var EXTRA = ['#8b5cf6', '#0e9fb3', '#b45309', '#db2777'];
    var registry = [];

    function css(n) { return getComputedStyle(document.documentElement).getPropertyValue(n).trim(); }
    function pal() {
        return {
            ink: css('--ink'), ink2: css('--ink2'), muted: css('--muted'), grid: css('--grid'), axis: css('--axis'),
            surface: css('--surface'), chip: css('--chip'), pos: css('--pos'), neg: css('--neg'),
            font: css('--font') || 'Inter, system-ui, sans-serif',
            series: [css('--s1'), css('--s2'), css('--s3'), css('--s4')].concat(EXTRA)
        };
    }
    function palette() { return pal().series; }
    function indexColor() { return css('--ink2'); }
    function alpha(hex, a) {
        var h = String(hex).replace('#', '');
        if (h.length === 3) h = h.replace(/./g, '$&$&');
        var n = parseInt(h, 16);
        if (isNaN(n)) return hex;
        return 'rgba(' + (n >> 16 & 255) + ',' + (n >> 8 & 255) + ',' + (n & 255) + ',' + a + ')';
    }
    var ISO = /^\d{4}-\d{2}-\d{2}/;
    function isoParts(s) { var p = String(s).slice(0, 10).split('-'); return [+p[0], +p[1] - 1, +p[2]]; }
    function dLong(s) { if (!ISO.test(s)) return s; var p = isoParts(s); return p[2] + ' ' + MON[p[1]] + ' ' + p[0]; }
    function dShort(s) { if (!ISO.test(s)) return s; var p = isoParts(s); return MON[p[1]] + ' ' + String(p[0]).slice(2); }
    function num(v, dp) {
        if (v == null || !isFinite(v)) return '–';
        return Number(v).toLocaleString('en-IN', { minimumFractionDigits: dp || 0, maximumFractionDigits: dp || 0 });
    }
    function auto(v) {
        if (v == null || !isFinite(v)) return '–';
        var a = Math.abs(v);
        return num(v, a >= 1000 ? 0 : (a >= 100 ? 1 : 2));
    }
    function esc(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
    function signPct(v) { return (v > 0 ? '+' : '') + (v * 100).toFixed(1) + '%'; }

    function colorOf(s, i, P) { return s.index ? P.ink2 : P.series[(s.color != null ? s.color : i) % P.series.length]; }

    function build(rec) {
        var c = rec.cfg, P = pal(), n = c.labels.length, dates = n && ISO.test(c.labels[0]);
        var yFmt = c.yFmt || auto, y2Fmt = c.y2Fmt || auto, tipFmt = c.tipFmt || yFmt;
        var hasY2 = c.series.some(function (s) { return s.axis === 1; });
        var axisText = { color: P.muted, fontFamily: P.font, fontSize: 11 };
        var yAxis = function (name, fmt, right) {
            return {
                type: 'value', name: name || '', position: right ? 'right' : 'left', scale: !c.zero,
                nameTextStyle: { color: P.muted, fontFamily: P.font, fontSize: 11, align: right ? 'right' : 'left' },
                axisLabel: { color: P.muted, fontFamily: P.font, fontSize: 11, formatter: function (v) { return fmt(v); } },
                splitLine: { show: !right, lineStyle: { color: P.grid } }, axisLine: { show: false }, axisTick: { show: false }
            };
        };
        var series = c.series.map(function (s, i) {
            var col = colorOf(s, i, P), bar = s.type === 'bar';
            return {
                name: s.name, type: bar ? 'bar' : 'line', data: s.data, yAxisIndex: s.axis === 1 ? 1 : 0,
                showSymbol: !bar && n <= 24, symbolSize: 5, connectNulls: true, sampling: n > 600 ? 'lttb' : undefined,
                barMaxWidth: 18, z: s.index ? 1 : 2, stack: s.stack,
                lineStyle: { width: s.index ? 1.6 : 2, color: col, type: s.index ? 'dashed' : 'solid' },
                itemStyle: { color: bar ? alpha(col, 0.55) : col, borderRadius: bar ? [3, 3, 0, 0] : 0 },
                emphasis: { focus: 'series' }
            };
        });
        if (rec.sel && series.length) {
            series[0].markArea = {
                silent: true, itemStyle: { color: alpha(P.series[0], 0.08) },
                data: [[{ xAxis: c.labels[rec.sel[0]] }, { xAxis: c.labels[rec.sel[1]] }]]
            };
            series[0].markLine = {
                silent: true, symbol: 'none', label: { show: false },
                lineStyle: { color: P.axis, type: 'solid', width: 1 },
                data: [{ xAxis: c.labels[rec.sel[0]] }, { xAxis: c.labels[rec.sel[1]] }]
            };
        }
        var many = c.series.length > 1;
        return {
            animation: false,
            textStyle: { fontFamily: P.font },
            color: c.series.map(function (s, i) { return colorOf(s, i, P); }),
            legend: many ? { top: 0, left: 0, icon: 'roundRect', itemWidth: 12, itemHeight: 4, itemGap: 14,
                             textStyle: { color: P.ink2, fontFamily: P.font, fontSize: 12 } } : undefined,
            grid: { left: 8, right: hasY2 ? 8 : 14, top: (many ? 34 : 18) + (c.yName || c.y2Name ? 18 : 0), bottom: n > 120 ? 54 : 8, containLabel: true },
            tooltip: {
                trigger: 'axis', backgroundColor: P.surface, borderColor: P.grid, borderWidth: 1, padding: [8, 10],
                textStyle: { color: P.ink, fontFamily: P.font, fontSize: 12 },
                extraCssText: 'box-shadow:0 8px 24px rgba(0,0,0,.14);border-radius:10px;',
                axisPointer: { type: 'line', lineStyle: { color: P.axis } },
                formatter: function (ps) {
                    if (!ps.length) return '';
                    var h = '<div style="color:' + P.muted + ';margin-bottom:4px">' + esc(dates ? dLong(ps[0].axisValue) : ps[0].axisValue) + '</div>';
                    ps.forEach(function (p) {
                        if (p.value == null) return;
                        var s = c.series[p.seriesIndex], f = s.axis === 1 ? y2Fmt : tipFmt;
                        h += '<div style="display:flex;gap:14px;justify-content:space-between"><span>' + p.marker + esc(p.seriesName) +
                             '</span><b style="font-variant-numeric:tabular-nums">' + esc(f(p.value)) + '</b></div>';
                    });
                    return h;
                }
            },
            xAxis: {
                type: 'category', data: c.labels, boundaryGap: series.some(function (s) { return s.type === 'bar'; }),
                axisLabel: { color: P.muted, fontFamily: P.font, fontSize: 11, hideOverlap: true,
                             formatter: function (v) { return dates ? dShort(v) : v; } },
                axisLine: { lineStyle: { color: P.axis } }, axisTick: { show: false }
            },
            yAxis: hasY2 ? [yAxis(c.yName, yFmt, false), yAxis(c.y2Name, y2Fmt, true)] : [yAxis(c.yName, yFmt, false)],
            dataZoom: n > 120 ? [{
                type: 'slider', height: 22, bottom: 6, borderColor: 'transparent', backgroundColor: P.chip,
                fillerColor: alpha(P.series[0], 0.12), dataBackground: { lineStyle: { color: P.axis }, areaStyle: { color: P.chip } },
                selectedDataBackground: { lineStyle: { color: P.series[0] }, areaStyle: { color: alpha(P.series[0], 0.1) } },
                handleStyle: { color: P.surface, borderColor: P.axis }, moveHandleStyle: { color: P.axis },
                textStyle: { color: P.muted, fontSize: 10 }, labelFormatter: function (i) { return dates ? dShort(c.labels[i]) : c.labels[i]; }
            }] : [],
            series: series
        };
    }

    // ---------- drag / two-click measurement ----------
    function readoutIdle(rec) {
        rec.readout.innerHTML = '<span>Drag across the chart, or click two points, to measure the change between two dates.</span>';
    }
    function readoutSel(rec) {
        var c = rec.cfg, a = rec.sel[0], b = rec.sel[1], P = pal();
        var h = '<span><b>' + esc(dLong(c.labels[a])) + '</b> → <b>' + esc(dLong(c.labels[b])) + '</b></span>';
        c.series.forEach(function (s, i) {
            var va = s.data[a], vb = s.data[b];
            if (va == null || vb == null) return;
            var col = colorOf(s, i, P), txt, ch;
            if (c.measure === 'return') {            // series are % returns from a common base
                ch = (1 + vb / 100) / (1 + va / 100) - 1;
                txt = signPct(ch);
            } else {
                var f = s.axis === 1 ? (c.y2Fmt || auto) : (c.tipFmt || c.yFmt || auto);
                ch = va !== 0 ? vb / va - 1 : null;
                txt = esc(f(va)) + ' → ' + esc(f(vb)) +
                      (ch != null ? ' <span class="' + (ch >= 0 ? 'tw-pos' : 'tw-neg') + '">(' + signPct(ch) + ')</span>' : '');
            }
            if (c.measure === 'return') txt = '<span class="' + (ch >= 0 ? 'tw-pos' : 'tw-neg') + '"><b>' + txt + '</b></span>';
            h += '<span><i style="display:inline-block;width:10px;height:3px;border-radius:2px;vertical-align:middle;margin-right:5px;background:' +
                 col + '"></i>' + esc(s.name) + ' ' + txt + '</span>';
        });
        // relative to the index series, when there is one
        if (c.measure === 'return') {
            var ix = c.series.filter(function (s) { return s.index; })[0];
            if (ix && ix.data[a] != null && ix.data[b] != null) {
                var ir = (1 + ix.data[b] / 100) / (1 + ix.data[a] / 100) - 1, parts = [];
                c.series.forEach(function (s) {
                    if (s.index || s.data[a] == null || s.data[b] == null) return;
                    var r = (1 + s.data[b] / 100) / (1 + s.data[a] / 100) - 1, d = r - ir;
                    parts.push(esc(s.name) + ' <span class="' + (d >= 0 ? 'tw-pos' : 'tw-neg') + '">' +
                               (d >= 0 ? 'beat ' : 'lagged ') + esc(ix.name) + ' by ' + Math.abs(d * 100).toFixed(1) + ' pts</span>');
                });
                if (parts.length) h += '<span class="tw-muted">' + parts.join(' · ') + '</span>';
            }
        }
        h += '<button class="x" type="button" title="Clear">✕</button>';
        rec.readout.innerHTML = h;
        rec.readout.querySelector('.x').onclick = function () { rec.sel = null; rec.pending = null; draw(rec); readoutIdle(rec); };
    }
    function idxAt(rec, x, y) {
        var p = rec.chart.convertFromPixel({ gridIndex: 0 }, [x, y]);
        if (!p) return null;
        var i = Math.round(p[0]);
        if (i < 0 || i >= rec.cfg.labels.length) return null;
        return rec.chart.containPixel({ gridIndex: 0 }, [x, y]) ? i : null;
    }
    function wireMeasure(rec) {
        var zr = rec.chart.getZr(), drag = null;
        zr.on('mousedown', function (e) { var i = idxAt(rec, e.offsetX, e.offsetY); if (i != null) drag = { a: i, moved: false }; });
        zr.on('mousemove', function (e) {
            if (!drag) return;
            var i = idxAt(rec, e.offsetX, e.offsetY);
            if (i == null || i === drag.a) return;
            drag.moved = true;
            rec.sel = [Math.min(drag.a, i), Math.max(drag.a, i)];
            draw(rec); readoutSel(rec);
        });
        zr.on('mouseup', function (e) {
            if (!drag) return;
            var d = drag; drag = null;
            if (d.moved) { rec.pending = null; return; }
            var i = idxAt(rec, e.offsetX, e.offsetY);
            if (i == null) return;
            if (rec.pending == null) { rec.pending = i; rec.sel = null; draw(rec); rec.readout.innerHTML = '<span>Start: <b>' + esc(dLong(rec.cfg.labels[i])) + '</b>. Click a second point.</span>'; return; }
            if (i !== rec.pending) { rec.sel = [Math.min(rec.pending, i), Math.max(rec.pending, i)]; rec.pending = null; draw(rec); readoutSel(rec); }
        });
    }

    function draw(rec) { rec.chart.setOption(build(rec), true); }

    function line(el, cfg) {
        if (typeof el === 'string') el = document.getElementById(el);
        if (!el || !window.echarts) return null;
        var old = registry.filter(function (r) { return r.el === el; })[0];
        if (old) { old.chart.dispose(); registry.splice(registry.indexOf(old), 1); }
        el.innerHTML = '';
        el.classList.add('tw-chartbox');
        var rec = { el: el, cfg: cfg, sel: null, pending: null };
        if (cfg.measure && cfg.labels.length > 2) {
            rec.readout = document.createElement('div');
            rec.readout.className = 'tw-readout';
            el.appendChild(rec.readout);
            readoutIdle(rec);
        }
        var box = document.createElement('div');
        box.className = 'tw-chart' + (cfg.measure ? ' tw-chart-measure' : '');
        box.style.height = (cfg.height || 300) + 'px';
        el.appendChild(box);
        rec.chart = echarts.init(box, null, { renderer: 'canvas' });
        draw(rec);
        if (rec.readout) wireMeasure(rec);
        registry.push(rec);
        return rec.chart;
    }

    function redrawAll() {
        registry = registry.filter(function (r) { return document.body.contains(r.el) && !r.chart.isDisposed(); });
        registry.forEach(draw);
    }
    window.addEventListener('resize', function () { registry.forEach(function (r) { if (!r.chart.isDisposed()) r.chart.resize(); }); });
    window.addEventListener('tw-themechange', function () { setTimeout(redrawAll, 0); });
    if (window.matchMedia) {
        var mq = window.matchMedia('(prefers-color-scheme: dark)');
        if (mq.addEventListener) mq.addEventListener('change', function () { setTimeout(redrawAll, 0); });
    }

    window.TwChart = { line: line, palette: palette, indexColor: indexColor, num: num, auto: auto, dLong: dLong, dShort: dShort };
})();
