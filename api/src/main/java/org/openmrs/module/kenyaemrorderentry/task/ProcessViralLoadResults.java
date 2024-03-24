package org.openmrs.module.kenyaemrorderentry.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * this class composes a POST request to an end point that processes lab results.
 * This class is a hacky approach to discontinue orders, which only works well if done through REST
 */
public class ProcessViralLoadResults {

    public static void processPayload(String payload) {
        Context.openSession();
        try {

            GlobalProperty gpPwd = Context.getAdministrationService().getGlobalPropertyObject("scheduler.password");
            GlobalProperty gpUsername = Context.getAdministrationService().getGlobalPropertyObject("scheduler.username");
            GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject("local.viral_load_result_end_point");

            String serverUrl = gpServerUrl.getPropertyValue();
            String username = gpUsername.getPropertyValue();
            String pwd = gpPwd.getPropertyValue();

            if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(username) || StringUtils.isBlank(pwd)) {
                System.out.println("Please set credentials for the openmrs scheduler");
                return;
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                //Define a postRequest request
                HttpPost postRequest = new HttpPost(serverUrl);

                //Set the API media type in http content-type header
                postRequest.addHeader("content-type", "application/json");

                String auth = username.trim() + ":" + pwd.trim();
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes("UTF-8"));
                String authHeader = "Basic " + new String(encodedAuth);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                //Set the request post body
                StringEntity userEntity = new StringEntity(payload);
                postRequest.setEntity(userEntity);

                //Send the request; It will immediately return the response in HttpResponse object if any
                HttpResponse response = httpClient.execute(postRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200 && statusCode != 201) {
                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                        JSONObject errorObj = (JSONObject) responseObj.get("error");
                        if (statusCode == 400) {// bad request
                        }
                        System.err.println("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                        throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                    } catch (Exception e) {}
                    System.err.println("Failed with HTTP error code : " + statusCode);
                } else {
                    //System.out.println("Successfully updated VL result"); -- remain mute
                }
            }
            finally {
                //Important: Close the connect
                httpClient.close();
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to update lab results through REST", e);
        }
    }

    public static void processFLUPayload(String payload) {
        Context.openSession();
        try {

            GlobalProperty gpPwd = Context.getAdministrationService().getGlobalPropertyObject("scheduler.password");
            GlobalProperty gpUsername = Context.getAdministrationService().getGlobalPropertyObject("scheduler.username");
            GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject("local.flu_result_end_point");

            String serverUrl = gpServerUrl.getPropertyValue();
            // String serverUrl = "http://127.0.0.1:8080/openmrs/ws/rest/v1/kemrorder/flulabresults";
            String username = gpUsername.getPropertyValue();
            String pwd = gpPwd.getPropertyValue();

            if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(username) || StringUtils.isBlank(pwd)) {
                System.out.println("Please set credentials for the openmrs scheduler");
                return;
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                //Define a postRequest request
                HttpPost postRequest = new HttpPost(serverUrl);

                //Set the API media type in http content-type header
                postRequest.addHeader("content-type", "application/json");

                String auth = username.trim() + ":" + pwd.trim();
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes("UTF-8"));
                String authHeader = "Basic " + new String(encodedAuth);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                //Set the request post body
                StringEntity userEntity = new StringEntity(payload);
                postRequest.setEntity(userEntity);

                //Send the request; It will immediately return the response in HttpResponse object if any
                HttpResponse response = httpClient.execute(postRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200 && statusCode != 201) {
                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                        JSONObject errorObj = (JSONObject) responseObj.get("error");
                        if (statusCode == 400) {// bad request
                        }
                        System.err.println("FLU Manifest Error: Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                        // throw new RuntimeException("FLU Manifest Error: Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                    } catch (Exception e) {}
                    System.err.println("FLU Manifest Error: Failed with HTTP error code : " + statusCode);
                } else {
                    System.out.println("FLU Manifest Success: Got FLU POST result: " + statusCode);
                }
            }
            finally {
                //Important: Close the connection
                httpClient.close();
            }
        }
        catch (Exception ex) {
            System.out.println("Unable to update lab results through REST" + ex);
            ex.printStackTrace();
            // throw new IllegalArgumentException("Unable to update lab results through REST", e);
        }
    }

    /**
     * Util method for extracting order ids from lab system response
     * The list is used with what was used in querying for results to determine which samples aren't in the lab
     * @param resultPayload
     * @return
     */
    public static List<Integer> extractOrderIdListFromLabResponse(String resultPayload) {

        JsonElement rootNode = JsonParser.parseString(resultPayload);
        List<Integer> orderIds = null;
        JsonArray resultsObj = null;
        try {
            if (rootNode.isJsonArray()) {
                resultsObj = rootNode.getAsJsonArray();
                orderIds = new ArrayList<Integer>();
            } else {
                System.out.println("Lab Results Get list of order ids: The payload could not be understood. An array is expected!");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Lab Results Get Results: Could not extract order ids: " + e.getMessage());
            e.printStackTrace();
        }

        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {

                JsonObject o = resultsObj.get(i).getAsJsonObject();
                Integer orderId = o.get("order_number").getAsInt();
                orderIds.add(orderId);
            }
        }
        return orderIds;
    }
}
