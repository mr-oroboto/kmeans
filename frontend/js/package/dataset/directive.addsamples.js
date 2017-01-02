/********************************************************************************************************************
 * Dataset Package - "Add Samples" Directive
 */
(function (angular) {
	"use strict";

	angular
		.module("kmeans.dataset.directive.addsamples", ["kmeans.dataset.service.dataset"])
		.directive("kmeansAddSamples", ["DataSet", "$rootScope", AddSamples])
	;

	/********************************************************************************************************************
	 * Methods
	 */

	/**
     * @desc Can be used to add samples to an existing dataset or create a new dataset. The specified
     *       scope properties "setId" and "creatingNewSet" determine which behaviour is used.
     *
	 * @constructor
	 * @memberOf kmeans.dataset.directive
	 * @param {DataSet} DataSet
	 * @param {$rootScope} $rootScope
	 * @returns {{restrict: string, template: string, scope: {show: string}, link: function}}
	 */
	function AddSamples(DataSet, $rootScope) {
		function link(scope, element, attrs) {
		    scope.trainingSamplesCSV = "";

			/**
			 * @desc Is the AJAX request to add samples in flight?
			 * @type {boolean}
			 */
			scope.addingSamples = false;

			$rootScope.$on("clustersRendered", function(event, data) {
			    // Samples have been added, re-enable interface
			    scope.addingSamples = false;
			});

			/**
			 * @type function
			 */
			scope.addSamples = function() {
			    if (scope.trainingSamplesCSV.length == 0) {
                    alert("Please enter samples as a list of comma-separated features with one sample per line, ie:\n\n10.5,12.2\n16.1,18.2");
			        return;
			    }

			    var lines = [], features = [], cleanFeatures = [], samples = [],
			        i, j, feature,
			        decimalRegex = /^[-+]?[0-9]+\.[0-9]+$/;

                // Parse trainingSampleCSV into array of objects each containing a single property 'features', itself an array of decimals
                lines = scope.trainingSamplesCSV.split("\n");

                for (i = 0; i < lines.length; i++) {
                    cleanFeatures = [];

                    features = lines[i].split(",");
                    if (features.length != 2) {
                        alert("Training sample " + (i+1) + " is not two-dimensional, this interface only supports 2D features");
                        return;
                    }

                    for (j = 0; j < features.length; j++) {
                        feature = features[j].trim();
                        if ( ! feature.match(decimalRegex)) {
                            alert("Feature '" + feature + "' in sample " + (i+1) + " is not a valid decimal, please enter samples as with at least one decimal place of precision (ie. 12.1)");
                            return;
                        }

                        cleanFeatures.push(parseFloat(feature));
                    }

                    samples.push({features: cleanFeatures});
                }

                if (samples.length != 0) {
                    scope.addingSamples = true;
                    DataSet.addTrainingSamples(scope.setId, samples);
                } else {
                    alert("Could not parse training samples, please ensure it is a list of comma-separated features with one sample per line, ie:\n\n10.5,12.2\n16.1,18.2");
                }
			};
		}

		return {
			restrict: "E",
			templateUrl: "partials/common/addsamples.html",
			scope: {
				setId: "=",
				creatingNewSet: "="
			},
			link: link
		};
	}

	/********************************************************************************************************************
	 * DI
	 */

	angular.module("kmeans.dataset").requires.push("kmeans.dataset.directive.addsamples");

})(angular);
