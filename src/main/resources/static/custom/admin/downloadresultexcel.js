var module = angular.module('DownloadResultExcelManagement', ['ui.bootstrap','angular.filter']);

module.controller('DownloadResultExcelController', function($scope, $http, $filter, $locale) {
    var urlBase="/admin/api";
    $scope.resultExcels = [];
    $scope.selectedExcel = "";

    showRecords();

    function showRecords(){
        $scope.clients = new Array;

        var url = "/getlatestresultexcels";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.resultExcels = response.data;
                } else {
                    $scope.resultExcels = [];
                }
            });
    }

    $scope.setSelectedExcel = function(excel) {
        $scope.selectedExcel = excel;
    }
});
