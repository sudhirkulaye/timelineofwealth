var module = angular.module('StockListManagement', []);

module.controller('StockListController', function($scope, $http, $filter) {
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.stocks = [];
    $scope.indexFilter = "SENSEX";

    showRecords();

    function showRecords(){

        publicapiurl = "/public/api/getassetclassifications";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.assetClasses = response.data;
                } else {
                    $scope.assetClasses = [];
                }
            });

        publicapiurl = "/public/api/getsubindustries";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.industries = response.data;
                } else {
                    $scope.industries = [];
                }
            });

        publicapiurl = "/public/api/getnsebse500";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.stocks = response.data;
                } else {
                    $scope.stocks = [];
                }
            });

    }

    $scope.searchStock = function (stock) {
        if ($scope.searchTextFilter == undefined) {
            return true;
        } else {
            if (stock.shortName.toLowerCase().indexOf($scope.searchTextFilter.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByIndex = function (stock) {
        if ($scope.indexFilter == undefined || $scope.indexFilter == "") {
            return true;
        } else {
            if($scope.indexFilter == "SENSEX") {
                if(stock.includedIndexName == "SENSEX") {
                    return true;
                }
            }
            if($scope.indexFilter == "NIFTY50") {
                if(stock.includedIndexName == "NIFTY50" || stock.includedIndexName == "SENSEX") {
                    return true;
                }
            }
            if($scope.indexFilter == "NIFTY Next 50") {
                if(stock.includedIndexName == "NIFTY Next 50") {
                    return true;
                }
            }
            if($scope.indexFilter == "NSE-BSE100") {
                if(stock.includedIndexName == "NSE-BSE100" ||
                   stock.includedIndexName == "NIFTY Next 50" ||
                   stock.includedIndexName == "NIFTY50" ||
                   stock.includedIndexName == "SENSEX") {
                    return true;
                }
            }
            if($scope.indexFilter == "NSE-BSE200") {
                if(stock.includedIndexName == "NSE-BSE200" ||
                   stock.includedIndexName == "NSE-BSE100" ||
                   stock.includedIndexName == "NIFTY Next 50" ||
                   stock.includedIndexName == "NIFTY50" ||
                   stock.includedIndexName == "SENSEX") {
                    return true;
                }
            }
            if($scope.indexFilter == "NSE-BSE500") {
//                if(stock.includedIndexName == "NSE-BSE500" ||
//                   stock.includedIndexName == "NSE-BSE200" ||
//                   stock.includedIndexName == "NSE-BSE100" ||
//                   stock.includedIndexName == "NIFTY Next 50" ||
//                   stock.includedIndexName == "NIFTY50" ||
//                   stock.includedIndexName == "SENSEX") {
//                    return true;
//                }
                return true;
            }
        }
        return false;
    }

    $scope.filterByIndustry = function (stock) {
        if ($scope.industryFilter == undefined || $scope.industryFilter == "") {
            return true;
        } else {
            if (stock.industryNameDisplay.toLowerCase().indexOf($scope.industryFilter.toLowerCase()) != -1 ) {
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