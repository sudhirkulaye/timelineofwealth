var module = angular.module('WealthDetailsManagement', ['ui.bootstrap']);

module.controller('WealthDetailsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.assetClasses = [];
    $scope.fundHouses = [];
    $scope.industries = [];
    $scope.fundsDTO = [];
    $scope.stocks = [];
    $scope.wealthDetailsRecords = [];
    $scope.dummy = {assetClassType : "", fundHouse : "", name : "", directRegular : "", dividendGrowth : "", realEstateType : "", altInvstType : ""};
    $scope.wealthDetailsRecordForm = {
        "key":{"memberid":-1,"buyDate":new Date("2010-01-01"), "ticker":""},
        "name":"",
        "shortName":"",
        "assetClassid":"",
        "subindustryid":0,
        "quantity":1,
        "rate":0,
        "brokerage":0,
        "tax":0,
        "totalCost":0,
        "netRate":0,
        "cmp":0,
        "marketValue":0,
        "holdingPeriod":0,
        "netProfit":0,
        "absoluteReturn":0,
        "annualizedReturn":0,
        "maturityValue":0,
        "maturityDate":new Date("2010-01-01"),
        "lastValuationDate":new Date(),
    }
    $scope.hideForm = true;
    $scope.editMode = false;

    $scope.bankAccountRecord = false;
    $scope.FDRecord = false;
    $scope.PPFRecord = false;
    $scope.endowmentInsuranceRecord = false;
    $scope.ULIPRecord = false;
    $scope.mutualFundRecord = false;
    $scope.stockRecord = false;
    $scope.commodityRecord = false;
    $scope.realEstateRecord = false;
    $scope.commodityRecord = false;
    $scope.alternativeInvestmentsRecord = false;

    $scope.buyDateFlag = false;
    $scope.tickerFlag = false;
    $scope.nameFlag = false;
    $scope.shortNameFlag = false;
    $scope.assetClassidFlag = false;
    $scope.subindustryidFlag = false;
    $scope.quantityFlag = false;
    $scope.rateFlag = false;
    $scope.brokerageFlag = false;
    $scope.taxFlag = false;
    $scope.totalCostFlag = false;
    $scope.netRateFlag = false;
    $scope.cmpFlag = false;
    $scope.marketValueFlag = false;
    $scope.holdingPeriodFlag = false;
    $scope.netProfitFlag = false;
    $scope.absoluteReturnFlag = false;
    $scope.annualizedReturnFlag = false;
    $scope.maturityValueFlag = false;
    $scope.maturityDateFlag = false;
    $scope.lastValuationDateFlag = false;
    $scope.mutualFundFlag = false;
    $scope.stockFlag = false;
    $scope.realEstateFlag = false;

    $scope.labelBuyDate = "Purchase Date";
    $scope.labelShortName = "Short Name";
    $scope.labelFullName = "Full Name";
    $scope.labelCMP = "Current Market Price";
    $scope.labelLastValuationDate = "Last Valuation Date";
    $scope.labelRate = "Rate";

    $scope.dateToday = new Date();

    showRecords();

    function showRecords(){
        $scope.wealthDetailsRecords = new Array;
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

        publicapiurl = "/public/api/getassetclassifications";
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

        url = "/getwealthdetailsrecords";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.wealthDetailsRecords = response.data;
                } else {
                    $scope.wealthDetailsRecords = [];
                }
            });
    }

    $scope.getSchemeNames = function getSchemeNames() {
        var publicapiurl = "/public/api/getschemenames";
        var params = "";
        //console.log($scope.sipForm.fundHouse);
        if ($scope.dummy.fundHouse != "" || $scope.dummy.fundHouse != undefined){
            params = params + "/" + $scope.dummy.fundHouse
            if ($scope.dummy.directRegular != "" || $scope.dummy.directRegular != undefined) {
                params = params + "/" + $scope.dummy.directRegular
                if ($scope.dummy.dividendGrowth != "" || $scope.dummy.dividendGrowth != undefined) {
                    params = params + "/" + $scope.dummy.dividendGrowth
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

    $scope.resetDirectRegularDividendGrowth = function() {
        $scope.dummy.directRegular = "";
        $scope.dummy.dividendGrowth = "";
    }

    $scope.setCMPForStocks = function(){
        console.log($scope.stockFlag);
        if($scope.stockFlag){
            if (!$scope.editMode){
                $scope.wealthDetailsRecordForm.cmp = $scope.wealthDetailsRecordForm.rate;
            }
        }
    }

    $scope.setSchemeDetails = function(fundDTO) {
        $scope.wealthDetailsRecordForm.key.ticker = fundDTO.schemeCode;
        $scope.dummy.directRegular = fundDTO.directRegular;
        $scope.dummy.dividendGrowth = fundDTO.dividendGrowth;
        $scope.wealthDetailsRecordForm.name = fundDTO.schemeNameFull;
        $scope.wealthDetailsRecordForm.shortName = fundDTO.schemeNamePart;
        $scope.wealthDetailsRecordForm.assetClassid = ""+fundDTO.assetClassid;
    }

    $scope.setStockDetails = function(stock) {
        $scope.wealthDetailsRecordForm.key.ticker = stock.ticker;
        $scope.wealthDetailsRecordForm.name = stock.name;
        $scope.wealthDetailsRecordForm.shortName = stock.shortName;
        $scope.wealthDetailsRecordForm.assetClassid = ""+stock.assetClassid;
        $scope.wealthDetailsRecordForm.subindustryid = ""+stock.subindustryid;
    }

    function setFieldsFlags(assetClassid, assetClassType) {

        if (assetClassid == 101010 || assetClassid == 101020 || assetClassType == 'Cash/Bank/Broker Account') {

            $scope.bankAccountRecord = true;
            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = false;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = false;
            $scope.netProfitFlag = false;
            $scope.absoluteReturnFlag = false;
            $scope.annualizedReturnFlag = false;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Account Opening Date";
            $scope.labelShortName = "Unique Short Name(e.g. SBI_1)";
            $scope.labelFullName = "Unique Full Name (e.g. SBI_XYZ_BRN_ACC1)";
            $scope.labelCMP = "Cash Balance";
            $scope.labelLastValuationDate = "Balance As On";
        }
        if (assetClassid == 201010 ||
            assetClassid == 202010 ||
            assetClassid == 203010 ||
            assetClassid == 203030 ||
            assetClassType == 'FDs/Notes/Bonds/Debentures/Commercial Paper') {

            $scope.FDRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = false;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = false;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = false;
            $scope.netProfitFlag = false;
            $scope.absoluteReturnFlag = false;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = true;
            $scope.maturityDateFlag = true;
            $scope.lastValuationDateFlag = false;

            $scope.labelBuyDate = "Opening/Purchase Date";
            $scope.labelShortName = "Unique Name (e.g. SBI_FD1_ExpirayDate_Amt)";
            $scope.labelRate = "Principal Amount";
            $scope.labelLastValuationDate = "Valuation As On";
        }
        if (assetClassid == 203020 ||
            assetClassType == 'PPF/NSC/Post Office/Govt. Savings') {
            $scope.PPFRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = false;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = false;
            $scope.netProfitFlag = false;
            $scope.absoluteReturnFlag = false;
            $scope.annualizedReturnFlag = false;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Account Opening Date";
            $scope.labelShortName = "NSC_SerialNo (e.g. PPF_1 Name)";
            $scope.labelCMP = "Cash Balance";
            $scope.labelLastValuationDate = "Balance As On";
        }
        if (assetClassid == 203050 ||
            assetClassType == 'Endowment Insurance') {
            $scope.endowmentInsuranceRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = false;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = false;
            $scope.netProfitFlag = false;
            $scope.absoluteReturnFlag = false;
            $scope.annualizedReturnFlag = false;
            $scope.maturityValueFlag = true;
            $scope.maturityDateFlag = true;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "First Premium Date";
            $scope.labelShortName = "Insurance UIN by IRDA or Unique Name (e.g. LIC_1 Name)";
            $scope.labelFullName = "Insurance Name"
            $scope.labelCMP = "Surrender Value";
            $scope.labelLastValuationDate = "Surrender Value As On";

        }
        if (assetClassid == 301030 ||
            assetClassType == 'ULIP/Pension Funds') {
            $scope.ULIPRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = true;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = true;
            $scope.totalCostFlag = true;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = true;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Date of Purchase";
            $scope.labelShortName = "ULIP/Pension Policy Code";
            $scope.labelFullName = "ULIP/Pension Policy Name"
            $scope.labelRate = "Purchase prise per Unit"
            $scope.labelCMP = "Latest NAV";
            $scope.labelLastValuationDate = "Latest NAV As On";
        }
        if (assetClassid == 201020 ||
            assetClassid == 202020 ||
            assetClassid == 203040 ||
            (assetClassid >= 301010 && assetClassid <= 301030) ||
            (assetClassid >= 401010 && assetClassid <= 405040) ||
            assetClassType == 'Mutual Funds') {
            $scope.mutualFundRecord = true;

            if (!$scope.editMode){
                $scope.mutualFundFlag = true;
            }

            $scope.buyDateFlag = true;

            if ($scope.mutualFundFlag){
                $scope.tickerFlag = false;
                $scope.nameFlag = false;
                $scope.shortNameFlag = false;
            } else {
                $scope.tickerFlag = true;
                $scope.nameFlag = true;
                $scope.shortNameFlag = false;
            }

            $scope.assetClassidFlag = true;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = true;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = true;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = true;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;


        }
        if ((assetClassid >= 406010 && assetClassid <= 408040) || assetClassType == 'Stocks') {
            $scope.StockRecord = true;

            $scope.buyDateFlag = true;

            if (!$scope.editMode){
                $scope.stockFlag = true;
            }

            $scope.buyDateFlag = true;
            $scope.tickerFlag = true;

            if ($scope.stockFlag){
                $scope.nameFlag = false;
            } else {
                $scope.nameFlag = true;
            }

            $scope.shortNameFlag = false;
            $scope.assetClassidFlag = true;
            $scope.subindustryidFlag = true;
            $scope.quantityFlag = true;
            $scope.rateFlag = true;
            $scope.brokerageFlag = true;
            $scope.taxFlag = true;
            $scope.totalCostFlag = true;
            $scope.netRateFlag = true;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = true;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;
        }
        if ((assetClassid >= 501010 && assetClassid <= 502030) || assetClassType == 'Commodity other than MF') {
            $scope.commodityRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.assetClassidFlag = false;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Date of Purchase";
            $scope.labelShortName = "Commodity Code";
            $scope.labelFullName = "Commodity Name"
            $scope.labelRate = "Purchase cost"
            $scope.labelCMP = "Latest Market Value";
            $scope.labelLastValuationDate = "Latest Value As On";
        }
        if ((assetClassid >= 601010 && assetClassid <= 602010) || assetClassType == 'Real Estate other than REIT') {
            $scope.realEstateRecord = true;

            if(!$scope.editMode){
                $scope.realEstateFlag = true;
                $scope.assetClassidFlag = false;
            } else {
                $scope.realEstateFlag = false;
                $scope.assetClassidFlag = true;
            }

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Date of Purchase/Inheritance";
            $scope.labelShortName = "Short Name";
            $scope.labelFullName = "Name"
            $scope.labelRate = "Purchase Cost/Inheritance Value"
            $scope.labelCMP = "Latest Market Value";
            $scope.labelLastValuationDate = "Latest Value As On";

        }
        if ((assetClassid >= 701010 && assetClassid <= 701070) || assetClassType == 'Alternative Investments') {
            $scope.alternativeInvestmentsRecord = true;

            if(!$scope.editMode){
                $scope.alternativeInvestmentsFlag = true;
                $scope.assetClassidFlag = false;
            } else {
                $scope.alternativeInvestmentsFlag = false;
                $scope.assetClassidFlag = true;
            }

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = true;
            $scope.shortNameFlag = true;
            $scope.subindustryidFlag = false;
            $scope.quantityFlag = false;
            $scope.rateFlag = true;
            $scope.brokerageFlag = false;
            $scope.taxFlag = false;
            $scope.totalCostFlag = false;
            $scope.netRateFlag = false;
            $scope.cmpFlag = true;
            $scope.marketValueFlag = false;
            $scope.holdingPeriodFlag = true;
            $scope.netProfitFlag = true;
            $scope.absoluteReturnFlag = true;
            $scope.annualizedReturnFlag = true;
            $scope.maturityValueFlag = false;
            $scope.maturityDateFlag = false;
            $scope.lastValuationDateFlag = true;

            $scope.labelBuyDate = "Date of Purchase/Inheritance";
            $scope.labelShortName = "Short Name";
            $scope.labelFullName = "Name"
            $scope.labelRate = "Purchase Cost/Inheritance Value"
            $scope.labelCMP = "Latest Market Value";
            $scope.labelLastValuationDate = "Latest Value As On";
        }

    }

    $scope.editWealthDetailsRecord = function editWealthDetailsRecord(wealthDetailsRecord){
        $scope.hideForm = false;
        $scope.editMode = true;

        $scope.wealthDetailsRecordForm.key.memberid = ""+wealthDetailsRecord.key.memberid;
        $scope.wealthDetailsRecordForm.key.buyDate = new Date(wealthDetailsRecord.key.buyDate);
        $scope.wealthDetailsRecordForm.key.ticker = wealthDetailsRecord.key.ticker;
        $scope.wealthDetailsRecordForm.name = wealthDetailsRecord.name;
        $scope.wealthDetailsRecordForm.shortName = wealthDetailsRecord.shortName;
        $scope.wealthDetailsRecordForm.assetClassid = ""+wealthDetailsRecord.assetClassid;
        $scope.wealthDetailsRecordForm.subindustryid = ""+wealthDetailsRecord.subindustryid;
        $scope.wealthDetailsRecordForm.quantity = wealthDetailsRecord.quantity;
        $scope.wealthDetailsRecordForm.rate = wealthDetailsRecord.rate;
        $scope.wealthDetailsRecordForm.brokerage = wealthDetailsRecord.brokerage;
        $scope.wealthDetailsRecordForm.tax = wealthDetailsRecord.tax;
        $scope.wealthDetailsRecordForm.totalCost = wealthDetailsRecord.totalCost;
        $scope.wealthDetailsRecordForm.netRate = wealthDetailsRecord.netRate;
        $scope.wealthDetailsRecordForm.cmp = wealthDetailsRecord.cmp;
        $scope.wealthDetailsRecordForm.marketValue = wealthDetailsRecord.marketValue;
        $scope.wealthDetailsRecordForm.holdingPeriod = wealthDetailsRecord.holdingPeriod;
        $scope.wealthDetailsRecordForm.netProfit = wealthDetailsRecord.netProfit;
        $scope.wealthDetailsRecordForm.absoluteReturn = wealthDetailsRecord.absoluteReturn;
        $scope.wealthDetailsRecordForm.annualizedReturn = wealthDetailsRecord.annualizedReturn;
        $scope.wealthDetailsRecordForm.maturityValue = wealthDetailsRecord.maturityValue;
        $scope.wealthDetailsRecordForm.maturityDate = new Date(wealthDetailsRecord.maturityDate);
        $scope.wealthDetailsRecordForm.lastValuationDate = new Date(wealthDetailsRecord.lastValuationDate);

        setFieldsFlags(wealthDetailsRecord.assetClassid, '');

    }

    $scope.onSelectionOfAssetType = function onSelectionOfAssetType(assetClassType) {
        setFieldsFlags(0, assetClassType);
    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.wealthDetailsRecordForm.key.memberid = "";
        $scope.wealthDetailsRecordForm.key.buyDate = new Date("2010-01-01");
        $scope.wealthDetailsRecordForm.key.ticker = "";
        $scope.wealthDetailsRecordForm.name = "";
        $scope.wealthDetailsRecordForm.shortName = "";
        $scope.wealthDetailsRecordForm.assetClassid = 0;
        $scope.wealthDetailsRecordForm.subindustryid = 0;
        $scope.wealthDetailsRecordForm.quantity = 1;
        $scope.wealthDetailsRecordForm.rate = 0;
        $scope.wealthDetailsRecordForm.brokerage = 0;
        $scope.wealthDetailsRecordForm.tax = 0;
        $scope.wealthDetailsRecordForm.totalCost = 0;
        $scope.wealthDetailsRecordForm.netRate = 0;
        $scope.wealthDetailsRecordForm.cmp = 0;
        $scope.wealthDetailsRecordForm.marketValue = 0;
        $scope.wealthDetailsRecordForm.holdingPeriod = 0;
        $scope.wealthDetailsRecordForm.netProfit = 0;
        $scope.wealthDetailsRecordForm.absoluteReturn = 0;
        $scope.wealthDetailsRecordForm.annualizedReturn = 0;
        $scope.wealthDetailsRecordForm.maturityValue = 0;
        $scope.wealthDetailsRecordForm.maturityDate = new Date("2010-01-01");
        $scope.wealthDetailsRecordForm.lastValuationDate = new Date();

        $scope.dummy.assetClassType = "";
        $scope.dummy.fundHouse = "";
        $scope.dummy.name = "";
        $scope.dummy.dividendGrowth = "";
        $scope.dummy.directRegular = "";
        $scope.dummy.realEstateType = "";
        $scope.dummy.altInvstType = "";

    }

    $scope.showTable = function showTable() {
        $scope.hideForm = true;

        $scope.buyDateFlag = false;
        $scope.tickerFlag = false;
        $scope.nameFlag = false;
        $scope.shortNameFlag = false;
        $scope.assetClassidFlag = false;
        $scope.subindustryidFlag = false;
        $scope.quantityFlag = false;
        $scope.rateFlag = false;
        $scope.brokerageFlag = false;
        $scope.taxFlag = false;
        $scope.totalCostFlag = false;
        $scope.netRateFlag = false;
        $scope.cmpFlag = false;
        $scope.marketValueFlag = false;
        $scope.holdingPeriodFlag = false;
        $scope.netProfitFlag = false;
        $scope.absoluteReturnFlag = false;
        $scope.annualizedReturnFlag = false;
        $scope.maturityValueFlag = false;
        $scope.maturityDateFlag = false;
        $scope.lastValuationDateFlag = false;

        $scope.mutualFundFlag = false;
        $scope.stockFlag = false;
        $scope.realEstateFlag = false;

        $scope.dummy.assetClassType = "";

        $scope.bankAccountRecord = false;
        $scope.FDRecord = false;
        $scope.PPFRecord = false;
        $scope.endowmentInsuranceRecord = false;
        $scope.ULIPRecord = false;
        $scope.mutualFundRecord = false;
        $scope.stockRecord = false;
        $scope.commodityRecord = false;
        $scope.realEstateRecord = false;
        $scope.commodityRecord = false;
        $scope.alternativeInvestmentsRecord = false;

        $scope.labelBuyDate = "Purchase Date";
        $scope.labelShortName = "Short Name";
        $scope.labelFullName = "Full Name";
        $scope.labelCMP = "Current Market Price";
        $scope.labelLastValuationDate = "Last Valuation Date";
        $scope.labelRate = "Rate";


    }

    $scope.deleteWealthDetailsRecord = function deleteWealthDetailsRecord(wealthDetailsRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "DELETE";
            var url = "/deletewealthdetailsrecord";
            $http({
                      method: method,
                      url: urlBase + url,
                      data: angular.toJson(wealthDetailsRecord),
                      headers: {
                          'Content-Type': 'application/json'
                      }
                  }).then(_success, _error);
        }

    }

    $scope.processWealthDetailsRecord = function processWealthDetailsRecord() {
        var method = "";
        var url = "";

        if ($scope.bankAccountRecord){
            //console.log("In Bank Acct...");
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.assetClassid = 101020;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.tax = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
            $scope.wealthDetailsRecordForm.rate = $scope.wealthDetailsRecordForm.cmp;
        }
        if ($scope.FDRecord){
            //console.log("In FDRecord...");
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                var fdTerm = (new Date($scope.wealthDetailsRecordForm.maturityDate)- new Date($scope.wealthDetailsRecordForm.key.buyDate))/(1000 * 60 * 60 * 24 * 365.25);
                //console.log("fdTerm: "+fdTerm);
                // if (type == "Bond") $scope.wealthDetailsRecordForm.ssetClassid == 203030;
                if (fdTerm > 3){
                    $scope.wealthDetailsRecordForm.assetClassid = 203010;
                } else if (fdTerm > 0.6){
                    $scope.wealthDetailsRecordForm.assetClassid = 202010;
                } else {
                    $scope.wealthDetailsRecordForm.assetClassid = 201010;
                }
                $scope.wealthDetailsRecordForm.name = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.tax = 0;
                $scope.wealthDetailsRecordForm.cmp = $scope.wealthDetailsRecordForm.rate;
            }
        }
        if ($scope.PPFRecord){
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.assetClassid = 203020;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.tax = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0.08;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
            $scope.wealthDetailsRecordForm.rate = $scope.wealthDetailsRecordForm.cmp;
        }
        if ($scope.endowmentInsuranceRecord){
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.assetClassid = 203050;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.tax = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
            }
            $scope.wealthDetailsRecordForm.rate = $scope.wealthDetailsRecordForm.cmp;
        }
        if ($scope.ULIPRecord){
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.assetClassid = 301030;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }
        if ($scope.mutualFundRecord){
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }
        if ($scope.stockRecord){
            if (!$scope.editMode) {
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }
        if ($scope.commodityRecord) {
            if(!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.assetClassid = 502030;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }
        if ($scope.realEstateRecord) {
            if(!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.assetClassid = $scope.dummy.realEstateType;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }
        if ($scope.alternativeInvestmentsRecord) {
            if(!$scope.editMode) {
                $scope.wealthDetailsRecordForm.key.ticker = $scope.wealthDetailsRecordForm.shortName;
                $scope.wealthDetailsRecordForm.quantity = 1;
                $scope.wealthDetailsRecordForm.assetClassid = $scope.dummy.altInvstType;
                $scope.wealthDetailsRecordForm.subindustryid = 0;
                $scope.wealthDetailsRecordForm.brokerage = 0;
                $scope.wealthDetailsRecordForm.absoluteReturn = 0;
                $scope.wealthDetailsRecordForm.annualizedReturn = 0;
                $scope.wealthDetailsRecordForm.maturityValue = 0;
                $scope.wealthDetailsRecordForm.maturityDate = new Date("2000-01-01");
            }
        }

        $scope.wealthDetailsRecordForm.totalCost = ($scope.wealthDetailsRecordForm.quantity*$scope.wealthDetailsRecordForm.rate) +
                                                    $scope.wealthDetailsRecordForm.brokerage + $scope.wealthDetailsRecordForm.tax;
        //$filter('number')($scope.wealthDetailsRecordForm.totalCost,2);
        $scope.wealthDetailsRecordForm.netRate = $scope.wealthDetailsRecordForm.totalCost/$scope.wealthDetailsRecordForm.quantity;
        //$filter('number')($scope.wealthDetailsRecordForm.netRate,2);
        $scope.wealthDetailsRecordForm.marketValue = $scope.wealthDetailsRecordForm.quantity*$scope.wealthDetailsRecordForm.cmp;
        //$filter('number')($scope.wealthDetailsRecordForm.marketValue,2);
        $scope.wealthDetailsRecordForm.holdingPeriod = (new Date()- new Date($scope.wealthDetailsRecordForm.key.buyDate))/(1000 * 60 * 60 * 24 * 365.25);
        $scope.wealthDetailsRecordForm.holdingPeriod = $filter('number')($scope.wealthDetailsRecordForm.holdingPeriod,2);
        $scope.wealthDetailsRecordForm.netProfit = $scope.wealthDetailsRecordForm.marketValue - $scope.wealthDetailsRecordForm.totalCost;
        //$filter('number')($scope.wealthDetailsRecordForm.netProfit,2);

        console.log($scope.wealthDetailsRecordForm);

        if($scope.wealthDetailsRecordHtmlForm.$valid) {
              console.log('Posting ...:');
              //Submit your form
              if ($scope.editMode != true) {
                  method = "POST";
                  url = "/addwealthdetailsrecord";
              } else {
                  method = "PUT";
                  url = "/updatewealthdetailsrecord";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.wealthDetailsRecordForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
    }

    function _success(res) {
        if (res != undefined) {
            $scope.wealthDetailsRecords = res.data;
        } else {
            $scope.wealthDetailsRecords = [];
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

    $scope.searchTransaction = function (wealthDetailsRecord) {
        if ($scope.searchText == undefined) {
            return true;
        } else {
            if (wealthDetailsRecord.shortName.toLowerCase().indexOf($scope.searchText.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByMember = function (wealthDetailsRecord) {
        if ($scope.searchMember == undefined || $scope.searchMember == 0) {
            return true;
        } else {
            if (wealthDetailsRecord.key.memberid == $scope.searchMember ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByAssetClass = function (wealthDetailsRecord) {
        if ($scope.searchAssetClass == undefined || $scope.searchAssetClass == "") {
            return true;
        } else {
            if ($scope.searchAssetClass == 'Cash - Cash/Bank Accounts' ) {
                if (wealthDetailsRecord.assetClassid == 101010 || wealthDetailsRecord.assetClassid == 101020) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Fixed Income - FDs/PPF' ) {
                if (wealthDetailsRecord.assetClassid == 201010 ||
                    wealthDetailsRecord.assetClassid == 202010 ||
                    wealthDetailsRecord.assetClassid == 203010 ||
                    wealthDetailsRecord.assetClassid == 203020) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Fixed Income - Endowment Insurance' ) {
                if (wealthDetailsRecord.assetClassid == 203050) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Fixed Income - Debt MFs' ) {
                if (wealthDetailsRecord.assetClassid == 201020 ||
                    wealthDetailsRecord.assetClassid == 202020 ||
                    wealthDetailsRecord.assetClassid == 203040) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Fixed Inc. & Equity - ULIP/Pension Funds' ) {
                if (wealthDetailsRecord.assetClassid == 301030) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Fixed Inc. &amp; Equity - Hybrid MFs' ) {
                if (wealthDetailsRecord.assetClassid >= 301010 &&
                    wealthDetailsRecord.assetClassid <= 301020) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Diversified Equity - ETFs,MFs' ) {
                if (wealthDetailsRecord.assetClassid >= 401010 &&
                    wealthDetailsRecord.assetClassid <= 405040) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Individual Equity - Stocks' ) {
                if (wealthDetailsRecord.assetClassid >= 406010 &&
                    wealthDetailsRecord.assetClassid <= 408040) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Commodity' ) {
                if (wealthDetailsRecord.assetClassid >= 501010 &&
                    wealthDetailsRecord.assetClassid <= 502030) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Real Estate' ) {
                if (wealthDetailsRecord.assetClassid >= 601010 &&
                    wealthDetailsRecord.assetClassid <= 602010) {
                    return true;
                } else {
                    return false;
                }
            }
            if ($scope.searchAssetClass == 'Alternative Investments' ) {
                if (wealthDetailsRecord.assetClassid >= 701010 &&
                    wealthDetailsRecord.assetClassid <= 701070) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

});