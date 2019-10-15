var module = angular.module('GeneratePDFReportManagement', ['ui.bootstrap','angular.filter']);

module.controller('GeneratePDFReportController', function($scope, $http, $filter, $locale) {
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

        var url = "/getpmsclients";
        $http.get(urlBase + url).
            then(function (response) {
                if (response != undefined) {
                    $scope.clients = response.data;
                    /*var sortedRecords = $filter('orderBy')($scope.clients,['userid']);
                    var map = $filter('groupBy')(sortedRecords, 'userid');
                    for(var userid in map){
                       $scope.clientEmails.push(userid);
                    }
                    $scope.clientEmails.push("ALL");*/
                } else {
                    $scope.clients = [];
                }
            });

    }

    $scope.setSelectedClient = function(client) {
        $scope.selectedClient = client;
    }
});
