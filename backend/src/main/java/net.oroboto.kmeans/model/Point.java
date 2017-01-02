package net.oroboto.kmeans.model;

import java.util.List;
import java.util.ArrayList;

public class Point
{
    List<Double> features;

    public List<Double> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<Double> features)
    {
        // Take our own copy
        this.features = new ArrayList<Double>(features);
    }

    public Point(List<Double> features)
    {
        this.features = features;
    }

    public void addFeature(double feature)
    {
        if (features == null)
        {
            features = new ArrayList();
        }

        features.add(feature);
    }

    public Double getFeature(int feature) throws Exception
    {
        if (feature >= features.size())
        {
            throw new Exception("Feature is out of range, point dimension is " + features.size());
        }

        return features.get(feature);
    }

    public int getDimension()
    {
        return features.size();
    }

    public Point()
    {
    }
}