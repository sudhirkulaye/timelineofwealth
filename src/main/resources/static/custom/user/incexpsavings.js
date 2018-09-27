var incExpSavingsModule = angular.module('IncExpSavingsManagement', []);

incExpSavingsModule.controller('IncExpSavingsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.incExpSavingsRecords =[];
    $scope.incExpSavingsRecordForm = {
        "memberid": "-1",
        "finyear":"2000",
        "grossIncome":"",
        "incomeTax":"",
        "netIncome":"",
        "taxRate": "",
        "investmentTotal":"",
        "investmentIncreaseInBankbalance":"",
        "investmentTaxSavings":"",
        "investmentInEquity":"",
        "investmentInFixedIncome": "",
        "investmentInOther":"",
        "grossTotalExpenses":"",
        "infrequentTotalExpenses":"",
        "infrequentMedicalExpenses":"",
        "infrequentRenovationExpenses":"",
        "infrequentOtherExpenses":"",
        "normalizedEegularExpenses":"",
        "adjustment":"0"
    };
    $http.defaults.headers.post["Content-Type"] = "application/json";
    $scope.hideForm = true;
    $scope.hideTable = false;
    $scope.isFormSubmit = false;
    $scope.editMode = false;
    $scope.selfEditMode = false;
    $scope.relationshipError = false;

    showMembers();

    function showMembers(){
        $scope.incExpSavingsRecords = new Array;
        $scope.hideForm = true;
        $scope.hideTable = false;

        var url = "/getusermembers";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.members = response.data;
                } else {
                    $scope.members = [];
                }
            });

        url = "/getIncExpSavingsRecords";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.incExpSavingsRecords = response.data;
                } else {
                    $scope.incExpSavingsRecords = [];
                }
            });
    }

    $scope.editIncExpSavingsRecord = function editIncExpSavingsRecord(incExpSavingsRecord){
        $scope.hideForm = false;
        $scope.hideTable = true;
        $scope.editMode = true;
        $scope.relationshipError=false;
        if (member.relationship == "Self") {
            $scope.selfEditMode = true;
        }
        $scope.incExpSavingsRecordForm.memberid = incExpSavingsRecord.memberid;
        $scope.incExpSavingsRecordForm.finyear = incExpSavingsRecord.finyear;
    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.hideTable = true;
        $scope.editMode = false;
        $scope.selfEditMode = false;
        $scope.relationshipError = false;
        $scope.incExpSavingsRecordForm.memberid = "-1";
        $scope.incExpSavingsRecordForm.finyear = "-1";
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
        $scope.hideTable = false;
    }

    $scope.processIncExpSavingsRecord = function processIncExpSavingsRecord(){

    }

    function _success(res) {
        if (res != undefined) {
            $scope.members = res.data;
        } else {
            $scope.members = [];
        }
        $scope.showTable();
    }

    function _error(res) {
        var data = res.data;
        var status = res.status;
        var header = res.header;
        var config = res.config;
        alert("Error: " + status + ":" + data);
        $scope.showTable();
    }
});