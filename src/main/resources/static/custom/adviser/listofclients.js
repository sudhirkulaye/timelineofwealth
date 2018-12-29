var module = angular.module('ListOfClientsManagement', ['ui.bootstrap','angular.filter']);

module.controller('ListOfClientsController', function($scope, $http, $filter, $locale) {
    var urlBase="/adviser/api";
    $scope.clients = [];
    $scope.clientEmails = [];
    $scope.assetClasses = [];
    $scope.industries = [];
    $scope.selectedClient = "";

    showRecords();

    function showRecords(){
        $scope.clients = new Array;
        $scope.clientEmails = new Array;

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

    $scope.setSelectedClient = function(client) {
        $scope.selectedClient = client;
    }
});
