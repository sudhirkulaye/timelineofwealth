var module = angular.module('CashflowsManagement', []);

module.controller('CashflowsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.portfolios = [];
    $scope.portfolioCashflows = [];
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

        url = "/getportfoliocashflows";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.portfolioCashflows = response.data;
                } else {
                    $scope.portfolioCashflows = [];
                }
            });
    }

    $scope.setFilter = function(memberid, portfolioid) {
        $scope.selectedMemberid = memberid;
        $scope.selectedPortfolioid = portfolioid;
    }

    $scope.filterCashflowsByMemberidAndPortfolioid = function (portfolioCashflow) {
        if (portfolioCashflow.key.memberid == $scope.selectedMemberid && portfolioCashflow.key.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

});