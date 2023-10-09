var module = angular.module('MFListManagement', []);
module.controller('MFListController', function($scope, $http, $filter) {
    $scope.fundsDTO = [];
    $scope.category = "Large Cap";
    $scope.flagYearlyReurns = false;
    $scope.flagTrailingReturns = true;
    $scope.flagSectorAllocation = true;
    $scope.flagTopStocks = true;
    $scope.reverseSort = false;
    $scope.dateToday = "";

    showRecords();

    function showRecords(){
        var publicapiurl = "/public/api/getselectedmf";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.fundsDTO = response.data;
                } else {
                    $scope.fundsDTO = [];
                }
            });

        var ublicapiurl1 = "/public/api/getDates";
                 $http.get(ublicapiurl1).
                     then(function (response) {
                         if (response != undefined) {
                             $scope.dateToday = response.data.dateToday;
                             console.log('dateToday:', $scope.dateToday);
                             console.log('API Response:', response.data);
                         } else {
                             $scope.dateToday = "";
                         }
                     });

    }

    $scope.showColumns = function(selectedOption) {
        if (selectedOption == "YearlyReturns") {
            $scope.flagYearlyReurns = false;
            $scope.flagTrailingReturns = true;
            $scope.flagSectorAllocation = true;
            $scope.flagTopStocks = true;
        }
        if (selectedOption == "TrailingReturns") {
            $scope.flagYearlyReurns = true;
            $scope.flagTrailingReturns = false;
            $scope.flagSectorAllocation = true;
            $scope.flagTopStocks = true;
        }
        if (selectedOption == "SectorAllocation") {
            $scope.flagYearlyReurns = true;
            $scope.flagTrailingReturns = true;
            $scope.flagSectorAllocation = false;
            $scope.flagTopStocks = true;
        }
        if (selectedOption == "TopStocks") {
            $scope.flagYearlyReurns = true;
            $scope.flagTrailingReturns = true;
            $scope.flagSectorAllocation = true;
            $scope.flagTopStocks = false;
        }
    }

    $scope.searchFund = function (fund) {
        if ($scope.searchText == undefined) {
            return true;
        } else {
            if (fund.schemeNamePart.toLowerCase().indexOf($scope.searchText.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByCategory = function (fund) {
        if ($scope.category == undefined || $scope.category == "") {
            return true;
        } else {
            if (fund.schemeType.toLowerCase().indexOf($scope.category.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.sortData = function (column) {
        $scope.reverseSort = ($scope.sortColumn == column) ? !$scope.reverseSort : false;
        $scope.sortColumn = column;
    }
    $scope.getSortClass = function (column) {
        if ($scope.sortColumn == column) {
            return $scope.reverseSort ? 'fa fa-sort-up fa-fw' : 'fa fa-sort-down fa-fw'
        }
        return '';
    }

    // Extract the year from dateToday
    $scope.extractYear = function() {
        if ($scope.dateToday) {
            var date = new Date($scope.dateToday);
            return date.getFullYear();
        }
        return '';
    };

});
