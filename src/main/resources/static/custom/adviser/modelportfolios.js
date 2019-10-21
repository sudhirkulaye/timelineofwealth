var module = angular.module('ModelPortfoliosManagement', ['ui.bootstrap','angular.filter']);

module.controller('ModelPortfoliosController', function($scope, $http, $filter, $locale) {
    var urlBase="/adviser/api";
    $scope.composites = [];
    $scope.compositeDetails = [];
    $scope.assetClasses = [];
    $scope.industries = [];

    $scope.selectedCompositeid = 0;

    showRecords();

    function showRecords(){
        $scope.composites = new Array;

        var url = "/getcomposites";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.composites = response.data;
                } else {
                    $scope.composites = [];
                }
            });

        url = "/getcompositedetails";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.compositeDetails = response.data;
                } else {
                    $scope.compositeDetails = [];
                }
            });

        var publicapiurl = "/public/api/getassetclassifications";
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

    }

    $scope.setFilter = function(compositeid) {
        $scope.selectedCompositeid = compositeid;
    }

    $scope.filterCompositeConstituents = function (compositeDetail) {
        if (compositeDetail.key.compositeid == $scope.selectedCompositeid ) {
            return true;
        }
        return false;
    }
});
