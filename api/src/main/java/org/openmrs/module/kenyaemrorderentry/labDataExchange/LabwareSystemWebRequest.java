package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
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
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * An implementation for Labware
 */
public class LabwareSystemWebRequest extends LabWebRequest {

    private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);

    public LabwareSystemWebRequest() {
        //
    }

    @Override
    public boolean checkRequirements(LabManifest toProcess) {
        // EID settings
        GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_API_TOKEN);

        // VL Settings
        GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_API_TOKEN);

        // FLU Settings
        GlobalProperty gpFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_RESULT_URL);
        GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_API_TOKEN);

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
            System.err.println("Labware Lab Results: Please set credentials for posting lab requests to the labware system");
            return false;
        }
        return true;
    }

    public boolean postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException {

        LabManifest toProcess = manifestOrder.getLabManifest();
        if (!checkRequirements(toProcess)) {
            System.err.println("Labware Lab Results POST: Failed to satisfy requirements");
            return(false);
        }

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";

        if(toProcess.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPushUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(toProcess.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPushUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        } else if(toProcess.getManifestType() == LabManifest.FLU_TYPE) {
            GlobalProperty gpFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_API_TOKEN);
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
            System.out.println("Labware Lab Results POST: Server URL: " + serverUrl);
            HttpPost postRequest = new HttpPost(serverUrl);

            //Set the API media type in http content-type header
            postRequest.addHeader("content-type", "application/json");
            postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY);

            //Set the request post body
            String payload = manifestOrder.getPayload();
            System.out.println("Labware Lab Results POST: Server Payload: " + payload);
            StringEntity userEntity = new StringEntity(payload);
            postRequest.setEntity(userEntity);

            HttpResponse response = httpClient.execute(postRequest);

            //verify the valid error code first
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) { // too many requests. just terminate
                System.out.println("Labware Lab Results POST: 429 The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                log.warn("Labware Lab Results POST: 429 The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
                return(false);
            }

            if (statusCode != 201 && statusCode != 200 && statusCode != 422 && statusCode != 403) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response
                manifestOrder.setStatus("Pending");
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                System.out.println("Labware Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                log.warn("Labware Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                return(false);
            } else if (statusCode == 403 || statusCode == 422) {
                manifestOrder.setStatus("Pending");
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                System.out.println("Labware Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                log.warn("Labware Lab Results POST: There was an error sending lab id = " + manifestOrder.getId() + " Status: " + statusCode);
                return(false);
            } else if (statusCode == 201 || statusCode == 200) {
                manifestOrder.setStatus("Sent");
                manifestOrder.setDateSent(new Date());
                System.out.println("Labware Lab Results POST: Successfully pushed a VL lab test id " + manifestOrder.getId());
                log.info("Labware Lab Results POST: Successfully pushed a VL lab test id " + manifestOrder.getId());
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);

                if (toProcess != null && manifestStatus.equals("Ready to send")) {
                    toProcess.setStatus("Sending");
                    kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                }
                Context.flushSession();

                System.out.println("Labware Lab Results POST: Push Successfull");
                return(true);
            }
        } catch (Exception e) {
            System.err.println("Labware Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            log.error("Labware Lab Results POST: Could not push requests to the lab! " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }

        return(false);
    }

    public void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds, LabManifest manifestToUpdateResults) throws IOException {

        if (!checkRequirements(manifestToUpdateResults)) {
            System.err.println("Labware Lab Results GET: Failed to satisfy requirements");
            return;
        }

        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

        String serverUrl = "";
        String API_KEY = "";

        if(manifestToUpdateResults.getManifestType() == LabManifest.EID_TYPE) {
            GlobalProperty gpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_RESULT_URL);
            GlobalProperty gpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_API_TOKEN);
            serverUrl = gpEIDServerPullUrl.getPropertyValue().trim();
            API_KEY = gpEIDApiToken.getPropertyValue().trim();
        } else if(manifestToUpdateResults.getManifestType() == LabManifest.VL_TYPE) {
            GlobalProperty gpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_RESULT_URL);
            GlobalProperty gpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_API_TOKEN);
            serverUrl = gpVLServerPullUrl.getPropertyValue().trim();
            API_KEY = gpVLApiToken.getPropertyValue().trim();
        } else if(manifestToUpdateResults.getManifestType() == LabManifest.FLU_TYPE) {
            GlobalProperty gpFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_RESULT_URL);
            GlobalProperty gpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_API_TOKEN);
            serverUrl = gpFLUServerPullUrl.getPropertyValue().trim();
            API_KEY = gpFLUApiToken.getPropertyValue().trim();
        }

        GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_MANIFEST_LAST_PROCESSED);
        GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_MANIFEST_LAST_UPDATETIME);


        //Using SSL
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
            URIBuilder builder = new URIBuilder(serverUrl);
            builder.addParameter("mfl_code", facilityCode);
            builder.addParameter("order_no", orderNumbers);
            URI uri = builder.build();
            System.out.println("Get Labware Lab Results URL: " + uri);

            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("content-type", "application/x-www-form-urlencoded");
            httpget.addHeader("Authorization", "Bearer " +API_KEY);
            httpget.addHeader("Accept", "application/json");

            CloseableHttpResponse response = httpClient.execute(httpget);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Get Labware Lab Results Failed with HTTP error code : " + statusCode);
            } else {
                System.out.println("Get Labware Lab Results: REST Call Success");
            }

           String jsonString = null;
           HttpEntity entity = response.getEntity();
           if (entity != null) {
               BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

               try {
                   jsonString = rd.lines().collect(Collectors.joining()).toString();
                   System.out.println("Labware Lab Results Get: Request JSON -> " + jsonString);
               } finally {
                   rd.close();
               }
           }

            JSONParser parser = new JSONParser();
            JSONArray responseArray = (JSONArray) parser.parse(jsonString);

            if (responseArray != null && !responseArray.isEmpty()) {
                // update orders
                ProcessViralLoadResults.processPayload(jsonString);

                // update manifest details appropriately for the next execution
                String [] incompleteStatuses = new String []{"Incomplete", "Pending", "Sending"};

                if (manifestToUpdateResults != null) {
                    System.out.println("Labware Lab Results Get: Updating manifest with status");
                    List<LabManifestOrder> pendingResultsForNextIteration = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent");
                    List<LabManifestOrder> incompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, incompleteStatuses);

                    System.out.println("Size of pending results: " + pendingResultsForNextIteration.size() + " Size of incomplete results: " + incompleteResults.size());

                    if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() < 1) {
                        manifestToUpdateResults.setStatus("Complete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Labware Lab Results Get: Updating manifest with status: Complete Results");
                    } else if (pendingResultsForNextIteration.size() < 1 && incompleteResults.size() > 0) {
                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);

                        gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        System.out.println("Labware Lab Results Get: Updating manifest with status: Incomplete Results");
                    }

                    // update manifest global property
                    if (pendingResultsForNextIteration.size() > 0) {
                        System.out.println("Labware Lab Results Get: Updating manifest global property");
                        gpLastProcessedManifest.setPropertyValue(manifestToUpdateResults.getId().toString());
                        gpLastProcessedManifestUpdatetime.setPropertyValue(Utils.getSimpleDateFormat(ModuleConstants.MANIFEST_LAST_UPDATE_PATTERN).format(new Date()));
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

            System.out.println("Labware Lab Results Get: Successfully executed the task that pulls lab requests");
            log.info("Labware Lab Results Get: Successfully executed the task that pulls lab requests");

            System.out.println("Labware Lab Results Get: Successfully Done");
        } catch (Exception e) {
            System.err.println("Get Labware Lab Results Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            httpClient.close();
        }
    }

    @Override
    public ObjectNode completePostPayload(Order order, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        ObjectNode node = baselinePostRequestPayload(order, dateSampleCollected, dateSampleSeparated, sampleType, manifestID);

        if (!node.isEmpty()) {

            if (getManifestType() == LabManifest.FLU_TYPE) {
                // Any custom payload for LABWARE FLU
            } else {
                node.put("mfl_code", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
                if (order.getPatient().getGender().equals("F")) {
                    node.put("female_status", "none");
                }
                node.put("lab", ModuleConstants.DEFAULT_APHL_LAB_CODE.toString());
                node.put("facility_email", "none");
                node.put("recency_id", "");
                node.put("emr_shipment", StringUtils.isNotBlank(manifestID) ? manifestID : "");
                node.put("date_separated", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleSeparated));


                // node.put("mfl_code", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
                if (order.getPatient().getGender().equals("F")) {
                    node.put("female_status", "none");
                }
            }

            // System.out.println("Order Entry: Using LABWARE System payload: " + node.toPrettyString());
        }

        return node;
    }

}
