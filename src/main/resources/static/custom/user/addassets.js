var module = angular.module('WealthDetailsManagement', ['ui.bootstrap']);

module.controller('WealthDetailsController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members = [];
    $scope.assetClasses = [];
    $scope.fundHouses = [];
    $scope.industries = [];
    $scope.fundsDTO = [];
    $scope.stocks = [];
    $scope.sips = [];
    $scope.wealthDetailsRecords = [];
    $scope.dummy = {assetClassType : "", fundHouse : "", name : "", directRegular : "", dividendGrowth : "", realEstateType : "", altInvstType : ""};
    $scope.wealthDetailsRecordForm = {
        "key":{"memberid":-1,"buyDate":"", "ticker":""},
        "name":"",
        "shortName":"",
        "assetClassid":"",
        "subindustryid":0,
        "quantity":1.000,
        "rate":0.00,
        "brokerage":0.00,
        "tax":0.00,
        "totalCost":0.00,
        "netRate":0.00,
        "cmp":0.00,
        "marketValue":0.00,
        "holdingPeriod":0.00,
        "netProfit":0.00,
        "absoluteReturn":0.0000,
        "annualizedReturn":0.0000,
        "maturityValue":0.00,
        "maturityDate":"",
        "lastValuationDate":new Date(),
        "sipid":0
    }
    var subindustryidInt = 0;
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
    $scope.alternativeInvestmentsFlag = false;
    $scope.sipidFlag = false;

    $scope.labelBuyDate = "Purchase Date";
    $scope.labelBuyDateToolTip = "Asset/Security Purchase/Transaction Date";
    $scope.labelShortName = "Short Name";
    $scope.labelShortNameToolTip = "Short Name for display purpose"
    $scope.labelFullName = "Full Name";
    $scope.labelFullNameToolTip = "Full Name/Description";
    $scope.labelCMP = "Current Market Price";
    $scope.labelCMPToolTip = "Current Market Price per unit";
    $scope.labelLastValuationDate = "Last Valuation Date";
    $scope.labelRate = "Rate";
    $scope.labelRateToolTip = "Purchase Rate per unit"

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
        //console.log($scope.stockFlag);
        if($scope.stockFlag){
            if (!$scope.editMode){
                //$scope.wealthDetailsRecordForm.cmp = $scope.wealthDetailsRecordForm.rate;
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
        $scope.wealthDetailsRecordForm.cmp = fundDTO.latestNav;
        //console.log(parseFloat(fundDTO.latestNav).toFixed(2));
        //$scope.wealthDetailsRecordForm.cmp = parseFloat(fundDTO.latestNav).toFixed(2);
        var urlForSip = urlBase +"/getsipbyschemecode/" +$scope.wealthDetailsRecordForm.key.memberid + "/"+ fundDTO.schemeCode;
        $http.get(urlForSip).
            then(function (response) {
                if (response != undefined) {
                    $scope.sips = response.data;
                } else {
                    $scope.sips = [];
                }
            });
    }

    $scope.setStockDetails = function(stock) {
        $scope.wealthDetailsRecordForm.key.ticker = stock.ticker;
        $scope.wealthDetailsRecordForm.name = stock.name;
        $scope.wealthDetailsRecordForm.shortName = stock.shortName;
        $scope.wealthDetailsRecordForm.assetClassid = ""+stock.assetClassid;
        $scope.wealthDetailsRecordForm.subindustryid = ""+stock.subindustryid;
        subindustryidInt = stock.subindustryid;
        $scope.wealthDetailsRecordForm.cmp = stock.latestPrice;
    }

    function setFieldsFlags(assetClassid, assetClassType) {
        if (assetClassType != "" && assetClassType != undefined) {
            var memberidSelected = $scope.wealthDetailsRecordForm.key.memberid;
            $scope.showClearForm();
            $scope.dummy.assetClassType = assetClassType;
            $scope.wealthDetailsRecordForm.key.memberid = memberidSelected;
        }
        if (assetClassid != "" && assetClassid != undefined){
            $scope.showClearForm();
            $scope.editMode = true;
        }

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
            $scope.labelBuyDateToolTip = "Account Opening Date";
            $scope.labelShortNameToolTip = "Unique Short Name for display purpose SBI_1 OR SBI_2 OR HDFC_1"
            $scope.labelFullNameToolTip = "Bank Name-Branch Name-Account Number";
            $scope.labelCMP = "Cash Balance";
            $scope.labelCMPToolTip = "Cash Balance needs to be updated regularly - at least monthly- by client"
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
            $scope.labelBuyDateToolTip = "FD Opening Date or Bond/CD Purchase Date"
            $scope.labelShortNameToolTip = "Unique Short Name e.g. SBI_FD1";
            $scope.labelRate = "Principal Amount";
            $scope.labelRateToolTip = "Principal Amount invested"
            $scope.labelLastValuationDate = "Valuation As On";
        }
        if (assetClassid == 203020 ||
            assetClassType == 'PPF/NSC/Post Office/Govt. Savings') {
            $scope.PPFRecord = true;

            $scope.buyDateFlag = true;
            $scope.tickerFlag = false;
            $scope.nameFlag = false;
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
            $scope.labelBuyDateToolTip = "PPF Account Opening Date OR NSC purchase date"
            $scope.labelShortNameToolTip = "Unique Short Name such as NSC_1, PPF_1";
            $scope.labelCMP = "Balance";
            $scope.labelCMPToolTip = "Balance of PPF and others needs to be updated regularly"
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
            $scope.labelBuyDateToolTip = "Insurance first premium date"
            $scope.labelShortNameToolTip = "Insurance UIN by IRDA OR Unique Name e.g. LIC_1 Name";
            $scope.labelFullNameToolTip = "Insurance Full Name"
            $scope.labelCMP = "Surrender Value";
            $scope.labelCMPToolTip = "Amount that will be received if policy is surrendered"
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
            $scope.labelBuyDateToolTip = "ULIP or Pension Fund purchase date"
            $scope.labelShortNameToolTip = "Unique ULIP/Pension Policy Code e.g. ULIP_1 ";
            $scope.labelFullNameToolTip = "ULIP/Pension Policy Full Name";
            $scope.labelRate = "Purchase prise per unit";
            $scope.labelRateToolTip = "Purchase price per unit";
            $scope.labelCMP = "Latest NAV";
            $scope.labelCMPToolTip = "Latest NAV of Mutual Fund";
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
            $scope.sipidFlag = true;

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
            $scope.labelBuyDateToolTip = "Date when the commodity purchased";
            $scope.labelShortNameToolTip = "Commodity Code";
            $scope.labelFullNameToolTip = "Commodity Full Name";
            $scope.labelRate = "Purchase cost";
            $scope.labelRateToolTip = "Purchase cost of commodity per unit"
            $scope.labelCMP = "Latest Market Value";
            $scope.labelCMPToolTip = "Latest Market Value of the commodity";
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
            $scope.labelBuyDateToolTip = "Date when Real Estate was bought or transferred";
            $scope.labelShortNameToolTip = "Short Name such as Rental_1, NA_Plot_1";
            $scope.labelFullNameToolTip = "Description of Property"
            $scope.labelRate = "Purchase Cost/Inheritance Value";
            $scope.labelRateToolTip = "Purchase cost of real estate";
            $scope.labelCMP = "Latest Market Value";
            $scope.labelCMPToolTip = "Latest Market Value of the Real Estate, needs to be updated at least annually"
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
            $scope.labelBuyDateToolTip = "Date when alternative investment was made or transferred"
            $scope.labelShortNameToolTip = "Short Name such as INVESCO_PMS_1, MOSL_PMS_2 or PE_1";
            $scope.labelFullNameToolTip = "Description of Alternative Investment";
            $scope.labelRate = "Purchase Cost/Inheritance Value";
            $scope.labelRateToolTip = "Purchase Cost/Inheritance Value of Investment";
            $scope.labelCMP = "Latest Market Value";
            $scope.labelCMPToolTip = "Latest Market Value of Investment, needs to be updated at least annually"
            $scope.labelLastValuationDate = "Latest Value As On";
        }

    }

    $scope.editWealthDetailsRecord = function editWealthDetailsRecord(wealthDetailsRecord){
        $scope.hideForm = false;
        $scope.editMode = true;
        setFieldsFlags(wealthDetailsRecord.assetClassid, '');

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


        if (wealthDetailsRecord.assetClassid == 201020 ||
                    wealthDetailsRecord.assetClassid == 202020 ||
                    wealthDetailsRecord.assetClassid == 203040 ||
                    (wealthDetailsRecord.assetClassid >= 301010 && wealthDetailsRecord.assetClassid <= 301030) ||
                    (wealthDetailsRecord.assetClassid >= 401010 && wealthDetailsRecord.assetClassid <= 405040) ) {
            var urlForSip = urlBase +"/getsipbyschemecode/" +wealthDetailsRecord.key.memberid + "/"+ wealthDetailsRecord.key.ticker;
            $http.get(urlForSip).
                then(function (response) {
                    if (response != undefined) {
                        $scope.sips = response.data;
                    } else {
                        $scope.sips = [];
                    }
                });
        }
        $scope.wealthDetailsRecordForm.sipid = ""+wealthDetailsRecord.sipid;

    }

    $scope.onSelectionOfAssetType = function onSelectionOfAssetType(assetClassType) {
        setFieldsFlags(0, assetClassType);
    }

    $scope.showClearForm = function showClearForm() {
        $scope.hideForm = false;
        $scope.editMode = false;

        $scope.wealthDetailsRecordForm.key.memberid = "";
        $scope.wealthDetailsRecordForm.key.buyDate = "";
        $scope.wealthDetailsRecordForm.key.ticker = "";
        $scope.wealthDetailsRecordForm.name = "";
        $scope.wealthDetailsRecordForm.shortName = "";
        $scope.wealthDetailsRecordForm.assetClassid = 0;
        $scope.wealthDetailsRecordForm.subindustryid = 0;
        $scope.wealthDetailsRecordForm.quantity = 1.000;
        $scope.wealthDetailsRecordForm.rate = 0.00;
        $scope.wealthDetailsRecordForm.brokerage = 0.00;
        $scope.wealthDetailsRecordForm.tax = 0.00;
        $scope.wealthDetailsRecordForm.totalCost = 0.00;
        $scope.wealthDetailsRecordForm.netRate = 0.00;
        $scope.wealthDetailsRecordForm.cmp = 0.00;
        $scope.wealthDetailsRecordForm.marketValue = 0.00;
        $scope.wealthDetailsRecordForm.holdingPeriod = 0.00;
        $scope.wealthDetailsRecordForm.netProfit = 0.00;
        $scope.wealthDetailsRecordForm.absoluteReturn = 0.0000;
        $scope.wealthDetailsRecordForm.annualizedReturn = 0.0000;
        $scope.wealthDetailsRecordForm.maturityValue = 0.00;
        $scope.wealthDetailsRecordForm.maturityDate = "";
        $scope.wealthDetailsRecordForm.lastValuationDate = new Date();
        $scope.wealthDetailsRecordForm.sipid = 0;

        subindustryidInt = 0;

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
        $scope.alternativeInvestmentsFlag = false;
        $scope.sipidFlag = false;

        $scope.dummy.assetClassType = "";
        $scope.dummy.fundHouse = "";
        $scope.dummy.name = "";
        $scope.dummy.dividendGrowth = "";
        $scope.dummy.directRegular = "";
        $scope.dummy.realEstateType = "";
        $scope.dummy.altInvstType = "";

        $scope.sips = [];

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
        $scope.alternativeInvestmentsFlag = false;
        $scope.sipidFlag = false;

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
        $scope.labelBuyDateToolTip = "Asset/Security Purchase/Transaction Date";
        $scope.labelShortName = "Short Name";
        $scope.labelShortNameToolTip = "Short Name for display purpose"
        $scope.labelFullName = "Full Name";
        $scope.labelFullNameToolTip = "Full Name/Description";
        $scope.labelCMP = "Current Market Price";
        $scope.labelCMPToolTip = "Current Market Price per unit";
        $scope.labelLastValuationDate = "Last Valuation Date";
        $scope.labelRate = "Rate";
        $scope.labelRateToolTip = "Purchase Rate per unit";

        $scope.sips = [];
    }

    $scope.deleteWealthDetailsRecord = function deleteWealthDetailsRecord(wealthDetailsRecord) {
        var result = confirm("Are you sure you want to delete this item?");
        if (result) {
            var method = "POST";
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
                $scope.wealthDetailsRecordForm.name = $scope.wealthDetailsRecordForm.shortName;
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
                if ($scope.wealthDetailsRecordForm.subindustryid == 0) {
                    $scope.wealthDetailsRecordForm.subindustryid = subindustryidInt;
                }
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
        //$scope.wealthDetailsRecordForm.holdingPeriod = $filter('number')($scope.wealthDetailsRecordForm.holdingPeriod,2);
        $scope.wealthDetailsRecordForm.netProfit = $scope.wealthDetailsRecordForm.marketValue - $scope.wealthDetailsRecordForm.totalCost;
        //$filter('number')($scope.wealthDetailsRecordForm.netProfit,2);

        console.log($scope.wealthDetailsRecordForm);

        if($scope.wealthDetailsRecordHtmlForm.$valid) {
              //console.log('Posting ...:');
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
            var index = $scope.assetClasses.findIndex(x=>x.classid === wealthDetailsRecord.assetClassid);
            if ($scope.assetClasses[index].assetClassGroup == $scope.searchAssetClass) {
                return true;
            }
        }
        return false;
    }

});

module.filter('unique', function() {
   // we will return a function which will take in a collection
   // and a keyname
   return function(collection, keyname) {
      // we define our output and keys array;
      var output = [],
          keys = [];

      // we utilize angular's foreach function
      // this takes in our original collection and an iterator function
      angular.forEach(collection, function(item) {
          // we check to see whether our object exists
          var key = item[keyname];
          // if it's not already part of our keys array
          if(keys.indexOf(key) === -1) {
              // add it to our keys array
              keys.push(key);
              // push this item to our final output array
              output.push(item);
          }
      });
      // return our array which should be devoid of
      // any duplicates
      return output;
   };
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