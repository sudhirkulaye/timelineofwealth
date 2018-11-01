var incExpSavingsModule = angular.module('SipsManagement', ['ui.bootstrap']);

incExpSavingsModule.controller('SipsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.fundHouses = [];
    $scope.fundsDTO = [];
    $scope.sips =[];
    $scope.sipForm = {
        "key":{"memberid":-1,"sipid":-1},
        "instrumentType":"",
        "fundHouse":"",
        "directRegular":"",
        "dividendGrowth":"",
        "dividendFreq":"",
        "schemeCode":"",
        "schemeName":"",
        "assetClassid":0,
        "category":"",
        "equityStyleBox":"",
        "debtStyleBox":"",
        "startDate":new Date("2010-01-01"),
        "endDate":new Date("2010-01-01"),
        "deductionDay":0,
        "amount":0,
        "sipFreq":0,
        "isActive":"Yes"
    };

    $scope.hideForm = true;
    $scope.editMode = false;
    $scope.mutualFundInstrument = false;
    //$scope.searchBySchemeCode = true;
    $scope.dateToday = new Date();


    showRecords();

    function showRecords(){
        $scope.sips = new Array;
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

        var publicapiurl = "/public/api/getdistinctfundhouse";
        $http.get(publicapiurl).
            then(function (response) {
                if (response != undefined) {
                    $scope.fundHouses = response.data;
                } else {
                    $scope.fundHouses = [];
                }
            });

        url = "/getsips";

        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.sips = response.data;
                } else {
                    $scope.sips = [];
                }
            });
    }

    $scope.onSelectionOfInstrumentType = function onSelectionOfInstrumentType() {
        //console.log("in onSelectionOfInstrumentType" + $scope.sipForm.instrumentType)
        if($scope.sipForm.instrumentType == "Mutual Fund") {
            mutualFundInstrument = true;
        } else {
            mutualFundInstrument = false;
        }
    }

    /*$scope.onSelectionOfSearchBy = function(value) {
        //console.log('$scope.searchBy:' + value);
        if (value == 'schemeCode') {
            $scope.searchBySchemeCode = true;
        } else {
            $scope.searchBySchemeCode = false;
        }
    }*/

    $scope.setSchemeDetails = function(fundDTO) {
        $scope.sipForm.schemeCode = fundDTO.schemeCode;
        $scope.sipForm.directRegular = fundDTO.directRegular;
        $scope.sipForm.dividendGrowth = fundDTO.dividendGrowth;
        $scope.sipForm.dividendFreq = fundDTO.dividendFreq;
        $scope.sipForm.category = fundDTO.category;
        $scope.sipForm.equityStyleBox = fundDTO.equityStyleBox;
        $scope.sipForm.debtStyleBox = fundDTO.debtStyleBox;
    }

    // on change of schemeCode in Add Mode
    $scope.getMFDetails = function getMFDetails() {

    }

    $scope.getSchemeNames = function getSchemeNames() {
        var publicapiurl = "/public/api/getschemenames";
        var params = "";
        //console.log($scope.sipForm.fundHouse);
        if ($scope.sipForm.fundHouse != "" || $scope.sipForm.fundHouse != undefined){
            params = params + "/" + $scope.sipForm.fundHouse
            if ($scope.sipForm.directRegular != "" || $scope.sipForm.directRegular != undefined) {
                params = params + "/" + $scope.sipForm.directRegular
                if ($scope.sipForm.dividendGrowth != "" || $scope.sipForm.dividendGrowth != undefined) {
                    params = params + "/" + $scope.sipForm.dividendGrowth
                }
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

    $scope.editSipRecord = function editSipRecord(sipRecord){
        $scope.hideForm = false;
        $scope.editMode = true;
        $scope.mutualFundInstrument = false;

        $scope.sipForm.key.memberid = ""+sipRecord.key.memberid;
        $scope.sipForm.key.sipid = sipRecord.key.sipid;
        $scope.sipForm.instrumentType = sipRecord.instrumentType;
        $scope.sipForm.fundHouse = sipRecord.fundHouse;
        $scope.sipForm.directRegular = sipRecord.directRegular;
        $scope.sipForm.dividendGrowth = sipRecord.dividendGrowth;
        $scope.sipForm.dividendFreq = ""+sipRecord.dividendFreq;
        $scope.sipForm.schemeCode = ""+sipRecord.schemeCode;
        $scope.sipForm.schemeName = sipRecord.schemeName;
        $scope.sipForm.assetClassid = ""+sipRecord.assetClassid;
        $scope.sipForm.category = sipRecord.category;
        $scope.sipForm.equityStyleBox = sipRecord.equityStyleBox;
        $scope.sipForm.debtStyleBox = sipRecord.debtStyleBox;
        $scope.sipForm.startDate = new Date(sipRecord.startDate);
        $scope.sipForm.endDate = new Date(sipRecord.endDate);
        $scope.sipForm.deductionDay = sipRecord.deductionDay;
        $scope.sipForm.amount = sipRecord.amount;
        $scope.sipForm.sipFreq = ""+sipRecord.sipFreq;
        $scope.sipForm.isActive = sipRecord.isActive;

        if($scope.sipForm.instrumentType == "Mutual Fund") {
            mutualFundInstrument = true;
        } else {
            mutualFundInstrument = false;
        }
    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;
        $scope.searchBy = 'schemeCode';

        $scope.sipForm.key.memberid = "-1";
        $scope.sipForm.key.sipid = "-1";
        $scope.sipForm.instrumentType = "";
        $scope.sipForm.fundHouse = "";
        $scope.sipForm.directRegular = "";
        $scope.sipForm.dividendGrowth = "";
        $scope.sipForm.dividendFreq = "";
        $scope.sipForm.schemeCode = "";
        $scope.sipForm.schemeName = "";
        $scope.sipForm.assetClassid = "";
        $scope.sipForm.category = "";
        $scope.sipForm.equityStyleBox = "";
        $scope.sipForm.debtStyleBox = "";
        $scope.sipForm.startDate = new Date("2010-01-01");
        $scope.sipForm.endDate = new Date("2010-01-01");
        $scope.sipForm.deductionDay = "";
        $scope.sipForm.amount = "";
        $scope.sipForm.sipFreq = "0";
        $scope.sipForm.isActive = "Yes";
    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;
    }

    $scope.deleteSipRecord = function deleteSipRecord(sipRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deletesip";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(sipRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }
    }

    $scope.processSipRecord = function processSipRecord(){
        var method = "";
        var url = "";
        if($scope.SipRecordHtmlForm.$valid)
        {
               $scope.sipForm.dateLastUpdate = new Date();
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addsip";
              } else {
                  method = "PUT";
                  url = "/updatesip";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.sipForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.sips = res.data;
        } else {
            $scope.sips = [];
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

incExpSavingsModule.filter("frequency", function () {
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