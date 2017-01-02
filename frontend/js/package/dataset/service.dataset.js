/********************************************************************************************************************
 * Dataset Package - Services
 */
(function (angular) {
	"use strict";

	angular
		.module("kmeans.dataset.service.dataset", ["ngResource", "kmeans.common.service.configbucket"])
		.service("DataSet", ["$http", "$rootScope", "ConfigBucket", DataSetService])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @name DataSetService
	 * @memberof kmeans.dataset.service.dataset
	 * @ngdoc service
	 *
	 * @param {$http} $http
	 * @param {$rootScope} $rootScope
	 * @param {ConfigBucket} ConfigBucket
	 * @returns {DataSetService}
	 */
	function DataSetService($http, $rootScope, ConfigBucket) {
		var self = this;

		/**
		 * @method
		 * @public
		 * @name findClusters
		 * @param {String} setId
		 * @param {int} clusterCount
		 * @returns {$promise}
		 */
		self.findClusters = function(setId, clusterCount) {
			return $http({
				method : "POST",
				url    : ConfigBucket.apiUrl,
				data   : {
                    command: "findClusters",
                    setId: setId,
                    clusterCount: clusterCount
				}
			}).then(function(response) {
				return response.data;
			});
		}

        /**
         * @method
         * @public
         * @name addTrainingSamples
         * @param {String} setId
         * @param {Array} trainingSamples array of objects containing a single Array property 'features'
         * @returns {$promise}
         */
		self.addTrainingSamples = function(setId, trainingSamples) {
		    return $http({
		        method : "POST",
		        url    : ConfigBucket.apiUrl,
		        data   : {
		            command : "addTrainingSamples",
		            setId   : setId,
		            samples : trainingSamples
		        }
		    }).then(function(response) {
                $rootScope.$emit("samplesAdded", {trainingSetId: setId});
		    });
		}
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.dataset").requires.push("kmeans.dataset.service.dataset");

})(angular);
