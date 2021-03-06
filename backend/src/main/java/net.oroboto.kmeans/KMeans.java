package net.oroboto.kmeans;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.oroboto.kmeans.model.Centroid;
import net.oroboto.kmeans.model.Point;

public class KMeans
{
    protected static final int  MAX_ITERATIONS = 100;
    protected static final double COMPARISON_EPILSON = 0.001;

    protected List<Point>  trainingSamples;
    protected int          numDimensions;
    protected List<Point>  rawCentroids;       // a centroid is just a list of features

    public KMeans(List<Point> trainingSamples) throws Exception
    {
        this.trainingSamples = trainingSamples;
        if (trainingSamples.size() <= 0)
        {
            throw new Exception("Training set has no training samples");
        }

        numDimensions = trainingSamples.get(0).getDimension();

        // Ensure each training sample has the same dimensions
        for (Point trainingSample : trainingSamples)
        {
            if (trainingSample.getDimension() != numDimensions)
            {
                throw new Exception("Training sample has mismatched dimensions, got " + trainingSample.getDimension() + " but expected " + numDimensions);
            }
        }
    }

    /**
     * @return
     */
    public List<Point> getRawCentroids()
    {
        return rawCentroids;
    }

    /**
     * @param rawCentroids
     */
    public void setRawCentroids(List<Point> rawCentroids)
    {
        this.rawCentroids = rawCentroids;
    }

    /**
     * @param numClusters
     * @return Array of centroids after clustering
     */
    public List<Centroid> performClustering(int numClusters) throws Exception
    {
        // Randomly initialise K cluster centroids (each centroid is a point in numDimensions)
        // by selecting random training samples to use for each random initialisation.
        rawCentroids = getRandomlyInitialisedCentroidsForTrainingSamples(numClusters);

        int[] centroidAssignments = new int[trainingSamples.size()];

        // Repeat until centroids cease moving or we exceed our maximum iteration count
        boolean centroidsMoved = true;
        for (int i = 0; i < MAX_ITERATIONS && centroidsMoved; i++)
        {
            centroidAssignments = assignTrainingSamplesToCentroids();
            centroidsMoved = moveCentroids(centroidAssignments);
        }

        // Create a list that bundles together the centroids and the training samples assigned to them
        return createCentroidList(rawCentroids, centroidAssignments);
    }

    /**
     * @return
     */
    protected List<Centroid> createCentroidList(List<Point> rawCentroids, int[] centroidAssignments)
    {
        assert(rawCentroids != null);

        List<Centroid> centroidList = new ArrayList();

        for (int k = 0; k < rawCentroids.size(); k++)
        {
            Point rawCentroid = rawCentroids.get(k);

            Centroid centroid = new Centroid();
            centroid.setFeatures(rawCentroid.getFeatures());

            for (int i = 0; i < trainingSamples.size(); i++)
            {
                if (centroidAssignments[i] == k)
                {
                    centroid.addTrainingSample(trainingSamples.get(i));
                }
            }

            centroidList.add(centroid);
        }

        return centroidList;
    }

    /**
     * @return Array where the position of each element represents a corresponding training sample and the value represents a centroid (0..k-1)
     */
    protected int[] assignTrainingSamplesToCentroids() throws Exception
    {
        int[] assignments = new int[trainingSamples.size()];     // each element in trainingSamples has a corresponding element here

        for (int i = 0; i < trainingSamples.size(); i++)
        {
            // Find the centroid closest to this training sample
            Point sample = trainingSamples.get(i);
            int closestCentroidIndex = 0;
            Double smallestDistance = distanceBetweenPoints(sample, rawCentroids.get(closestCentroidIndex));

            for (int k = 1; k < rawCentroids.size(); k++)
            {
                Double distance = distanceBetweenPoints(sample, rawCentroids.get(k));
                if (distance < smallestDistance)
                {
                    closestCentroidIndex = k;
                    smallestDistance = distance;
                }
            }

            assignments[i] = closestCentroidIndex;
        }

        return assignments;
    }

    /**
     * @param a First point
     * @param b Second point
     * @return Distance between the two points across all dimensions
     */
    protected Double distanceBetweenPoints(Point a, Point b) throws Exception
    {
        Double sum = 0.0;

        assert(a.getDimension() == b.getDimension());

        for (int n = 0; n < a.getDimension(); n++)
        {
            Double featureA = a.getFeature(n);
            Double featureB = b.getFeature(n);

            sum += Math.pow(featureA - featureB, 2);
        }

        return sum;
    }

    /**
     * "Move" the centroids (recalculate their current co-ordinates) based on the mean of each feature of
     * each training sample currently assigned to the centroid.
     *
     * @param centroidAssignments The current centroid assignments for each training sample
     * @return Whether or not the operation caused the centroids to move.
     */
    protected boolean moveCentroids(int[] centroidAssignments) throws Exception
    {
        boolean centroidsMoved = false;

        // @todo: update this so rawCentroids is taken as a parameter

        // Calculate new centroid location based on mean of all training samples assigned to the centroid
        int k = 0;
        for (Iterator<Point> iterator = rawCentroids.iterator(); iterator.hasNext(); k++)
        {
            Point rawCentroid = iterator.next();

            int numTrainingSamplesAssigned = 0;
            Double[] means = new Double[numDimensions];      // @dragon: KMeans only supports numeric features

            for (int i = 0; i < trainingSamples.size(); i++)
            {
                if (centroidAssignments[i] == k)
                {
                    // This training sample is assigned to centroid k, take it into account for the new mean
                    numTrainingSamplesAssigned++;

                    for (int feature = 0; feature < numDimensions; feature++)
                    {
                        if (means[feature] == null)
                        {
                            means[feature] = new Double(0.0);
                        }

                        means[feature] += trainingSamples.get(i).getFeature(feature);
                    }
                }
            }

            // If no training samples were assigned to the centroid it should be dropped
            if (numTrainingSamplesAssigned == 0)
            {
                iterator.remove();
                centroidsMoved = true;
                continue;
            }

            // All training samples have been parsed for this centroid, set its new co-ordinates (features)
            List<Double> centroidFeatures = rawCentroid.getFeatures();
            List<Double> centroidNewFeatures = new ArrayList();
            boolean thisCentroidMoved = false;

            for (int feature = 0; feature < numDimensions; feature++)
            {
                Double newFeature = means[feature] / numTrainingSamplesAssigned;
                double currentFeature = centroidFeatures.get(feature);

                centroidNewFeatures.add(newFeature);

                // Use comparison epsilon to determine change to deal with doubles
                if (Math.abs(newFeature - currentFeature) > COMPARISON_EPILSON)
                {
                    thisCentroidMoved = centroidsMoved = true;
                }
            }

            if (thisCentroidMoved)
            {
                rawCentroid.setFeatures(centroidNewFeatures);
            }
        }

        return centroidsMoved;
    }

    /**
     * @param numCentroids
     * @return
     */
    protected List<Point> getRandomlyInitialisedCentroidsForTrainingSamples(int numCentroids)
    {
        List<Point> centroids = new ArrayList();
        Random randomGenerator = new Random();

        for (int i = 0; i < numCentroids; i++)
        {
            Point prototypeSample = trainingSamples.get(randomGenerator.nextInt(trainingSamples.size()));

            // Create a new Point based on the prototype
            Point centroid = new Point();
            centroid.setFeatures(prototypeSample.getFeatures());

            centroids.add(centroid);
        }

        return centroids;
    }

}