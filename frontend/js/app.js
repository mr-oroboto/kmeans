(function(angular) {
	angular.module("kmeans", ["chart.js", "ui.bootstrap"])
		.controller("AppCtrl", [AppCtrl])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @memberof kmeans
	 * @name AppCtrl
	 * @ngdoc controller
	 */
	function AppCtrl() {
        Chart.defaults.global.colors = [ '#0000FF', '#00FFFF', '#0000FF', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'];
	}

})(angular);