package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation for EIDVLLabSystem - commonly referred to as CHAI system
 */
public class ChaiSystemWebRequest extends LabWebRequest {

    public static final String GP_LAST_PULL_DATE_TIME = "kemrorder.vl_server_last_execution_time";

    private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);

    public ChaiSystemWebRequest() {
        //
    }

    @Override
    public boolean checkRequirements(LabManifest toProcess) {
        // EID settings
        GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_API_TOKEN);

        // VL Settings
        GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_API_TOKEN);

        // FLU Settings
        GlobalProperty gpFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_RESULT_URL);
        GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_API_TOKEN);

        String EIDServerPushUrl = gpEIDServerPushUrl.getPropertyValue();
        String EIDServerPullUrl = gpEIDServerPullUrl.getPropertyValue();
        String EIDApiToken = gpEIDApiToken.getPropertyValue();

        String VLServerPushUrl = gpVLServerPushUrl.getPropertyValue();
        String VLServerPullUrl = gpVLServerPullUrl.getPropertyValue();
        String VLApiToken = gpVLApiToken.getPropertyValue();

        String FLUServerPushUrl = gpFLUServerPushUrl.getPropertyValue();
        String FLUServerPullUrl = gpFLUServerPullUrl.getPropertyValue();
        String FLUApiToken = gpFLUApiToken.getPropertyValue();

        if ((toProcess.getManifestType() == LabManifest.VL_TYPE && (StringUtils.isBlank(VLServerPushUrl) || StringUtils.isBlank(VLServerPullUrl) || StringUtils.isBlank(VLApiToken)))
                || (toProcess.getManifestType() == LabManifest.EID_TYPE && (StringUtils.isBlank(EIDServerPushUrl) || StringUtils.isBlank(EIDServerPullUrl) || StringUtils.isBlank(EIDApiToken)))
                || (toProcess.getManifestType() == LabManifest.FLU_TYPE && (StringUtils.isBlank(FLUServerPushUrl) || StringUtils.isBlank(FLUServerPullUrl) || StringUtils.isBlank(FLUApiToken)))
                || LabOrderDataExchange.getSystemType() == ModuleConstants.NO_SYSTEM_CONFIGURED
        ) {
            System.err.println("CHAI Lab Results: Please set credentials for posting lab requests to the CHAI system");
            return false;
        }
        return true;
    }

    public boolean postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException {

        LabManifest toProcess = manifestOrder.getLabManifest();
        if (!checkRequirements(toProcess)) {
            System.err.println("CHAI Lab Results POST: Failed to satisfy requirements");
            return(false);
        }

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";

        if(toProcess.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPushUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(toProcess.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPushUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        } else if(toProcess.getManifestType() == LabManifest.FLU_TYPE) {
            GlobalProperty gpFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_API_TOKEN);
            serverUrl = gpFLUServerPushUrl.getPropertyValue().trim();
            API_KEY = gpFLUApiToken.getPropertyValue().trim();
        }


        SSLConnectionSocketFactory sslsf = null;
        GlobalProperty gpSslVerification = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

        if (gpSslVerification != null) {
            String sslVerificationEnabled = gpSslVerification.getPropertyValue();
            if (StringUtils.isNotBlank(sslVerificationEnabled)) {
                if (sslVerificationEnabled.equals("true")) {
                    sslsf = Utils.sslConnectionSocketFactoryDefault();
                } else {
                    sslsf = Utils.sslConnectionSocketFactoryWithDisabledSSLVerification();
                }
            }
        }

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        try {

            //Define a postRequest request
            System.out.println("CHAI Lab Results POST: Server URL: " + serverUrl);
            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            postRequest.setHeader("apikey", API_KEY);

            //Set the request post body
            String payload = manifestOrder.getPayload();
            //System.out.println("CHAI Lab POST: Server Payload: " + payload);
            StringEntity userEntity = new StringEntity(payload);
            postRequest.setEntity(userEntity);

            HttpResponse response = httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(response.getEntity(), "UTF-8");
            String duplicateEntryMessage = "already exists in database";


            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("CHAI Lab Results POST: 429 The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("CHAI Lab Results POST: 429 The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return(false);
            }

            if (statusCode != 201 && statusCode != 200 && statusCode != 403 && statusCode != 500) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response

                try {
                    JSONParser parser = new JSONParser();
                    JSONObject responseObj = (JSONObject) parser.parse(message);
                    JSONObject errorObj = (JSONObject) responseObj.get("error");

                    if (statusCode == 400 ) { // sample already exists in the database. Change status to sample already exist
                        if (StringUtils.isNotBlank(message) && message.contains(duplicateEntryMessage)) {
                            manifestOrder.setStatus("Duplicate entry");
                            manifestOrder.setDateChanged(new Date());
                            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                            System.out.println("CHAI Lab Results POST: Error - duplicate entry. " + "Error - " + message);
                            return(true);
                        }
                    } else if (statusCode == 422 ) { // General validation errors
                        manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
                        manifestOrder.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                        System.out.println("CHAI Lab Results POST: Error - validation error. " + "Error - " + message);
                        return(true);
                    }
                    System.out.println("CHAI Lab Results POST: Error while sending manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));

                    manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
                } catch(Exception ex) {
                    manifestOrder.setStatus("Error - " + statusCode);
                }
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                return(false);
            } else if (statusCode == 403 || statusCode == 500) { // just skip for now. There should be a new attempt
                System.out.println("CHAI Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode + ", message: " + message);
                log.error("CHAI Lab Results POST: Error while submitting manifest sample. " + "Error - " + statusCode);
                return(false);
            } else if (statusCode == 201 || statusCode == 200) { // successful attempt
                manifestOrder.setStatus("Sent");
                manifestOrder.setDateSent(new Date());
                System.out.println("CHAI Lab Results POST: Successfully pushed " + (toProcess.getManifestType() == LabManifest.EID_TYPE ? "an EID" : "a Viral load")  + " test");
                
                // Now we process the message to extract the batch number
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject responseObj = (JSONObject) parser.parse(message);
                    String batchNumber = responseObj.get("batch_id").toString();
                    System.out.println("CHAI Lab Results POST: Got the batch number: " + batchNumber);
                    manifestOrder.setBatchNumber(batchNumber);
                } catch(Exception ex) {
                    System.out.println("CHAI Lab Results POST: ERROR: Failed to get the batch number");
                }
                
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);

                if (toProcess != null && manifestStatus.equals("Ready to send")) {
                    toProcess.setStatus("Sending");
                    kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                }
                Context.flushSession();

                System.out.println("CHAI Lab Results POST: Push Successful");
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

        //TODO: the EIDVL server seems to have a request limit and rejects samples after some time.
        /*GlobalProperty gpLastExecutionTime = Context.getAdministrationService().getGlobalPropertyObject(GP_LAST_PULL_DATE_TIME);
        String lastExecutionTime = gpLastExecutionTime.getPropertyValue();

        if (StringUtils.isNotBlank(lastExecutionTime)) {

        }*/

        if (!checkRequirements(manifestToUpdateResults)) {
            System.err.println("CHAI Lab Results GET: Failed to satisfy requirements");
            return;
        }

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";
        System.out.println("Processing manifest ID: " + manifestToUpdateResults.getId());
        if(manifestToUpdateResults.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPullUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(manifestToUpdateResults.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPullUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        } else if(manifestToUpdateResults.getManifestType() == LabManifest.FLU_TYPE) {
            GlobalProperty gpFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_RESULT_URL);
            GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_API_TOKEN);
            serverUrl = gpFLUServerPullUrl.getPropertyValue().trim();
            API_KEY = gpFLUApiToken.getPropertyValue().trim();
        }

        GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_MANIFEST_LAST_PROCESSED);
        GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_MANIFEST_LAST_UPDATETIME);

        SSLConnectionSocketFactory sslsf = null;
        GlobalProperty gpSslVerification = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

        if (gpSslVerification != null) {
            String sslVerificationEnabled = gpSslVerification.getPropertyValue();
            if (StringUtils.isNotBlank(sslVerificationEnabled)) {
                if (sslVerificationEnabled.equals("true")) {
                    sslsf = Utils.sslConnectionSocketFactoryDefault();
                } else {
                    sslsf = Utils.sslConnectionSocketFactoryWithDisabledSSLVerification();
                }
            }
        }

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        try {

            String facilityCode = Utils.getDefaultLocationMflCode(Utils.getDefaultLocation());
            String orderNumbers = StringUtils.join(orderIds, ",");

            System.out.println("Get CHAI Lab Results URL: " + serverUrl);

            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            postRequest.addHeader("Accept", "application/json");
            postRequest.addHeader("apikey", API_KEY);

            ObjectNode request = Utils.getJsonNodeFactory().objectNode();
            request.put("test", manifestToUpdateResults.getManifestType().toString());
            request.put("facility_code", facilityCode);
            request.put("order_numbers", orderNumbers);

            System.out.println("CHAI Lab GET: Server Payload: " + request);
            StringEntity userEntity = new StringEntity(request.toString());
            postRequest.setEntity(userEntity);

            CloseableHttpResponse response = httpClient.execute(postRequest);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String message = EntityUtils.toString(response.getEntity(), "UTF-8");

                if (statusCode == 429) { // too many requests. just terminate
                    System.out.println("CHAI Lab Results POST: 429 The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                    log.warn("CHAI Lab Results POST: 429 The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                    return;
                }
                throw new RuntimeException("Get CHAI Lab Results Failed with HTTP error code : " + statusCode + ", message: " + message);
            } else {
                System.out.println("Get CHAI Lab Results: REST Call Success");
            }

           String jsonString = null;
           HttpEntity entity = response.getEntity();
           if (entity != null) {
               BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

               try {
                   jsonString = rd.lines().collect(Collectors.joining()).toString();
               } finally {
                   rd.close();
               }
            }

            JSONParser parser = new JSONParser();
            JSONObject responseObject = (JSONObject) parser.parse(jsonString);
            JSONArray responseArray = (JSONArray)responseObject.get("data");
            String dataString = responseArray.toString();
            List<Integer> listOfOrderIdsInResponse = ProcessViralLoadResults.extractOrderIdListFromLabResponse(dataString);

            // check and mark status for all orders that may have not been found in the lab system. It is still not clear how this happens since the records are marked as sent in the manifest
            // in every pull, a list of submitted ids are tracked and any that is missing in the results is discontinued

            orderIds.removeAll(listOfOrderIdsInResponse);

            if (orderIds.size() > 0) {
                System.out.println("Lab result GET: Updating samples not found in lab: " + orderIds.size());
                // getManifest orders
                for (Integer oId : orderIds) {
                    LabManifestOrder mOrder = kenyaemrOrdersService.getLabManifestOrderByOrderId(Context.getOrderService().getOrder(oId));
                    mOrder.setStatus("Record not found");
                    mOrder.setDateChanged(new Date());
                    kenyaemrOrdersService.saveLabManifestOrder(mOrder);
                }
            }

            if (responseArray != null && !responseArray.isEmpty()) {
                ProcessViralLoadResults.processPayload(dataString);

                // update manifest details appropriately for the next execution
                String [] incompleteStatuses = new String []{"Incomplete", "Pending", "Sending"};
                if (manifestToUpdateResults != null) {
                    List<LabManifestOrder> samplesYetToCheckResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent"); // yet to be processed
                    List<LabManifestOrder> samplesWithIncompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, incompleteStatuses);

                    System.out.println("Size of pending results: " + samplesYetToCheckResults.size() + " Size of incomplete results: " + samplesWithIncompleteResults.size());

                    if (samplesYetToCheckResults.size() < 1 && samplesWithIncompleteResults.size() < 1) {
                        manifestToUpdateResults.setStatus("Complete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Lab Results Get: Updating manifest with status: COMPLETE Results");
                    } else if (samplesYetToCheckResults.size() < 1 && samplesWithIncompleteResults.size() > 0) {
                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Lab Results Get: Updating manifest with status: INCOMPLETE Results");
                    }

                    // update manifest global property
                    if (samplesYetToCheckResults.size() > 0) {
                        gpLastProcessedManifest.setPropertyValue(manifestToUpdateResults.getId().toString());
                        gpLastProcessedManifestUpdatetime.setPropertyValue(Utils.getSimpleDateFormat(ModuleConstants.MANIFEST_LAST_UPDATE_PATTERN).format(new Date()));
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifestUpdatetime);
                    }

                }

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

    @Override
    public ObjectNode completePostPayload(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        ObjectNode node = baselinePostRequestPayload(o, dateSampleCollected, dateSampleSeparated, sampleType, manifestID);
        if (!node.isEmpty()) {

            if (getManifestType() == LabManifest.FLU_TYPE) {
                // Any custom payload for CHAI FLU
            } else {
                node.put("mflCode", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));

                if (o.getPatient().getGender().equals("F")) {
                    node.put("pmtct", "3");
                }
                node.put("lab", "");
            }
            // System.out.println("Order Entry: Using CHAI System payload: " + node.toPrettyString());
        }
        
        return node;
    }

}
