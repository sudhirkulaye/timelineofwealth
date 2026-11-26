var module = angular.module('StockCompareManagement', ['angular.filter', 'ui.bootstrap']);

module.controller('StockCompareController', function($scope, $http, $filter, $window) {

    var urlBase="/public/api";

    // === CONFIGS ===
//    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#3498db', '#f39c12', '#46BFBD', '#FDB45C'];
//    $scope.newColors = ['#26B99A', '#03586A', '#3498db', '#9b59b6', '#f1c40f', '#46BFBD', '#FDB45C'];
//    $scope.newColors = ['#26B99A', '#3498db', '#1abc9c', '#9b59b6', '#e67e22', '#46BFBD', '#FDB45C'];
    console.log("stockcompare v0.5")

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

    // === Compare Button Action ===
    $scope.compareStocks = function() {
        let enteredTickers = [];

        $scope.tickerInputs.forEach(function(stock) {
            if (stock.ticker) {
                enteredTickers.push(stock.ticker.trim().toUpperCase());
            }
        });

        // Remove duplicates
        enteredTickers = [...new Set(enteredTickers)];

        if (enteredTickers.length < 1) {
            alert('Please select at least 1 stock to compare.');
            return;
        }

        console.log("Tickers selected for comparison:", enteredTickers);

        loadExcelChartsForTickers(enteredTickers);
    };

    // === Load All Excel Charts for Entered Tickers ===
    function loadExcelChartsForTickers(tickers) {
        $scope.tickers = tickers;
        $scope.comparisonData = [];

        let allPromises = [];

        tickers.forEach(function(ticker) {
            let url = "/getstockdetails/" + ticker;
            allPromises.push($http.get(urlBase + url));
        });

        var status = document.getElementById('cmpStatus');
        if (status) status.textContent = 'Loading…';
        Promise.all(allPromises).then(function(responses) {
            responses.forEach(function(response) {
                if (response && response.data) {
                    $scope.comparisonData.push(response.data);
                }
            });
            console.log("loadExcelChartsForTickers D:", $scope.comparisonData);
            findCommonAndRender();
        });
    }

    function findCommonAndRender() {
        let allCombinationSets = [];

        $scope.comparisonData.forEach(function(stock) {
            if (stock.excelCharts && stock.excelCharts.length > 0) {
                let comboSet = new Set(stock.excelCharts.map(c => c.combinationId));
                allCombinationSets.push(comboSet);
            } else {
                allCombinationSets.push(new Set(Array.from({length: 36}, (_, i) => i + 1)));
            }
        });

        // Find intersection of all sets
        let commonSet = allCombinationSets.reduce((a, b) => new Set([...a].filter(x => b.has(x))));

        // Use order from first stock's chart list
        let orderedByFirstStock = $scope.comparisonData[0].excelCharts
            .map(c => c.combinationId)
            .filter(id => commonSet.has(id)); // keep only common ones

        $scope.selectedCombinations = orderedByFirstStock;

        renderComparisonCharts();
    }

    // === Render All Comparison Charts (themed ECharts via /custom/twcharts.js) ===
    // A workbook chart holds one series (values) or several (series[], e.g. segments). With one stock every series
    // is drawn; with several stocks one series is compared at a time, picked from the list of series names.
    function chartSeries(c) {
        if (c.series && c.series.length) return c.series.map(function (s) { return { name: s.label, values: s.values || [] }; });
        return [{ name: c.fieldName || c.title, values: c.values || [] }];
    }
    function isPct(c, list) {
        if (!/%/.test(c.title)) return false;
        var mx = 0;
        list.forEach(function (a) { a.forEach(function (v) { if (v != null && isFinite(v)) mx = Math.max(mx, Math.abs(v)); }); });
        return mx <= 3;   // stored as fractions
    }
    function periodLabel(l) {
        var m = /^FY(\d{2})Q([1-4])$/.exec(l);
        return m ? 'Q' + m[2] + ' FY' + m[1] : l;
    }
    function aligned(c, s, labels) {   // workbook values line up with the end of the chart's labels
        var map = {}, off = (c.labels || []).length - s.values.length;
        s.values.forEach(function (v, k) { var l = c.labels[off + k]; if (l != null) map[l] = v; });
        return labels.map(function (l) { return map[l] != null && isFinite(map[l]) ? map[l] : null; });
    }
    function lastOf(a) { for (var i = a.length - 1; i >= 0; i--) if (a[i] != null) return a[i]; return null; }
    function escHtml(s) { return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) { return '&#' + c.charCodeAt(0) + ';'; }); }

    function renderComparisonCharts() {
        var container = document.getElementById('compareChartsContainer');
        container.innerHTML = '';
        var stocks = $scope.comparisonData.filter(function (s) { return s && s.excelCharts; });
        var single = stocks.length === 1;
        var P = TwChart.palette();
        var status = document.getElementById('cmpStatus');
        if (status) status.textContent = $scope.selectedCombinations.length ? '' : 'No charts in common for these stocks.';

        $scope.selectedCombinations.forEach(function (combinationId) {
            var perStock = stocks.map(function (st) {
                return { st: st, c: st.excelCharts.find(function (c) { return c.combinationId === combinationId; }) };
            }).filter(function (x) { return x.c; });
            if (!perStock.length) return;
            var first = perStock[0].c;
            var labelSet = {};
            perStock.forEach(function (x) { (x.c.labels || []).forEach(function (l) { labelSet[l] = 1; }); });
            var labels = Object.keys(labelSet).sort();
            if (!labels.length) return;
            var names = [];
            perStock.forEach(function (x) { chartSeries(x.c).forEach(function (s) { if (names.indexOf(s.name) < 0) names.push(s.name); }); });

            var card = document.createElement('section');
            card.className = 'tw-card';
            var title = String(first.title).replace(/^annaul/i, 'Annual');
            card.innerHTML = '<div style="display:flex;justify-content:space-between;align-items:flex-start;gap:10px;flex-wrap:wrap">' +
                '<h2>' + escHtml(title) + '</h2>' +
                (!single && names.length > 1 ? '<select class="form-control" style="width:auto;max-width:220px;height:30px;padding:2px 8px;font-size:12.5px">' +
                    names.map(function (n) { return '<option>' + escHtml(n) + '</option>'; }).join('') + '</select>' : '') +
                '</div><div class="tw-latest"></div><div class="tw-cmp-chart"></div>';
            container.appendChild(card);

            var draw = function (pick) {
                var series = [], latest = [];
                perStock.forEach(function (x) {
                    var idx = stocks.indexOf(x.st);
                    var list = chartSeries(x.c);
                    if (!single && list.length > 1) list = list.filter(function (s) { return s.name === pick; });
                    list.forEach(function (s, k) {
                        var bar = single && x.c.chartType === 'bar';
                        series.push({
                            name: single ? s.name : x.st.ticker, data: aligned(x.c, s, labels), color: single ? k : idx,
                            type: bar ? 'bar' : 'line', stack: bar && x.c.stacked ? 'a' : undefined
                        });
                    });
                });
                var pct = isPct(first, series.map(function (s) { return s.data; }));
                if (pct) series.forEach(function (s) { s.data = s.data.map(function (v) { return v == null ? null : v * 100; }); });
                var fmt = pct ? function (v) { return TwChart.num(v, 1) + '%'; } : TwChart.auto;
                series.forEach(function (s) {
                    var v = lastOf(s.data);
                    if (v != null) latest.push('<span style="color:' + P[s.color % P.length] + '">&#9679;</span> ' + escHtml(s.name) + ' <b>' + fmt(v) + '</b>');
                });
                card.querySelector('.tw-latest').innerHTML = latest.length
                    ? 'Latest (' + escHtml(periodLabel(labels[labels.length - 1])) + '): ' + latest.join(' &#183; ') : 'No values';
                TwChart.line(card.querySelector('.tw-cmp-chart'), {
                    labels: labels.map(periodLabel), series: series, yFmt: fmt,
                    zero: series.some(function (s) { return s.type === 'bar'; }), measure: 'value', height: 280
                });
            };
            var sel = card.querySelector('select');
            if (sel) sel.onchange = function () { draw(sel.value); };
            draw(names[0]);
        });
    }

    // === Smart Format Values ===
    function formatValues(values) {
        return values.map(function(val) {
            if (val == null) return null;
            if (Math.abs(val) <= 1) {
                return +(val * 100).toFixed(1); // percentage formatting
            } else if (Math.abs(val) > 1000) {
                return Math.round(val); // big number formatting
            } else {
                return +val.toFixed(1);
            }
        });
    }

});