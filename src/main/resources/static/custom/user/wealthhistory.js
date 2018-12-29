var module = angular.module('WealthHistoryDistributionManagement', ['angular.filter','chart.js','dx']);

module.controller('WealthHistoryDistributionController', function($scope, $http, $filter, $locale) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.wealthHistoryRecords = {};
    $scope.latestTotalWealth = 0.0;

    $scope.wealthHistoryData = [];
    $scope.labelsDates = [];
    $scope.chartSeries = ['Total Wealth'];
    $scope.newColors = ['#26B99A', '#03586A', '#1E947B', '#1a3c33', '#DCDCDC', '#46BFBD', '#FDB45C'];
    $scope.chartOptions = { scales: { yAxes: [{ ticks: { min:0 } }] } };

    showRecords();

    function showRecords(){
        $scope.wealthHistoryRecords = new Map;
        $scope.hideForm = true;

        var url = "/getusermembers";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.members = response.data;
                } else {
                    $scope.members = [];
                }
            });

        url = "/getwealthhistory";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.wealthHistoryRecords = response.data;
                    setChartData();
                } else {
                    $scope.wealthHistoryRecords = {};
                }
            });
    }

    function setChartData(){

        var dates = [];
        var wealthData = [];

        for (var date in $scope.wealthHistoryRecords ) {
          dates.push($filter('date')(new Date(date),'yyyy-MM-dd'));
          wealthData.push($scope.wealthHistoryRecords[date][$scope.searchMember]);
          $scope.latestTotalWealth = $scope.wealthHistoryRecords[date][$scope.searchMember]; //last value will be set
        }

        $scope.labelsDates = dates;
        $scope.wealthHistoryData = [];
        $scope.wealthHistoryData.push(wealthData);
    }

    $scope.callSetChartData = function callSetChartData() {
        setChartData();
    }

    $scope.getMarketValue = function(wealthDetailsRecord) {
        return wealthDetailsRecord
        .map(function(x) { return x.marketValue; })
        .reduce(function(a, b) { return a + b; });
    }

    $scope.filterByMember = function (wealthDetailsRecord) {
        if ($scope.searchMember == undefined || $scope.searchMember == 0) {
            return true;
        } else {
            if (wealthDetailsRecord.key.memberid == $scope.searchMember ) {
                return true;
            }
        }
        return false;
    }

});

module.filter('unique', function() {
   // we will return a function which will take in a collection
   // and a keyname
   return function(collection, keyname) {
      // we define our output and keys array;
      var output = [],
          keys = [];

      // we utilize angular's foreach function
      // this takes in our original collection and an iterator function
      angular.forEach(collection, function(item) {
          // we check to see whether our object exists
          var key = item[keyname];
          // if it's not already part of our keys array
          if(keys.indexOf(key) === -1) {
              // add it to our keys array
              keys.push(key);
              // push this item to our final output array
              output.push(item);
          }
      });
      // return our array which should be devoid of
      // any duplicates
      return output;
   };
});