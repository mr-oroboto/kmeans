package net.oroboto.kmeans.model;

import java.util.List;
import java.util.ArrayList;

public class Centroid
{
    List<Double> features;
    List<Point> trainingSamples;

    public List<Double> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<Double> features)
    {
        this.features = features;
    }

    public List<Point> getTrainingSamples()
    {
        return trainingSamples;
    }

    public void setTrainingSamples(List<Point> trainingSamples)
    {
        this.trainingSamples = trainingSamples;
    }

    public void addTrainingSample(Point trainingSample)
    {
        if (this.trainingSamples == null)
        {
            this.trainingSamples = new ArrayList();
        }

        this.trainingSamples.add(trainingSample);
    }

    public Centroid(List<Double> features, List<Point> trainingSamples)
    {
        this.features = features;
        this.trainingSamples = trainingSamples;
    }

    public Centroid()
    {
    }
}