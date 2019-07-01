var module = angular.module('HistoricalHoldingsManagement', []);

module.controller('HistoricalHoldingsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.portfolios = [];
    $scope.historicalHoldings = [];
    $scope.finYearProfits = [];
    $scope.selectedMemberid = 0;
    $scope.selectedPortfolioid = 0;

    showRecords();

    function showRecords(){
        var url = "/getusermembers";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.members = response.data;
                } else {
                    $scope.members = [];
                }
            });

        url = "/getportfolios";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.portfolios = response.data;
                    $scope.selectedMemberid = $scope.portfolios[0].key.memberid;
                    $scope.selectedPortfolioid = $scope.portfolios[0].key.portfolioid;
                } else {
                    $scope.portfolios = [];
                }
            });

        url = "/getportfoliohistoricalholdings";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.historicalHoldings = response.data;
                } else {
                    $scope.historicalHoldings = [];
                }
            });

        url = "/getfinyearprofit";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.finYearProfits = response.data;
                } else {
                    $scope.finYearProfits = [];
                }
            });
    }

    $scope.setFilter = function(memberid, portfolioid) {
        $scope.selectedMemberid = memberid;
        $scope.selectedPortfolioid = portfolioid;
    }

    $scope.filterHistoricalByMemberidAndPortfolioid = function (historicalHolding) {
        if (historicalHolding.key.memberid == $scope.selectedMemberid && historicalHolding.key.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

    $scope.filterFinYearProfitByMemberidAndPortfolioid = function (finYearProfit) {
        if (finYearProfit.memberid == $scope.selectedMemberid && finYearProfit.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

});