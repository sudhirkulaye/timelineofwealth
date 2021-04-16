var module = angular.module('StockListManagement', []);

module.controller('StockListController', function($scope, $http, $filter) {
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.stocks = [];
    $scope.indexFilter = "SENSEX";
    $scope.flagBasicInfo = false;
    $scope.flagFundamentalInfo = true;
    $scope.flagCapitalStructureInfo = true;
    $scope.flagValuations = true;
    $scope.flagPricePerformance = true;
    $scope.flagAdditionalInfo = true;
    $scope.flagAnalystsCall = true;
    $scope.reverseSort = false;

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
            var right_text = $scope.industryFilter.split('-')[1].toLowerCase();
            //console.log("right_text ", right_text);
            if (stock.industryNameDisplay.toLowerCase().indexOf(right_text) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterBySector = function (stock) {
            if ($scope.sectorFilter == undefined || $scope.sectorFilter == "") {
                return true;
            } else {
                if (stock.sectorNameDisplay.toLowerCase().indexOf($scope.sectorFilter.toLowerCase()) != -1 ) {
                    return true;
                }
            }
            return false;
        }

    $scope.showColumns = function(selectedOption) {
        if (selectedOption == "BasicInfo") {
            $scope.flagBasicInfo = false;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "FundamentalInfo") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = false;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "CapitalStructureInfo") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = false;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "Valuations") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = false;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "PricePerformance") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = false;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "AdditionalInfo") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = false;
            $scope.flagAnalystsCall = true;
        }
        if (selectedOption == "AnalystsCall") {
            $scope.flagBasicInfo = true;
            $scope.flagFundamentalInfo = true;
            $scope.flagCapitalStructureInfo = true;
            $scope.flagValuations = true;
            $scope.flagPricePerformance = true;
            $scope.flagAdditionalInfo = true;
            $scope.flagAnalystsCall = false;
        }
    }

    $scope.sortData = function (column) {
        $scope.reverseSort = ($scope.sortColumn == column) ? !$scope.reverseSort : false;
        $scope.sortColumn = column;
    }
    $scope.getSortClass = function (column) {
        if ($scope.sortColumn == column) {
            return $scope.reverseSort ? 'fa fa-sort-up fa-fw' : 'fa fa-sort-down fa-fw'
        }
        return '';
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