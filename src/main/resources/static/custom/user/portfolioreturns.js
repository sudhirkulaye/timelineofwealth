var module = angular.module('ReturnsManagement', []);

module.controller('ReturnsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.portfolios = [];
    $scope.portfolioTwrrSummaries = [];
    $scope.portfolioTwrrMonthlies = [];
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

        url = "/getportfoliotwrrsummary";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.portfolioTwrrSummaries = response.data;
                } else {
                    $scope.portfolioTwrrSummaries = [];
                }
            });

        url = "/getportfoliotwrrmonthly";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.portfolioTwrrMonthlies = response.data;
                } else {
                    $scope.portfolioTwrrMonthlies = [];
                }
            });
    }

    $scope.setFilter = function(memberid, portfolioid) {
        $scope.selectedMemberid = memberid;
        $scope.selectedPortfolioid = portfolioid;
    }

    $scope.filterTwrrSummaryByMemberidAndPortfolioid = function (portfolioTwrrSummary) {
        if (portfolioTwrrSummary.key.memberid == $scope.selectedMemberid && portfolioTwrrSummary.key.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

    $scope.filterTwrrMonthlyByMemberidAndPortfolioid = function (portfolioTwrrMonthly) {
        if (portfolioTwrrMonthly.key.memberid == $scope.selectedMemberid && portfolioTwrrMonthly.key.portfolioid == $scope.selectedPortfolioid ) {
            return true;
        }
        return false;
    }

});