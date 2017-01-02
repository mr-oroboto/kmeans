package net.oroboto.kmeans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import net.oroboto.kmeans.KMeans;
import net.oroboto.kmeans.model.Centroid;
import net.oroboto.kmeans.model.Point;

public class TestKMeans
{
    protected static final double COMPARISON_EPILSON = 0.001;

    @Test(expected = Exception.class)
    public void testEmptyTrainingSampleListConstructor() throws Exception
    {
        List<Point> emptyTrainingSampleList = new ArrayList();
        KMeans kmeans = new KMeans(emptyTrainingSampleList);
    }

    @Test(expected = Exception.class)
    public void testMismatchedTrainingSampleDimensionsConstructor() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();
        Point trainingSample1 = new Point();
        trainingSample1.addFeature(1);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(1);
        trainingSample2.addFeature(2);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);

        KMeans kmeans = new KMeans(trainingSamples);
    }

    @Test
    public void testConstructor() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();
        Point trainingSample = new Point();
        trainingSample.addFeature(1);
        trainingSamples.add(trainingSample);

        KMeans kmeans = new KMeans(trainingSamples);
    }

    @Test
    public void testPerformClustering() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);
        trainingSample1.addFeature(20);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(-30);
        trainingSample2.addFeature(10);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(30);
        trainingSample3.addFeature(-20);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        List<Centroid> clusters = kmeans.performClustering(2);
        assertTrue(clusters.size() != 0 && clusters.size() <= 2);

        // Ensure we ended up at a local minima for each cluster
        for (int k = 0; k < clusters.size(); k++)
        {
            // Ensure each training sample assigned to this cluster's centroid is no closer to any other centroid
            Centroid centroid = clusters.get(k);
            List<Point> clusterSamples = centroid.getTrainingSamples();
            Point centroidPoint = new Point(centroid.getFeatures());

            for (int i = 0; i < clusterSamples.size(); i++)
            {
                Point assignedTrainingSample = clusterSamples.get(i);
                Double distanceToCentroid = kmeans.distanceBetweenPoints(assignedTrainingSample, centroidPoint);

                for (int otherCentroidIndex = 0; otherCentroidIndex < clusters.size(); otherCentroidIndex++)
                {
                    if (otherCentroidIndex == k)
                    {
                        continue;           // skip the current centroid
                    }

                    Centroid otherCentroid = clusters.get(otherCentroidIndex);
                    Point otherCentroidPoint = new Point(otherCentroid.getFeatures());
                    assertTrue(distanceToCentroid <= kmeans.distanceBetweenPoints(assignedTrainingSample, otherCentroidPoint));
                }
            }
        }
    }

    /**
     * @desc Training samples should not change when finding clusters: we should always find
     *       the samples we put in after clusters have been found. This is a regression test
     *       for a bug where assigning initial centroid locations based on a sample was causing
     *       the sample to move when the centroid did.
     *
     * @throws Exception
     */
    @Test
    public void testPerformClusteringEnsuringImmutableTrainingSamples() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);
        trainingSample1.addFeature(10);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(10);
        trainingSample2.addFeature(10);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(10);
        trainingSample3.addFeature(10);

        Point trainingSample4 = new Point();
        trainingSample4.addFeature(15);
        trainingSample4.addFeature(15);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);
        trainingSamples.add(trainingSample4);

        KMeans kmeans = new KMeans(trainingSamples);

        List<Centroid> clusters = kmeans.performClustering(2);
        assertTrue(clusters.size() != 0 && clusters.size() <= 2);

        int found10s = 0, found15s = 0;

        for (int k = 0; k < clusters.size(); k++)
        {
            Centroid centroid = clusters.get(k);
            List<Point> clusterSamples = centroid.getTrainingSamples();

            for (int i = 0; i < clusterSamples.size(); i++)
            {
                Point assignedTrainingSample = clusterSamples.get(i);

                if (assignedTrainingSample.getFeature(0) == 10 && assignedTrainingSample.getFeature(1) == 10)
                {
                    found10s++;
                }
                else if (assignedTrainingSample.getFeature(0) == 15 && assignedTrainingSample.getFeature(1) == 15)
                {
                    found15s++;
                }
                else
                {
                    assertTrue("Found unexpected point: " + assignedTrainingSample.getFeature(0) + "," + assignedTrainingSample.getFeature(1), false);
                }
            }
        }

        assertEquals("Found incorrect number of 10 points", 3, found10s);
        assertEquals("Found incorrect number of 15 points", 1, found15s);
    }

    @Test
    public void testGetRandomlyInitialisedCentroidsForTrainingSamples() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(20);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(30);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        List<Point> centroids = kmeans.getRandomlyInitialisedCentroidsForTrainingSamples(2);
        assertEquals(2, centroids.size());

        Point centroid1 = centroids.get(0);
        Point centroid2 = centroids.get(1);

        // The random initialisation method assigns centroids to existing training samples so we
        // should see that the first and second centroid has a position equal to that of one of
        // the training samples but not a position not occupied by a training sample.
        assertTrue(centroid1.getFeature(0) == 10.0 || centroid1.getFeature(0) == 20.0 || centroid1.getFeature(0) == 30.0);
        assertTrue(centroid2.getFeature(0) == 10.0 || centroid2.getFeature(0) == 20.0 || centroid2.getFeature(0) == 30.0);
        assertFalse(centroid2.getFeature(0) == 40.0 || centroid2.getFeature(0) == 50.0 || centroid2.getFeature(0) == 60.0);
    }

    @Test
    public void testAssignTrainingSamplesToCentroids() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(5);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(50);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(100);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        // Set the centroids directly for testing
        List<Point> centroids = new ArrayList();
        Point centroid1 = new Point();
        centroid1.addFeature(-10);                              // trainingSample1 should be assigned to this
        Point centroid2 = new Point();
        centroid2.addFeature(26);                               // trainingSample2 and trainingSample3 should be assigned to this
        centroids.add(centroid1);
        centroids.add(centroid2);
        kmeans.setRawCentroids(centroids);

        // Assign the training samples to the centroids so that the centroids do not need to move
        int[] centroidAssignments = kmeans.assignTrainingSamplesToCentroids();
        assertEquals(0, centroidAssignments[0]);
        assertEquals(1, centroidAssignments[1]);
        assertEquals(1, centroidAssignments[2]);
    }

    @Test
    public void testAssignTrainingSamplesToCentroidsMultiDimensional() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);
        trainingSample1.addFeature(20);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(-30);
        trainingSample2.addFeature(10);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(30);
        trainingSample3.addFeature(-20);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        // Set the centroids directly for testing
        List<Point> centroids = new ArrayList();
        Point centroid1 = new Point();
        centroid1.addFeature(0);                                // trainingSample1 and trainingSample2 should be assigned to this
        centroid1.addFeature(20);
        Point centroid2 = new Point();
        centroid2.addFeature(30);                               // trainingSample3 should be assigned to this
        centroid2.addFeature(-10);
        centroids.add(centroid1);
        centroids.add(centroid2);
        kmeans.setRawCentroids(centroids);

        // Assign the training samples to the centroids so that the centroids do not need to move
        int[] centroidAssignments = kmeans.assignTrainingSamplesToCentroids();
        assertEquals(0, centroidAssignments[0]);
        assertEquals(0, centroidAssignments[1]);
        assertEquals(1, centroidAssignments[2]);
    }

    @Test
    public void testMoveCentroidsNoMovement() throws Exception
    {
        /**
         * "Moving" the centroids recalculates the value for each centroid feature based on the mean of
         * the training samples currently assigned to it.
         */

        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(10);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(100);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        // Set the centroids directly for testing
        List<Point> centroids = new ArrayList();
        centroids.add(trainingSample1);
        centroids.add(trainingSample3);
        kmeans.setRawCentroids(centroids);

        // Assign the training samples to the centroids so that the centroids do not need to move
        int[] centroidAssignments = new int[trainingSamples.size()];
        centroidAssignments[0] = 0;     // trainingSample1 assigned to centroid 0
        centroidAssignments[1] = 0;     // trainingSample2 assigned to centroid 0
        centroidAssignments[2] = 1;     // trainingSample3 assigned to centroid 1

        assertFalse(kmeans.moveCentroids(centroidAssignments));
    }

    @Test
    public void testMoveCentroidsWithMovement() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(50);

        Point trainingSample3 = new Point();
        trainingSample3.addFeature(100);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);
        trainingSamples.add(trainingSample3);

        KMeans kmeans = new KMeans(trainingSamples);

        // Set the centroids directly for testing
        List<Point> centroids = new ArrayList();
        centroids.add(trainingSample1);                         // position: 10
        centroids.add(trainingSample3);                         // position: 100
        kmeans.setRawCentroids(centroids);

        // Assign the training samples to the centroids so that the centroids do not need to move
        int[] centroidAssignments = new int[trainingSamples.size()];
        centroidAssignments[0] = 0;     // trainingSample1 assigned to centroid 0
        centroidAssignments[1] = 1;     // trainingSample2 assigned to centroid 1
        centroidAssignments[2] = 1;     // trainingSample3 assigned to centroid 1

        assertTrue(kmeans.moveCentroids(centroidAssignments));

        // The second centroid should have moved slightly closer to the second training sample at position 50
        centroids = kmeans.getRawCentroids();
        Point centroid = centroids.get(1);
        assertTrue((double)centroid.getFeature(0) == 75.0);
    }

    @Test
    public void testDistanceBetweenPoints() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(50);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);

        KMeans kmeans = new KMeans(trainingSamples);

        double distance = kmeans.distanceBetweenPoints(trainingSample1, trainingSample2);
        assertEquals(1600.0, distance, 0.01);
    }

    @Test
    public void testDistanceBetweenPointsMultiDimensional() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);
        trainingSample1.addFeature(100);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(50);
        trainingSample2.addFeature(20);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);

        KMeans kmeans = new KMeans(trainingSamples);

        double distance = kmeans.distanceBetweenPoints(trainingSample1, trainingSample2);
        assertEquals(8000.0, distance, 0.01);
    }

    @Test
    public void testMoveCentroidsWithEmptyCentroid() throws Exception
    {
        List<Point> trainingSamples = new ArrayList();

        Point trainingSample1 = new Point();
        trainingSample1.addFeature(10);
        trainingSample1.addFeature(100);

        Point trainingSample2 = new Point();
        trainingSample2.addFeature(10);
        trainingSample2.addFeature(100);

        trainingSamples.add(trainingSample1);
        trainingSamples.add(trainingSample2);

        for (int i = 0; i < 100; i++)
        {
            KMeans kmeans = new KMeans(trainingSamples);

            // Set up the raw centroid list manually so all training samples are assigned to the first centroid
            // Set the centroids directly for testing
            List<Point> centroids = new ArrayList();
            Point centroid1 = new Point();
            centroid1.addFeature(20);
            centroid1.addFeature(100);
            Point centroid2 = new Point();
            centroid2.addFeature(-100);
            centroid2.addFeature(-100);
            centroids.add(centroid2);
            kmeans.setRawCentroids(centroids);

            int[] centroidAssignments = kmeans.assignTrainingSamplesToCentroids();
            assertEquals(0, centroidAssignments[0]);
            assertEquals(0, centroidAssignments[1]);

            // Move the centroids, all samples should end up being assigned to the first centroid
            assertTrue(kmeans.moveCentroids(centroidAssignments));

            List<Centroid> centroidList = kmeans.createCentroidList(kmeans.getRawCentroids(), centroidAssignments);
            assertEquals(1, centroidList.size());

            Centroid centroid = centroidList.get(0);
            assertEquals(2, centroid.getTrainingSamples().size());
        }
    }
}