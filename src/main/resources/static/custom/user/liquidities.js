var module = angular.module('LiquiditiesManagement', []);

module.controller('LiquiditiesController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.liquidities =[];
    $scope.liquidityForm = {
        "key":{"memberid":-1,"liquidityid":-1},
        "liquidityDesc":"",
        "priority":"",
        "expectedStartDate":new Date("2010-01-01"),
        "amountRequiredStartDate":0,
        "frequency":"0",
        "expectedEndDate":new Date("2010-01-01"),
        "dateLastUpdate":new Date("2010-01-01")
    };

    $scope.hideForm = true;
    $scope.editMode = false;


    showRecords();

    function showRecords(){
        $scope.liquidities = new Array;
        $scope.hideForm = true;

        var url = "/getusermembers";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.members = response.data;
                } else {
                    $scope.members = [];
                }
            });

        url = "/getliquidities";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.liquidities = response.data;
                } else {
                    $scope.liquidities = [];
                }
            });
    }


    $scope.editLiquidityRecord = function editLiquidityRecord(liquidityRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.liquidityForm.key.memberid = ""+liquidityRecord.key.memberid;
        $scope.liquidityForm.key.liquidityid = liquidityRecord.key.liquidityid;
        $scope.liquidityForm.liquidityDesc = liquidityRecord.liquidityDesc;
        $scope.liquidityForm.priority = ""+liquidityRecord.priority;
        $scope.liquidityForm.expectedStartDate = new Date(liquidityRecord.expectedStartDate);
        $scope.liquidityForm.amountRequiredStartDate = liquidityRecord.amountRequiredStartDate;
        $scope.liquidityForm.frequency = ""+liquidityRecord.frequency;
        $scope.liquidityForm.expectedEndDate = new Date(liquidityRecord.expectedEndDate);
        $scope.liquidityForm.dateLastUpdate = new Date(liquidityRecord.dateLastUpdate);

    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.liquidityForm.key.memberid = "-1";
        $scope.liquidityForm.key.liquidityid = "-1";
        $scope.liquidityForm.liquidityDesc = "";
        $scope.liquidityForm.priority = "";
        $scope.liquidityForm.expectedStartDate = new Date("2010-01-01");
        $scope.liquidityForm.amountRequiredStartDate = 0;
        $scope.liquidityForm.frequency = "0";
        $scope.liquidityForm.expectedEndDate = new Date("2010-01-01");
        $scope.liquidityForm.dateLastUpdate = new Date();
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.deleteLiquidityRecord = function deleteLiquidityRecord(liquidityRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deleteliquidity";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(liquidityRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }
    }

    $scope.processLiquidityRecord = function processLiquidityRecord(){
        var method = "";
        var url = "";
        if($scope.LiquidityRecordHtmlForm.$valid)
        {
               $scope.liquidityForm.dateLastUpdate = new Date();
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addliquidity";
              } else {
                  method = "PUT";
                  url = "/updateliquidity";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.liquidityForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.liquidities = res.data;
        } else {
            $scope.liquidities = [];
        }
        $scope.showTable();
    }

    function _error(res) {
        var data = res.data;
        var status = res.status;
        var header = res.header;
        var config = res.config;
        alert("Error: " + status + ":" + data);
        //$scope.showTable();
    }

});

module.filter("frequency", function () {
    return function (frequency) {
        switch (frequency) {
            case 0: return "One Time";
            case 1: return "Monthly";
            case 3: return "Quarterly";
            case 6: return "Semi Annually";
            case 12: return "Annually";
        }
    }
})