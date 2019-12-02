var module = angular.module('ModelPortfoliosManagement', ['ui.bootstrap','angular.filter']);

module.controller('ModelPortfoliosController', function($scope, $http, $filter, $locale) {
    var urlBase="/adviser/api";
    $scope.composites = [];
    $scope.compositeDetails = [];
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.stocks = [];

    $scope.selectedCompositeid = 0;
    $scope.enableAdd = true;
    $scope.hideForm = true;
    $scope.compositeDetailRecordForm = {
            "key":{"compositeid":0,"ticker":""},
            "name":"",
            "shortName":"",
            "assetClassid":"",
            "subindustryid":0,
            "targetWeight":0,
            "maxWeight":0,
            "minWeight":0,
            }
    $scope.dummyName = "";

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

        publicapiurl = "/public/api/getallstocks";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.stocks = response.data;
                } else {
                    $scope.stocks = [];
                }
            });

    }

    $scope.setFilter = function(compositeid) {
        $scope.enableAdd = false;
        $scope.hideForm = true;
        $scope.selectedCompositeid = compositeid;
    }

    $scope.filterCompositeConstituents = function (compositeDetail) {
        if (compositeDetail.key.compositeid == $scope.selectedCompositeid ) {
            return true;
        }
        return false;
    }

    $scope.showForm = function showForm() {
        $scope.hideForm = false;
        $scope.editMode = false;
        $scope.compositeDetailRecordForm.key.compositeid = $scope.selectedCompositeid;
        $scope.compositeDetailRecordForm.key.ticker = "";
        $scope.compositeDetailRecordForm.name = "";
        $scope.compositeDetailRecordForm.shortName = "";
        $scope.compositeDetailRecordForm.assetClassid = 0;
        $scope.compositeDetailRecordForm.subindustryid = 0;
        $scope.compositeDetailRecordForm.targetWeight = 0;
        $scope.compositeDetailRecordForm.maxWeight = 0;
        $scope.compositeDetailRecordForm.minWeight = 0;
        $scope.dummyName = "";
    }

    $scope.setStockDetails = function(stock) {
        $scope.compositeDetailRecordForm.key.ticker = stock.ticker;
        $scope.compositeDetailRecordForm.name = ""+stock.name;
        $scope.compositeDetailRecordForm.shortName = stock.shortName;
        $scope.compositeDetailRecordForm.assetClassid = ""+stock.assetClassid;
        $scope.compositeDetailRecordForm.subindustryid = ""+stock.subindustryid;
        console.log("stock.name : " + stock.name);
        console.log("$scope.compositeDetailRecordForm.name : " + $scope.compositeDetailRecordForm.name);
    }

    $scope.editCompositeDetail = function editCompositeDetail(compositeDetail){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.compositeDetailRecordForm.key.compositeid = compositeDetail.key.compositeid;
        $scope.compositeDetailRecordForm.key.ticker = compositeDetail.key.ticker;
        $scope.compositeDetailRecordForm.name = compositeDetail.name;
        $scope.compositeDetailRecordForm.shortName = compositeDetail.shortName;
        $scope.compositeDetailRecordForm.assetClassid = compositeDetail.assetClassid;
        $scope.compositeDetailRecordForm.subindustryid = compositeDetail.subindustryid;
        $scope.compositeDetailRecordForm.targetWeight = compositeDetail.targetWeight;
        $scope.compositeDetailRecordForm.maxWeight = compositeDetail.maxWeight;
        $scope.compositeDetailRecordForm.minWeight = compositeDetail.minWeight;

    }

    $scope.deleteCompositeDetail = function deleteCompositeDetail(compositeDetail) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "POST";
            var url = "/deletecompositedetail";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(compositeDetail),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }

    }

    $scope.processCompositeDetailRecord = function processCompositeDetailRecord() {
        var method = "";
        var url = "";
        if($scope.compositeDetailRecordHtmlForm.$valid) {
              //console.log('Posting ...:');
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addcompositedetail";
              } else {
                  method = "PUT";
                  url = "/updatewcompositedetail";
              }
              console.log("Before $scope.compositeDetailRecordForm.name : " + $scope.compositeDetailRecordForm.name);
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.compositeDetailRecordForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.compositeDetails = res.data;
        } else {
            $scope.compositeDetails = [];
        }
        //$scope.showTable();
        $scope.hideForm = true;
    }

    function _error(res) {
        var data = res.data;
        var status = res.status;
        var header = res.header;
        var config = res.config;
        alert("Error: " + status + ":" + data);
        //$scope.showTable();
        $scope.hideForm = true;
    }
});
