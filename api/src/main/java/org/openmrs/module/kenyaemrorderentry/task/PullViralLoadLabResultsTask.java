package org.openmrs.module.kenyaemrorderentry.task;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * this task pulls viral load lab results from the lab system.
 *
 */
public class PullViralLoadLabResultsTask extends AbstractTask {
    private Log log = LogFactory.getLog(getClass());
    private String url = "http://www.google.com:80/index.html";

    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        Context.openSession();

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            try {

                GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_RESULT_URL);
                GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);
                GlobalProperty gpVLUpdateEndpoint = Context.getAdministrationService().getGlobalPropertyObject("local.viral_load_result_end_point");
                GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_PROCESSED);
                GlobalProperty gpRetryPeriodForIncompleteResults = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS);
                GlobalProperty gpLabTatForVlResults = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_TAT_FOR_VL_RESULTS);
                GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_UPDATETIME);

                String serverUrl = gpServerUrl.getPropertyValue();
                String API_KEY = gpApiToken.getPropertyValue();
                String updatesEndpoint = gpVLUpdateEndpoint.getPropertyValue();
                String lastProcessedManifest = gpLastProcessedManifest.getPropertyValue();
                String retryPeriodForIncompleteResults = gpRetryPeriodForIncompleteResults.getPropertyValue();
                String labTatForVlResults = gpLabTatForVlResults.getPropertyValue();
                LabManifest manifestToUpdateResults = null;

                if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY) || StringUtils.isBlank(updatesEndpoint)) {
                    System.out.println("Please set credentials for pulling lab requests from the lab system");
                    return;
                }


                //Collect New Sample, Missing Sample ( Physical Sample Missing) result is a complete result.
                // Requires manual update in the lab module status will also not make a template be rendered incomplete as the result is already pulled
                String [] incompleteStatuses = new String []{"Incomplete"};

                /**
                 * we can do some cleanup i.e close all manifests with results
                 */
                List<LabManifest> allManifests = kenyaemrOrdersService.getLabOrderManifest("Incomplete results");
                for (LabManifest manifest : allManifests) {

                    List<LabManifestOrder> manifestOrdersWithIncompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, incompleteStatuses);

                    if (manifestOrdersWithIncompleteResults.size() < 1) {
                        manifest.setStatus("Complete results");
                        manifest.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifest);
                        if(StringUtils.isNotBlank(lastProcessedManifest) && Integer.valueOf(lastProcessedManifest).equals(manifest.getId())) {
                            lastProcessedManifest = "";
                            gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                            Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                        }
                        System.out.println("Manifest with ID " + manifest.getId() + " has no pending orders. It has been marked as complete");

                    }
                }


                /**
                 * the order of execution should be:
                 * 1. the vl requests with incomplete results and were last checked over 1 day ago
                 * 2. manifests with submitted status
                 */
                int retryPeriod = 0;
                if (StringUtils.isNotBlank(retryPeriodForIncompleteResults)) {
                    retryPeriod = Integer.valueOf(retryPeriodForIncompleteResults);
                }
                if (retryPeriod == 0) {
                    retryPeriod = 2;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.add(Calendar.DATE, -retryPeriod);


                Date effectiveDate =  calendar.getTime();
                System.out.println("Preparing to pull VL results for tests dispatched on or before : " + effectiveDate);
                List<LabManifestOrder> ordersWithPendingResults = new ArrayList<LabManifestOrder>();

                List<LabManifestOrder> previouslyCheckedOrders = kenyaemrOrdersService.getLabManifestOrderByStatusBeforeDate("Incomplete", effectiveDate );

                if (previouslyCheckedOrders.size() > 0) {
                    ordersWithPendingResults = previouslyCheckedOrders;
                    System.out.println("Number of samples with results previously not ready:  " + previouslyCheckedOrders.size() + "");

                } else {

                    if (StringUtils.isNotBlank(lastProcessedManifest)) {
                        manifestToUpdateResults = kenyaemrOrdersService.getLabOrderManifestById(Integer.valueOf(lastProcessedManifest));
                        System.out.println("Currently processing manifest: " + manifestToUpdateResults.getId());

                    } else {

                        int tatPeriod = 0;
                        if (StringUtils.isNotBlank(labTatForVlResults)) {
                            tatPeriod = Integer.valueOf(labTatForVlResults);
                        }
                        if (tatPeriod == 0) {
                            tatPeriod = 10; // defaults to 10 days if left blank
                        }
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(Calendar.HOUR_OF_DAY, 23);
                        calendar1.set(Calendar.MINUTE, 59);
                        calendar1.set(Calendar.SECOND, 59);
                        calendar1.add(Calendar.DATE, -tatPeriod);

                        Date tatEffectiveDate =  calendar1.getTime();
                        manifestToUpdateResults = kenyaemrOrdersService.getLabOrderManifestByStatus("Submitted", tatEffectiveDate);
                    }


                    if (manifestToUpdateResults == null) {
                        System.out.println("There are no manifests to pull results for");
                        log.info("There are no manifests to pull results for");

                        // reset global properties
                        gpLastProcessedManifest.setPropertyValue("");
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);

                        gpLastProcessedManifestUpdatetime.setPropertyValue("");
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifestUpdatetime);
                        return;
                    }

                    ordersWithPendingResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent");

                    // terminate if there are no pending results
                    if (ordersWithPendingResults.size() < 1) {
                        System.out.println("The manifest : " + manifestToUpdateResults.getId() + " in queue has no pending samples. It will be marked as incomplete");

                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);
                        // reset global properties
                        gpLastProcessedManifest.setPropertyValue("");
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);

                        gpLastProcessedManifestUpdatetime.setPropertyValue("");
                        Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifestUpdatetime);

                        return;
                    }

                    System.out.println("Polling results for  " + ordersWithPendingResults.size() + " samples in currently processing manifest with id :" + manifestToUpdateResults.getId());
                }

                if (ordersWithPendingResults.size() < 1) { // exit there are no pending samples
                    return;
                }

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        SSLContexts.createDefault(),
                        new String[]{"TLSv1.2"},
                        null,
                        SSLConnectionSocketFactory.getDefaultHostnameVerifier());

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                List<Integer> orderIds = new ArrayList<Integer>();
                List<Integer> manifestOrderIds = new ArrayList<Integer>();
                for (LabManifestOrder manifestOrder : ordersWithPendingResults) {
                    orderIds.add(manifestOrder.getOrder().getOrderId());
                    manifestOrderIds.add(manifestOrder.getId());
                }

                try {

                    // we want to create a comma separated list of order id


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
                    String removeBackslash = finalChars.replace("\\", "");


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
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to execute task that pulls lab requests", e);
            } finally {
                Context.closeSession();
            }
        } catch (IOException ioe) {

            try {
                String text = "At " + new Date() + " there was an error reported connecting to the internet. The system did not attempt checking for viral load results ";
                log.warn(text);
            }
            catch (Exception e) {
                log.error("Failed to check internet connectivity", e);
            }
        }
    }

}
