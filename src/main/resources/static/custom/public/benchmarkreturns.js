var module = angular.module('BenchmarkReturnsManagement', []);

module.controller('BenchmarkReturnsController', function($scope, $http, $filter) {

    $scope.benchmarkTwrrSummaries = [];
    $scope.benchmarkTwrrMonthlies = [];
    $scope.hideTrailing = false;

    showRecords();

    function showRecords(){

        url = "/public/api/getbenchmarktwrrsummary";
        $http.get(url).
            then(function (response) {
                if (response != undefined) {
                    $scope.benchmarkTwrrSummaries = response.data;
                } else {
                    $scope.benchmarkTwrrSummaries = [];
                }
            });

        url = "/public/api/getbenchmarktwrrmonthly";
        $http.get(url).
            then(function (response) {
                if (response != undefined) {
                    $scope.benchmarkTwrrMonthlies = response.data;
                } else {
                    $scope.benchmarkTwrrMonthlies = [];
                }
            });
    }

    $scope.showView = function(selectedOption) {
        if (selectedOption == "Trailing") {
            $scope.hideTrailing = false;
        } else {
            $scope.hideTrailing = true;
        }
    }

});