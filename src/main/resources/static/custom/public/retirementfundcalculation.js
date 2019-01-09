var module = angular.module('RetirementFundCalculationManagement', []);

module.controller('RetirementFundCalculationController', function($scope, $http) {
    $scope.returnsAssumed = 8.5;
    $scope.inflationAssumed = 4.5;
    $scope.spendingNeedAssumed = 10;
    $scope.yearsToRetire = 5;
    var spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(( 1 + ( $scope.inflationAssumed / 100)),($scope.yearsToRetire+1)));
    $scope.fundRequired = spendingAtRetirement / (($scope.returnsAssumed/100) - ($scope.inflationAssumed/100));

    spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(1.03,($scope.yearsToRetire+1)));
    $scope.minFundRequired = spendingAtRetirement / (0.105 - 0.03);

    spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(1.06,($scope.yearsToRetire+1)));
    $scope.maxFundRequired = spendingAtRetirement / (0.08 - 0.06);

    $scope.getRetirementFundAmount = function() {
        spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(( 1 + ( $scope.inflationAssumed / 100)),($scope.yearsToRetire+1)));
        $scope.fundRequired = spendingAtRetirement / (($scope.returnsAssumed/100) - ($scope.inflationAssumed/100));

        spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(1.03,($scope.yearsToRetire+1)));
        $scope.minFundRequired = spendingAtRetirement / (0.105 - 0.03);

        spendingAtRetirement =  $scope.spendingNeedAssumed * (Math.pow(1.06,($scope.yearsToRetire+1)));
        $scope.maxFundRequired = spendingAtRetirement / (0.08 - 0.06);
    }
});
