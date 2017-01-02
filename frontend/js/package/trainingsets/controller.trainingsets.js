/********************************************************************************************************************
 * TrainingSets Package - Controller
 */

(function(angular) {
	"use strict";

	angular
		.module("kmeans.trainingsets.controller.trainingsets", ["kmeans.trainingsets.service.trainingsets"])
		.controller("TrainingSetsCtrl", ["TrainingSets", "$rootScope", TrainingSetsCtrl])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @name TrainingSetsCtrl
	 * @memberof kmeans.trainingsets.controller
	 * @ngdoc controller
	 *
	 * @param {TrainingSets} TrainingSets
	 * @param {$rootScope} $rootScope
	 */
	function TrainingSetsCtrl(TrainingSets, $rootScope) {
		var vm = this;

        vm.loading = true;

        /**
         * @desc Known training set names
         */
		vm.trainingSets = [];

		/**
		 * @desc Name of the currently selected training set
		 */
		vm.selectedSet = null;

        /**
	     * @type {function}
         */
		vm.createNewTrainingSet = createNewTrainingSet;

		$rootScope.$on("samplesAdded", function(event, data) {
		    // Reload training sets if we don't have this training set
		    if (vm.trainingSets.indexOf(data.trainingSetId) < 0) {
		        reloadTrainingSets(data.trainingSetId);
		    }
		});

		activate();

		/***************************************************************************************************************
		 * Controller Methods
		 */

		/**
		 * @desc init
		 */
		function activate() {
		    reloadTrainingSets(null);
		}

		/**
		 * @desc Load all training set names and then select the specified one
		 * @param {String} Name of the training set to select after loading all of them
		 */
		function reloadTrainingSets(selectedSet) {
		    vm.loading = true;
		    vm.trainingSets = [];
		    vm.selectedSet = null;

			TrainingSets.getTrainingSets().then(function(trainingSets) {
			    var i, selectedSetIndex = 0;

			    for (i = 0; i < trainingSets.length; i++) {
			        vm.trainingSets.push({
			            id: trainingSets[i],
			            name: trainingSets[i]
			        });

			        if (trainingSets[i] == selectedSet) {
			            selectedSetIndex = i;
			        }
			    }

                vm.selectedSet = vm.trainingSets[selectedSetIndex].id;
                vm.loading = false;
            });
		}

        /**
         * @desc Create a new training set
         */
		function createNewTrainingSet() {
		    vm.selectedSet = null;
		}
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.trainingsets").requires.push("kmeans.trainingsets.controller.trainingsets");

})(angular);
