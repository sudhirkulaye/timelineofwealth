var module = angular.module('StockAnalysisManagement', ['angular.filter','chart.js']);

module.controller('StockAnalysisController', function($scope, $http, $filter, $window) {
    var urlBase="/public/api";
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];
    $scope.newColors1 = ['#26B99A', '#03586A', '#1a53ff'];
    $scope.chartOptions = { scales: { yAxes: [{ ticks: { min:0 } }] } };
    $http.defaults.headers.post["Content-Type"] = "application/json";

    $scope.ticker = $window.location.href.split("/")[5];
    //console.log($scope.ticker);

    $scope.stockQuarter = [];
    $scope.stockQuarterOriginal = [];
    $scope.labelsYearsStockQuarter = [];
    $scope.stockQuarterDataOverride = [];
    $scope.chartOptionsStockQuarter = {};

    $scope.stockPnl = [];
    $scope.stockPnlOriginal = [];
    $scope.labelsYearsStockPnl = [];
    $scope.stockPnlDataOverride = [];
    $scope.chartOptionsStockPnl = {};

    $scope.stockMargin = [];
    $scope.labelsYearsStockMargin = [];
    $scope.stockMarginDataOverride = [];
    $scope.chartOptionsStockMargin = {};
    $scope.chartStockGrowthSeries = [];

    $scope.stockGrowth = [];
    $scope.labelsYearsStockGrowth = [];
    $scope.stockGrowthDataOverride = [];
    $scope.chartOptionsStockGrowth = {};

    $scope.stockValuation = [];
    $scope.labelsYearsStockValuation = [];
    $scope.stockValuationDataOverride = [];
    $scope.chartOptionsStockValuation = {};

    $scope.recentValuations = [];
    $scope.dataRecentPE = [];
    $scope.dataRecentMCap = []; // Added new variable
    $scope.chartOptionsMCapAndPrice = {};
    $scope.chartMCapAndPriceDatasetOverride = [];
    $scope.chartMCapAndPriceSeries = [];
    $scope.dataRecentMarketPrice = [];
    $scope.dataRecentMCapAndPrice = [];
    $scope.dataRecentPB = [];
    $scope.dataRecentEvToEbita = [];
    $scope.labelsRecentPE = [];
    $scope.chartOptionsRecentPE = {};
    $scope.chartRecentPEDatasetOverride = [];
    $scope.chartOptionsRecentPB = {};
    $scope.chartRecentPBDatasetOverride = [];
    $scope.chartOptionsRecentEvToEbita = {};
    $scope.chartRecentEvToEbitaDatasetOverride = [];

    $scope.priceMovements = [];
    $scope.dataPriceMovements = [];
    $scope.data1DPriceMovements = [];
    $scope.data1WPriceMovements = [];
    $scope.data2WPriceMovements = [];
    $scope.data1MPriceMovements = [];
    $scope.labelsPriceMovements = [];
    $scope.mean1D = 0.00;
    $scope.median1D = 0.00;
    $scope.mean1W = 0.00;
    $scope.median1W = 0.00;
    $scope.mean2W = 0.00;
    $scope.median2W = 0.00;
    $scope.mean1M = 0.00;
    $scope.median1M = 0.00;

    $scope.salesGrowth3Yr = 0.00;
    $scope.salesGrowth5Yr = 0.00;
    $scope.salesGrowth10Yr = 0.00;
    $scope.operatingProfitGrowth3Yr = 0.00;
    $scope.operatingProfitGrowth3Yr = 0.00;
    $scope.operatingProfitGrowth3Yr = 0.00;
    $scope.opm3Yr = 0.00;
    $scope.opm5Yr = 0.00;
    $scope.opm10Yr = 0.00;
    $scope.roic3Yr = 0.00;
    $scope.roic5Yr = 0.00;
    $scope.roic10Yr = 0.00;

    $scope.stockPnlSales = [];
    $scope.stockQuarterSales = [];

    showRecords();

    function showRecords() {
        $scope.stockPnl = [];

        var url = "/getstockdetails/" + $scope.ticker;
        $http.get(urlBase + url)
            .then(function (response) {
                if (response != undefined) {
                    $scope.stockDetails = response.data;
                    renderExcelCharts();
                    renderReportNotes();
                } else {
                    $scope.stockDetails = [];
                }

                // After getStockDetails done
                url = "/getstockquarter/" + $scope.ticker;
                return $http.get(urlBase + url);
            })
            .then(function (response) {
                if (response != undefined) {
                    $scope.stockQuarter = response.data;
                    $scope.stockQuarterOriginal = response.data;
                    populateStockQuarterChart();

                    // After getStockQuarter done
                    url = "/getstockpnl/" + $scope.ticker;
                    return $http.get(urlBase + url);
                } else {
                    $scope.stockQuarter = [];
                    $scope.stockQuarterOriginal = [];
                }
            })
            .then(function (response) {
                if (response != undefined) {
                    $scope.stockPnl = response.data;
                    $scope.stockPnlOriginal = response.data;
                    populateStockPnlChart();
                } else {
                    $scope.stockPnl = [];
                    $scope.stockPnlOriginal = [];
                }

                // Now call getrecentvaluations
                url = "/getrecentvaluations/" + $scope.ticker;
                return $http.get(urlBase + url);
            })
            .then(function (response) {
                if (response != undefined) {
                    $scope.recentValuations = response.data;
                    populateStockRecentPE();
                } else {
                    $scope.recentValuations = [];
                }
            });
    }

    function populateStockPnlChart() {

        /* Begin: Populate Annual Sales and Profit */
        var sortedStockPnl = $filter('orderBy')($scope.stockPnl,'key.date');
        var map = $filter('groupBy')(sortedStockPnl, 'key.date');
        //console.log(map);

        var years = [];
        var sales = [];
        var noplat = [];
        var salesG3yr = [];
        var salesG5yr = [];
        var salesG10yr = [];
        var salesGrowth = [];
        var operatingProfitGrowth = [];
        var margin = [];
        var pe = [];
        var i = Object.keys(map).length;

        for(var yr in map){
           i = i - 1;
           if (i > 10) {
             continue;
           }
           years.push(yr);
           $scope.labelsYearsStockGrowth.push(yr);
           //console.log(yr);
           sales.push(Math.round(map[yr][0].sales*100)/100);
           //console.log(map[yr][0].sales);
           noplat.push(Math.round(map[yr][0].noplat*100)/100);
//           salesGrowth.push(Math.round(map[yr][0].salesG*100)/100);
//           operatingProfitGrowth.push(Math.round(map[yr][0].ebitdaG*100)/100);
           salesG3yr.push(Number(Math.round(map[yr][0].salesG3yr*100 * 100)/100 || 1));
           salesG5yr.push(Number(Math.round(map[yr][0].salesG5yr*100 * 100)/100 || 1));
           salesG10yr.push(Number(Math.round(map[yr][0].salesG10yr*100 * 100)/100 || 1));
           salesGrowth.push(Number(Math.round(map[yr][0].salesG*100 * 100)/100 || 1));
           operatingProfitGrowth.push(Number(Math.round(map[yr][0].ebitdaG*100 * 100)/100 || 1));
           pe.push(Math.round(map[yr][0].pe*100)/100);
           $scope.salesGrowth3Yr = map[yr][0].salesG3yr;
           $scope.salesGrowth5Yr = map[yr][0].salesG5yr;
           $scope.salesGrowth10Yr = map[yr][0].salesG10yr;
           $scope.operatingProfitGrowth3Yr = map[yr][0].ebitdaG3yr;
           $scope.operatingProfitGrowth5Yr = map[yr][0].ebitdaG5yr;
           $scope.operatingProfitGrowth10Yr = map[yr][0].ebitdaG10yr;
           $scope.opm3Yr = map[yr][0].avgOpm3yr;
           $scope.opm5Yr = map[yr][0].avgOpm5yr;
           $scope.opm10Yr = map[yr][0].avgOpm10yr;
           $scope.roic3Yr = map[yr][0].avgRoic3yr;
           $scope.roic5Yr = map[yr][0].avgRoic5yr;
           $scope.roic10Yr = map[yr][0].avgRoic10yr;

           margin.push(Number(Math.round(map[yr][0].opm*100 * 100)/100 || 1));

        }

        var reverseSortedStockQuarter = $filter('orderBy')($scope.stockQuarterOriginal,'key.date',true);
        var map1 = $filter('groupBy')(reverseSortedStockQuarter, 'key.date');
        i = 0;
        var ttmSales = Number(0.00||0);
        var ttmNoplat = Number(0.00||0);
        var ttmSalesGrowth = Number(0.00||0);
        var ttmOperatingProfitGrowth = Number(0.00||0);

        for(var yr in map1){
           i = i + 1;
           if (i > 1) {
             break;
           }
           ttmSales = Number(map1[yr][0].ttmSales || 0);
           ttmNoplat = Number(map1[yr][0].ttmNoplat || 0);
//           ttmSalesGrowth = ttmSalesGrowth + Number(map1[yr][0].ttmSalesG || 0);
//           ttmOperatingProfitGrowth = ttmOperatingProfitGrowth + Number(map1[yr][0].ttmEbitdaG || 0);
           ttmSalesGrowth = Number(Math.round(map1[yr][0].ttmSalesG*100 * 100) / 100 || 1);
           ttmOperatingProfitGrowth = Number(Math.round(map1[yr][0].ttmEbitdaG*100 * 100) / 100 || 1);
//           computedNpm = (Number(map1[yr][0].netProfit || 0)/Number(map1[yr][0].sales || 0));
//           computedNpm = Math.round(computedNpm * 100)/100;
//           ttmNpm = ttmNpm + computedNpm;
           margin.push(Number(Math.round(map1[yr][0].ttmOpm*100 * 100)/100 || 1));
        }
//        ttmOpm = ttmOperatingProfit/ttmSales;
//        ttmNpm = ttmNpm/4;
//        ttmOpm = Math.round(ttmOpm*100)/100;
//        ttmNpm = Math.round(ttmNpm*100)/100;

        //years.push($filter('date')(new Date(), 'yyyy-MM-dd'));
        years.push("TTM");
        sales.push(Math.round(ttmSales*100)/100);
        noplat.push(Math.round(ttmNoplat*100)/100);
        salesGrowth.push(ttmSalesGrowth);
        operatingProfitGrowth.push(ttmOperatingProfitGrowth);

        $scope.labelsYearsStockPnl = years;
        $scope.stockPnl = [];
        //$scope.stockPnl.push(sales);
        $scope.stockPnl.push(noplat);
        //$scope.stockPnl.push(salesGrowth);
        $scope.stockPnl.push(margin);
        //$scope.stockPnl.push(operatingProfitGrowth);
        //$scope.stockPnl.push(margin);

        $scope.labelsYearsStockMargin = years;
        $scope.stockMargin = [];
        $scope.stockMargin.push(margin);

        $scope.stockGrowth = [];
        $scope.stockGrowth.push(salesG3yr);
        //console.log(salesG3yr);
        $scope.stockGrowth.push(salesG5yr);
        //console.log(salesG5yr);
        $scope.stockGrowth.push(salesG10yr);
        //console.log(salesG10yr);
        //console.log($scope.stockGrowth);


        $scope.labelsYearsStockValuation = years;
        $scope.stockValuation = [];
        pe.push($scope.stockDetails.dailyDataS.peTtm);
        $scope.stockValuation.push(pe);

        /*$scope.stockValuationDataOverride = [
            {
                label: "PE",
                yAxisID: 'y-axis-1',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4
            }
        ];*/


        $scope.chartOptionsStockPnl = { scales: {
                                            yAxes: [
                                                {
                                                    id: 'y-axis-1',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'left',
                                                    ticks: { min:0 }
                                                },
                                                {
                                                    id: 'y-axis-2',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'right',
                                                    ticks: { min:0 }
                                                }
                                            ]
                                       } };

        $scope.stockPnlDataOverride = [
//              {
//                label: "Sales",
//                yAxisID: 'y-axis-1',
//                type: 'bar'
//              },
              {
                label: "NOPLAT",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "OPM%",
                yAxisID: 'y-axis-2',
                type: 'line'
              },
//              {
//                label: "EBIT%",
//                yAxisID: 'y-axis-2',
//                type: 'line'
//              },
        ];

        $scope.chartStockGrowthSeries = ['3 Yr Sales g%', '5 Yr Sales g%', '10 Yr Sales g%'];

        $scope.chartOptionsStockGrowth = { scales: {
                                            yAxes: [
                                                {
                                                    id: 'y-axis-1',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'left',
                                                    ticks: { min:0 }
                                                }
                                            ]
                                       } };

        $scope.stockGrowthDataOverride = [
              {
                label: "3 Yr Sales g%",
                yAxisID: 'y-axis-1',
                type: 'line'
              },
              {
                label: "5 Yr Sales g%",
                yAxisID: 'y-axis-1',
                type: 'line'
              },
              {
                label: "10 Yr Sales g%",
                yAxisID: 'y-axis-1',
                type: 'line'
              },
        ];

        $scope.stockPnlSales = [sales];

        $scope.stockPnlSalesDataOverride = [
          {
            label: "Sales",
            yAxisID: 'y-axis-1',
            type: 'bar'
          }
        ];

        $scope.chartOptionsStockPnlSales = {
          scales: {
            yAxes: [
              {
                id: 'y-axis-1',
                type: 'linear',
                display: true,
                position: 'left',
                ticks: { min: 0 }
              }
            ]
          }
        };

    }

    function populateStockPriceMovements() {
        var data1dpricemovements = [];
        var data1wpricemovements = [];
        var data2wpricemovements = [];
        var data1mpricemovements = [];
        var labelspricemovements = [];

        for (var i = 0; i < $scope.priceMovements.length; i++) {
            data1dpricemovements.push($scope.priceMovements[i].return1D);
            data1wpricemovements.push($scope.priceMovements[i].return1W);
            data2wpricemovements.push($scope.priceMovements[i].return2W);
            data1mpricemovements.push($scope.priceMovements[i].return1M);
            labelspricemovements.push($scope.priceMovements[i].key.date);
        }
        $scope.data1DPriceMovements.push(data1dpricemovements);
        $scope.data1WPriceMovements.push(data1wpricemovements);
        $scope.data2WPriceMovements.push(data2wpricemovements);
        $scope.data1MPriceMovements.push(data1mpricemovements);
       /* $scope.dataPriceMovements.push(data1dpricemovements);
        $scope.dataPriceMovements.push(data1wpricemovements);
        $scope.dataPriceMovements.push(data2wpricemovements);
        $scope.dataPriceMovements.push(data1mpricemovements);*/

        $scope.labelsPriceMovements = labelspricemovements;

        var count1D = data1dpricemovements.length;
        var sum1D = data1dpricemovements.reduce((previous, current) => current += previous);
        $scope.mean1D = sum1D/count1D;
        var dummyArray1D = Array.from(data1dpricemovements);
        dummyArray1D.sort((a, b) => a - b);
        var lowMiddle1D = Math.floor((dummyArray1D.length - 1) / 2);
        var highMiddle1D = Math.ceil((dummyArray1D.length - 1) / 2);
        $scope.median1D = (dummyArray1D[lowMiddle1D] + dummyArray1D[highMiddle1D]) / 2;

        var count1W = data1wpricemovements.length;
        var sum1W = data1wpricemovements.reduce((previous, current) => current += previous);
        $scope.mean1W = sum1W/count1W;
        var dummyArray1W = Array.from(data1wpricemovements);
        dummyArray1W.sort((a, b) => a - b);
        var lowMiddle1W = Math.floor((dummyArray1W.length - 1) / 2);
        var highMiddle1W = Math.ceil((dummyArray1W.length - 1) / 2);
        $scope.median1W = (dummyArray1W[lowMiddle1W] + dummyArray1W[highMiddle1W]) / 2;

        var count2W = data2wpricemovements.length;
        var sum2W = data2wpricemovements.reduce((previous, current) => current += previous);
        $scope.mean2W = sum2W/count2W;
        var dummyArray2W = Array.from(data2wpricemovements);
        dummyArray2W.sort((a, b) => a - b);
        var lowMiddle2W = Math.floor((dummyArray2W.length - 1) / 2);
        var highMiddle2W = Math.ceil((dummyArray2W.length - 1) / 2);
        $scope.median2W = (dummyArray2W[lowMiddle2W] + dummyArray2W[highMiddle2W]) / 2;

        var count1M = data1mpricemovements.length;
        var sum1M = data1mpricemovements.reduce((previous, current) => current += previous);
        $scope.mean1M = sum1M/count1M;
        var dummyArray1M = Array.from(data1mpricemovements);
        dummyArray1M.sort((a, b) => a - b);
        var lowMiddle1M = Math.floor((dummyArray1M.length - 1) / 2);
        var highMiddle1M = Math.ceil((dummyArray1M.length - 1) / 2);
        $scope.median1M = (dummyArray1M[lowMiddle1M] + dummyArray1M[highMiddle1M]) / 2;

    }

    function populateStockRecentPE() {
        //console.log($scope.stockRecentPE);
        var recentpe = [];
        var recentmcap = [];
        var recentMarketPrice = [];
        var recentpb = [];
        var recentevtoebita = [];
        var recentpedate = [];
        var resultDateMCap = [];

        for (var i = 0; i < $scope.recentValuations.length; i++) {
            recentpe.push($scope.recentValuations[i].pe);
            recentmcap.push($scope.recentValuations[i].marketCap); //populate
            recentMarketPrice.push($scope.recentValuations[i].marketPrice);
            recentpb.push($scope.recentValuations[i].pb);
            recentevtoebita.push($scope.recentValuations[i].evToEbita);
            recentpedate.push($scope.recentValuations[i].date);
            var resultMCap = $scope.recentValuations[i].resultDateMCap;
            resultDateMCap.push((resultMCap && resultMCap > 0) ? resultMCap : null);
        }
        $scope.dataRecentPE.push(recentpe);
        $scope.dataRecentMCap.push(recentmcap);
        $scope.dataRecentMarketPrice.push(recentMarketPrice);
        $scope.dataRecentMCapAndPrice.push(recentmcap);
        $scope.dataRecentMCapAndPrice.push(recentMarketPrice);
        $scope.dataRecentMCapAndPrice.push(resultDateMCap);
        //console.log(resultDateMCap);
        $scope.dataRecentPB.push(recentpb);
        $scope.dataRecentEvToEbita.push(recentevtoebita);
        $scope.labelsRecentPE = recentpedate;

        $scope.chartOptionsMCapAndPrice = { scales: {
                                            yAxes: [
                                                {
                                                    id: 'y-axis-1',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'left',
                                                    ticks: { min:0 }
                                                },
                                                {
                                                    id: 'y-axis-2',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'right',
                                                    ticks: { min:0 }
                                                }
                                            ]
                                       } };
        $scope.chartMCapAndPriceDatasetOverride = [
            {
                label: "MCap",
                yAxisID: 'y-axis-1',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                lineTension: 0.4
            },
            {
                label: "Price",
                yAxisID: 'y-axis-2',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                lineTension: 0.4
            },
            {
                label: "Result MCap",
                yAxisID: 'y-axis-1',
                type: 'line',
                showLine: false,
                pointRadius: 8,
                pointHoverRadius: 10,
                pointStyle: 'triangle',
                borderWidth: 0,
                backgroundColor: '#d9534f',
                borderColor: '#d9534f',
                pointBackgroundColor: '#d9534f',
                pointBorderColor: '#d9534f'
            }
        ];
        $scope.chartMCapAndPriceSeries = ['Market Cap', 'Price', 'Result MCap'];

        $scope.chartOptionsRecentPE = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    position: 'left',
                    ticks: {
                        min: 0
                    }
                }]
            }
        };
        $scope.chartRecentPEDatasetOverride = [
            {
                label: "TTM PE",
                yAxisID: 'y-axis-1',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4
            }
        ];
        $scope.chartOptionsRecentPB = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    position: 'left',
                    ticks: {
                        min: 0
                    }
                }]
            }
        };

        $scope.chartRecentPBDatasetOverride = [
            {
                label: "TTM PB",
                yAxisID: 'y-axis-1',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4
            }
        ];

        $scope.chartOptionsRecentEvToEbita = {
            scales: {
                yAxes: [{
                    id: 'y-axis-1',
                    type: 'linear',
                    position: 'left',
                    ticks: {
                        min: 0
                    }
                }]
            }
        };

        $scope.chartRecentEvToEbitaDatasetOverride = [
            {
                label: "EV/EBITA",
                yAxisID: 'y-axis-1',
                type: 'line',
                borderDash: [],
                borderWidth: 2,
                pointRadius: 0,
                tension: 0.4
            }
        ];


    }

    function populateStockQuarterChart() {

        /* Begin: Populate Quarter Sales and Profit */
        var sortedStockQuarter = $filter('orderBy')($scope.stockQuarter,'key.date');
        //console.log("$scope.stockQuarter"+$scope.stockQuarter);
        var map = $filter('groupBy')(sortedStockQuarter, 'key.date');
        //console.log("map"+map);

        var years = [];
        var sales = [];
        var noplat = [];
        var salesGrowth = [];
        var opm = [];
        var operatingProfitGrowth = [];
//        var computedNpm = 0.00;
        var i = Object.keys(map).length;

        for(var yr in map){
           i = i - 1;
           if (i > 10) {
             continue;
           }
           years.push(yr);
           //console.log(yr);
           sales.push(map[yr][0].sales);
           //console.log(map[yr][0].sales);
           noplat.push(map[yr][0].noplat);
           salesGrowth.push(Number(Math.round(map[yr][0].salesG*100 * 100)/100 || 1));
           operatingProfitGrowth.push(Number(Math.round(map[yr][0].ebitdaG*100 * 100)/100 || 1));
           opm.push(Number(Math.round(map[yr][0].opm*100 * 100)/100 || 1));
        }

        $scope.labelsYearsStockQuarter = years;
        $scope.stockQuarter = [];
        $scope.stockQuarter.push(noplat);
        $scope.stockQuarter.push(opm);

        $scope.chartOptionsStockQuarter = { scales: {
                                            yAxes: [
                                                {
                                                    id: 'y-axis-1',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'left',
                                                    ticks: { min:0 }
                                                },
                                                {
                                                    id: 'y-axis-2',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'right',
                                                    ticks: { min:0 }
                                                }
                                            ]
                                       } };

        $scope.stockQuarterDataOverride = [
              {
                label: "NOPLAT",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "OPM%",
                yAxisID: 'y-axis-2',
                type: 'line', borderDash: [], borderWidth: 2, pointRadius: 0, tension: 0.4
              },
        ];

        $scope.stockQuarterSales = [sales];

        $scope.stockQuarterSalesDataOverride = [
          {
            label: "Sales",
            yAxisID: 'y-axis-1',
            type: 'bar'
          }
        ];

        $scope.chartOptionsStockQuarterSales = {
          scales: {
            yAxes: [
              {
                id: 'y-axis-1',
                type: 'linear',
                display: true,
                position: 'left',
                ticks: { min: 0 }
              }
            ]
          }
        };


    }

    function renderExcelCharts() {
        if ($scope.stockDetails.excelCharts && $scope.stockDetails.excelCharts.length > 0) {
            let container = document.getElementById('dynamicCustomChartsContainer');
            container.innerHTML = ""; // Clear previous charts

            $scope.stockDetails.excelCharts.forEach(function (chartInfo, index) {

                if (index % 2 === 0) {
                    // Create new row every 2 charts
                    rowDiv = document.createElement('div');
                    rowDiv.className = 'row';
                    container.appendChild(rowDiv);
                }
                // === Outer Column Wrapper ===
                let wrapperColDiv = document.createElement('div');
                wrapperColDiv.className = 'col-md-6 col-sm-12 col-xs-12';
                wrapperColDiv.style.padding = "10px";

                // === x_panel ===
                let panel = document.createElement('div');
                panel.className = 'x_panel';

                // === x_title ===
                let titleDiv = document.createElement('div');
                titleDiv.className = 'x_title';

                let titleH2 = document.createElement('h2');

                let chartSeries = (chartInfo.series && chartInfo.series.length > 0)
                    ? chartInfo.series
                    : [{ label: chartInfo.fieldName || chartInfo.title, values: chartInfo.values, chartType: chartInfo.chartType || 'line' }];
                let isStacked = chartInfo.stacked === true;
                let showLegend = (chartInfo.showLegend === true) || chartSeries.length > 1;

                let latestVal = null;
                let latestSeriesName = '';
                let latestBySeries = [];
                chartSeries.forEach(function (series) {
                    if (!series || !series.values) return;
                    for (let i = series.values.length - 1; i >= 0; i--) {
                        if (series.values[i] != null && isFinite(series.values[i])) {
                            latestBySeries.push({
                                name: series.label || '',
                                value: series.values[i]
                            });
                            if (latestVal == null) {
                                latestVal = series.values[i];
                                latestSeriesName = series.label || '';
                            }
                            break;
                        }
                    }
                });

                // Format value
                let formattedLatest = "";
                if (latestVal != null) {
                    if (Math.abs(latestVal) <= 1) {
                        formattedLatest = (latestVal * 100).toFixed(1) + "%";
                    } else if (Math.abs(latestVal) > 1000) {
                        formattedLatest = Math.round(latestVal).toLocaleString('en-IN');
                    } else {
                        formattedLatest = latestVal.toFixed(1);
                    }
                }

                let latestSummary = '';
                if (latestBySeries.length > 0) {
                    latestSummary = latestBySeries
                        .slice(0, 10)
                        .map(function(item) {
                            let val = item.value;
                            let fmt = '';
                            if (Math.abs(val) <= 1) {
                                fmt = (val * 100).toFixed(1) + '%';
                            } else if (Math.abs(val) > 1000) {
                                fmt = Math.round(val).toLocaleString('en-IN');
                            } else {
                                fmt = val.toFixed(1);
                            }
                            return `${item.name}: ${fmt}`;
                        })
                        .join(' | ');
                }

                // Title text with subtitle
                titleH2.innerHTML = `${chartInfo.title} <small style="
                    display: block;
                    font-size: 13px;
                    font-weight: normal;
                    color: #555;
                    margin-top: 6px;
                    line-height: 1.6;
                    white-space: normal;
                    word-break: break-word;
                    overflow-wrap: break-word;
                ">(${latestSummary || (latestSeriesName ? `Series: ${latestSeriesName}` : `Field: ${chartInfo.fieldName}`)}${(!latestSummary && formattedLatest) ? `, Latest: ${formattedLatest}` : ''})</small>`;

                titleDiv.appendChild(titleH2);

                let toolbox = document.createElement('ul');
                toolbox.className = 'nav navbar-right panel_toolbox';
                toolbox.innerHTML = `
                    <li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a></li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                            <i class="fa fa-wrench"></i>
                        </a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a href="#">Settings 1</a></li>
                            <li><a href="#">Settings 2</a></li>
                        </ul>
                    </li>
                    <li><a class="close-link"><i class="fa fa-close"></i></a></li>
                `;
                titleDiv.appendChild(toolbox);

                let clearFix = document.createElement('div');
                clearFix.className = 'clearfix';
                titleDiv.appendChild(clearFix);

                panel.appendChild(titleDiv);

                // === x_content ===
                let contentDiv = document.createElement('div');
                contentDiv.className = 'x_content';

                let canvas = document.createElement('canvas');
                canvas.style.width = '100%';
                canvas.style.height = '320px';
                contentDiv.appendChild(canvas);

                panel.appendChild(contentDiv);
                wrapperColDiv.appendChild(panel);
                rowDiv.appendChild(wrapperColDiv);

                const palette = ['#26B99A', '#3498DB', '#9B59B6', '#F39C12', '#E74C3C', '#1ABC9C', '#2ECC71', '#34495E'];
                let datasets = [];

                chartSeries.forEach(function(series, idx) {
                    if (!series || !series.values) return;
                    let formattedValues = series.values.map(function(val) {
                        if (val == null) return null;
                        if (Math.abs(val) <= 1) {
                            return +(val * 100).toFixed(1);
                        } else if (Math.abs(val) > 1000) {
                            return Math.round(val);
                        } else {
                            return +val.toFixed(1);
                        }
                    });

                    const color = palette[idx % palette.length];
                    let datasetType = series.chartType || chartInfo.chartType || 'line';
                    let validPointCount = formattedValues.filter(function(v) {
                        return v !== null && v !== undefined && !isNaN(v);
                    }).length;

                    datasets.push({
                        label: series.label || ('Series ' + (idx + 1)),
                        data: formattedValues,
                        borderWidth: 2,
                        borderColor: color,
                        backgroundColor: color,
                        fill: false,
                        type: datasetType,
                        pointRadius: (datasetType === 'bar') ? 0 : 2,
                        tension: 0,
                        showLine: (datasetType === 'line') ? (validPointCount > 1) : true,
                        spanGaps: true,
                        stack: isStacked ? 'stack-group-1' : undefined   // ADD THIS LINE
                    });
                });

                datasets = datasets.filter(function(ds) {
                    return ds.data && ds.data.some(function(v) { return v !== null && v !== undefined && !isNaN(v); });
                });
                if (datasets.length === 0) {
                    return;
                }

                let primaryType = datasets[0].type || 'line';
                let normalizedLabels = (chartInfo.labels || []).map(function(lbl, idx) {
                    if (lbl === null || lbl === undefined || lbl === '') {
                        return 'P' + (idx + 1);
                    }
                    return lbl;
                });

                let maxDataPoints = datasets.reduce(function(maxLen, ds) {
                    return Math.max(maxLen, (ds.data && ds.data.length) ? ds.data.length : 0);
                }, 0);

                if (normalizedLabels.length === 0 && maxDataPoints > 0) {
                    normalizedLabels = Array.from({ length: maxDataPoints }, function(_, idx) {
                        return 'P' + (idx + 1);
                    });
                } else if (normalizedLabels.length < maxDataPoints) {
                    for (let i = normalizedLabels.length; i < maxDataPoints; i++) {
                        normalizedLabels.push('P' + (i + 1));
                    }
                }

                datasets = datasets.map(function(ds) {
                    let nextData = (ds.data || []).slice(0, normalizedLabels.length);
                    while (nextData.length < normalizedLabels.length) {
                        nextData.push(null);
                    }
                    ds.data = nextData;
                    return ds;
                });

                // === Create Chart ===
                try {
                    new Chart(canvas.getContext('2d'), {
                    type: primaryType,
                    data: {
                        labels: normalizedLabels,
                        datasets: datasets
                    },
                    options: {
                        responsive: true,
                        plugins: {
                            title: {
                                display: false
                            },
                            legend: {
                                display: showLegend
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        let val = context.raw;
                                        if (val == null) return '';
                                        if (Math.abs(val) >= 1000) {
                                            return Number(val).toLocaleString('en-IN', { maximumFractionDigits: 0 });
                                        } else if (Math.abs(val) <= 100) {
                                            return Number(val).toLocaleString('en-IN', { maximumFractionDigits: 1 });
                                        } else {
                                            return Number(val).toFixed(1);
                                        }
                                    }
                                }
                            }
                        },
                        scales: {
                            y: {
                                stacked: isStacked,
                                beginAtZero: true,
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
                                stacked: isStacked,
                                ticks: {
                                    autoSkip: true,
                                    maxTicksLimit: 10
                                }
                            }
                        }
                    }
                });
                } catch (e) {
                    console.error('Failed rendering custom excel chart', chartInfo, e);
                }
            });
        }
    }

    // === Extended: Display Notes Section for Each Ticker ===
    function renderReportNotes() {
        let container = document.getElementById("reportNotesContainer");
        if (!container) {
            return;
        }
        container.innerHTML = "";

        let notes = $scope.stockDetails.reportNotes;
        if (!notes || notes.length === 0) {
            container.innerHTML = "<p>No notes available.</p>";
            return;
        }

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
    }
});