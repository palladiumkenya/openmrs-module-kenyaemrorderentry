package org.openmrs.module.kenyaemrorderentry.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Integer GRACE_PERIOD_FOR_LAB_ORDERS = 2; // days

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

                String serverUrl = gpServerUrl.getPropertyValue();
                String API_KEY = gpApiToken.getPropertyValue();
                String updatesEndpoint = gpVLUpdateEndpoint.getPropertyValue();

                if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY) || StringUtils.isBlank(updatesEndpoint)) {
                    System.out.println("Please set credentials for pulling lab requests from the lab system");
                    return;
                }

                /**
                 * the order of execution should be:
                 * 1. the vl requests with incomplete results and were last checked over 1 day ago
                 * 2. manifests with submitted status
                 */
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.add(Calendar.DATE, -GRACE_PERIOD_FOR_LAB_ORDERS);


                Date effectiveDate =  calendar.getTime();

                System.out.println("Effective date: " + effectiveDate);
                List<LabManifestOrder> ordersWithPendingResults = new ArrayList<LabManifestOrder>();

                List<LabManifestOrder> previouslyCheckedOrders = kenyaemrOrdersService.getLabManifestOrderByStatusBeforeDate("Incomplete", effectiveDate );
                System.out.println("Total samples : " + previouslyCheckedOrders.size());

                if (previouslyCheckedOrders.size() > 0) {
                    ordersWithPendingResults = previouslyCheckedOrders;
                    System.out.println("Executing block for incomplete results. Total samples : " + previouslyCheckedOrders.size());

                } else {
                    LabManifest manifestToUpdateResults = kenyaemrOrdersService.getLabOrderManifestByStatus("Submitted");

                    if (manifestToUpdateResults == null) {
                        System.out.println("There are no manifests to pull results for");
                        log.info("There are no manifests to pull results for");
                        return;
                    }

                    ordersWithPendingResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Sent");
                    List<LabManifestOrder> ordersWithIncompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifestToUpdateResults, "Incomplete");

                    if (ordersWithPendingResults.size() < 1 && ordersWithIncompleteResults.size() < 1) {
                        System.out.println("There are no active labs to pull results for");
                        log.info("There are no active labs to pull results for");
                        manifestToUpdateResults.setStatus("Complete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);
                        return;
                    } else if (ordersWithPendingResults.size() < 1 && ordersWithIncompleteResults.size() > 0) {
                        System.out.println("Manifest has incomplete results");
                        log.info("Manifest has incomplete results");
                        manifestToUpdateResults.setStatus("Incomplete results");
                        manifestToUpdateResults.setDateChanged(new Date());
                        kenyaemrOrdersService.saveLabOrderManifest(manifestToUpdateResults);
                        return;
                    }
                }


                if (ordersWithPendingResults.size() < 1) { // exit if manifest has 0 orders with pending results
                    System.out.println("Manifest doesn't have orders awaiting results");
                    log.info("Manifest doesn't have orders awaiting results");
                    return;
                }

                System.out.println("Preparing to pull results ... ... ...");

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        SSLContexts.createDefault(),
                        new String[]{"TLSv1.2"},
                        null,
                        SSLConnectionSocketFactory.getDefaultHostnameVerifier());

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                try {

                    // we want to create a comma separated list of order id
                    List<Integer> orderIds = new ArrayList<Integer>();
                    for (LabManifestOrder manifestOrder : ordersWithPendingResults) {
                        orderIds.add(manifestOrder.getOrder().getOrderId());
                    }

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
                    String responseString = EntityUtils.toString(response.getEntity());

                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode resultsObj = null;
                    try {
                        JsonNode actualObj = mapper.readTree(responseString);
                        resultsObj = (ObjectNode) actualObj;
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    ArrayNode resultArray = (ArrayNode) resultsObj.get("data");

                    if (resultArray != null && !resultArray.isEmpty()) {
                        ProcessViralLoadResults.processPayload(resultArray.toString());// the only way that works for now is posting this through REST
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
