var module = angular.module('HoldingsManagement', []);

module.controller('HoldingsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.portfolios = [];
    $scope.consolidatedHoldings = [];
    $scope.individualHoldings = [];
    $scope.hideConsolidated = false;
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

        url = "/getindividualholdings";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.individualHoldings = response.data;
                } else {
                    $scope.individualHoldings = [];
                }
            });

        url = "/getconsolidatedportfolioholdings";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.consolidatedHoldings = response.data;
                } else {
                    $scope.consolidatedHoldings = [];
                }
            });
    }

    $scope.showView = function(selectedOption) {
        if (selectedOption == "Consolidated") {
            $scope.hideConsolidated = false;
        } else {
            $scope.hideConsolidated = true;
        }
    }

    $scope.setFilter = function(memberid, portfolioid) {
        $scope.selectedMemberid = memberid;
        $scope.selectedPortfolioid = portfolioid;
    }

    $scope.filterIndividualByMemberidAndPortfolioid = function (individualHolding) {
        if (individualHolding.key.memberid == $scope.selectedMemberid && individualHolding.key.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

    $scope.filterConsolidatedByMemberidAndPortfolioid = function (consolidatedHolding) {
        if (consolidatedHolding.memberid == $scope.selectedMemberid && consolidatedHolding.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

});