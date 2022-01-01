var module = angular.module('StockAnalysisManagement', ['angular.filter','chart.js']);

module.controller('StockAnalysisController', function($scope, $http, $filter, $window) {
    var urlBase="/public/api";
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];
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

    showRecords();

    function showRecords(){
        $scope.stockPnl = new Array();
        //console.log($scope.ticker);
        var url = "/getstockdetails/"+$scope.ticker;
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.stockDetails = response.data;
                } else {
                    $scope.stockDetails = [];
                }
            });

        url = "/getstockquarter/"+$scope.ticker;
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.stockQuarter = response.data;
                    $scope.stockQuarterOriginal = response.data;
                    url = "/getstockpnl/"+$scope.ticker;
                    $http.get(urlBase + url).
                        then(function (response) {
                            if (response != undefined) {
                                $scope.stockPnl = response.data;
                                $scope.stockPnlOriginal = response.data;
                                populateStockPnlChart();
                            } else {
                                $scope.stockPnl = [];
                                $scope.stockPnlOriginal = [];
                            }
                        });
                    populateStockQuarterChart();
                } else {
                    $scope.stockQuarter = [];
                    $scope.stockQuarterOriginal = [];
                }
        });

        url = "/getrecentvaluations/"+$scope.ticker;
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.recentValuations = response.data;
                    populateStockRecentPE();
                } else {
                    $scope.recentValuations = [];
                }
        });

        url = "/getpricemovements/"+$scope.ticker;
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.priceMovements = response.data;
                    populateStockPriceMovements();
                } else {
                    $scope.priceMovements = [];
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
        var operatingProfit = [];
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
           //console.log(yr);
           sales.push(Math.round(map[yr][0].sales*100)/100);
           //console.log(map[yr][0].sales);
           operatingProfit.push(Math.round(map[yr][0].operatingProfit*100)/100);
//           salesGrowth.push(Math.round(map[yr][0].salesG*100)/100);
//           operatingProfitGrowth.push(Math.round(map[yr][0].ebitdaG*100)/100);
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
        var ttmOperatingProfit = Number(0.00||0);
        var ttmSalesGrowth = Number(0.00||0);
        var ttmOperatingProfitGrowth = Number(0.00||0);

        for(var yr in map1){
           i = i + 1;
           if (i > 1) {
             break;
           }
           ttmSales = Number(map1[yr][0].ttmSales || 0);
           ttmOperatingProfit = Number(map1[yr][0].ttmEbitda || 0);
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
        operatingProfit.push(Math.round(ttmOperatingProfit*100)/100);
        salesGrowth.push(ttmSalesGrowth);
        operatingProfitGrowth.push(ttmOperatingProfitGrowth);

        $scope.labelsYearsStockPnl = years;
        $scope.stockPnl = [];
        $scope.stockPnl.push(sales);
        $scope.stockPnl.push(operatingProfit);
        $scope.stockPnl.push(salesGrowth);
        $scope.stockPnl.push(operatingProfitGrowth);

        $scope.labelsYearsStockMargin = years;
        $scope.stockMargin = [];
        $scope.stockMargin.push(margin);

        $scope.labelsYearsStockValuation = years;
        $scope.stockValuation = [];
        pe.push($scope.stockDetails.dailyDataS.peTtm);
        $scope.stockValuation.push(pe);


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
                                                    position: 'right'
                                                }
                                            ]
                                       } };

        $scope.stockPnlDataOverride = [
              {
                label: "Sales",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "Operating Profit",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "Sales g%",
                yAxisID: 'y-axis-2',
                type: 'line'
              },
              {
                label: "Op. Profit g%",
                yAxisID: 'y-axis-2',
                type: 'line'
              },
        ];

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

        for (var i = 0; i < $scope.recentValuations.length; i++) {
            recentpe.push($scope.recentValuations[i].pe);
            recentmcap.push($scope.recentValuations[i].marketCap); //populate
            recentMarketPrice.push($scope.recentValuations[i].marketPrice);
            recentpb.push($scope.recentValuations[i].pb);
            recentevtoebita.push($scope.recentValuations[i].evToEbita);
            recentpedate.push($scope.recentValuations[i].date);
        }
        $scope.dataRecentPE.push(recentpe);
        $scope.dataRecentMCap.push(recentmcap);
        $scope.dataRecentMarketPrice.push(recentMarketPrice);
        $scope.dataRecentMCapAndPrice.push(recentmcap);
        $scope.dataRecentMCapAndPrice.push(recentMarketPrice);
        //console.log($scope.dataRecentMCap);
        $scope.dataRecentPB.push(recentpb);
        $scope.dataRecentEvToEbita.push(recentevtoebita);
        $scope.labelsRecentPE = recentpedate;

        $scope.chartOptionsMCapAndPrice = { scales: {
                                            yAxes: [
                                                {
                                                    id: 'y-axis-1',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'left'
                                                },
                                                {
                                                    id: 'y-axis-2',
                                                    type: 'linear',
                                                    display: true,
                                                    position: 'right'
                                                }
                                            ]
                                       } };
        $scope.chartMCapAndPriceDatasetOverride = [{ yAxisID: 'y-axis-1' }, { yAxisID: 'y-axis-2' }];
        $scope.chartMCapAndPriceSeries = ['Market Cap', 'Price'];


    }

    function populateStockQuarterChart() {

        /* Begin: Populate Quarter Sales and Profit */
        var sortedStockQuarter = $filter('orderBy')($scope.stockQuarter,'key.date');
        //console.log("$scope.stockQuarter"+$scope.stockQuarter);
        var map = $filter('groupBy')(sortedStockQuarter, 'key.date');
        //console.log("map"+map);

        var years = [];
        var sales = [];
        var operatingProfit = [];
        var salesGrowth = [];
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
           operatingProfit.push(map[yr][0].operatingProfit);
           salesGrowth.push(Number(Math.round(map[yr][0].salesG*100 * 100)/100 || 1));
           operatingProfitGrowth.push(Number(Math.round(map[yr][0].ebitdaG*100 * 100)/100 || 1));
//           computedNpm = (map[yr][0].netProfit/map[yr][0].sales);
//           computedNpm = Math.round(computedNpm * 100)/100;
//           npm.push(computedNpm);
        }

        $scope.labelsYearsStockQuarter = years;
        $scope.stockQuarter = [];
        $scope.stockQuarter.push(sales);
        $scope.stockQuarter.push(operatingProfit);
        $scope.stockQuarter.push(salesGrowth);
        $scope.stockQuarter.push(operatingProfitGrowth);

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
                                                    position: 'right'
                                                }
                                            ]
                                       } };

        $scope.stockQuarterDataOverride = [
              {
                label: "Sales",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "Operating Profit",
                yAxisID: 'y-axis-1',
                type: 'bar'
              },
              {
                label: "Sales g%",
                yAxisID: 'y-axis-2',
                type: 'line'
              },
              {
                label: "Op. Profit g%",
                yAxisID: 'y-axis-2',
                type: 'line'
              },
        ];

    }

});