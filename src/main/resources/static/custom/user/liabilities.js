var incExpSavingsModule = angular.module('LiabilitiesManagement', []);

incExpSavingsModule.controller('LiabilitiesController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.liabilities =[];
    $scope.liabilityForm = {
        "key":{"memberid":-1,"loanid":-1},
        "loanDesc":"",
        "loanType":"",
        "disbursementAmount":0,
        "disbursementDate":new Date("2010-01-01"),
        "initialTotalEmis":0,
        "firstEmiDate":new Date("2010-01-01"),
        "initialEmiAmount":0,
        "currentEmiAmount": 0,
        "currentEmiDay":1,
        "lastEmiMonth":0,
        "lastEmiYear":0,
        "remainingEmis":0,
        "interestRate":0,
        "pvOutstandingEmis":0,
        "activeStatus":"Active",
        "dateLastUpdate":new Date("2010-01-01")
    };

    $scope.hideForm = true;
    $scope.editMode = false;


    showRecords();

    function showRecords(){
        $scope.liabilities = new Array;
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

        url = "/getliabilities";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.liabilities = response.data;
                } else {
                    $scope.liabilities = [];
                }
            });
    }


    $scope.editLiabilityRecord = function editLiabilityRecord(liabilityRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.liabilityForm.key.memberid = ""+liabilityRecord.key.memberid;
        $scope.liabilityForm.key.loanid = liabilityRecord.key.loanid;
        $scope.liabilityForm.loanDesc = liabilityRecord.loanDesc;
        $scope.liabilityForm.loanType = liabilityRecord.loanType;
        $scope.liabilityForm.disbursementAmount = liabilityRecord.disbursementAmount;
        $scope.liabilityForm.disbursementDate = new Date(liabilityRecord.disbursementDate);
        $scope.liabilityForm.initialTotalEmis = liabilityRecord.initialTotalEmis;
        $scope.liabilityForm.firstEmiDate = new Date(liabilityRecord.firstEmiDate);
        $scope.liabilityForm.initialEmiAmount = liabilityRecord.initialEmiAmount;
        $scope.liabilityForm.currentEmiAmount = liabilityRecord.currentEmiAmount;
        $scope.liabilityForm.currentEmiDay = parseInt(liabilityRecord.currentEmiDay);
        $scope.liabilityForm.lastEmiMonth = ""+liabilityRecord.lastEmiMonth;
        $scope.liabilityForm.lastEmiYear = parseInt(liabilityRecord.lastEmiYear);
        $scope.liabilityForm.remainingEmis = liabilityRecord.remainingEmis;
        $scope.liabilityForm.interestRate = liabilityRecord.interestRate;
        $scope.liabilityForm.pvOutstandingEmis = liabilityRecord.pvOutstandingEmis;
        $scope.liabilityForm.activeStatus = liabilityRecord.activeStatus;
        $scope.liabilityForm.dateLastUpdate = new Date(liabilityRecord.dateLastUpdate);

    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.liabilityForm.key.memberid = "";
        $scope.liabilityForm.key.loanid = "-1";
        $scope.liabilityForm.loanDesc = "";
        $scope.liabilityForm.loanType = "";
        $scope.liabilityForm.disbursmentAmount = "";
        $scope.liabilityForm.disbursmentDate = "";
        $scope.liabilityForm.initialTotalEmis = "";
        $scope.liabilityForm.firstEmiDate = "";
        $scope.liabilityForm.initialEmiAmount = "";
        $scope.liabilityForm.currentEmiAmount = "";
        $scope.liabilityForm.currentEmiDay = "";
        $scope.liabilityForm.lastEmiMonth = "";
        $scope.liabilityForm.lastEmiYear = "";
        $scope.liabilityForm.remainingEmis = "";
        $scope.liabilityForm.interestRate = "";
        $scope.liabilityForm.pvOutstandingEmis = "";
        $scope.liabilityForm.activeStatus = "Active";
        $scope.liabilityForm.dateLastUpdate = new Date();
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.deleteLiabilityRecord = function deleteLiabilityRecord(liabilityRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deleteliability";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(liabilityRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }
    }

    $scope.processLiabilityRecord = function processLiabilityRecord(){
        var method = "";
        var url = "";
        //console.log($scope.LiabilityRecordHtmlForm.$valid);
        $scope.calculateRemainingEmis();
        $scope.calculatePV();
        if ($scope.liabilityForm.remainingEmis == 0) {
            $scope.liabilityForm.activeStatus = "Fully Paid";
        }
        if($scope.LiabilityRecordHtmlForm.$valid)
        {
              $scope.liabilityForm.dateLastUpdate = new Date();
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addliability";
              } else {
                  method = "PUT";
                  url = "/updateliability";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.liabilityForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    $scope.calculateRemainingEmis = function calculateRemainingEmis() {
        if ($scope.liabilityForm.lastEmiMonth !=null && $scope.liabilityForm.currentEmiDay != null && $scope.liabilityForm.lastEmiYear != null){
            var today = new Date();
            var month = parseInt($scope.liabilityForm.lastEmiMonth) - 1;
            var lastEmiDate = new Date($scope.liabilityForm.lastEmiYear, month, $scope.liabilityForm.currentEmiDay);
            var months;
            months = (lastEmiDate.getFullYear() - today.getFullYear()) * 12;
            months -= today.getMonth() + 1;
            months += lastEmiDate.getMonth() + 1;
            if ($scope.liabilityForm.currentEmiDay >= today.getDate()) {
                months = months + 1; //increase count with current month
            }
            $scope.liabilityForm.remainingEmis =  months <= 0 ? 0 : months;
            $scope.calculatePV();
        }
    }

    $scope.calculatePV = function calculatePV() {
        if ($scope.liabilityForm.remainingEmis !=null && $scope.liabilityForm.currentEmiAmount != null && $scope.liabilityForm.interestRate != null){
//            console.log($scope.liabilityForm.remainingEmis);
//            console.log($scope.liabilityForm.currentEmiAmount);
//            console.log($scope.liabilityForm.interestRate);
            if ($scope.liabilityForm.remainingEmis > 0 && $scope.liabilityForm.currentEmiAmount > 0 && $scope.liabilityForm.interestRate >= 0) {
                var rate = parseFloat($scope.liabilityForm.interestRate).toFixed(2);
                rate = parseFloat(rate/1200).toFixed(2);
                var n = parseInt($scope.liabilityForm.remainingEmis);
                var pmt = parseFloat($scope.liabilityForm.currentEmiAmount).toFixed(0);
//                $scope.liabilityForm.pvOutstandingEmis = $filter('number')(pv(rate, n, -pmt, 0, 0),0);
                $scope.liabilityForm.pvOutstandingEmis =  pv(rate, n, -pmt, 0, 0);
            }
        }
    }
    function pv(rate, periods, payment, future, type) {
      // Initialize type
      var type = (typeof type === 'undefined') ? 0 : type;

      // Evaluate rate and periods (TODO: replace with secure expression evaluator)
      rate = eval(rate);
      periods = eval(periods);

      // Return present value
      if (rate === 0) {
        return - payment * periods - future;
      } else {
        return (((1 - Math.pow(1 + rate, periods)) / rate) * payment * (1 +rate * type) - future) / Math.pow(1 + rate, periods);
      }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.liabilities = res.data;
        } else {
            $scope.liabilities = [];
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
