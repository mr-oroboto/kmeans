package net.oroboto.kmeans.apigateway;

import java.util.List;

/**
 * @desc Encapsulates a single training sample in an API request.
 */
public class APIGatewayRequestSample
{
    List features;

    public List getFeatures() { return features; }

    public void setFeatures(List features) { this.features = features; }

    public APIGatewayRequestSample(List features)
    {
        this.features = features;
    }

    public APIGatewayRequestSample()
    {
    }
}