package net.oroboto.kmeans.apigateway;

import java.util.List;

/**
 * @desc AWS API Gateway Response
 *
 * This class is deserialised from JSON by our request handler. API Gateway has been configured
 * with an "Integration type" of "Lambda Function" and *without* the "Use Lambda Proxy integration"
 * option.
 */
public class APIGatewayRequest
{
    String command;
    String setId;
    int clusterCount;
    List<APIGatewayRequestSample> samples;

    public void setCommand(String command) { this.command = command; }

    public String getCommand() { return command; }

    public String getSetId()
    {
        return setId;
    }

    public void setSetId(String setId)
    {
        this.setId = setId;
    }

    public void setClusterCount(int clusterCount) { this.clusterCount = clusterCount; }

    public int getClusterCount() { return this.clusterCount; }

    public void setSamples(List<APIGatewayRequestSample> samples) { this.samples = samples; }

    public List<APIGatewayRequestSample> getSamples() { return samples; }

    public APIGatewayRequest(String command, String setId, int clusterCount, List<APIGatewayRequestSample> samples)
    {
        this.command = command;
        this.setId = setId;
        this.clusterCount = clusterCount;
        this.samples = samples;
    }

    public APIGatewayRequest()
    {
    }
}