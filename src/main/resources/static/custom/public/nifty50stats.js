var module = angular.module('IndexStatsManagement', ['angular.filter']);

module.controller('IndexStatController', function($scope, $http, $filter) {
    var urlBase = "/public/api";
    $scope.indexStatRecords = [];
    $scope.indexConsolidatedStatistics = [];
    $scope.indexMonthlyReturns = [];
    $scope.yearFilter = 1;
    $scope.months = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

    // soft green / red cell backgrounds for the calendar-returns table (value is a fraction)
    $scope.heat = function (value) {
        if (value == null || isNaN(value)) return {};
        var a = Math.min(Math.abs(value) / 0.08, 1) * 0.30 + 0.05;
        return { 'background-color': value >= 0 ? 'rgba(27,175,122,' + a.toFixed(2) + ')' : 'rgba(208,59,59,' + a.toFixed(2) + ')' };
    };

    $http.get(urlBase + "/getindexmonthlyreturns/NIFTY").then(function (response) {
        $scope.indexMonthlyReturns = (response && response.data) || [];
    });
    $http.get(urlBase + "/getindexvaluation").then(function (response) {
        $scope.indexStatRecords = (response && response.data) || [];
        drawChart();
    });
    $http.get(urlBase + "/getindexstatistics").then(function (response) {
        $scope.indexConsolidatedStatistics = (response && response.data) || [];
    });

    $scope.setYears = function (y) { $scope.yearFilter = y; drawChart(); };

    // lastUpdated arrives as [yyyy, m, d] (or an ISO string)
    $scope.updated = function () {
        var s = $scope.indexConsolidatedStatistics[0], u = s && s.lastUpdated;
        if (!u) return '';
        var d = Array.isArray(u) ? new Date(u[0], u[1] - 1, u[2]) : new Date(u);
        return isNaN(d) ? String(u) : $filter('date')(d, 'd MMM yyyy');
    };

    // daily points for the last year, weekly (Fridays) before that, as before
    function drawChart() {
        var recs = $scope.indexStatRecords;
        if (!recs || !recs.length || !window.TwChart) return;
        var filterDate = new Date(), oneYear = new Date();
        filterDate.setDate(filterDate.getDate() - $scope.yearFilter * 365);
        oneYear.setDate(oneYear.getDate() - 365);
        var dates = [], pe = [], value = [];
        recs.forEach(function (r) {
            var d = new Date(r.key.date);
            if (d <= filterDate) return;
            if (d > oneYear || d.getDay() === 5) {
                dates.push($filter('date')(d, 'yyyy-MM-dd'));
                pe.push(r.pe);
                value.push(r.value);
            }
        });
        TwChart.line('n50-pe-chart', {
            labels: dates,
            series: [
                { name: 'Trailing PE', data: pe, color: 0 },
                { name: 'NIFTY 50', data: value, axis: 1, index: true }
            ],
            yName: 'PE (x)', y2Name: 'Index',
            yFmt: function (v) { return Number(v).toFixed(1) + 'x'; },
            y2Fmt: function (v) { return TwChart.num(v, 0); },
            measure: 'value', height: 380
        });
    }
});
