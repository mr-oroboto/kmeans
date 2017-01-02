package net.oroboto.kmeans;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import net.oroboto.kmeans.model.Centroid;
import net.oroboto.kmeans.model.Point;

public class TestCentroid
{
    protected static final double COMPARISON_EPILSON = 0.001;

    @Test
    public void testConstructor()
    {
        // Test with default properties
        Centroid centroid = new Centroid();
        assertNull(centroid.getFeatures());
        assertNull(centroid.getTrainingSamples());

        // Test with supplied properties
        List<Double> features = new ArrayList();
        features.add(1.0);
        List<Point> trainingSamples = new ArrayList();
        centroid = new Centroid(features, trainingSamples);
        assertNotNull(centroid.getFeatures());
        assertNotNull(centroid.getTrainingSamples());
        assertSame(centroid.getFeatures(), features);
        assertSame(centroid.getTrainingSamples(), trainingSamples);
        assertArrayEquals(centroid.getFeatures().toArray(), features.toArray());
    }

    @Test
    public void testAddTrainingSample()
    {
        Centroid centroid = new Centroid();
        assertNull(centroid.getTrainingSamples());

        Point trainingSample = new Point();
        trainingSample.addFeature(100.0);
        trainingSample.addFeature(200.0);
        centroid.addTrainingSample(trainingSample);
        assertNotNull(centroid.getTrainingSamples());
        assertEquals(centroid.getTrainingSamples().size(), 1);

        List<Point> trainingSamples = centroid.getTrainingSamples();
        List<Double> trainingSample1Features = trainingSamples.get(0).getFeatures();
        assertTrue(((double)trainingSample1Features.get(0) >= 100.0 - COMPARISON_EPILSON) && ((double)trainingSample1Features.get(0) <= 100.0 + COMPARISON_EPILSON));
        assertTrue(((double)trainingSample1Features.get(1) >= 200.0 - COMPARISON_EPILSON) && ((double)trainingSample1Features.get(0) <= 200.0 + COMPARISON_EPILSON));
    }
}