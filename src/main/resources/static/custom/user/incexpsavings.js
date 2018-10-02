var incExpSavingsModule = angular.module('IncExpSavingsManagement', ['angular.filter','chart.js']);

incExpSavingsModule.controller('IncExpSavingsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.incExpSavingsRecords =[];
    $scope.incExpSavingsRecordForm = {
        "key":{"memberid":-1,"finyear":2017},
        "regularIncome":0,
        "interestDividendIncome":0,
        "rentIncome":0,
        "otherIncome":0,
        "grossTotalIncome":0,
        "incomeTax":0,
        "netIncome":0,
        "taxRate": 0,
        "investmentTotal":0,
        "investmentIncreaseInBankbalance":0,
        "investmentTaxSavings":0,
        "investmentInEquity":0,
        "investmentInFixedIncome":0,
        "investmentInOther":0,
        "grossTotalExpenses":0,
        "infrequentTotalExpenses":0,
        "infrequentMedicalExpenses":0,
        "infrequentRenovationExpenses":0,
        "infrequentOtherExpenses":0,
        "normalizedRegularExpenses":0,
        "adjustment":0
    };
    $scope.consolidatedIncExp = [];
    $scope.labelsYears = [];
    $scope.chartSeriesIncExp = [];
    $scope.newColors = ['#26B99A', '#03586A', '#DCDCDC', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'];
    $http.defaults.headers.post["Content-Type"] = "application/json";
    $scope.hideForm = true;
    $scope.editMode = false;
    $scope.nonZeroAdjustment = false;
    $scope.recordAlreadyExist = false;


    showRecords();
//    $scope.populateChart();

    function showRecords(){
        $scope.incExpSavingsRecords = new Array;
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

        url = "/getincexpsavingsrecords";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.incExpSavingsRecords = response.data;
                    populateChart();
                } else {
                    $scope.incExpSavingsRecords = [];
                }
            });
    }

    $scope.getConsInc = function(records) {
        return records
        .map(function(x) { return x.netIncome; })
        .reduce(function(a, b) { return a + b; });
    }
    $scope.getConsExp = function(records) {
        return records
        .map(function(x) { return x.grossTotalExpenses; })
        .reduce(function(a, b) { return a + b; });
    }

    function populateChart() {
        var sortedIncExpSavingsRecords = $filter('orderBy')($scope.incExpSavingsRecords,'key.finyear');
        var map = $filter('groupBy')(sortedIncExpSavingsRecords, 'key.finyear');
        //console.log(map);

        var years = [];
        var consInc = [];
        var consExp = [];

        for(var yr in map){
           years.push(yr);
           consInc.push($scope.getConsInc(map[yr]));
           consExp.push($scope.getConsExp(map[yr]));
        }

        $scope.labelsYears = years;
        $scope.consolidatedIncExp = [];
        $scope.consolidatedIncExp.push(consInc);
        $scope.consolidatedIncExp.push(consExp);
        $scope.chartSeriesIncExp = ['Net Inc','Net Exp']

//        $scope.consolidatedIncExp = [[10, 12, 15, 16, 17, 20, 25], [5, 7, 10, 11, 12, 14, 15]];
//        $scope.labelsYears = ["2011", "2012", "2013", "2014", "2015", "2016", "2017"];
//        $scope.chartSeriesIncExp = ['Net Inc','Net Exp'];


    }

    $scope.editIncExpSavingsRecord = function editIncExpSavingsRecord(incExpSavingsRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.incExpSavingsRecordForm.key.memberid = ""+incExpSavingsRecord.key.memberid;
        $scope.incExpSavingsRecordForm.key.finyear = ""+incExpSavingsRecord.key.finyear;
        $scope.incExpSavingsRecordForm.regularIncome = incExpSavingsRecord.regularIncome;
        $scope.incExpSavingsRecordForm.interestDividendIncome = incExpSavingsRecord.interestDividendIncome;
        $scope.incExpSavingsRecordForm.rentIncome = incExpSavingsRecord.rentIncome;
        $scope.incExpSavingsRecordForm.otherIncome = incExpSavingsRecord.otherIncome;
        $scope.incExpSavingsRecordForm.grossTotalIncome = incExpSavingsRecord.grossTotalIncome;
        $scope.incExpSavingsRecordForm.incomeTax = incExpSavingsRecord.incomeTax;
        $scope.incExpSavingsRecordForm.netIncome = incExpSavingsRecord.netIncome;
        $scope.incExpSavingsRecordForm.taxRate = ""+incExpSavingsRecord.taxRate;
        $scope.incExpSavingsRecordForm.investmentTotal = incExpSavingsRecord.investmentTotal;
        $scope.incExpSavingsRecordForm.investmentIncreaseInBankbalance = incExpSavingsRecord.investmentIncreaseInBankbalance;
        $scope.incExpSavingsRecordForm.investmentTaxSavings = incExpSavingsRecord.investmentTaxSavings;
        $scope.incExpSavingsRecordForm.investmentInEquity = incExpSavingsRecord.investmentInEquity;
        $scope.incExpSavingsRecordForm.investmentInFixedIncome = incExpSavingsRecord.investmentInFixedIncome;
        $scope.incExpSavingsRecordForm.investmentInOther = incExpSavingsRecord.investmentInOther;
        $scope.incExpSavingsRecordForm.grossTotalExpenses = incExpSavingsRecord.grossTotalExpenses;
        $scope.incExpSavingsRecordForm.infrequentTotalExpenses = incExpSavingsRecord.infrequentTotalExpenses;
        $scope.incExpSavingsRecordForm.infrequentMedicalExpenses = incExpSavingsRecord.infrequentMedicalExpenses;
        $scope.incExpSavingsRecordForm.infrequentRenovationExpenses = incExpSavingsRecord.infrequentRenovationExpenses;
        $scope.incExpSavingsRecordForm.infrequentOtherExpenses = incExpSavingsRecord.infrequentOtherExpenses;
        $scope.incExpSavingsRecordForm.normalizedRegularExpenses = incExpSavingsRecord.normalizedRegularExpenses;
        $scope.incExpSavingsRecordForm.adjustment = incExpSavingsRecord.adjustment;

        $scope.nonZeroAdjustment = false;
        $scope.recordAlreadyExist = false;
    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;
        $scope.selfEditMode = false;
        $scope.nonZeroAdjustment = false;
        $scope.recordAlreadyExist = false;
        $scope.incExpSavingsRecordForm.key.memberid = "";
        $scope.incExpSavingsRecordForm.key.finyear = "";
        $scope.incExpSavingsRecordForm.regularIncome = 0;
        $scope.incExpSavingsRecordForm.interestDividendIncome = 0;
        $scope.incExpSavingsRecordForm.rentIncome = 0;
        $scope.incExpSavingsRecordForm.otherIncome = 0;
        $scope.incExpSavingsRecordForm.grossTotalIncome = 0;
        $scope.incExpSavingsRecordForm.incomeTax = 0;
        $scope.incExpSavingsRecordForm.netIncome = 0;
        $scope.incExpSavingsRecordForm.taxRate = ""+0;
        $scope.incExpSavingsRecordForm.investmentTotal = 0;
        $scope.incExpSavingsRecordForm.investmentIncreaseInBankbalance = 0;
        $scope.incExpSavingsRecordForm.investmentTaxSavings = 0;
        $scope.incExpSavingsRecordForm.investmentInEquity = 0;
        $scope.incExpSavingsRecordForm.investmentInFixedIncome = 0;
        $scope.incExpSavingsRecordForm.investmentInOther = 0;
        $scope.incExpSavingsRecordForm.grossTotalExpenses = 0;
        $scope.incExpSavingsRecordForm.infrequentTotalExpenses = 0;
        $scope.incExpSavingsRecordForm.infrequentMedicalExpenses = 0;
        $scope.incExpSavingsRecordForm.infrequentRenovationExpenses = 0;
        $scope.incExpSavingsRecordForm.infrequentOtherExpenses = 0;
        $scope.incExpSavingsRecordForm.normalizedRegularExpenses = 0;
        $scope.incExpSavingsRecordForm.adjustment = 0;
    }

    $scope.deesRecordexist = function deesRecordexist(){
        if ($scope.incExpSavingsRecordForm.key.memberid != "" && $scope.incExpSavingsRecordForm.key.finyear && ""){
            var index = $filter('filter')($scope.incExpSavingsRecords, {"key":{"memberid":$scope.incExpSavingsRecordForm.key.memberid,"finyear":$scope.incExpSavingsRecordForm.key.finyear}}).length;
            if(index > 0) {
                //console.log('Populate Other Field');
                $scope.incExpSavingsRecordForm = $filter('filter')($scope.incExpSavingsRecords, {"key":{"memberid":$scope.incExpSavingsRecordForm.key.memberid,"finyear":$scope.incExpSavingsRecordForm.key.finyear}})[0]
                //$scope.recordAlreadyExist = true;
            }
        }
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.processIncExpSavingsRecord = function processIncExpSavingsRecord(){
        var method = "";
        var url = "";
        $scope.incExpSavingsRecordForm.grossTotalIncome = $scope.incExpSavingsRecordForm.regularIncome
                                                      + $scope.incExpSavingsRecordForm.interestDividendIncome
                                                      + $scope.incExpSavingsRecordForm.rentIncome
                                                      + $scope.incExpSavingsRecordForm.otherIncome;
        $scope.incExpSavingsRecordForm.netIncome = $scope.incExpSavingsRecordForm.regularIncome
                                                  + $scope.incExpSavingsRecordForm.interestDividendIncome
                                                  + $scope.incExpSavingsRecordForm.rentIncome
                                                  + $scope.incExpSavingsRecordForm.otherIncome
                                                  - $scope.incExpSavingsRecordForm.incomeTax;
        $scope.incExpSavingsRecordForm.investmentTotal = $scope.incExpSavingsRecordForm.investmentIncreaseInBankbalance
                                                     + $scope.incExpSavingsRecordForm.investmentTaxSavings
                                                     + $scope.incExpSavingsRecordForm.investmentInEquity
                                                     + $scope.incExpSavingsRecordForm.investmentInFixedIncome
                                                     + $scope.incExpSavingsRecordForm.investmentInOther;
        $scope.incExpSavingsRecordForm.grossTotalExpenses = $scope.incExpSavingsRecordForm.normalizedRegularExpenses
                                                        + $scope.incExpSavingsRecordForm.infrequentTotalExpenses;
        $scope.incExpSavingsRecordForm.adjustment = $scope.incExpSavingsRecordForm.regularIncome
                                                + $scope.incExpSavingsRecordForm.interestDividendIncome
                                                + $scope.incExpSavingsRecordForm.rentIncome
                                                + $scope.incExpSavingsRecordForm.otherIncome
                                                - $scope.incExpSavingsRecordForm.incomeTax
                                                - (
                                                + $scope.incExpSavingsRecordForm.investmentIncreaseInBankbalance
                                                + $scope.incExpSavingsRecordForm.investmentTaxSavings
                                                + $scope.incExpSavingsRecordForm.investmentInEquity
                                                + $scope.incExpSavingsRecordForm.investmentInFixedIncome
                                                + $scope.incExpSavingsRecordForm.investmentInOther
                                                ) - (
                                                $scope.incExpSavingsRecordForm.normalizedRegularExpenses
                                                + $scope.incExpSavingsRecordForm.infrequentTotalExpenses);

        if($scope.incExpSavingsRecordForm.adjustment != 0){
            $scope.nonZeroAdjustment = true;
        }

        var index = $filter('filter')($scope.incExpSavingsRecords, {"key":{"memberid":$scope.incExpSavingsRecordForm.key.memberid,"finyear":$scope.incExpSavingsRecordForm.key.finyear}}).length;
        if (index > 0 && $scope.editMode == false) {
            //console.log('Record Already Exist/Key fields -FinYear and Member- must be unique');
            $scope.recordAlreadyExist = true;
        }

//        console.log($scope.incExpSavingsRecordForm);
//        console.log($scope.IncExpSavingsRecordHtmlForm.$valid);
        if($scope.IncExpSavingsRecordHtmlForm.$valid && !$scope.nonZeroAdjustment && !$scope.recordAlreadyExist)
        {
              //Submit your form
              if ($scope.editMode == false) {
                  method = "POST";
                  url = "/addincexpsavingsrecords";
              } else {
                  method = "PUT";
                  url = "/updateincexpsavingsrecords";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.incExpSavingsRecordForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.incExpSavingsRecords = res.data;
        } else {
            $scope.incExpSavingsRecords = [];
        }
        $scope.showTable();
        populateChart();
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
