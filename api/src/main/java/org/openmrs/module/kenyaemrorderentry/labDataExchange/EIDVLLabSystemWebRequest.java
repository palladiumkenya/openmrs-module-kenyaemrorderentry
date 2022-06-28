package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.task.ProcessViralLoadResults;
import org.openmrs.module.kenyaemrorderentry.task.PushLabRequestsTask;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation for EIDVLLabSystem - commonly referred to as CHAI system
 */
public class EIDVLLabSystemWebRequest extends LabWebRequest {

    private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);

    public EIDVLLabSystemWebRequest() {
        setManifestType(LabManifest.EID_TYPE);
    }

    @Override
    public boolean checkRequirements() {
        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_API_TOKEN);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();

        if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
            System.out.println("Please set credentials for posting lab requests to the lab system");
            return false;
        }
        return true;
    }

    public boolean postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException {

        if (!checkRequirements()) {
            System.out.println("EID Lab Results POST: Failed to satisfy requirements");
            return(false);
        }

        LabManifest toProcess = manifestOrder.getLabManifest();
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_API_TOKEN);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();

        // SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
        //         SSLContexts.createDefault(),
        //         new String[]{"TLSv1.2"},
        //         null,
        //         SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        // CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {

            //Define a postRequest request
            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            //postRequest.addHeader("apikey", API_KEY);
            postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY);
            //Set the request post body
            String payload = manifestOrder.getPayload();
            System.out.println("EID Lab Results POST: Server Payload: " + payload);
            StringEntity userEntity = new StringEntity(payload);
            postRequest.setEntity(userEntity);

            //Send the request; It will immediately return the response in HttpResponse object if any
            HttpResponse response = httpClient.execute(postRequest);

            //return response;
            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("EID Lab Results POST: The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("EID Lab Results POST: The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return(false);
            }

            if (statusCode != 201 && statusCode != 200 && statusCode != 422 && statusCode != 403) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject errorObj = (JSONObject) responseObj.get("error");
                manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
                System.out.println("EID Lab Results POST: There was an error sending lab id = " + manifestOrder.getId());
                log.warn("EID Lab Results POST: There was an error sending lab id = " + manifestOrder.getId());
                // throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
            } else if (statusCode == 201 || statusCode == 200) {
                manifestOrder.setStatus("Sent");
                System.out.println("EID Lab Results POST: Successfully pushed a EID lab test id " + manifestOrder.getId());
                log.info("EID Lab Results POST: Successfully pushed a EID lab test id " + manifestOrder.getId());
            } else if (statusCode == 403 || statusCode == 422) {
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject errorObj = (JSONObject) responseObj.get("error");
                System.out.println("EID Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
                log.error("EID Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
            }

            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);

            if (toProcess != null && manifestStatus.equals("Ready to send")) {
                toProcess.setStatus("Sending");
                kenyaemrOrdersService.saveLabOrderManifest(toProcess);
            }
            Context.flushSession();

            return(true);
        } catch (Exception e) {
            System.out.println("EID Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            log.error("EID Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
        return(false);
    }

    public void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds, LabManifest manifestToUpdateResults) throws IOException {

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_EID_LAB_SERVER_API_TOKEN);
        GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_PROCESSED);
        GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_UPDATETIME);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();
        //LabManifest manifestToUpdateResults = null;

        // SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
        //         SSLContexts.createDefault(),
        //         new String[]{"TLSv1.2"},
        //         null,
        //         SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        // CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {

            // //Define a postRequest request
            // HttpPost postRequest = new HttpPost(serverUrl);

            // //Set the API media type in http content-type header
            // postRequest.addHeader("content-type", "application/json");
            // postRequest.addHeader("apikey", API_KEY);

            // ObjectNode request = Utils.getJsonNodeFactory().objectNode();
            // request.put("test", manifestToUpdateResults.getManifestType().toString());
            // request.put("facility_code", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
            // request.put("order_numbers", StringUtils.join(orderIds, ","));

            // //Set the request post body
            // StringEntity userEntity = new StringEntity(request.toString());
            // postRequest.setEntity(userEntity);

            // //Send the request; It will immediately return the response in HttpResponse object if any
            // HttpResponse response = httpClient.execute(postRequest);


            // //verify the valid error code first
            // int statusCode = response.getStatusLine().getStatusCode();

            // if (statusCode == 429) { // too many requests. just terminate
            //     System.out.println("The pull lab result scheduler has been configured to run at very short intervals. Please change this to at least 30min");
            //     log.warn("The pull lab result scheduler has been configured to run at very short intervals. Please change this to at least 30min");
            //     return;
            // }
            // if (statusCode != 200) {
            //     throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            // }

            // String responseStringRaw = EntityUtils.toString(response.getEntity());
            // String responseStringEscape = StringEscapeUtils.escapeJava(responseStringRaw);
            // String strippedUnicodeChars = new UnicodeUnescaper().translate(responseStringEscape);

            // String finalChars = StringEscapeUtils.unescapeJava(strippedUnicodeChars);

            // Gson gson = new GsonBuilder().serializeNulls().create();
            // JsonElement rootNode = gson.fromJson(finalChars, JsonElement.class);

            // JsonArray resultArray = null;
            // if(rootNode.isJsonObject()){
            //     JsonObject jsonObject = rootNode.getAsJsonObject();
            //     JsonElement vlResultArray = jsonObject.get("data");
            //     if(vlResultArray.isJsonArray()){
            //         resultArray = vlResultArray.getAsJsonArray();
            //     }
            // }

            // JsonArray cleanedArray = new JsonArray();
            // if (resultArray != null && !resultArray.isEmpty()) {
            //     for (int i =0; i < resultArray.size(); i++) {
            //         JsonObject result = resultArray.get(i).getAsJsonObject();
            //         result.addProperty("full_names", "Replaced Name"); // this is a short workaround to handle data coming from eid/vl system and were pushed from kenyaemr with unicode literals
            //         cleanedArray.add(result);

            //     }
            // }

            String facilityCode = Utils.getDefaultLocationMflCode(Utils.getDefaultLocation());
            //String facilityCode = "17747";
            String orderNumbers = StringUtils.join(orderIds, ",");
            URIBuilder builder = new URIBuilder(serverUrl);
            builder.addParameter("mfl_code", facilityCode);
            builder.addParameter("order_no", orderNumbers);
            URI uri = builder.build();
            System.out.println("Get Lab Results URL: " + uri);

            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("content-type", "application/x-www-form-urlencoded");
            httpget.addHeader("Authorization", "Bearer " +API_KEY);
            httpget.addHeader("Accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(httpget);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Get Lab Results Failed with HTTP error code : " + statusCode);
            } else {
                System.out.println("Get Lab Results: REST Call Success");
            }

