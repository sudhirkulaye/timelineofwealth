var module = angular.module('StockBubbleChartManagement', ['angular.filter', 'chart.js', 'dx']);

module.controller('StockBubbleChartController', function ($scope, $http, $filter, $locale) {
    // Initialize data arrays and options
    $scope.xAxisData = [];
    $scope.yAxisData = [];
    $scope.bubbleSizeData = [];
    $scope.stocks = [];

    // Initialize labels and series
    $scope.chartLabels = [];
    $scope.chartSeries = ['MCap-OPM-NOPLAT'];
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];

    // Define unique sector and industry options for the dropdowns
    $scope.sectorOptions = [];
    $scope.industryOptions = [];

    // Set a default value for sectorFilter
    $scope.sectorFilter = "DiscretionaryApparel";

    // Set a default value for industryFilter (blank)
    $scope.industryFilter = "";

    showRecords();

    function showRecords() {
        publicapiurl = "/public/api/getnsebse500";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.stocks = response.data;

                    // Populate sector and industry options and make them unique
                    $scope.sectorOptions = $filter('unique')($scope.stocks, 'sectorNameDisplay');
                    $scope.industryOptions = $filter('unique')($scope.stocks, function (stock) {
                        return stock.sectorNameDisplay + '-' + stock.industryNameDisplay;
                    });

                    $scope.updateChart();
                } else {
                    $scope.stocks = [];
                }
            });
    }

    // Function to update the chart based on selected filters
    $scope.updateChart = function () {
        // Filter stocks based on sectorFilter and industryFilter
        var filteredStocks = $scope.stocks;

        if ($scope.sectorFilter && $scope.sectorFilter !== "" && $scope.industryFilter && $scope.industryFilter !== "") {
            filteredStocks = filteredStocks.filter(function (stock) {
                return (
                    $scope.sectorFilter + '-' + stock.industryNameDisplay
                ) === $scope.industryFilter;
            });
        } else if ($scope.sectorFilter && $scope.sectorFilter !== "") {
            filteredStocks = filteredStocks.filter(function (stock) {
                return stock.sectorNameDisplay === $scope.sectorFilter;
            });
        } else if ($scope.industryFilter && $scope.industryFilter !== "") {
            var right_text = $scope.industryFilter.split('-')[1].toLowerCase();
            filteredStocks = filteredStocks.filter(function (stock) {
                return stock.industryNameDisplay.toLowerCase().indexOf(right_text) !== -1;
            });
        }

        // Update the chart data based on the filtered stocks
        updateChartData(filteredStocks);
    };

    // Function to handle dropdown change event
    $scope.handleDropdownChange = function () {
        // Call the updateChart function when the dropdown value changes
        $scope.updateChart();
    };

    // Define a function to update the chart data
    function updateChartData() {

//        console.log("$scope.sectorFilter 1 - ", $scope.sectorFilter, " - $scope.sectorFilter - ", $scope.industryFilter);

        // Apply sectorFilter and industryFilter based on their values
        var filteredStocks = $scope.stocks.filter(function (stock) {
            return $scope.filterBySector(stock) && $scope.filterByIndustry(stock);
        });

        // Populate the dataset with the selected data
        $scope.chartData = filteredStocks.map(function (stock, index) {
            if (stock.dailyDataS) {
                return {
                    x: stock.dailyDataS.opmLastYear ? stock.dailyDataS.opmLastYear.toFixed(2) : 0,
                    y: stock.dailyDataS.noplat ? (stock.dailyDataS.noplat / 1000).toFixed(1) : 0,
                    r: stock.dailyDataS.marketCap ? (stock.dailyDataS.marketCap / 1000).toFixed(0) : 0
                };
            } else {
                return {
                    x: 0,
                    y: 0,
                    r: 0
                };
            }
        });

        // Update labels if needed
        $scope.chartLabels = filteredStocks.map(function (stock, index) {
            return 'Bubble ' + (index + 1);
        });
    }

    $scope.filterByIndustry = function(stock) {
        if ($scope.industryFilter == undefined || $scope.industryFilter == "") {
            return true;
        } else {
            var right_text = $scope.industryFilter.split('-')[1].toLowerCase();
            if (stock.industryNameDisplay.toLowerCase().indexOf(right_text) != -1) {
                return true;
            }
        }
        return false;
    };

    $scope.filterBySector = function(stock) {
        if ($scope.sectorFilter == undefined || $scope.sectorFilter == "") {
            return true;
        } else {
            if (stock.sectorNameDisplay.toLowerCase().indexOf($scope.sectorFilter.toLowerCase()) != -1) {
                return true;
            }
        }
        return false;
    };

    // Watch for changes in sectorFilter and industryFilter
    $scope.$watchGroup(['sectorFilter', 'industryFilter'], function (newValues, oldValues) {
        updateChartData();
    });

});