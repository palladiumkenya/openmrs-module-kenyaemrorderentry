package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An implementation for EIDVLLabSystem - commonly referred to as CHAI system
 */
public class ChaiSystemWebRequest extends LabWebRequest {

    private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);

    public ChaiSystemWebRequest() {
        //
    }

    @Override
    public boolean checkRequirements() {
        // EID settings
        GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_API_TOKEN);

        // VL Settings
        GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_API_TOKEN);

        String EIDServerPushUrl = gpEIDServerPushUrl.getPropertyValue();
        String EIDServerPullUrl = gpEIDServerPullUrl.getPropertyValue();
        String EIDApiToken = gpEIDApiToken.getPropertyValue();

        String VLServerPushUrl = gpVLServerPushUrl.getPropertyValue();
        String VLServerPullUrl = gpVLServerPullUrl.getPropertyValue();
        String VLApiToken = gpVLApiToken.getPropertyValue();

        if (StringUtils.isBlank(EIDServerPushUrl) || StringUtils.isBlank(EIDServerPullUrl) || StringUtils.isBlank(EIDApiToken) || StringUtils.isBlank(VLServerPushUrl) || StringUtils.isBlank(VLServerPullUrl) || StringUtils.isBlank(VLApiToken)) {
            System.out.println("CHAI Lab Results: Please set credentials for posting lab requests to the CHAI system");
            return false;
        }
        return true;
    }

    public boolean postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException {

        if (!checkRequirements()) {
            System.out.println("CHAI Lab Results POST: Failed to satisfy requirements");
            return(false);
        }

        LabManifest toProcess = manifestOrder.getLabManifest();
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";

        if(toProcess.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPushUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(toProcess.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPushUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        }

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {

            //Define a postRequest request
            System.out.println("CHAI Lab Results POST: Server URL: " + serverUrl);
            URI uri = convertJSONTOURLEncoded(serverUrl, manifestOrder.getPayload());
            // String facilityCode = Utils.getDefaultLocationMflCode(Utils.getDefaultLocation());
            // URIBuilder builder = new URIBuilder(serverUrl);

            // JSONParser parser = new JSONParser();
            // JSONObject payloadObject = (JSONObject) parser.parse(manifestOrder.getPayload());

            // builder.addParameter("mflCode", facilityCode);
            // builder.addParameter("patient_identifier", payloadObject.containsKey("patient_identifier") ? payloadObject.get("patient_identifier").toString() : "" );
            // builder.addParameter("dob", payloadObject.containsKey("dob") ? payloadObject.get("dob").toString() : "" );
            // builder.addParameter("datecollected", payloadObject.containsKey("datecollected") ? payloadObject.get("datecollected").toString() : "" );
            // builder.addParameter("datereceived", payloadObject.containsKey("datecollected") ? payloadObject.get("datecollected").toString() : "" );
            // builder.addParameter("sex", payloadObject.containsKey("sex") ? payloadObject.get("sex").toString() : "" );
            // builder.addParameter("datetested", payloadObject.containsKey("patient") ? payloadObject.get("patient").toString() : "" );
            // builder.addParameter("datedispatched", payloadObject.containsKey("patient") ? payloadObject.get("patient").toString() : "" );
            // builder.addParameter("receivedstatus", payloadObject.containsKey("patient") ? payloadObject.get("patient").toString() : "" );
            // builder.addParameter("result", payloadObject.containsKey("patient") ? payloadObject.get("patient").toString() : "" );
            // builder.addParameter("specimenlabelID", payloadObject.containsKey("patient") ? payloadObject.get("patient").toString() : "" );
            // builder.addParameter("lab", payloadObject.containsKey("lab") ? payloadObject.get("lab").toString() : "" );
            // builder.addParameter("order_no", payloadObject.containsKey("order_no") ? payloadObject.get("order_no").toString() : "" );

            // if(toProcess.getManifestType() == LabManifest.VL_TYPE) {
            //     builder.addParameter("prophylaxis", payloadObject.containsKey("prophylaxis") ? payloadObject.get("prophylaxis").toString() : "");
            //     builder.addParameter("regimenline", payloadObject.containsKey("regimenline") ? payloadObject.get("regimenline").toString() : "");
            //     builder.addParameter("sampletype", payloadObject.containsKey("sampletype") ? payloadObject.get("sampletype").toString() : "");
            //     builder.addParameter("justification", payloadObject.containsKey("justification") ? payloadObject.get("justification").toString() : "");
            //     builder.addParameter("pmtct", payloadObject.containsKey("pmtct") ? payloadObject.get("pmtct").toString() : "");
            // } else if(toProcess.getManifestType() == LabManifest.EID_TYPE) {
            //     builder.addParameter("feeding", facilityCode);
            //     builder.addParameter("pcrtype", facilityCode);
            //     builder.addParameter("regimen", facilityCode);
            //     builder.addParameter("entry_point", facilityCode);
            //     builder.addParameter("mother_prophylaxis", facilityCode);
            //     builder.addParameter("spots", facilityCode);
            //     builder.addParameter("mother_last_result", facilityCode);
            //     builder.addParameter("mother_age", facilityCode);
            //     builder.addParameter("ccc_no", facilityCode);
            // }

            // URI uri = builder.build();
            System.out.println("Get CHAI Lab Results URL: " + uri);

            HttpPost postRequest = new HttpPost(uri);
            postRequest.addHeader("content-type", "application/x-www-form-urlencoded");
            postRequest.addHeader("Authorization", "Bearer " + API_KEY);
            postRequest.addHeader("Accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(postRequest);

            //return response;
            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("CHAI Lab Results POST: 429 The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("CHAI Lab Results POST: 429 The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return(false);
            }

            if (statusCode != 201 && statusCode != 200 && statusCode != 422 && statusCode != 403) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response
                manifestOrder.setStatus("Pending");
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                System.out.println("CHAI Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                log.warn("CHAI Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                // throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                return(false);
            } else if (statusCode == 403 || statusCode == 422) {
                manifestOrder.setStatus("Pending");
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                System.out.println("CHAI Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode);
                log.error("CHAI Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode);
                return(false);
            } else if (statusCode == 201 || statusCode == 200) {
                manifestOrder.setStatus("Sent");
                System.out.println("CHAI Lab Results POST: Successfully pushed a EID lab test id " + manifestOrder.getId());
                log.info("CHAI Lab Results POST: Successfully pushed a EID lab test id " + manifestOrder.getId());

                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);

                if (toProcess != null && manifestStatus.equals("Ready to send")) {
                    toProcess.setStatus("Sending");
                    kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                }
                Context.flushSession();

                System.out.println("CHAI Lab Results POST: Push Successfull");
                return(true);
            }
        } catch (Exception e) {
            System.out.println("CHAI Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            log.error("CHAI Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
        return(false);
    }

    public void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds, LabManifest manifestToUpdateResults) throws IOException {

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";

        if(manifestToUpdateResults.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPullUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(manifestToUpdateResults.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPullUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        }

        GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_PROCESSED);
        GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_UPDATETIME);

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {

            String facilityCode = Utils.getDefaultLocationMflCode(Utils.getDefaultLocation());
            String orderNumbers = StringUtils.join(orderIds, ",");
            URIBuilder builder = new URIBuilder(serverUrl);
            builder.addParameter("test", manifestToUpdateResults.getManifestType().toString());
            builder.addParameter("order_numbers", orderNumbers);
            builder.addParameter("facility_code", facilityCode);
            URI uri = builder.build();
            System.out.println("Get CHAI Lab Results URL: " + uri);

            HttpPost postRequest = new HttpPost(uri);
            postRequest.addHeader("content-type", "application/x-www-form-urlencoded");
            postRequest.addHeader("Authorization", "Bearer " + API_KEY);
            postRequest.addHeader("Accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(postRequest);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Get CHAI Lab Results Failed with HTTP error code : " + statusCode);
            } else {
                System.out.println("Get CHAI Lab Results: REST Call Success");
            }

           String jsonString = null;
           HttpEntity entity = response.getEntity();
           if (entity != null) {
               BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

               try {
                   jsonString = rd.lines().collect(Collectors.joining()).toString();
                   System.out.println("CHAI Lab Results Get: Request JSON -> " + jsonString);
               } finally {
                   rd.close();
               }
            }

            JSONParser parser = new JSONParser();
            JSONObject responseObject = (JSONObject) parser.parse(jsonString);
            JSONArray responseArray = (JSONArray)responseObject.get("data");
            String dataString = responseArray.toString();

            if (responseArray != null && !responseArray.isEmpty()) {

                ProcessViralLoadResults.processPayload(dataString);

                // update manifest details appropriately for the next execution
                String [] incompleteStatuses = new String []{"Incomplete", "Pending", "Sending"};
                if (manifestToUpdateResults != null) {
                    List<LabManifestOrder> pendingResultsForNextIteration = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent");
                    List<LabManifestOrder> incompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, incompleteStatuses);

                    System.out.println("Size of pending results: " + pendingResultsForNextIteration.size() + " Size of incomplete results: " + incompleteResults.size());

                    if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() < 1) {
                        manifestToUpdateResults.setStatus("Complete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Lab Results Get: Updating manifest with status: Complete Results");
                    } else if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() > 0) {
                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Lab Results Get: Updating manifest with status: InComplete Results");
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

            System.out.println("Get CHAI Lab Results: Successfully executed the task that pulls lab requests");
            log.info("Get CHAI Lab Results: Successfully executed the task that pulls lab requests");

        } catch (Exception e) {
            System.err.println("Get CHAI Lab Results Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
    }

    public URI convertJSONTOURLEncoded(String URL, String jsonPayload) {
        URI uri = null;
        try {
            URIBuilder builder = new URIBuilder(URL);
            JSONParser parser = new JSONParser();
            JSONObject payloadObject = (JSONObject) parser.parse(jsonPayload);
            Iterator iterator = payloadObject.keySet().iterator();
            //Set<String> keys = payloadObject.keySet();
            //for(String key : keys) {
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                builder.addParameter(key, payloadObject.get(key).toString());
            }
            builder.addParameter("facility_code", "");
            uri = builder.build();
        } catch(Exception ex) {}
        return(uri);
    }

    @Override
    public ObjectNode completePostPayload(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        ObjectNode node = baselinePostRequestPayload(o, dateSampleCollected, dateSampleSeparated, sampleType, manifestID);
        node.put("mfl_code", Utils.getDefaultLocationMflCode(null));
        node.put("facility_email", "info@example.com");
        return node;
    }

}
