var module = angular.module('IndexStatsManagement', ['angular.filter','chart.js','dx']);

module.controller('IndexStatController', function($scope, $http, $filter, $locale) {
    var urlBase="/public/api";
    $scope.indexStatRecords = [];
    $scope.indexConsolidatedStatistics = [];
    $scope.chartData = [];
    $scope.labelsDates = [];
    $scope.chartSeries = ['Index Trailing PE', 'Index Value'];
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];
    $scope.chartDatasetOverride = [{ yAxisID: 'y-axis-1' }, { yAxisID: 'y-axis-2' }];
    //$scope.chartOptions = { scales: { yAxes: [{ ticks: { min:0 } }] } };
    $scope.chartOptions = {
        scales: {
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
        }
      };
    $scope.labelsDates = [];
    $scope.chartData = [];

    showRecords();

    function showRecords(){
        $scope.indexStatRecords = new Map;

        url = "/getindexvaluation";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.indexStatRecords = response.data;
                    setChartData();
                } else {
                    $scope.indexStatRecords = [];
                }
            });

        url = "/getindexstatistics";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.indexConsolidatedStatistics = response.data;
                    setChartData();
                } else {
                    $scope.indexConsolidatedStatistics = [];
                }
            });
    }

    function setChartData(){
        //console.log("index data length" + $scope.indexStatRecords.length);
        $scope.labelsDates = [];
        $scope.chartData = [];

        var dates = [];
        var peData = [];
        var indexValue = [];
        var filterDate = new Date();
        var oneYearBeforeDate = new Date();
        //console.log("$scope.yearFilter " + $scope.yearFilter);
        if ($scope.yearFilter == undefined){
            $scope.yearFilter = 1;
        }
        filterDate.setDate(filterDate.getDate() - $scope.yearFilter*365);
        oneYearBeforeDate.setDate(oneYearBeforeDate.getDate() - 365);
        //console.log("filterDate " + filterDate);
        //console.log("oneYearBeforeDate " + oneYearBeforeDate);
        for (var i = 0; i < $scope.indexStatRecords.length; i++ ) {
          if (new Date(filterDate) < new Date($scope.indexStatRecords[i].key.date)){
              if (new Date(oneYearBeforeDate) < new Date($scope.indexStatRecords[i].key.date)) { // put all records
                  dates.push($filter('date')(new Date($scope.indexStatRecords[i].key.date),'yyyy-MM-dd'));
                  peData.push($scope.indexStatRecords[i].pe);
                  indexValue.push($scope.indexStatRecords[i].value);
              } else { // put only weekly data
                  //console.log("Day"+ (new Date($scope.indexStatRecords[i].key.date)).getDay());
                  if ((new Date($scope.indexStatRecords[i].key.date)).getDay() == "5") {
                      dates.push($filter('date')(new Date($scope.indexStatRecords[i].key.date),'yyyy-MM-dd'));
                      peData.push($scope.indexStatRecords[i].pe);
                      indexValue.push($scope.indexStatRecords[i].value);
                  }
              }
          }
        }
        //console.log(peData);
        $scope.labelsDates = dates;
        $scope.chartData.push(peData);
        $scope.chartData.push(indexValue);
    }

    $scope.callSetChartData = function callSetChartData() {
        setChartData();
    }

});