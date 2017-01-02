package net.oroboto.kmeans;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.regions.Regions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.math.BigDecimal;

import net.oroboto.kmeans.apigateway.APIGatewayRequest;
import net.oroboto.kmeans.apigateway.APIGatewayRequestSample;
import net.oroboto.kmeans.apigateway.APIGatewayResponse;

import net.oroboto.kmeans.model.Centroid;
import net.oroboto.kmeans.model.Point;

public class RequestHandler
{
    protected LambdaLogger logger;
    protected DynamoDB dynamoDB;

    /**
     * @desc Entry point. All calls from AWS API Gateway end up here.
     *
     * @param requestStream
     * @param responseStream
     * @param context
     * @throws Exception
     */
    public void handleRequest(InputStream requestStream, OutputStream responseStream, Context context) throws Exception
    {
        logger = context.getLogger();

        /**
         * AWS API Gateway is configured using "Integration type" of "Lambda Function" which means it packs
         * details of the originating HTTP request into a JSON structure and makes it available as an input
         * stream.
         *
         * The JSON structure includes:
         *
         * resource (string)
         * path (string)
         * httpMethod (string)
         * headers (hash)
         * queryStringParameters (hash)
         * pathParameters (hash)
         * stageVariables (hash)
         * requestContext (hash)
         * body (string representing a JSON object)
         *
         * See: http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-set-up-simple-proxy.html#api-gateway-simple-proxy-for-lambda-input-format
         *
         * The AWS API Gateway requires that we return our output as JSON as well in a format it can decode.
         */

        JsonParser parser = new JsonParser();
        JsonObject inputObj;

        try
        {
            inputObj = parser.parse(IOUtils.toString(requestStream)).getAsJsonObject();
        }
        catch (IOException e)
        {
            logger.log("Error reading request: " + e.getMessage());
            throw new Exception(e.getMessage());
        }

        JsonObject body = null;
        if (inputObj.get("body") != null)
        {
            // @todo: can we cast this to a POJO?
            body = inputObj.get("body").getAsJsonObject();
        }

        APIGatewayRequest  request = getGson().fromJson(body, APIGatewayRequest.class);
        APIGatewayResponse response = new APIGatewayResponse();

        response.setStatusCode(500);
        response.setBody("Unknown command: " + (request.getCommand() != null ? request.getCommand() : "undefined"));

        if (request.getCommand() == null)
        {
            response.setBody("command must be specified");
        }
        else if (request.getCommand().equals("getTrainingSets"))
        {
            List<String> trainingSets = getTrainingSets();

            response.setStatusCode(200);
            response.setBody(getGson().toJson(trainingSets));
        }
        else if (request.getCommand().equals("findClusters"))
        {
            if (request.getClusterCount() >= 1)
            {
                List<Point> trainingSamples = getTrainingSamples(request.getSetId());
                KMeans kmeans = new KMeans(trainingSamples);

                List<Centroid> centroids = kmeans.performClustering(request.getClusterCount());
                if (centroids != null)
                {
                    response.setStatusCode(200);
                    response.setBody(getGson().toJson(centroids));
                }
                else
                {
                    response.setBody("Failed to find clusters");
                }
            }
            else
            {
                response.setStatusCode(400);
                response.setBody("clusterCount must be >= 1");
            }
        }
        else if (request.getCommand().equals("addTrainingSamples"))
        {
            if (addTrainingSamples(request))
            {
                response.setStatusCode(200);
                response.setBody("");
            }
            else
            {
                response.setBody("Could not add training samples to specified set");
            }
        }

        try
        {
            IOUtils.write(getGson().toJson(response), responseStream);
        }
        catch (final IOException e)
        {
            logger.log("Error while writing response: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * @desc Add training samples to a new or existing training set.
     *
     * @param request
     * @return Success or failure
     */
    protected boolean addTrainingSamples(APIGatewayRequest request)
    {
        boolean success = false;

        try
        {
            logger.log("Adding samples to training set '" + request.getSetId() + "'");

            if (request.getSamples() == null)
            {
                throw new Exception("samples must be specified");
            }

            addTrainingSet(request.getSetId());      // adds the set if it doesn't yet exist

            Table table = getDynamoTable("TrainingSamples");

            for (int i = 0; i < request.getSamples().size(); i++)
            {
                APIGatewayRequestSample sample = request.getSamples().get(i);

                if (sample.getFeatures() == null)
                {
                    throw new Exception("features must be specified for sample " + i);
                }

                for (int j = 0; j < sample.getFeatures().size(); j++)
                {
                    if ( ! (sample.getFeatures().get(i) instanceof java.lang.Double))
                    {
                        throw new Exception("Invalid feature type: " + sample.getFeatures().get(i).getClass() + ", expecting Double for feature " + j + " on sample " + i);
                    }
                }

                PutItemOutcome outcome = table.putItem(new Item()
                        .withPrimaryKey("setId", request.getSetId(), "uuid", UUID.randomUUID().toString())
                        .withList("features", sample.getFeatures()));
            }

            success = true;
        }
        catch (Exception e)
        {
            logger.log("Unable to add training samples: " + e.getMessage());
        }

        return success;
    }

    /**
     * @desc Add a new training set if it doesn't already exist.
     *
     * @param setId
     * @return Success or failure
     */
    protected boolean addTrainingSet(String setId)
    {
        boolean success = false;

        try
        {
            logger.log("Adding training training set: " + setId);

            Table table = getDynamoTable("TrainingSets");

            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("setId", setId)
            );

            success = true;
        }
        catch (Exception e)
        {
            logger.log("Unable to add new training set: " + e.getMessage());
        }

        return success;
    }

    /**
     * @desc Get known training sets.
     *
     * @return The list of known training set names
     */
    protected List<String> getTrainingSets()
    {
        List<String> trainingSets = new ArrayList();
        Table table;

        try
        {
            table = getDynamoTable("TrainingSets");

            ItemCollection<ScanOutcome> items = table.scan();
            Iterator<Item> iterator = items.iterator();
            Item item = null;

            while (iterator.hasNext())
            {
                item = iterator.next();
                trainingSets.add(item.getString("setId"));
            }
        }
        catch (Exception e)
        {
            logger.log("Unable to query training sets: " + e.getMessage());
        }

        return trainingSets;
    }

    /**
     * Get all training samples for a specific data set.
     *
     * @param setId Name of the training set to retrieve samples for
     * @return Array of training samples, each item is itself an array of features
     */
    protected List<Point> getTrainingSamples(String setId)
    {
        List<Point> trainingSamples = new ArrayList();
        Table table;

        try
        {
            table = getDynamoTable("TrainingSamples");

            HashMap<String, String> queryPredicateNames = new HashMap<String, String>();
            queryPredicateNames.put("#setId", "setId");

            HashMap<String, Object> queryPredicateValues = new HashMap<String, Object>();
            queryPredicateValues.put(":setId", setId);

            QuerySpec query = new QuerySpec()
                    .withKeyConditionExpression("#setId = :setId")
                    .withNameMap(queryPredicateNames)
                    .withValueMap(queryPredicateValues);

            ItemCollection<QueryOutcome> items = table.query(query);
            Iterator<Item> iterator = items.iterator();
            Item item = null;

            while (iterator.hasNext())
            {
                item = iterator.next();

                // Our list is inserted into Dynamo as Doubles but comes out as BigDecimal, convert back.
                List<BigDecimal> rawFeatures = item.getList("features");
                List<Double> features = new ArrayList();
                for (BigDecimal feature : rawFeatures)
                {
                    features.add(feature.doubleValue());
                }

                Point trainingSample = new Point(features);
                trainingSamples.add(trainingSample);
            }
        }
        catch (Exception e)
        {
            logger.log("Unable to query training samples for training set '" + setId + "': " + e.getMessage());
        }

        return trainingSamples;
    }

    /**
     * @desc Get an initialised GSON object with the default configuration.
     *
     * @return GSON instance
     */
    protected Gson getGson()
    {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * @desc Get the DynamoDB API client.
     *
     * @return DynamoDB
     * @throws Exception
     */
    protected DynamoDB getDynamoDBClient() throws Exception
    {
        if (dynamoDB == null)
        {
            AmazonDynamoDBClient client = new AmazonDynamoDBClient()
                    .withRegion(Regions.AP_SOUTHEAST_2);        // @todo: this should be configured via Lambda environment variables

            dynamoDB = new DynamoDB(client);

            if (dynamoDB == null)
            {
                logger.log("Warning: Could not create DynamoDB client!");
                throw new Exception("Can't get DynamoDB client");
            }
        }

        return dynamoDB;
    }

    /**
     * @desc Get the DynamoDB table used to store all training samples across all data sets.
     *
     * @param String tableName
     * @return The DynamoDB table
     * @throws Exception
     */
    protected Table getDynamoTable(String tableName) throws Exception
    {
        DynamoDB dynamoDB = getDynamoDBClient();

        Table table = dynamoDB.getTable(tableName);
        if (table == null)
        {
            throw new Exception("Can't find table: " + tableName);
        }

        return table;
    }
}
