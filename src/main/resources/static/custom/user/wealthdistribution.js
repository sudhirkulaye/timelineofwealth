var module = angular.module('WealthDistributionManagement', ['angular.filter','chart.js']);

module.controller('WealthDistributionController', function($scope, $http, $filter, $locale) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.wealthDetailsRecords = [];
    $scope.wealthAssetAllocationHistoryRecords = [];
    $scope.totalMarketValue = 0.0;
    $scope.totalMarketValueByClassGroup = 0.0;

    showRecords();

    function showRecords(){
        $scope.wealthDetailsRecords = new Array;
        $scope.wealthAssetAllocationHistoryRecords = new Array;

        var url = "/getusermembers";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.members = response.data;
                } else {
                    $scope.members = [];
                }
            });

        publicapiurl = "/public/api/getassetclassifications";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.assetClasses = response.data;
                } else {
                    $scope.assetClasses = [];
                }
            });

        /*publicapiurl = "/public/api/getsubindustries";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.industries = response.data;
                } else {
                    $scope.industries = [];
                }
            });*/

        url = "/getwealthdetailsrecords";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.wealthDetailsRecords = response.data;
                    setChartData();
                } else {
                    $scope.wealthDetailsRecords = [];
                }
            });

        url = "/getcurretassetallocation";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.wealthAssetAllocationHistoryRecords = response.data;
                    setChartData();
                } else {
                    $scope.wealthAssetAllocationHistoryRecords = [];
                }
            });
    }

    function setChartData(){
        $scope.assetSubClasses = [];
        $scope.marketValuesBySubClasses = [];
        $scope.totalMarketValue = 0;
        $locale.NUMBER_FORMATS.GROUP_SEP = '';
        var filteredRecords = $scope.wealthDetailsRecords;
        if ($scope.searchMember != undefined && $scope.searchMember != 0) {
           filteredRecords = $filter('filter')(filteredRecords, {key:{memberid:$scope.searchMember}} );
        }
        var sortedRecords = $filter('orderBy')(filteredRecords,['assetClassid']);
        if (sortedRecords == undefined || sortedRecords.length < 1) {
            $scope.totalMarketValue = 0;
        } else {
            for (var i = sortedRecords.length - 1; i >= 0; i--){
                $scope.totalMarketValue += parseInt(sortedRecords[i].marketValue);
            }
        }
        var map = $filter('groupBy')(sortedRecords, 'assetClassid');
        for(var assetClassid in map){
           $scope.assetSubClasses.push($filter('filter')($scope.assetClasses, {classid:assetClassid})[0].subclassName);
           $scope.marketValuesBySubClasses.push($filter('number')($scope.getMarketValue(map[assetClassid]),0));
        }

        //set asset class chart
        $scope.assetClassGroups = [];
        $scope.marketValuesByClassGroups = [];
        $scope.totalMarketValueByClassGroup = 0;
        $scope.wealthAssetAllocationHistoryRecords = $filter('orderBy')($scope.wealthAssetAllocationHistoryRecords, 'key.assetClassGroup')
        var classwiseFilteredRecords = $scope.wealthAssetAllocationHistoryRecords;

        if ($scope.searchMember != undefined && $scope.searchMember != 0) {
           classwiseFilteredRecords = $filter('filter')(classwiseFilteredRecords, {key:{memberid:$scope.searchMember}} );
        }
        if (classwiseFilteredRecords == undefined || classwiseFilteredRecords.length < 1) {
            $scope.totalMarketValueByClassGroup = 0;
        } else {
            for (var i = classwiseFilteredRecords.length - 1; i >= 0; i--){
                $scope.totalMarketValueByClassGroup += parseInt(classwiseFilteredRecords[i].value);
            }
        }
        var map1 = $filter('groupBy')(classwiseFilteredRecords, 'key.assetClassGroup');
        //console.log(classwiseFilteredRecords);
        for(var assetClassGroup in map1){
           $scope.assetClassGroups.push(assetClassGroup);
           $scope.marketValuesByClassGroups.push($filter('number')($scope.getMarketValueByGroup(map1[assetClassGroup]),0));
        }
        //console.log($scope.assetClassGroups);
        //console.log($scope.marketValuesByClassGroups);

        $locale.NUMBER_FORMATS.GROUP_SEP = ',';
    }

    $scope.callSetChartData = function callSetChartData() {
        setChartData();
    }

    $scope.getMarketValue = function(wealthDetailsRecord) {
        return wealthDetailsRecord
        .map(function(x) { return x.marketValue; })
        .reduce(function(a, b) { return a + b; });
    }

    $scope.getMarketValueByGroup = function(wealthAssetAllocationHistoryRecord) {
        return wealthAssetAllocationHistoryRecord
        .map(function(x) { return x.value; })
        .reduce(function(a, b) { return a + b; });
    }

    $scope.filterByMember = function (record) {
        if ($scope.searchMember == undefined || $scope.searchMember == 0) {
            return true;
        } else {
            if (record.key.memberid == $scope.searchMember ) {
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