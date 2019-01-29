var module = angular.module('MidAndSmallCapIndexStatsManagement', []);

module.controller('MidAndSmallCapIndexStatsController', function($scope, $http, $filter) {
    var urlBase="/public/api";
    $scope.indexConsolidatedStatistics = [];

    showRecords();

    function showRecords(){
        $scope.indexStatRecords = new Map;

        url = "/getmidandsmallcapindexstatistics";
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