//            //Testing
           String jsonString = null;
           HttpEntity entity = response.getEntity();
           if (entity != null) {
               BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

               try {
                   jsonString = rd.lines().collect(Collectors.joining()).toString();
                   System.out.println("Lab Results Get: Request JSON -> " + jsonString);
               } finally {
                   rd.close();
               }
           }

            JSONParser parser = new JSONParser();
            // JSONObject responseObj = (JSONObject) parser.parse(jsonString);
            ////JSONObject errorObj = (JSONObject) responseObj.get("error");
            JSONArray responseArray = (JSONArray) parser.parse(jsonString);


            // if (resultArray != null && !resultArray.isEmpty()) {
            if (responseArray != null && !responseArray.isEmpty()) {
                // String json = gson.toJson(cleanedArray);
                // ProcessViralLoadResults.processPayload(json);// the only way that works for now is posting this through REST
                // update orders
                // LabOrderDataExchange lode = new LabOrderDataExchange();
                // lode.processIncomingViralLoadLabResults(json);
                ProcessViralLoadResults.processPayload(jsonString);

                // update manifest details appropriately for the next execution
                String [] incompleteStatuses = new String []{"Incomplete"};
                if (manifestToUpdateResults != null) {
                    List<LabManifestOrder> pendingResultsForNextIteration = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent");
                    List<LabManifestOrder> incompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, incompleteStatuses);

                    if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() < 1) {
                        manifestToUpdateResults.setStatus("Complete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                    } else if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() > 0) {
                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                    }

                    // update manifest global property
                    if (pendingResultsForNextIteration.size() > 0) {
                        gpLastProcessedManifest.setPropertyValue(manifestToUpdateResults.getId().toString());
                        gpLastProcessedManifestUpdatetime.setPropertyValue(Utils.getSimpleDateFormat(LabOrderDataExchange.MANIFEST_LAST_UPDATE_PATTERN).format(new Date()));
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifestUpdatetime);
                    }
                }

            }

            // check and mark status for all orders that may have not been found in the lab system. It is still not clear how this happens since the records are marked as sent in the manifest

            Integer[] intArray = new Integer[manifestOrderIds.size()];
            intArray = manifestOrderIds.toArray(intArray);

            List<LabManifestOrder> ordersNotInLabSystem = kenyaemrOrdersService.getLabManifestOrderByNotFoundInLabSystem(intArray);

            for (LabManifestOrder o : ordersNotInLabSystem) {
                o.setStatus("Record not found");
                o.setDateChanged(new Date());
                kenyaemrOrdersService.saveLabManifestOrder(o);
            }

            System.out.println("Successfully executed the task that pulls lab requests");
            log.info("Successfully executed the task that pulls lab requests");

        } catch (Exception e) {
            System.err.println("Get EID Lab Results Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
    }

    @Override
    public ObjectNode completePostPayload(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        ObjectNode node = baselinePostRequestPayload(o, dateSampleCollected, dateSampleSeparated, sampleType, manifestID);
        node.put("mflCode", Utils.getDefaultLocationMflCode(null));
        return node;
    }

}
