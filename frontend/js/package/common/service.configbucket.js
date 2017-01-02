/********************************************************************************************************************
 * Common Package - Services
 */
(function (angular) {
	"use strict";

	angular
		.module("kmeans.common.service.configbucket", [])
		.service("ConfigBucket", [ConfigBucketService])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @name ConfigBucketService
	 * @memberof kmeans.common.service.configbucket
	 * @ngdoc service
	 *
	 * @returns {ConfigBucket}
	 */
	function ConfigBucketService() {
		var self = this;

		/**
		 * @type {string}
		 */
		self.apiUrl = "https://l9wv40guy2.execute-api.ap-southeast-2.amazonaws.com/prod/test";

		/***************************************************************************************************************
		 * Service Methods
		 */
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.common").requires.push("kmeans.common.service.configbucket");

})(angular);
