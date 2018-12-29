var module = angular.module('ConsolidatedAssetsManagement', ['ui.bootstrap','angular.filter']);

module.controller('ConsolidatedAssetsController', function($scope, $http, $filter, $locale) {
    var urlBase="/adviser/api";
    $scope.clients = [];
    $scope.clientEmails = [];
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.consolidatedAssets = [];
    $scope.selectedClient = "";
    $scope.searchAssetClass = "";

    showRecords();

    function showRecords(){
        $scope.clients = new Array;
        $scope.clientEmails = new Array;
        $scope.assetClasses = new Array;
        $scope.industries = new Array;
        $scope.consolidatedAsset = new Array;

        var url = "/getclients";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.clients = response.data;
                    var sortedRecords = $filter('orderBy')($scope.clients,['userid']);
                    var map = $filter('groupBy')(sortedRecords, 'userid');
                    for(var userid in map){
                       $scope.clientEmails.push(userid);
                    }
                    $scope.clientEmails.push("ALL");
                } else {
                    $scope.clients = [];
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

    }

    $scope.getConsolidatedAssets = function(){
        var url = "/getconsolidatedassetsofclient";
        if ($scope.selectedClient == "ALL"){
            $scope.selectedClient = "";
        }
        $http({
              method: "POST",
              url: urlBase + url,
              data: angular.toJson($scope.selectedClient),
              headers: {
                  'Content-Type': 'application/json'
              }
          }).then(_success, _error);
    }

    function _success(res) {
        if (res != undefined) {
            $scope.consolidatedAssets = res.data;
        } else {
            $scope.consolidatedAssets = [];
        }
    }

    function _error(res) {
        var data = res.data;
        var status = res.status;
        var header = res.header;
        var config = res.config;
        alert("Error: " + status + ":" + data);
    }

    $scope.setSelectedClient = function(client) {
        $scope.selectedClient = client;
        $scope.getConsolidatedAssets();
    }

    $scope.searchTransaction = function (consolidatedAsset) {
        if ($scope.searchText == undefined) {
            return true;
        } else {
            if (consolidatedAsset.shortName.toLowerCase().indexOf($scope.searchText.toLowerCase()) != -1 ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByMember = function (consolidatedAsset) {
        if ($scope.searchMember == undefined || $scope.searchMember == 0) {
            return true;
        } else {
            if (consolidatedAsset.memberid == $scope.searchMember ) {
                return true;
            }
        }
        return false;
    }

    $scope.filterByAssetClass = function (consolidatedAsset) {
        if ($scope.searchAssetClass == undefined || $scope.searchAssetClass == "") {
            return true;
        } else {
            var index = $scope.assetClasses.findIndex(x=>""+x.classid === ""+consolidatedAsset.assetClassid);
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
