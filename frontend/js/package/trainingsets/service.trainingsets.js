/********************************************************************************************************************
 * TrainingSets Package - Services
 */
(function (angular) {
	"use strict";

	angular
		.module("kmeans.trainingsets.service.trainingsets", ["ngResource", "kmeans.common.service.configbucket"])
		.service("TrainingSets", ["$http", "ConfigBucket", TrainingSetsService])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @name TrainingSetsService
	 * @memberof kmeans.common.service.trainingsets
	 * @ngdoc service
	 *
	 * @param {$http} $http
	 * @param {ConfigBucket} ConfigBucket
	 * @returns {TrainingSetsService}
	 */
	function TrainingSetsService($http, ConfigBucket) {
		var self = this;

		/**
		 * @method
		 * @public
		 * @name getTrainingSets
		 * @param {String} setId
		 * @returns {$promise}
		 */
		self.getTrainingSets = function(setId) {
			return $http({
				method : "POST",
				url    : ConfigBucket.apiUrl,
				data   : {
                    command: "getTrainingSets"
				}
			}).then(function(response) {
				return response.data;
			});
		}
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.trainingsets").requires.push("kmeans.trainingsets.service.trainingsets");

})(angular);
