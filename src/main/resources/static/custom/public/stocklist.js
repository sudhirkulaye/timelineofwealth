var module = angular.module('StockListManagement', []);

module.controller('StockListController', function($scope, $http, $filter) {
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.stocks = [];
    $scope.indexFilter = "SENSEX";
    $scope.watchlistFilter = "";
    $scope.watchlists = [];
    $scope.consensus = {};
    $scope.consensusAsOf = "";
    $scope.flagBasicInfo = false;
    $scope.flagFundamentalInfo = true;
    $scope.flagCapitalStructureInfo = true;
    $scope.flagValuations = true;
    $scope.flagPricePerformance = true;
    $scope.flagAdditionalInfo = true;
    $scope.flagConsensus = true;
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
                    attachConsensus();
                } else {
                    $scope.stocks = [];
                }
            });

        $http.get("/public/api/getwatchlists").then(function (response) {
            $scope.watchlists = (response && response.data) || [];
        });

        $http.get("/public/api/getstocklistconsensus").then(function (response) {
            $scope.consensus = (response && response.data) || {};
            attachConsensus();
        });

    }

    // Consensus is weekly; upside is worked out against each stock's latest (daily) price.
    function attachConsensus() {
        var latest = "";
        angular.forEach($scope.stocks, function (stock) {
            var c = $scope.consensus[stock.ticker];
            if (!c) { stock.cons = null; return; }
            var price = Number(stock.latestPrice);
            var cons = angular.extend({}, c);
            cons.upside = (c.tpAvg && price > 0) ? c.tpAvg / price - 1 : null;
            cons.brokerList = [];
            angular.forEach(['1', '2', '3', '4'], function (k) {
                var b = c.brokers && c.brokers[k];
                if (b) cons.brokerList.push({ no: k, label: 'Broker ' + k, reco: b.reco, tp: b.tp, date: b.date,
                                              upside: (b.tp && price > 0) ? b.tp / price - 1 : null });
            });
            if (c.asOf && c.asOf > latest) latest = c.asOf;
            stock.cons = cons;
        });
        if (latest) $scope.consensusAsOf = latest;
    }

    // soft green / red cell background; value and cap in the same unit (percent), cap = value for the darkest shade
    $scope.heat = function (value, cap) {
        if (value === null || value === undefined || value === '' || isNaN(value)) return {};
        var v = Number(value);
        var a = Math.min(Math.abs(v) / cap, 1) * 0.42 + 0.04;
        return { 'background-color': v >= 0 ? 'rgba(27,175,122,' + a.toFixed(2) + ')' : 'rgba(208,59,59,' + a.toFixed(2) + ')' };
    };

    $scope.recoClass = function (reco) {
        var r = String(reco || '').toUpperCase();
        if (r === 'BUY' || r === 'ADD' || r === 'ACCUMULATE' || r === 'STRONG BUY') return 'sl-buy';
        if (r === 'SELL' || r === 'REDUCE') return 'sl-sell';
        return 'sl-hold';
    };

    $scope.pct = function (x) {
        if (x === null || x === undefined || isNaN(x)) return '';
        var v = x * 100;
        var s = v.toFixed(1);
        if (Number(s) === 0) s = s.replace('-', '');
        return (Number(s) > 0 ? '+' : '') + s + '%';
    };

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

    $scope.filterByFno = function (stock) {
        if ($scope.fnoFilter == undefined || $scope.fnoFilter == "") {
            return true;
        } else {
            if($scope.fnoFilter == "FnO") {
                if(stock.isFno == "1") {
                    return true;
                }
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


    $scope.filterByWatchlist = function (stock) {
        if ($scope.watchlistFilter == undefined || $scope.watchlistFilter == "") {
            return true;
        }

        if (stock.watchlistNames == undefined) {
            return false;
        }

        return stock.watchlistNames.indexOf($scope.watchlistFilter) !== -1;
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

    var VIEWS = { BasicInfo: 'flagBasicInfo', FundamentalInfo: 'flagFundamentalInfo',
        CapitalStructureInfo: 'flagCapitalStructureInfo', Valuations: 'flagValuations',
        PricePerformance: 'flagPricePerformance', AdditionalInfo: 'flagAdditionalInfo', Consensus: 'flagConsensus' };
    $scope.showColumns = function(selectedOption) {
        angular.forEach(VIEWS, function (flag, view) { $scope[flag] = (view !== selectedOption); });
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

module.filter('quarterSplit', function() {
  return function(input) {
    if (!input) return input;
    var lines = input.split(' FY');
    for (var i = 1; i < lines.length; i++) {
      lines[i] = lines[i].substring(2);
    }
    if(lines[0].length > 4)
        lines[0] = lines[0].substring(4);
    return lines.join('\n');
  };
});

module.filter('removeQuarter', function() {
  return function(input) {
    if (!input) return input;
    var lines = input.split(' FY');
    for (var i = 1; i < lines.length; i++) {
      lines[i] = lines[i].substring(5);
    }
    if(lines[0].length > 7)
        lines[0] = lines[0].substring(7);
    return lines.join('\n');
  };
});

/*module.filter('recoSplit', function() {
  return function(input) {
    if (!input) return input;
    var lines = input.split(' FY');
    lines[0] = lines[0].replace("FY", "").trim();
    for (var i = 1; i < lines.length; i++) {
      lines[i] = lines[i].trim();
    }
    return lines.join('\n');
  };
});*/

/*module.filter('recoSplit', function() {
  return function(input) {
    if (!input) return input;
    var lines = input.split(' FY');
    for (var i = 1; i < lines.length; i++) {
      lines[i] = 'FY' + lines[i];
    }
    return lines.join('\n') + '\n';
  };
});*/









