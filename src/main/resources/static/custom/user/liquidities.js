var incExpSavingsModule = angular.module('LiquditiesManagement', []);

incExpSavingsModule.controller('LiquditiesController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.liqudities =[];
    $scope.liqudityForm = {
        "key":{"memberid":-1,"liqudityid":-1},
        "liquidityDesc":"",
        "priority":"",
        "expectedBeginingDate":new Date("2010-01-01"),
        "amountRequiredBeginingDate":0,
        "frequency":"0",
        "expectedEndDate":new Date("2010-01-01"),
        "dateLastUpdate":new Date("2010-01-01")
    };

    $scope.hideForm = true;
    $scope.editMode = false;


    showRecords();

    function showRecords(){
        $scope.liqudities = new Array;
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

        url = "/getliqudities";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.liqudities = response.data;
                } else {
                    $scope.liqudities = [];
                }
            });
    }


    $scope.editLiqudityRecord = function editLiqudityRecord(liqudityRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.liqudityForm.key.memberid = ""+liqudityRecord.key.memberid;
        $scope.liqudityForm.key.liqudityid = liqudityRecord.key.liqudityid;
        $scope.liqudityForm.liquidityDesc = liqudityRecord.liquidityDesc;
        $scope.liqudityForm.priority = ""+liqudityRecord.priority;
        $scope.liqudityForm.expectedBeginingDate = new Date(liqudityRecord.expectedBeginingDate);
        $scope.liqudityForm.amountRequiredBeginingDate = liqudityRecord.amountRequiredBeginingDate;
        $scope.liqudityForm.frequency = ""+liqudityRecord.frequency;
        $scope.liqudityForm.expectedEndDate = new Date(liqudityRecord.expectedEndDate);
        $scope.liqudityForm.dateLastUpdate = new Date(liqudityRecord.dateLastUpdate);

    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.liqudityForm.key.memberid = "-1";
        $scope.liqudityForm.key.liqudityid = "-1";
        $scope.liqudityForm.liquidityDesc = "";
        $scope.liqudityForm.priority = "";
        $scope.liqudityForm.expectedBeginingDate = new Date("2010-01-01");
        $scope.liqudityForm.amountRequiredBeginingDate = 0;
        $scope.liqudityForm.frequency = "0";
        $scope.liqudityForm.expectedEndDate = new Date("2010-01-01");
        $scope.liqudityForm.dateLastUpdate = new Date();
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.deleteLiqudityRecord = function deleteLiqudityRecord(liqudityRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deleteliqudity";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(liqudityRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }
    }

    $scope.processLiqudityRecord = function processLiqudityRecord(){
        var method = "";
        var url = "";
        if($scope.LiqudityRecordHtmlForm.$valid)
        {
               $scope.liqudityForm.dateLastUpdate = new Date();
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addliqudity";
              } else {
                  method = "PUT";
                  url = "/updateliqudity";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.liqudityForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.liqudities = res.data;
        } else {
            $scope.liqudities = [];
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
