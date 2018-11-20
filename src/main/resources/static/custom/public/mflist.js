var module = angular.module('MFListManagement', []);
module.controller('MFListController', function($scope, $http, $filter) {
    $scope.fundHouses = [];
    $scope.fundsDTO = [];
    $scope.assetClasses = [];
    $scope.fundHouse = "Aditya Birla";
    $scope.category = "Equity";

    showRecords();

    function showRecords(){
        var publicapiurl = "/public/api/getdistinctfundhouse";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.fundHouses = response.data;
                } else {
                    $scope.fundHouses = [];
                }
            });

        publicapiurl = "/public/api/getassetclassifications";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.assetClasses = response.data;
                } else {
                    $scope.assetClasses = [];
                }
            });

        publicapiurl = "/public/api/getSchemeDetails/"+$scope.fundHouse+"/"+$scope.category;
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.fundsDTO = response.data;
                } else {
                    $scope.fundsDTO = [];
                }
            });

    }

    $scope.getSchemeDetails = function() {
        var publicapiurl = "/public/api/getSchemeDetails";
        var params = "";
        if ($scope.fundHouse != "" || $scope.fundHouse != undefined){
            params = params + "/" + $scope.fundHouse;
            if ($scope.category != "" || $scope.category != undefined) {
                params = params + "/" + $scope.category;
            }
        }

        if (params != "") {
            $http.get(publicapiurl+params).
                then(function (response) {
                    if (response != undefined) {
                        $scope.fundsDTO = response.data;
                    } else {
                        $scope.fundsDTO = [];
                    }
                });
        }

    }

    $scope.searchFund = function (fund) {
        if ($scope.searchText == undefined) {
            return true;
        } else {
            if (fund.schemeNameFull.toLowerCase().indexOf($scope.searchText.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }
});
