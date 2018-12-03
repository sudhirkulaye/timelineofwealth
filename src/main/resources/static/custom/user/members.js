var module = angular.module('MemberManagement', []);

module.controller('MemberController', function($scope, $http, $filter) {
    var urlBase="/user/api";
    $scope.members =[];
    $scope.memberForm = {
        "memberid": "-1",
        "firstName":"",
        "middleName":"",
        "lastName":"",
        "relationship":"",
        "birthDate": new Date("2000-01-01"),
        "gender":"",
        "maritalStatus":"",
        "email":"",
        "cellNo":"",
        "earningStatus": "",
        "profession":"",
        "industry":"",
        "isSecuredByPension":"",
        "education":"",
        "isFinanceProfessional":"",
        "expectedRetirementDate": new Date("2030-01-01"),
        "isAlive":"Y",
        "dateLastUpdate": new Date()
    };
    $http.defaults.headers.post["Content-Type"] = "application/json";
    $scope.hideForm = true;
    $scope.hideTable = false;
    $scope.isFormSubmit = false;
    $scope.editMode = false;
    $scope.selfEditMode = false;
    $scope.relationshipError = false;
    $scope.dateToday = new Date();

    showMembers();

    function showMembers(){
        $scope.members = new Array;
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
    }

    $scope.editMember = function editMember(member){
        $scope.hideForm = false;
        $scope.hideTable = true;
        $scope.editMode = true;
        $scope.relationshipError=false;
        if (member.relationship == "Self") {
            $scope.selfEditMode = true;
        }
        $scope.memberForm.memberid = member.memberid;
        $scope.memberForm.firstName = member.firstName;
        $scope.memberForm.middleName = member.middleName;
        $scope.memberForm.lastName = member.lastName;
        $scope.memberForm.relationship = member.relationship;
        $scope.memberForm.birthDate = new Date(member.birthDate); //$filter('date')(member.birthDate, "yyyy-MM-dd");
        $scope.memberForm.gender = member.gender;
        $scope.memberForm.maritalStatus = member.maritalStatus;
        $scope.memberForm.email = member.email;
        $scope.memberForm.cellNo = member.cellNo;
        $scope.memberForm.earningStatus = member.earningStatus;
        $scope.memberForm.profession = member.profession;
        $scope.memberForm.industry = member.industry;
        $scope.memberForm.isSecuredByPension = member.isSecuredByPension;
        $scope.memberForm.education = member.education;
        $scope.memberForm.isFinanceProfessional = member.isFinanceProfessional;
        $scope.memberForm.expectedRetirementDate = new Date(member.expectedRetirementDate);
        $scope.memberForm.isAlive = member.isAlive;
        $scope.memberForm.dateLastUpdate = new Date(member.dateLastUpdate);
//        console.log($scope.memberForm);
    }

    $scope.showClearForm = function showClearForm(){
        $scope.hideForm = false;
        $scope.hideTable = true;
        $scope.editMode = false;
        $scope.selfEditMode = false;
        $scope.relationshipError = false;
        $scope.memberForm.memberid = "-1";
        $scope.memberForm.firstName = "";
        $scope.memberForm.middleName = "";
        $scope.memberForm.lastName = "";
        $scope.memberForm.relationship = "";
        $scope.memberForm.birthDate = new Date("2000-01-01");
        $scope.memberForm.gender = "";
        $scope.memberForm.maritalStatus = "";
        $scope.memberForm.email = "";
        $scope.memberForm.cellNo = "";
        $scope.memberForm.earningStatus = "";
        $scope.memberForm.profession = "";
        $scope.memberForm.industry = "";
        $scope.memberForm.isSecuredByPension = "";
        $scope.memberForm.education = "";
        $scope.memberForm.isFinanceProfessional = "";
        $scope.memberForm.expectedRetirementDate = new Date("2000-01-01");
        $scope.memberForm.isAlive = "Y";
        $scope.memberForm.dateLastUpdate = new Date();
    }

    $scope.showTable = function showTable(){
        //showMembers();
        $scope.hideForm = true;
        $scope.hideTable = false;
    }

    $scope.processMember = function processMember(){
        var method = "";
        var url = "";
        if ($scope.editMode == false) {
             if ($scope.memberForm.relationship == "Self") {
                $scope.relationshipError = true;
             }
        }
        if($scope.memberHtmlForm.$valid && !$scope.relationshipError)
        {
              //Submit your form
              $scope.isFormSubmit = false
//            $scope.memberForm.birthDate = $filter('date')(new Date($scope.memberForm.birthDate),'yyyy-MM-dd');
//            $scope.memberForm.expectedRetirementDate = $filter('date')(new Date($scope.memberForm.expectedRetirementDate),'yyyy-MM-dd');
//            console.log($scope.memberForm);
              $scope.memberForm.dateLastUpdate = new Date();
              if ($scope.memberForm.memberid == -1) {
                  method = "POST";
                  url = "/addmember";
              } else {
                  method = "PUT";
                  url = "/updatemember";
              }
              $http({
                  method: method,
                  url: urlBase + url,
                  data: angular.toJson($scope.memberForm),
                  headers: {
                      'Content-Type': 'application/json'
                  }
              }).then(_success, _error);
        }
        else
        {
             $scope.isFormSubmit = true
        }

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

    $scope.calculateAge = function calculateAge(birthday) { // birthday is a date
        var birthday1 = new Date(birthday);
        var ageDifMs = Date.now() - birthday1.getTime();
        var ageDate = new Date(ageDifMs); // miliseconds from epoch
        return Math.abs(ageDate.getUTCFullYear() - 1970);
    }

});