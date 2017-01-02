package net.oroboto.kmeans.apigateway;

import java.util.Map;
import java.util.HashMap;

/**
 * @desc AWS API Gateway Response
 *
 * This class is serialised to JSON and returned from our request handler. API Gateway has been
 * configured with an "Integration type" of "Lambda Function" and *without* the
 * "Use Lambda Proxy integration" option.
 *
 * The API Gateway output integration response has been configured with an explicit header
 * mapping for Access-Control-Allow-Origin such that we can specify the allowed CORS origins.
 */
public class APIGatewayResponse
{
    int     statusCode;             // HTTP response code
    String  body;                   // needs to be JSON
    Map<String, String> headers;    // required to return CORS headers

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public Map<String, String> getHeaders() { return headers; }

    public APIGatewayResponse(int statusCode, String body)
    {
        this.statusCode = statusCode;
        this.body = body;

        this.headers = new HashMap<String, String>();
        this.headers.put("Access-Control-Allow-Origin", "*");
    }

    public APIGatewayResponse()
    {
        this.headers = new HashMap<String, String>();
        this.headers.put("Access-Control-Allow-Origin", "*");
    }
}