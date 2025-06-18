var module = angular.module('StockCompareManagement', ['angular.filter','chart.js', 'ui.bootstrap']);

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
        renderComparisonNotes();
    }

    // === Render All Comparison Charts ===
    function renderComparisonCharts() {
        let container = document.getElementById('compareChartsContainer');
        container.innerHTML = "";

        $scope.selectedCombinations.forEach(function(combinationId, chartIndex) {
            let panel = document.createElement('div');
            panel.className = 'x_panel';

            let titleDiv = document.createElement('div');
            titleDiv.className = 'x_title';

            let excelChart = null;
            for (let stock of $scope.comparisonData) {
                excelChart = stock.excelCharts.find(c => c.combinationId === combinationId);
                if (excelChart) break;
            }

            if (!excelChart) {
                console.log("No chart found for combinationId: " + combinationId);
                return;
            }

            let titleH2 = document.createElement('h2');
            titleH2.innerHTML = `${excelChart.title} <small id="compare_chart_subtitle_${chartIndex}" style="
                display: block;
                font-size: 13px;
                font-weight: normal;
                color: #555;
                margin-top: 6px;
                line-height: 1.4;
                white-space: normal;
                word-break: break-word;
            ">Loading...</small>`;
            titleDiv.appendChild(titleH2);

            let clearFix = document.createElement('div');
            clearFix.className = 'clearfix';
            titleDiv.appendChild(clearFix);
            panel.appendChild(titleDiv);

            let contentDiv = document.createElement('div');
            contentDiv.className = 'x_content';

            let canvas = document.createElement('canvas');
            canvas.id = 'compare_chart_' + chartIndex;
            canvas.style.height = "300px";
            canvas.style.maxHeight = "300px";
            contentDiv.appendChild(canvas);
            panel.appendChild(contentDiv);

            let wrapperColDiv = document.createElement('div');
            wrapperColDiv.className = 'col-md-6 col-sm-12 col-xs-12';
            wrapperColDiv.style.padding = "10px";
            wrapperColDiv.appendChild(panel);
            container.appendChild(wrapperColDiv);

            let allLabelSets = [];
            let datasets = [];

            $scope.comparisonData.forEach(function(stock) {
                let matchedChart = stock.excelCharts.find(c => c.combinationId === combinationId);
                if (matchedChart) {
                    allLabelSets.push(matchedChart.labels);
                }
            });

            let allLabelsFlat = allLabelSets.flat();
            let labelSet = Array.from(new Set(allLabelsFlat));
            let commonLabels = labelSet.sort();

            if (commonLabels.length === 0) {
                console.log("No common labels for combinationId:", combinationId);
                return;
            }

            $scope.comparisonData.forEach(function(stock, idx) {
                let matchedChart = stock.excelCharts.find(c => c.combinationId === combinationId);
                if (matchedChart) {
                    let alignedValues = [];
                    commonLabels.forEach(function(lbl) {
                        const i = matchedChart.labels.indexOf(lbl);
                        alignedValues.push(i >= 0 ? matchedChart.values[i] : null);
                    });

                    let formattedValues = alignedValues.map(function(val) {
                        if (val == null) return null;
                        if (Math.abs(val) <= 1) {
                            return +(val * 100).toFixed(1);
                        } else if (Math.abs(val) > 1000) {
                            return Math.round(val);
                        } else {
                            return +val.toFixed(1);
                        }
                    });

                    datasets.push({
                        label: stock.ticker + ' - ' + matchedChart.fieldName,
                        data: formattedValues,
                        custom: {
                            fieldName: matchedChart.fieldName
                        },
                        borderColor: $scope.newColors[idx % $scope.newColors.length],
                        backgroundColor: $scope.newColors[idx % $scope.newColors.length],
                        borderWidth: 2,
                        fill: false
                    });
                }
            });

            let subtitleSegments = [];

            datasets.forEach((ds, i) => {
                let values = ds.data;
                let latestVal = null;

                for (let j = values.length - 1; j >= 0; j--) {
                    if (values[j] !== null && values[j] !== undefined && !isNaN(values[j])) {
                        latestVal = values[j];
                        break;
                    }
                }

                if (latestVal !== null) {
                    let color = ds.borderColor;
                    subtitleSegments.push(`<span style="color:${color};"><strong>${ds.label}</strong></span>: ${latestVal}`);
                }
            });

            // Inject subtitle HTML
            document.getElementById('compare_chart_subtitle_' + chartIndex).innerHTML = subtitleSegments.join(" | ");

            new Chart(canvas.getContext('2d'), {
                type: 'line',
                data: {
                    labels: commonLabels,
                    datasets: datasets
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: false
                        },
                        legend: {
                            display: true
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    let val = context.raw;
                                    if (val == null) return '';

                                    let parts = (context.dataset.label - '').split(' - ');
                                    let ticker = parts[0] - '';
                                    let fieldName = parts[1] - '';

                                    let valueFormatted = '';
                                    if (Math.abs(val) >= 1000) {
                                        valueFormatted = Number(val).toLocaleString('en-IN', { maximumFractionDigits: 0 });
                                    } else if (Math.abs(val) <= 100) {
                                        valueFormatted = Number(val).toLocaleString('en-IN', { maximumFractionDigits: 1 });
                                    } else {
                                        valueFormatted = Number(val).toFixed(1);
                                    }
                                    return `${ticker} - ${fieldName}: ${valueFormatted}`;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
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
                                maxTicksLimit: 10
                            }
                        }
                    }
                }
            });
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

    function renderComparisonNotes() {
        let container = document.getElementById("reportNotesContainer");
        container.innerHTML = "";

        if (!$scope.comparisonData || $scope.comparisonData.length === 0) {
            container.innerHTML = "<p>No notes available.</p>";
            return;
        }

        $scope.comparisonData.forEach(stock => {
            let notes = stock.reportNotes;
            if (!notes || notes.length === 0) return;

            // === Group notes by "ticker|date|documentSource"
            let grouped = {};
            notes.forEach(note => {
                let mainKey = `${note.ticker}|${note.date}|${note.documentSource}`;
                let subKey = `${note.documentSection}|${note.infoCategory}`;
                if (!grouped[mainKey]) grouped[mainKey] = {};
                if (!grouped[mainKey][subKey]) grouped[mainKey][subKey] = [];
                grouped[mainKey][subKey].push(note);
            });

            Object.keys(grouped).forEach(mainKey => {
                let [ticker, rawDate, documentSource] = mainKey.split("|");
                let formattedDate = /^\d{4}-\d{2}-\d{2}$/.test(rawDate) ? rawDate : rawDate;

                // === Big Bold Header ===
                let h2 = document.createElement("h2");
                h2.innerText = `${ticker} - ${formattedDate} - ${documentSource}`;
                h2.style.fontWeight = "bold";
                h2.style.marginTop = "30px";
                container.appendChild(h2);

                let subGroup = grouped[mainKey];
                Object.keys(subGroup).forEach(subKey => {
                    let [docSection, infoCategory] = subKey.split("|");

                    // === Sub Header (smaller bold) ===
                    let h4 = document.createElement("h4");
                    h4.innerText = `${docSection} - ${infoCategory}`;
                    h4.style.fontWeight = "bold";
                    h4.style.marginTop = "15px";
                    h4.style.fontSize = "16px";
                    container.appendChild(h4);

                    // === Bullet List ===
                    subGroup[subKey].forEach(note => {
                        let bullet = document.createElement("div");
                        bullet.innerText = `${note.infoSubCategory}: ${note.information}`;
                        bullet.style.marginLeft = "20px";
                        bullet.style.fontSize = "14px";
                        bullet.style.marginBottom = "4px";
                        container.appendChild(bullet);
                    });
                });
            });
        });
    }

});