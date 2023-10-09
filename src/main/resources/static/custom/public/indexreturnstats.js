var module = angular.module('IndexReturnStatsManagement', []);

module.controller('IndexReturnStatsController', function($scope, $http, $filter) {
    var urlBase="/public/api";
    $scope.indexConsolidatedStatistics = [];

    showRecords();

    function showRecords(){
        $scope.indexStatRecords = new Map;

        url = "/getindexreturnstats";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.indexConsolidatedStatistics = response.data;
                } else {
                    $scope.indexConsolidatedStatistics = [];
                }
            });
    }

});