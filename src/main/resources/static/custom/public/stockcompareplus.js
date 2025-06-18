var module = angular.module('StockComparePlusManagement', ['angular.filter','chart.js', 'ui.bootstrap']);

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

        // Call both APIs in parallel
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

            document.getElementById('compareChartsContainer').style.display = 'block';

        }).catch(function(error) {
            console.error("Error loading chart data:", error);
        });

    };

    function renderMarketCapChart(data) {
        const container = document.getElementById('marketCapChartPanel');
        container.innerHTML = ""; // Clear previous chart

        const canvas = document.createElement('canvas');
        canvas.id = "marketCapChartCanvas";
        canvas.style.height = "300px";
        container.appendChild(canvas);

        let allDates = new Set();
        data.forEach(series => {
            series.series.forEach(p => allDates.add(p.date));
        });
        const commonLabels = Array.from(allDates).sort();

        const datasets = [];
        const latestText = [];

        data.forEach((series, idx) => {
            const dateToCap = {};
            series.series.forEach(p => {
                dateToCap[p.date] = p.marketcap;
            });

            const alignedValues = commonLabels.map(date => {
                let val = dateToCap[date];
                if (val == null) return null;
                if (Math.abs(val) >= 1000) return Math.round(val);
                else if (Math.abs(val) <= 100) return +val.toFixed(1);
                else return +val.toFixed(0);
            });

            // Capture latest value
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                let lastVal = dateToCap[commonLabels[i]];
                if (lastVal != null) {
                    latestText.push(`${series.ticker}: ${Math.round(lastVal).toLocaleString('en-IN')} Cr`);
                    break;
                }
            }

            datasets.push({
                label: series.ticker,
                data: alignedValues,
                borderColor: $scope.newColors[idx % $scope.newColors.length],
                backgroundColor: $scope.newColors[idx % $scope.newColors.length],
                fill: false,
                tension: 0.2,
                pointRadius: 0,
                borderWidth: 3
            });
        });

        // Update subheader
        let htmlSegments = [];

        data.forEach((series, idx) => {
            let latestVal = null;

            // Build date→value map
            const dateToCap = {};
            series.series.forEach(p => dateToCap[p.date] = p.marketcap);

            // Find latest market cap
            const allDates = Object.keys(dateToCap).sort();
            for (let i = allDates.length - 1; i >= 0; i--) {
                const val = dateToCap[allDates[i]];
                if (val != null) {
                    latestVal = Math.round(val).toLocaleString('en-IN');
                    break;
                }
            }

            if (latestVal !== null) {
                const color = $scope.newColors[idx % $scope.newColors.length];
                htmlSegments.push(`<span style="color:${color};"><strong>${series.ticker}</strong></span>: ${latestVal} Cr`);
            }
        });

        document.getElementById('marketCapSubtitle').innerHTML = htmlSegments.join(" | ");

        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: commonLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                },
                plugins: {
                    title: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let val = context.raw;
                                if (val == null) return '';
                                if (Math.abs(val) >= 1000) {
                                    return `${context.dataset.label}: ${val.toLocaleString('en-IN', { maximumFractionDigits: 0 })} Cr`;
                                } else if (Math.abs(val) <= 100) {
                                    return `${context.dataset.label}: ${val.toLocaleString('en-IN', { maximumFractionDigits: 1 })} Cr`;
                                } else {
                                    return `${context.dataset.label}: ${val.toFixed(1)} Cr`;
                                }
                            }
                        }
                    }
                },
                elements: {
                    point: { radius: 0 },
                    line: { borderWidth: 3 }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Market Cap (₹ Cr)'
                        },
                        ticks: {
                            callback: function(value) {
                                if (Math.abs(value) >= 1000) {
                                    return Number(value).toLocaleString('en-IN', { maximumFractionDigits: 0 });
                                } else if (Math.abs(value) <= 100) {
                                    return Number(value).toLocaleString('en-IN', { maximumFractionDigits: 1 });
                                } else {
                                    return Number(value).toFixed(1);
                                }
                            }
                        }
                    },
                    x: {
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12
                        }
                    }
                }
            }
        });
    }

    function renderAbsoluteReturnChart(data) {
        const chartContainer = document.getElementById('returnChartPanel');
        const subtitleEl = document.getElementById('returnSubtitle');
        chartContainer.innerHTML = ""; // Clear old chart

        // === Step 1: Find common start date
        const minDates = data.map(s => s.prices?.[0]?.date).filter(Boolean);
        const commonStartDate = minDates.sort().reverse()[0];
        if (!commonStartDate) return;

        // === Step 2: Generate common label set
        const labelSet = new Set();
        data.forEach(s => {
            s.prices.filter(p => p.date >= commonStartDate).forEach(p => labelSet.add(p.date));
        });
        const commonLabels = Array.from(labelSet).sort();

        // === Step 3: Create returns summary block (HTML in subheader)
        const indexSeries = data.find(s => s.ticker === "NIFTY");
        if (!indexSeries) return;

        const indexFiltered = indexSeries.prices.filter(p => p.date >= commonStartDate);
        const indexMap = {};
        indexFiltered.forEach(p => indexMap[p.date] = p.price);

        const indexStart = indexFiltered[0].price;
        const indexEnd = indexFiltered[indexFiltered.length - 1].price;
        const indexReturn = ((indexEnd - indexStart) / indexStart) * 100;

        const lines = [];
        lines.push(`<span style="color:red;"><strong>${indexSeries.ticker}</strong></span> Return: ${indexReturn.toFixed(2)}%`);

        const datasets = [];

        data.forEach((series, idx) => {
            const priceMap = {};
            series.prices.filter(p => p.date >= commonStartDate).forEach(p => {
                priceMap[p.date] = p.price;
            });

            const base = priceMap[commonLabels[0]];
            if (!base || base === 0) return;

            const values = commonLabels.map(d => {
                const v = priceMap[d];
                return v !== undefined ? ((v - base) / base) * 100 : null;
            });

            datasets.push({
                label: series.ticker,
                data: values.map(v => v !== null ? +v.toFixed(2) : null),
                borderColor: (series.ticker === "NIFTY") ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                backgroundColor: (series.ticker === "NIFTY") ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                fill: false,
                tension: 0.2,
                pointRadius: 0,
                borderWidth: (series.ticker === "NIFTY") ? 4 : 3
            });

            if (series.ticker !== "NIFTY") {
                const endVal = priceMap[commonLabels[commonLabels.length - 1]];
                if (endVal) {
                    const ret = ((endVal - base) / base) * 100;
                    const delta = ret - indexReturn;

                    const direction = delta >= 0 ? 'OUTPERFORMED' : 'UNDERPERFORMED';
                    const color = delta >= 0 ? 'green' : 'red';
                    const tickerColor = $scope.newColors[idx % $scope.newColors.length];
                    lines.push(`<span style="color:${tickerColor};"><strong>${series.ticker}</strong></span> Return: ` +
                               `<span style="color:${color};">${ret.toFixed(2)}%</span> ` +
                               `(${direction} ${indexSeries.ticker} by ${Math.abs(delta).toFixed(2)}%)`);
                }
            }
        });

        // Update subheader (HTML allowed here)
        subtitleEl.innerHTML = lines.join("<br>");

        // === Step 4: Chart rendering
        const canvas = document.createElement('canvas');
        canvas.id = "absReturnChartCanvas";
        canvas.style.height = "400px";
        chartContainer.appendChild(canvas);

        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: commonLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Absolute Return Chart (Base Normalized)'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `${context.dataset.label}: ${context.raw?.toFixed(2)}%`;
                            }
                        }
                    }
                },
                elements: {
                    point: { radius: 0 },
                    line: { borderWidth: 3 }
                },
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: 'Return (%)'
                        },
                        ticks: {
                            callback: val => `${val.toFixed(0)}%`
                        }
                    },
                    x: {
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12
                        }
                    }
                }
            }
        });
    }

    function renderTtmPeChart(data) {
        const container = document.getElementById('ttmPeChartPanel');
        const subtitleEl = document.getElementById('ttmPeSubtitle');
        container.innerHTML = ""; // Clear previous chart

        const canvas = document.createElement('canvas');
        canvas.id = "ttmPeChartCanvas";
        canvas.style.height = "400px";
        container.appendChild(canvas);

        // Step 1: Get all dates
        const allDates = new Set();
        data.forEach(series => {
            if (series.series) {
                series.series.forEach(p => allDates.add(p.date));
            }
        });
        const commonLabels = Array.from(allDates).sort();

        // Step 2: Build aligned datasets and compute latest PEs
        const datasets = [];
        const latestText = [];

        data.forEach((series, idx) => {
            const peMap = {};
            series.series.forEach(p => {
                if (p.pe != null) peMap[p.date] = p.pe;
            });

            const values = commonLabels.map(date => {
                return peMap[date] !== undefined ? +peMap[date].toFixed(2) : null;
            });

            // Find latest available PE value
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                const val = peMap[commonLabels[i]];
                if (val != null) {
                    latestText.push(`${series.ticker}: ${val.toFixed(2)}x`);
                    break;
                }
            }

            const isIndex = series.ticker === "NIFTY";

            datasets.push({
                label: series.ticker,
                data: values,
                borderColor: isIndex ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                backgroundColor: isIndex ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                fill: false,
                tension: 0.2,
                pointRadius: 0,
                borderWidth: isIndex ? 4 : 3
            });
        });

        // Step 3: Update subheader
        let htmlSegments = [];
        data.forEach((series, idx) => {
            let latestVal = null;
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                const val = series.series.find(p => p.date === commonLabels[i])?.pe;
                if (val != null) {
                    latestVal = val.toFixed(2);
                    break;
                }
            }

            if (latestVal != null) {
                const color = (series.ticker === "NIFTY") ? 'red' : $scope.newColors[idx % $scope.newColors.length];
                htmlSegments.push(`<span style="color:${color};"><strong>${series.ticker}</strong></span>: ${latestVal}x`);
            }
        });
        subtitleEl.innerHTML = htmlSegments.join(" | ");

        // Step 4: Render chart
        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: commonLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'TTM PE Chart (Stock + Index)'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `${context.dataset.label}: ${context.raw?.toFixed(2)}x`;
                            }
                        }
                    }
                },
                elements: {
                    point: { radius: 0 },
                    line: { borderWidth: 3 }
                },
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: 'TTM PE Ratio (x)'
                        },
                        ticks: {
                            callback: val => `${val.toFixed(0)}x`
                        }
                    },
                    x: {
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12
                        }
                    }
                }
            }
        });
    }

    function renderForwardPeChart(data) {
        const container = document.getElementById('forwardPeChartPanel');
        const subtitleEl = document.getElementById('forwardPeSubtitle');
        container.innerHTML = ""; // Clear previous chart

        const canvas = document.createElement('canvas');
        canvas.id = "forwardPeChartCanvas";
        canvas.style.height = "400px";
        container.appendChild(canvas);

        // Step 1: Collect all valid dates
        const allDates = new Set();
        data.forEach(series => {
            if (series.series) {
                series.series.forEach(p => {
                    if (p.forward_pe != null) allDates.add(p.date);
                });
            }
        });
        const commonLabels = Array.from(allDates).sort();

        // Step 2: Build datasets + collect latest values
        const datasets = [];
        const latestText = [];

        data.forEach((series, idx) => {
            const peMap = {};
            series.series.forEach(p => {
                if (p.forward_pe != null) peMap[p.date] = p.forward_pe;
            });

            const values = commonLabels.map(date =>
                peMap[date] !== undefined ? +peMap[date].toFixed(2) : null
            );

            // Capture latest available forward PE
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                const val = peMap[commonLabels[i]];
                if (val != null) {
                    latestText.push(`${series.ticker}: ${val.toFixed(2)}x`);
                    break;
                }
            }

            const isIndex = (series.ticker === "NIFTY");

            datasets.push({
                label: series.ticker,
                data: values,
                borderColor: isIndex ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                backgroundColor: isIndex ? 'red' : $scope.newColors[idx % $scope.newColors.length],
                fill: false,
                tension: 0.2,
                pointRadius: 0,
                borderWidth: isIndex ? 4 : 3
            });
        });

        // Step 3: Update subtitle
        let htmlSegments = [];
        data.forEach((series, idx) => {
            let latestVal = null;
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                const val = series.series.find(p => p.date === commonLabels[i])?.forward_pe;
                if (val != null) {
                    latestVal = val.toFixed(2);
                    break;
                }
            }

            if (latestVal != null) {
                const color = (series.ticker === "NIFTY") ? 'red' : $scope.newColors[idx % $scope.newColors.length];
                htmlSegments.push(`<span style="color:${color};"><strong>${series.ticker}</strong></span>: ${latestVal}x`);
            }
        });
        subtitleEl.innerHTML = htmlSegments.join(" | ");

        // Step 4: Draw chart
        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: commonLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Forward PE Chart (1-Year Ahead EPS)'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `${context.dataset.label}: ${context.raw?.toFixed(2)}x`;
                            }
                        }
                    }
                },
                elements: {
                    point: { radius: 0 },
                    line: { borderWidth: 3 }
                },
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: 'Forward PE Ratio (x)'
                        },
                        ticks: {
                            callback: val => `${val.toFixed(0)}x`
                        }
                    },
                    x: {
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12
                        }
                    }
                }
            }
        });
    }

    function renderRelativePeCharts(data) {
        const indexData = data.find(d => d.ticker === "NIFTY");
        if (!indexData) return;

        let relTtmSeries = [];
        let relFwdSeries = [];

        data.forEach((series) => {
            if (series.ticker === "NIFTY") return;

            const peMap = {};
            const fwdMap = {};
            const indexPeMap = {};
            const indexFwdMap = {};

            series.series.forEach(p => {
                if (p.pe != null) peMap[p.date] = p.pe;
                if (p.forward_pe != null) fwdMap[p.date] = p.forward_pe;
            });
            indexData.series.forEach(p => {
                if (p.pe != null) indexPeMap[p.date] = p.pe;
                if (p.forward_pe != null) indexFwdMap[p.date] = p.forward_pe;
            });

            const allDates = Array.from(new Set([
                ...Object.keys(peMap),
                ...Object.keys(indexPeMap),
                ...Object.keys(fwdMap),
                ...Object.keys(indexFwdMap)
            ])).sort();

            const relTtmValues = allDates.map(d => {
                const sPe = peMap[d];
                const iPe = indexPeMap[d];
                return (sPe != null && iPe != null && iPe !== 0) ? +(sPe / iPe).toFixed(2) : null;
            });

            const relFwdValues = allDates.map(d => {
                const sFwd = fwdMap[d];
                const iFwd = indexFwdMap[d];
                return (sFwd != null && iFwd != null && iFwd !== 0) ? +(sFwd / iFwd).toFixed(2) : null;
            });

            relTtmSeries.push({ ticker: series.ticker, dates: allDates, values: relTtmValues });
            relFwdSeries.push({ ticker: series.ticker, dates: allDates, values: relFwdValues });
        });

        renderLineChart("relativeTtmPeChartPanel", "Relative TTM PE Chart", "relativeTtmPeSubtitle", relTtmSeries, "Relative TTM PE (Stock / Index)", "x");
        renderLineChart("relativeForwardPeChartPanel", "Relative Forward PE Chart", "relativeForwardPeSubtitle", relFwdSeries, "Relative Forward PE (Stock / Index)", "x");
    }

    function renderTradeVolumeValueCharts(data) {
        console.log("Trade chart render: ", data);
        $scope.tradeChartStocks = [];

        data.forEach((series, idx) => {
            if (series.ticker === "NIFTY") return;

            const points = series.series || series.prices || [];
            const dateMap = {};
            points.forEach(p => {
                if (p.date && (p.volume != null || p.value != null)) {
                    dateMap[p.date] = { volume: p.volume, value: p.value };
                }
            });

            const dates = Object.keys(dateMap).sort();
            const volumes = dates.map(d => dateMap[d]?.volume ?? null);
            const values = dates.map(d => dateMap[d]?.value ?? null);

            if (!dates.length || volumes.every(v => v == null) || values.every(v => v == null)) {
                console.warn(`Skipping ${series.ticker}, no data`);
                return;
            }

            const stock = {
                ticker: series.ticker,
                labels: dates,
                data: [volumes, values],
                series: ['Volume', 'Trade Value (₹ Cr)'],
                datasetOverride: [
                    {
                        label: "Volume",
                        yAxisID: 'y-axis-1',
                        type: 'bar',
                        backgroundColor: $scope.newColors[idx % $scope.newColors.length],  // consistent with theme
                        borderWidth: 0
                    },
                    {
                        label: "Trade Value",
                        yAxisID: 'y-axis-2',
                        type: 'line',
                        borderColor: $scope.newColors[(idx + 2) % $scope.newColors.length], // alt theme color
                        backgroundColor: 'transparent',
                        borderDash: [], // solid line
                        borderWidth: 2,
                        pointRadius: 0, // ✅ no dots
                        tension: 0.4     // ✅ smooth line
                    }
                ],
                options: {
                    responsive: true,
                    maintainAspectRatio: false,  // ✅ full-width with controlled height
                    layout: {
                        padding: { top: 10, bottom: 10, left: 10, right: 10 }
                    },
                    scales: {
                        yAxes: [
                            {
                                id: 'y-axis-1',
                                type: 'linear',
                                position: 'left',
                                scaleLabel: { display: true, labelString: 'Volume (Shares)' },
                                ticks: {
                                    callback: val => Number(val).toLocaleString('en-IN'),
                                    beginAtZero: true
                                }
                            },
                            {
                                id: 'y-axis-2',
                                type: 'linear',
                                position: 'right',
                                scaleLabel: { display: true, labelString: 'Trade Value (₹ Cr)' },
                                gridLines: { drawOnChartArea: false },
                                ticks: {
                                    callback: val => `₹${Number(val).toLocaleString('en-IN')}`,
                                    beginAtZero: true
                                }
                            }
                        ],
                        xAxes: [{
                            ticks: {
                                autoSkip: true,
                                maxTicksLimit: 12
                            }
                        }]
                    },
                    tooltips: {
                        callbacks: {
                            label: function(context) {
                                const label = context.dataset.label;
                                const value = context.yLabel;
                                if (label.includes('Value')) {
                                    return `${label}: ₹${Number(value).toLocaleString('en-IN')}`;
                                } else {
                                    return `${label}: ${Number(value).toLocaleString('en-IN')}`;
                                }
                            }
                        }
                    }
                }
            };

            $scope.tradeChartStocks.push(stock);
        });
    }

    function renderLineChart(panelId, title, subtitleId, seriesList, yAxisTitle, formatSuffix = "") {
        const container = document.getElementById(panelId);
        const subtitleEl = document.getElementById(subtitleId);
        container.innerHTML = "";

        // === Step 1: Collect all dates
        let allDates = new Set();
        seriesList.forEach(s => s.dates.forEach(d => allDates.add(d)));
        let commonLabels = Array.from(allDates).sort();

        // === Step 2: Build datasets and track latest values
        let datasets = [];
        let subtitleSegments = [];

        seriesList.forEach((s, idx) => {
            let dateToVal = {};
            s.dates.forEach((d, i) => dateToVal[d] = s.values[i]);

            let aligned = commonLabels.map(d => dateToVal[d] ?? null);

            // Find latest value
            let latestVal = null;
            for (let i = commonLabels.length - 1; i >= 0; i--) {
                const val = dateToVal[commonLabels[i]];
                if (val != null && isFinite(val)) {
                    latestVal = val.toFixed(2);
                    break;
                }
            }

            const color = $scope.newColors[idx % $scope.newColors.length];

            if (latestVal !== null && subtitleEl) {
                subtitleSegments.push(`<span style="color:${color};"><strong>${s.ticker}</strong></span>: ${latestVal}${formatSuffix}`);
            }

            datasets.push({
                label: s.ticker,
                data: aligned,
                borderColor: color,
                backgroundColor: color,
                fill: false,
                tension: 0.3,
                pointRadius: 0,
                borderWidth: 3
            });
        });

        // === Step 3: Create canvas and append
        const canvas = document.createElement('canvas');
        canvas.style.height = "400px"; // ✅ Same height as TTM PE
        container.appendChild(canvas);

        // === Step 4: Render Chart
        new Chart(canvas.getContext('2d'), {
            type: 'line',
            data: {
                labels: commonLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                },
                plugins: {
                    title: { display: true, text: title },
                    tooltip: {
                        callbacks: {
                            label: ctx => `${ctx.dataset.label}: ${ctx.raw?.toFixed(2)}${formatSuffix}`
                        }
                    }
                },
                elements: {
                    point: { radius: 0 },
                    line: { borderWidth: 3 }
                },
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: yAxisTitle
                        },
                        ticks: {
                            callback: val => `${val.toFixed(0)}${formatSuffix}`
                        }
                    },
                    x: {
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12
                        }
                    }
                }
            }
        });

        // === Step 5: Sub-header text update
        if (subtitleEl) {
            subtitleEl.innerHTML = subtitleSegments.join(" | ");
        }
    }

});
