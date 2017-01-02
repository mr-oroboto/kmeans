/***********************************************************************************************************************
 * TrainingSets Package - Core
 */
(function(angular) {
	"use strict";

	angular
		.module("kmeans.trainingsets", [])
	;

	/*******************************************************************************************************************
	 * DI
	 */
	angular.module("kmeans").requires.push("kmeans.trainingsets");

})(angular);
