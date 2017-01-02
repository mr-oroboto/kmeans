/********************************************************************************************************************
 * Dataset Package - DataSet Controller
 */

(function(angular) {
	"use strict";

	angular
		.module("kmeans.dataset.controller.dataset", ["kmeans.dataset.service.dataset"])
		.controller("DataSetCtrl", ["DataSet", "$scope", "$rootScope", "$log", DataSetCtrl])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
	 * @constructor
	 * @name DataSetCtrl
	 * @memberof kmeans.dataset.controller
	 * @ngdoc controller
	 *
	 * @param {DataSet} DataSet
	 * @param {$scope} $scope
	 * @param {$rootScope} $rootScope
	 * @param {$log} $log
	 */
	function DataSetCtrl(DataSet, $scope, $rootScope, $log) {
		var vm = this;

        vm.loading = true;

        /**
         * @desc The name of the currently selected dataset
         */
        vm.setId = '';

        /**
         * @desc The number of clusters to look for in current dataset (eventually expose via UI)
         */
        vm.clusterCount = 2;

        /**
         * @desc The number of clusters found in the current dataset
         */
        vm.clusterCountFound = 0;

        /**
         * @desc ChartJS backing data
         */
		vm.series = [];
		vm.data = [];

		$scope.$watch("trainingSets.selectedSet", function(trainingSetId) {
		    // Find clusters in any newly selected dataset (debounced: only if it changes)
		    if (trainingSetId != null && trainingSetId != vm.setId) {
		        findClusters(trainingSetId, vm.clusterCount);
		    }
		});

		$scope.$watch("dataset.clusterCount", function(clusterCount) {
		    if (clusterCount >= 1 && clusterCount != vm.clousterCount) {
		        vm.clusterCount = clusterCount;
		        findClusters(vm.setId, clusterCount);
		    }
		});

		$rootScope.$on("samplesAdded", function(event, data) {
		    // Find clusters once new samples have been added to the current dataset
            findClusters(data.trainingSetId, vm.clusterCount);
		});

		/***************************************************************************************************************
		 * Controller Methods
		 */

		/**
		 * @desc Find clusters in the specified dataset
		 */
		function findClusters(setId, clusterCount) {
		    vm.loading = true;
		    vm.setId = setId;

			DataSet.findClusters(setId, clusterCount).then(function(clusters) {
				var i, j, cluster, chartData = [], chartSeries = [];

				vm.clusterCountFound = clusters.length;
				vm.data = [];

                // Each cluster is modelled as a separate series so it will have its own colour
				for (i = 0; i < clusters.length; i++) {
				    cluster = clusters[i];

                    $log.debug("Cluster " + (i+1) + " has centroid at " + cluster.features[0] + "," + cluster.features[1]);

				    chartSeries[i] = "Cluster " + (i+1);
				    chartData[i] = [];

                    // @dragon: This assumes a 2-dimensional dataset!

                    // The 'features' array is an n-dimensional point that specifies the centroid location
                    chartData[i].push({
                        x: cluster.features[0],
                        y: cluster.features[1],
                        r: 15
                    });

                    // The 'trainingSamples' array contains the training samples assigned to the cluster
                    for (j = 0; j < cluster.trainingSamples.length; j++) {
                        $log.debug(" (" + cluster.trainingSamples[j].features[0] + "," + cluster.trainingSamples[j].features[1] + ")");

                        chartData[i].push({
                            x: cluster.trainingSamples[j].features[0],
                            y: cluster.trainingSamples[j].features[1],
                            r: 5
                        });
                    }

                    vm.data = chartData;
                    vm.series = chartSeries;
                    vm.loading = false;

                    $rootScope.$emit("clustersRendered", vm.setId);
                }
            });
		}
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.dataset").requires.push("kmeans.dataset.controller.dataset");

})(angular);
