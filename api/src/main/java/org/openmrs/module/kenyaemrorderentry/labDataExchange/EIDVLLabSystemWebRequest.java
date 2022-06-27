package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
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

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();

        if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
            System.out.println("Please set credentials for posting lab requests to the lab system");
            return false;
        }
        return true;
    }

    public void postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException {

        if (!checkRequirements())
            return;

        LabManifest toProcess = manifestOrder.getLabManifest();
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        try {

            //Define a postRequest request
            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            postRequest.addHeader("apikey", API_KEY);
            //Set the request post body
            String payload = manifestOrder.getPayload();
            StringEntity userEntity = new StringEntity(payload);
            postRequest.setEntity(userEntity);

            //Send the request; It will immediately return the response in HttpResponse object if any
            HttpResponse response = httpClient.execute(postRequest);

            //return response;
            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return;
            }

            if (statusCode != 201 && statusCode != 200 && statusCode != 422 && statusCode != 403) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject errorObj = (JSONObject) responseObj.get("error");
                manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
                System.out.println("There was an error sending lab id = " + manifestOrder.getId());
                log.warn("There was an error sending lab id = " + manifestOrder.getId());
                // throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
            } else if (statusCode == 201 || statusCode == 200) {
                manifestOrder.setStatus("Sent");
                log.info("Successfully pushed a VL lab test id " + manifestOrder.getId());
            } else if (statusCode == 403 || statusCode == 422) {
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject errorObj = (JSONObject) responseObj.get("error");
                System.out.println("Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
                log.error("Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
            }

            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);

            if (toProcess != null && manifestStatus.equals("Ready to send")) {
                toProcess.setStatus("Sending");
                kenyaemrOrdersService.saveLabOrderManifest(toProcess);
            }
            Context.flushSession();
        } catch (Exception e) {
            System.out.println("Could not push requests to the lab! " + e.getCause());
            log.error("Could not push requests to the lab! " + e.getCause());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
    }

    public void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds) throws IOException {

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_RESULT_URL);
        GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);
        GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_PROCESSED);
        GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_UPDATETIME);

        String serverUrl = gpServerUrl.getPropertyValue();
        String API_KEY = gpApiToken.getPropertyValue();
        LabManifest manifestToUpdateResults = null;

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        try {


            //Define a postRequest request
            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            postRequest.addHeader("apikey", API_KEY);

            ObjectNode request = Utils.getJsonNodeFactory().objectNode();
            request.put("test", "2");
            request.put("facility_code", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
            request.put("order_numbers", StringUtils.join(orderIds, ","));

            //Set the request post body
            StringEntity userEntity = new StringEntity(request.toString());
            postRequest.setEntity(userEntity);

            //Send the request; It will immediately return the response in HttpResponse object if any
            HttpResponse response = httpClient.execute(postRequest);


            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("The pull lab result scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("The pull lab result scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return;
            }
            if (statusCode != 200) {
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }

            String responseStringRaw = EntityUtils.toString(response.getEntity());
            String responseStringEscape = StringEscapeUtils.escapeJava(responseStringRaw);
            String strippedUnicodeChars = new UnicodeUnescaper().translate(responseStringEscape);

            String finalChars = StringEscapeUtils.unescapeJava(strippedUnicodeChars);

            Gson gson = new GsonBuilder().serializeNulls().create();
            JsonElement rootNode = gson.fromJson(finalChars, JsonElement.class);

            JsonArray resultArray = null;
            if(rootNode.isJsonObject()){
                JsonObject jsonObject = rootNode.getAsJsonObject();
                JsonElement vlResultArray = jsonObject.get("data");
                if(vlResultArray.isJsonArray()){
                    resultArray = vlResultArray.getAsJsonArray();
                }
            }

            JsonArray cleanedArray = new JsonArray();
            if (resultArray != null && !resultArray.isEmpty()) {
                for (int i =0; i < resultArray.size(); i++) {
                    JsonObject result = resultArray.get(i).getAsJsonObject();
                    result.addProperty("full_names", "Replaced Name"); // this is a short workaround to handle data coming from eid/vl system and were pushed from kenyaemr with unicode literals
                    cleanedArray.add(result);

                }
            }


            if (resultArray != null && !resultArray.isEmpty()) {
                String json = gson.toJson(cleanedArray);
                ProcessViralLoadResults.processPayload(json);// the only way that works for now is posting this through REST

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
