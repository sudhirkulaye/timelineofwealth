var module = angular.module('StockComparePlusManagement', ['angular.filter', 'ui.bootstrap']);

module.controller('StockComparePlusController', function($scope, $http, $filter, $window) {

    var urlBase="/public/api";

    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#3498db', '#f39c12', '#46BFBD', '#FDB45C'];
    $scope.selectedRange = 'ALL';
    $scope.customStartDate = null;
    $scope.customEndDate = null;
    $scope.tradeChartStocks = [];  // Clear before repopulating

    console.log("stockcompare v0.05")

    $scope.stocks = []; // Will be filled from API
    // Create 5 empty ticker input objects
    $scope.tickerInputs = [
        { name: '', ticker: '' },
        { name: '', ticker: '' },
        { name: '', ticker: '' },
        { name: '', ticker: '' },
        { name: '', ticker: '' }
    ];

    // Smart Filtering function (no customFilter needed!)
    $scope.stocksFiltered = function(viewValue) {
        if (!viewValue) return $scope.stocks;

        var search = viewValue.toLowerCase();
        return $scope.stocks.filter(function(stock) {
            return (stock.shortName && stock.shortName.toLowerCase().includes(search)) ||
                   (stock.ticker && stock.ticker.toLowerCase().includes(search));
        });
    };

    // Load stock master list
    $http.get(urlBase + "/getallstocks")
    .then(function (response) {
        if (response && response.data) {
            $scope.stocks = response.data;
        } else {
            $scope.stocks = [];
        }
    });

    // Setting ticker and name after selection
    $scope.setStockTicker = function (stock, index) {
        if (stock && stock.ticker) {
            $scope.tickerInputs[index].ticker = stock.ticker;
            $scope.tickerInputs[index].name = stock.shortName;
        }
    };

    $scope.onSearchTextChange = function(index) {
        let searchText = $scope.tickerInputs[index].searchText || '';

        const matched = $scope.stocks.find(s =>
            (s.shortName + ' (' + s.ticker + ')') === searchText ||
            s.ticker === searchText
        );

        if (matched) {
            $scope.tickerInputs[index].ticker = matched.ticker;
            $scope.tickerInputs[index].name = matched.shortName;
        } else {
            $scope.tickerInputs[index].ticker = '';
            $scope.tickerInputs[index].name = '';
        }
    };

    $scope.compareStocks = function() {
        let enteredTickers = [];

        $scope.tickerInputs.forEach(function(stock) {
            if (stock.ticker) {
                enteredTickers.push(stock.ticker.trim().toUpperCase());
            }
        });

        if (enteredTickers.length < 1) {
            alert('Please select at least 1 stock to compare.');
            return;
        }

        let rangeParam = $scope.selectedRange === 'custom' ? 'custom' : $scope.selectedRange;
        let fromDate = ($scope.selectedRange === 'custom' && $scope.customStartDate)
            ? $filter('date')($scope.customStartDate, 'yyyy-MM-dd') : null;
        let toDate = ($scope.selectedRange === 'custom' && $scope.customEndDate)
            ? $filter('date')($scope.customEndDate, 'yyyy-MM-dd') : null;

        let indexTicker = "NIFTY"; // You can later make this dynamic

        console.log("Calling getpricehistory with:", enteredTickers, "range:", rangeParam, "index:", indexTicker);

        if (fromDate && toDate && new Date(fromDate) > new Date(toDate)) {
            alert('From Date cannot be after To Date');
            return;
        }

        var status = document.getElementById('cmpStatus');
        if (status) status.textContent = 'Loading…';
        // Call the APIs in parallel
        Promise.all([
            $http.get(urlBase + "/getpricehistory", {
                params: {
                    tickers: enteredTickers,
                    range: rangeParam,
                    index: indexTicker,
                    from: fromDate,
                    to: toDate
                }
            }),
            $http.get(urlBase + "/getmarketcaphistory", {
                params: {
                    tickers: enteredTickers,
                    range: rangeParam,
                    from: fromDate,
                    to: toDate
                }
            }),
            $http.get(urlBase + "/getttmpehistory", {
                params: {
                    tickers: enteredTickers,
                    range: rangeParam,
                    index: indexTicker,
                    from: fromDate,
                    to: toDate
                }
            })
        ]).then(function([priceResponse, marketcapResponse, peResponse]) {
            // show the grid first so the charts are measured at their real width
            document.getElementById('compareChartsContainer').hidden = false;
            if (status) status.textContent = '';

            let pbSeries = [];
            let evtoebitSeries = [];

            marketcapResponse.data.forEach((series, idx) => {
                const pbRowMap = {};
                const evRowMap = {};
                const dates = [];

                series.series.forEach(p => {
                    if (p.date) {
                        dates.push(p.date);
                        pbRowMap[p.date] = p.ttmpb ?? null;
                        evRowMap[p.date] = p.evtoebit ?? null;
                    }
                });

                const sortedDates = dates.sort();
                const pbValues = sortedDates.map(d => pbRowMap[d]);
                const evValues = sortedDates.map(d => evRowMap[d]);

                pbSeries.push({ ticker: series.ticker, dates: sortedDates, values: pbValues });
                evtoebitSeries.push({ ticker: series.ticker, dates: sortedDates, values: evValues });
            });

            if (marketcapResponse && marketcapResponse.data) {
                renderMarketCapChart(marketcapResponse.data);
                renderLineChart('pbChartPanel', 'TTM PB Chart', 'pbSubtitle', pbSeries, 'Price to Book Ratio');
                renderLineChart('evToEbitChartPanel', 'EV to EBITA Chart', 'evtoebitSubtitle', evtoebitSeries, 'EV / EBITA');
            }

            if (priceResponse && priceResponse.data) {
                let fullData = priceResponse.data;
                let tickerOnlyData = fullData.filter(d => d.ticker !== indexTicker);
                renderAbsoluteReturnChart(fullData);
                renderTradeVolumeValueCharts(tickerOnlyData);
                $scope.$applyAsync();
            }

            if (peResponse && peResponse.data) {
                renderTtmPeChart(peResponse.data);
                renderForwardPeChart(peResponse.data);
                renderRelativePeCharts(peResponse.data);
            }

        }).catch(function(error) {
            if (status) status.textContent = 'Could not load the data.';
            console.error("Error loading chart data:", error);
        });

    };

    // =====================================================================
    // Charts: themed Apache ECharts via /custom/twcharts.js (drag across a chart to measure a period)
    // =====================================================================
    var INDEX = "NIFTY";
    function escHtml(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }
    function colorFor(i, isIndex) { var P = TwChart.palette(); return isIndex ? TwChart.indexColor() : P[i % P.length]; }
    function dot(i, isIndex) { return '<span style="color:' + colorFor(i, isIndex) + '">&#9679;</span> '; }
    function fmtX(v) { return v == null || !isFinite(v) ? '–' : Number(v).toFixed(1) + 'x'; }
    function fmtCr(v) { return v == null || !isFinite(v) ? '–' : '₹' + TwChart.num(v, 0) + ' Cr'; }
    function fmtPct(v) { return v == null || !isFinite(v) ? '–' : (v > 0 ? '+' : '') + Number(v).toFixed(1) + '%'; }

    // rows: [{ticker, points:[{date, value}]}] -> aligned labels + series; color index follows the API order
    function alignRows(rows) {
        var all = {};
        rows.forEach(function (r) { r.points.forEach(function (p) { if (p.date && p.value != null) all[p.date] = 1; }); });
        var labels = Object.keys(all).sort();
        var series = rows.map(function (r) {
            var m = {};
            r.points.forEach(function (p) { if (p.date) m[p.date] = p.value; });
            return { name: r.ticker, index: r.ticker === INDEX, color: r.color,
                     data: labels.map(function (d) { var v = m[d]; return v != null && isFinite(v) ? +v : null; }) };
        });
        return { labels: labels, series: series };
    }
    function latestLine(series, fmt) {
        return series.map(function (s) {
            for (var i = s.data.length - 1; i >= 0; i--) if (s.data[i] != null)
                return dot(s.color, s.index) + escHtml(s.name) + ' <b>' + fmt(s.data[i]) + '</b>';
            return null;
        }).filter(Boolean).join(' &#183; ');
    }
    function drawSeries(panelId, subtitleId, rows, fmt, measure, zero, axisFmt) {
        var a = alignRows(rows);
        var sub = document.getElementById(subtitleId);
        if (!a.labels.length) {
            document.getElementById(panelId).innerHTML = '<div class="tw-muted tw-small" style="padding:30px 0;text-align:center">No data for this range.</div>';
            if (sub) sub.innerHTML = '';
            return;
        }
        if (sub) sub.innerHTML = latestLine(a.series, fmt);
        TwChart.line(panelId, { labels: a.labels, series: a.series, yFmt: axisFmt || fmt, tipFmt: fmt,
                                measure: measure || 'value', zero: !!zero, height: 320 });
    }
    function fmtCrAxis(v) { var a = Math.abs(v); return a >= 100000 ? '₹' + TwChart.num(v / 100000, a >= 1000000 ? 0 : 1) + ' L Cr' : '₹' + TwChart.num(v, 0) + ' Cr'; }
    function rowsOf(data, field) {
        return data.map(function (s, idx) {
            return { ticker: s.ticker, color: idx, points: (s.series || []).map(function (p) { return { date: p.date, value: p[field] }; }) };
        });
    }

    function renderMarketCapChart(data) {
        drawSeries('marketCapChartPanel', 'marketCapSubtitle', rowsOf(data, 'marketcap'), fmtCr, 'value', false, fmtCrAxis);
    }

    function renderAbsoluteReturnChart(data) {
        var sub = document.getElementById('returnSubtitle');
        // common start: the latest first date across all series, so every line starts at 0%
        var starts = data.map(function (s) { return s.prices && s.prices.length ? s.prices[0].date : null; }).filter(Boolean).sort();
        var start = starts[starts.length - 1];
        if (!start) { sub.innerHTML = ''; return; }
        var rows = data.map(function (s, idx) {
            var ps = (s.prices || []).filter(function (p) { return p.date >= start && p.price; });
            var base = ps.length ? ps[0].price : null;
            return { ticker: s.ticker, color: idx, points: ps.map(function (p) { return { date: p.date, value: base ? (p.price / base - 1) * 100 : null }; }) };
        });
        var a = alignRows(rows);
        var ix = a.series.filter(function (s) { return s.index; })[0];
        var last = function (s) { for (var i = s.data.length - 1; i >= 0; i--) if (s.data[i] != null) return s.data[i]; return null; };
        var ixr = ix ? last(ix) : null;
        sub.innerHTML = a.series.map(function (s) {
            var r = last(s);
            if (r == null) return '';
            var h = dot(s.color, s.index) + escHtml(s.name) + ' <b class="' + (r >= 0 ? 'tw-pos' : 'tw-neg') + '">' + fmtPct(r) + '</b>';
            if (!s.index && ixr != null) {
                h += ' <span class="tw-muted">(' + (r >= ixr ? 'beat ' : 'lagged ') + INDEX + ' by ' + Math.abs(r - ixr).toFixed(1) + ' pts)</span>';
            }
            return h;
        }).filter(Boolean).join('<br>');
        TwChart.line('returnChartPanel', { labels: a.labels, series: a.series, yFmt: fmtPct, measure: 'return', height: 320 });
    }

    function renderTtmPeChart(data) { drawSeries('ttmPeChartPanel', 'ttmPeSubtitle', rowsOf(data, 'pe'), fmtX); }
    function renderForwardPeChart(data) { drawSeries('forwardPeChartPanel', 'forwardPeSubtitle', rowsOf(data, 'forward_pe'), fmtX); }

    function renderRelativePeCharts(data) {
        var ixd = data.find(function (d) { return d.ticker === INDEX; });
        if (!ixd) return;
        var ipe = {}, ifw = {};
        ixd.series.forEach(function (p) { ipe[p.date] = p.pe; ifw[p.date] = p.forward_pe; });
        var rel = function (field, im) {
            return data.map(function (s, idx) {
                if (s.ticker === INDEX) return null;
                return { ticker: s.ticker, color: idx, points: s.series.map(function (p) {
                    var a = p[field], b = im[p.date];
                    return { date: p.date, value: a != null && b ? a / b : null };
                }) };
            }).filter(Boolean);
        };
        drawSeries('relativeTtmPeChartPanel', 'relativeTtmPeSubtitle', rel('pe', ipe), fmtX);
        drawSeries('relativeForwardPeChartPanel', 'relativeForwardPeSubtitle', rel('forward_pe', ifw), fmtX);
    }

    function renderLineChart(panelId, title, subtitleId, seriesList, yAxisTitle, formatSuffix) {
        var rows = seriesList.map(function (s, idx) {
            return { ticker: s.ticker, color: idx, points: s.dates.map(function (d, i) { return { date: d, value: s.values[i] }; }) };
        });
        drawSeries(panelId, subtitleId, rows, fmtX);
    }

    function renderTradeVolumeValueCharts(data) {
        var box = document.getElementById('tradeVolumeValueChartsContainer');
        box.innerHTML = '';
        data.forEach(function (s, idx) {
            if (s.ticker === INDEX) return;
            var pts = (s.series || s.prices || []).filter(function (p) { return p.date && (p.volume != null || p.value != null); });
            pts.sort(function (a, b) { return a.date < b.date ? -1 : 1; });
            if (!pts.length) return;
            var card = document.createElement('section');
            card.className = 'tw-card';
            card.innerHTML = '<h2>' + escHtml(s.ticker) + ': traded volume and value</h2>' +
                '<p class="tw-sub" style="margin:2px 0 6px">Bars: shares traded (left axis). Line: traded value in &#8377; crore (right axis).</p><div></div>';
            box.appendChild(card);
            TwChart.line(card.lastChild, {
                labels: pts.map(function (p) { return p.date; }),
                series: [
                    { name: 'Volume', type: 'bar', color: idx, data: pts.map(function (p) { return p.volume; }) },
                    { name: 'Traded value (₹ Cr)', axis: 1, index: true, data: pts.map(function (p) { return p.value; }) }
                ],
                yFmt: function (v) { var a = Math.abs(v); return a >= 1e7 ? (v / 1e7).toFixed(1) + ' Cr' : a >= 1e5 ? (v / 1e5).toFixed(1) + ' L' : TwChart.num(v, 0); },
                y2Fmt: function (v) { return '₹' + TwChart.num(v, v < 100 ? 1 : 0); },
                zero: true, height: 260
            });
        });
    }

});
