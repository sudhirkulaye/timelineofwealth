var module = angular.module('InsurancesManagement', []);

module.controller('InsurancesController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.insurances =[];
    $scope.insuranceForm = {
        "key":{"memberid":-1,"insuranceid":-1},
        "productUIN":"",
        "productName":"",
        "category":"",
        "coverAmount":0,
        "premiumAmount":0,
        "premiumFrequencyInMonths":"12",
        "lastDateOfPremium":"",
        "lifeTimeCover":"No",
        "expiryDate":"",
        "maturityAmount": 0,
        "maturityFrequency": "0",
        "expectedBonusAmount":0,
        "dateLastUpdate":new Date("2010-01-01")
    };

    $scope.hideForm = true;
    $scope.editMode = false;


    showRecords();

    function showRecords(){
        $scope.insurances = new Array;
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

        url = "/getinsurances";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.insurances = response.data;
                } else {
                    $scope.insurances = [];
                }
            });
    }


    $scope.editInsuranceRecord = function editInsuranceRecord(insuranceRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.insuranceForm.key.memberid = ""+insuranceRecord.key.memberid;
        $scope.insuranceForm.key.insuranceid = insuranceRecord.key.insuranceid;
        $scope.insuranceForm.productUIN = insuranceRecord.productUIN;
        $scope.insuranceForm.productName = insuranceRecord.productName;
        $scope.insuranceForm.category = insuranceRecord.category;
        $scope.insuranceForm.coverAmount = insuranceRecord.coverAmount;
        $scope.insuranceForm.premiumAmount = insuranceRecord.premiumAmount;
        $scope.insuranceForm.premiumFrequencyInMonths = ""+insuranceRecord.premiumFrequencyInMonths;
        $scope.insuranceForm.lastDateOfPremium = new Date(insuranceRecord.lastDateOfPremium);
        $scope.insuranceForm.lifeTimeCover = insuranceRecord.lifeTimeCover;
        $scope.insuranceForm.expiryDate = new Date(insuranceRecord.expiryDate);
        $scope.insuranceForm.maturityAmount = insuranceRecord.maturityAmount;
        $scope.insuranceForm.maturityFrequency = ""+insuranceRecord.maturityFrequency;
        $scope.insuranceForm.expectedBonusAmount = insuranceRecord.expectedBonusAmount;
        $scope.insuranceForm.dateLastUpdate = new Date(insuranceRecord.dateLastUpdate);

    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.insuranceForm.key.memberid = "-1";
        $scope.insuranceForm.key.insuranceid = "-1";
        $scope.insuranceForm.productUIN = "";
        $scope.insuranceForm.productName = "";
        $scope.insuranceForm.category = "";
        $scope.insuranceForm.coverAmount = 0;
        $scope.insuranceForm.premiumAmount = 0;
        $scope.insuranceForm.premiumFrequencyInMonths = "12";
        $scope.insuranceForm.lastDateOfPremium = new Date();
        $scope.insuranceForm.lifeTimeCover = "No";
        $scope.insuranceForm.expiryDate = new Date();
        $scope.insuranceForm.maturityAmount = 0;
        $scope.insuranceForm.maturityFrequency = "0";
        $scope.insuranceForm.expectedBonusAmount = 0;
        $scope.insuranceForm.dateLastUpdate = new Date();
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.deleteInsuranceRecord = function deleteInsuranceRecord(insuranceRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deleteinsurance";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(insuranceRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }
    }

    $scope.processInsuranceRecord = function processInsuranceRecord(){
        var method = "";
        var url = "";
        if($scope.InsuranceRecordHtmlForm.$valid)
        {
               $scope.insuranceForm.dateLastUpdate = new Date();
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addinsurance";
              } else {
                  method = "PUT";
                  url = "/updateinsurance";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.insuranceForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.insurances = res.data;
        } else {
            $scope.insurances = [];
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
