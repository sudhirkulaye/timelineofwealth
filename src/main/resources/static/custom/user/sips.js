var module = angular.module('SipsManagement', ['ui.bootstrap']);

module.controller('SipsController', function($scope, $http, $filter) {
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
    $scope.totalAnnualInvestment = 0;
    $scope.totalAnnualInvestmentInEndowmentInsurance = 0;
    $scope.totalAnnualInvestmentInEndowmentInsurancePercent = 0;
    $scope.totalAnnualInvestmentInPPFFDRD = 0;
    $scope.totalAnnualInvestmentInPPFFDRDPercent = 0;
    $scope.totalAnnualInvestmentInULIP = 0;
    $scope.totalAnnualInvestmentInULIPPercent = 0;
    $scope.totalAnnualInvestmentInOther = 0;
    $scope.totalAnnualInvestmentInOtherPercent = 0;
    $scope.totalAnnualInvestmentInMutualFunds = 0;
    $scope.totalAnnualInvestmentInMutualFundsPercent = 0;

    $scope.totalAnnualInvestmentInDebtMF = 0;
    $scope.totalAnnualInvestmentInHybridMF = 0;
    $scope.totalAnnualInvestmentInEquityLargeMF = 0;
    $scope.totalAnnualInvestmentInEquityMidMF = 0;
    $scope.totalAnnualInvestmentInEquitySmallMF = 0;
    $scope.totalAnnualInvestmentInEquityMultiMF = 0;
    $scope.totalAnnualInvestmentInEquityElssMF = 0;
    $scope.totalAnnualInvestmentInEquityOtherMF = 0;

    $scope.totalAnnualInvestmentInDebtMFPercent = 0;
    $scope.totalAnnualInvestmentInHybridMFPercent = 0;
    $scope.totalAnnualInvestmentInEquityLargeMFPercent = 0;
    $scope.totalAnnualInvestmentInEquityMidMFPercent = 0;
    $scope.totalAnnualInvestmentInEquitySmallMFPercent = 0;
    $scope.totalAnnualInvestmentInEquityMultiMFPercent = 0;
    $scope.totalAnnualInvestmentInEquityElssMFPercent = 0;
    $scope.totalAnnualInvestmentInEquityOtherMFPercent = 0;



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
                    computeSummary();
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
        //$scope.searchBy = 'schemeCode';

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
        computeSummary();
    }

    $scope.deleteSipRecord = function deleteSipRecord(sipRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "POST";
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
        computeSummary();
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

    function computeSummary() {
        $scope.totalAnnualInvestment = 0;
        $scope.totalAnnualInvestmentInEndowmentInsurance = 0;
        $scope.totalAnnualInvestmentInEndowmentInsurancePercent = 0;
        $scope.totalAnnualInvestmentInPPFFDRD = 0;
        $scope.totalAnnualInvestmentInPPFFDRDPercent = 0;
        $scope.totalAnnualInvestmentInULIP = 0;
        $scope.totalAnnualInvestmentInULIPPercent = 0;
        $scope.totalAnnualInvestmentInOther = 0;
        $scope.totalAnnualInvestmentInOtherPercent = 0;
        $scope.totalAnnualInvestmentInMutualFunds = 0;
        $scope.totalAnnualInvestmentInMutualFundsPercent = 0;

        $scope.totalAnnualInvestmentInDebtMF = 0;
        $scope.totalAnnualInvestmentInHybridMF = 0;
        $scope.totalAnnualInvestmentInEquityLargeMF = 0;
        $scope.totalAnnualInvestmentInEquityMidMF = 0;
        $scope.totalAnnualInvestmentInEquitySmallMF = 0;
        $scope.totalAnnualInvestmentInEquityMultiMF = 0;
        $scope.totalAnnualInvestmentInEquityElssMF = 0;
        $scope.totalAnnualInvestmentInEquityOtherMF = 0;

        $scope.totalAnnualInvestmentInDebtMFPercent = 0;
        $scope.totalAnnualInvestmentInHybridMFPercent = 0;
        $scope.totalAnnualInvestmentInEquityLargeMFPercent = 0;
        $scope.totalAnnualInvestmentInEquityMidMFPercent = 0;
        $scope.totalAnnualInvestmentInEquitySmallMFPercent = 0;
        $scope.totalAnnualInvestmentInEquityMultiMFPercent = 0;
        $scope.totalAnnualInvestmentInEquityElssMFPercent = 0;
        $scope.totalAnnualInvestmentInEquityOtherMFPercent = 0;

        var multiplier = 0;
        for (i=0; i<$scope.sips.length; i++){

//            if ($scope.sips[i].sipFreq == 0) {multiplier = 0;}
//            if ($scope.sips[i].sipFreq == 1) {multiplier = 12;}
//            if ($scope.sips[i].sipFreq == 3) {multiplier = 4;}
//            if ($scope.sips[i].sipFreq == 12) {multiplier = 1;}
            multiplier = $scope.sips[i].sipFreq;

            $scope.totalAnnualInvestment = $scope.totalAnnualInvestment + ($scope.sips[i].amount*multiplier);

            if($scope.sips[i].instrumentType == "Mutual Fund") {
                $scope.totalAnnualInvestmentInMutualFunds = $scope.totalAnnualInvestmentInMutualFunds + ($scope.sips[i].amount*multiplier);
                var category = $scope.sips[i].category;
                if(category.includes("Debt-")){
                    $scope.totalAnnualInvestmentInDebtMF = $scope.totalAnnualInvestmentInDebtMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Hybrid-")){
                    $scope.totalAnnualInvestmentInHybridMF = $scope.totalAnnualInvestmentInHybridMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Equity-ELSS")){
                    $scope.totalAnnualInvestmentInEquityElssMF = $scope.totalAnnualInvestmentInEquityElssMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Equity-Large")){
                    $scope.totalAnnualInvestmentInEquityLargeMF = $scope.totalAnnualInvestmentInEquityLargeMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Equity-Midcap")){
                    $scope.totalAnnualInvestmentInEquityMidMF = $scope.totalAnnualInvestmentInEquityMidMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Equity-Small")){
                    $scope.totalAnnualInvestmentInEquitySmallMF = $scope.totalAnnualInvestmentInEquitySmallMF + ($scope.sips[i].amount*multiplier);
                }
                if(category.includes("Equity-Multi")){
                    $scope.totalAnnualInvestmentInEquityMultiMF = $scope.totalAnnualInvestmentInEquityMultiMF + ($scope.sips[i].amount*multiplier);
                }
            }
            if($scope.sips[i].instrumentType == "PPF-FD-RD") {
                $scope.totalAnnualInvestmentInPPFFDRD = $scope.totalAnnualInvestmentInPPFFDRD + ($scope.sips[i].amount*multiplier);
            }
            if($scope.sips[i].instrumentType == "Endowment Insurance") {
                $scope.totalAnnualInvestmentInEndowmentInsurance = $scope.totalAnnualInvestmentInEndowmentInsurance + ($scope.sips[i].amount*multiplier);
            }
            if($scope.sips[i].instrumentType == "ULIP") {
                $scope.totalAnnualInvestmentInULIP = $scope.totalAnnualInvestmentInULIP + ($scope.sips[i].amount*multiplier);
            }
            if($scope.sips[i].instrumentType == "Other") {
                $scope.totalAnnualInvestmentInOther = $scope.totalAnnualInvestmentInOther + ($scope.sips[i].amount*multiplier);
            }
        }

        $scope.totalAnnualInvestmentInEquityOtherMF = $scope.totalAnnualInvestmentInMutualFunds -
                                                    (
                                                        $scope.totalAnnualInvestmentInDebtMF + $scope.totalAnnualInvestmentInHybridMF +
                                                        $scope.totalAnnualInvestmentInEquityElssMF + $scope.totalAnnualInvestmentInEquityLargeMF +
                                                        $scope.totalAnnualInvestmentInEquityMidMF + $scope.totalAnnualInvestmentInEquitySmallMF +
                                                        $scope.totalAnnualInvestmentInEquityMultiMF
                                                    );
        $scope.totalAnnualInvestmentInMutualFundsPercent = ($scope.totalAnnualInvestmentInMutualFunds/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInPPFFDRDPercent = ($scope.totalAnnualInvestmentInPPFFDRD/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEndowmentInsurancePercent = ($scope.totalAnnualInvestmentInEndowmentInsurance/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInULIPPercent = ($scope.totalAnnualInvestmentInULIP/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEndowmentInsurancePercent = ($scope.totalAnnualInvestmentInEndowmentInsurance/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInOtherPercent = ($scope.totalAnnualInvestmentInOther/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInDebtMFPercent = ($scope.totalAnnualInvestmentInDebtMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInHybridMFPercent = ($scope.totalAnnualInvestmentInHybridMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquityLargeMFPercent = ($scope.totalAnnualInvestmentInEquityLargeMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquityMidMFPercent = ($scope.totalAnnualInvestmentInEquityMidMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquitySmallMFPercent = ($scope.totalAnnualInvestmentInEquitySmallMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquityMultiMFPercent = ($scope.totalAnnualInvestmentInEquityMultiMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquityElssMFPercent = ($scope.totalAnnualInvestmentInEquityElssMF/$scope.totalAnnualInvestment)*100;
        $scope.totalAnnualInvestmentInEquityOtherMFPercent = ($scope.totalAnnualInvestmentInEquityOtherMF/$scope.totalAnnualInvestment)*100;
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

module.filter("frequency", function () {
    return function (frequency) {
        switch (frequency) {
            case 0: return "One Time";
            case 1: return "Annually";
            case 2: return "Semi Annually";
            case 4: return "Quarterly";
            case 12: return "Monthly";
            case 24: return "Fortnightly";
            case 52: return "Weekly";
            case 250: return "Daily";
        }
    }
})