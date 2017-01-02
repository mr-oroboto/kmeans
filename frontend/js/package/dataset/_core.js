/***********************************************************************************************************************
 * Dataset Package - Core
 */
(function(angular) {
	"use strict";

	angular
		.module("kmeans.dataset", [])
	;

	/*******************************************************************************************************************
	 * DI
	 */
	angular.module("kmeans").requires.push("kmeans.dataset");

})(angular);
