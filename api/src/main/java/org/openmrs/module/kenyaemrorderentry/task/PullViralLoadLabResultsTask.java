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
import org.openmrs.module.kenyaemrorderentry.labDataExchange.EIDVLLabSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabwareSystemWebRequest;
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
        System.out.println("Get Lab Results: PULL TASK Starting");
        Context.openSession();

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            try {

                // GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_RESULT_URL);
                // GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);
                // GlobalProperty gpVLUpdateEndpoint = Context.getAdministrationService().getGlobalPropertyObject("local.viral_load_result_end_point");
                GlobalProperty gpLastProcessedManifest = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_PROCESSED);
                GlobalProperty gpRetryPeriodForIncompleteResults = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS);
                GlobalProperty gpLabTatForVlResults = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_TAT_FOR_VL_RESULTS);
                GlobalProperty gpLastProcessedManifestUpdatetime = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_MANIFEST_LAST_UPDATETIME);

                // String serverUrl = gpServerUrl.getPropertyValue();
                // String API_KEY = gpApiToken.getPropertyValue();
                // String updatesEndpoint = gpVLUpdateEndpoint.getPropertyValue();
                String lastProcessedManifest = gpLastProcessedManifest.getPropertyValue();
                String retryPeriodForIncompleteResults = gpRetryPeriodForIncompleteResults.getPropertyValue();
                String labTatForVlResults = gpLabTatForVlResults.getPropertyValue();
                LabManifest manifestToUpdateResults = null;

                //if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY) || StringUtils.isBlank(updatesEndpoint)) {
                // if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
                //     System.out.println("Lab Results Get: Please set credentials for pulling lab requests from the lab system");
                //     return;
                // }


                //Collect New Sample, Missing Sample ( Physical Sample Missing) result is a complete result.
                // Requires manual update in the lab module status will also not make a template be rendered incomplete as the result is already pulled
                String [] incompleteStatuses = new String []{"Incomplete"};

                /**
                 * we can do some cleanup i.e close all manifests with results
                 */
                // List<LabManifest> allManifests = kenyaemrOrdersService.getLabOrderManifest("Incomplete results");
                // for (LabManifest manifest : allManifests) {

                //     List<LabManifestOrder> manifestOrdersWithIncompleteResults = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, incompleteStatuses);

                //     if (manifestOrdersWithIncompleteResults.size() < 1) {
                //         manifest.setStatus("Complete results");
                //         manifest.setDateChanged(new Date());
                //         kenyaemrOrdersService.saveLabOrderManifest(manifest);
                //         if(StringUtils.isNotBlank(lastProcessedManifest) && Integer.valueOf(lastProcessedManifest).equals(manifest.getId())) {
                //             lastProcessedManifest = "";
                //             gpLastProcessedManifest.setPropertyValue(""); // set value to null so that the execution gets to the next manifest
                //             Context.getAdministrationService().saveGlobalProperty(gpLastProcessedManifest);
                //         }
                //         System.out.println("Lab Results Get: Manifest with ID " + manifest.getId() + " has no pending orders. It has been marked as complete");

                //     }
                // }


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
                System.out.println("Lab Results Get: Preparing to pull VL results for tests dispatched on or before : " + effectiveDate);
                List<LabManifestOrder> ordersWithPendingResults = new ArrayList<LabManifestOrder>();

                List<LabManifestOrder> previouslyCheckedOrders = kenyaemrOrdersService.getLabManifestOrderByStatusBeforeDate("Incomplete", effectiveDate );

                if (previouslyCheckedOrders.size() > 0) {
                    ordersWithPendingResults = previouslyCheckedOrders;
                    System.out.println("Lab Results Get: Number of samples with results previously not ready:  " + previouslyCheckedOrders.size() + "");

                } else {

                    if (StringUtils.isNotBlank(lastProcessedManifest)) {
                        manifestToUpdateResults = kenyaemrOrdersService.getLabOrderManifestById(Integer.valueOf(lastProcessedManifest));
                        System.out.println("Lab Results Get: Currently processing manifest: " + manifestToUpdateResults.getId());

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
                        System.out.println("Lab Results Get: geting manifest with status SUBMITTED upto date: " + tatEffectiveDate);
                        
                        manifestToUpdateResults = kenyaemrOrdersService.getLabOrderManifestByStatus("Submitted", tatEffectiveDate);
                    }


                    if (manifestToUpdateResults == null) {
                        System.out.println("Lab Results Get: There are no manifests to pull results for");
                        log.info("Lab Results Get: There are no manifests to pull results for");

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
                        System.out.println("Lab Results Get: The manifest : " + manifestToUpdateResults.getId() + " in queue has no pending samples. It will be marked as incomplete");

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

                    System.out.println("Lab Results Get: Polling results for  " + ordersWithPendingResults.size() + " samples in currently processing manifest with id :" + manifestToUpdateResults.getId());
                }

                if (ordersWithPendingResults.size() < 1) { // exit there are no pending samples
                    System.out.println("Lab Results Get: No pending samples Exiting");
                    return;
                }

                // SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                //         SSLContexts.createDefault(),
                //         new String[]{"TLSv1.2"},
                //         null,
                //         SSLConnectionSocketFactory.getDefaultHostnameVerifier());

                // CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                List<Integer> orderIds = new ArrayList<Integer>();
                List<Integer> manifestOrderIds = new ArrayList<Integer>();
                for (LabManifestOrder manifestOrder : ordersWithPendingResults) {
                    orderIds.add(manifestOrder.getOrder().getOrderId());
                    manifestOrderIds.add(manifestOrder.getId());
                }

                // Pull Lab Results
                LabWebRequest pullRequest;

                //if (LabOrderDataExchange.isEidVlLabSystem()) {
                if(manifestToUpdateResults.getManifestType() == LabManifest.EID_TYPE) {
                    pullRequest = new EIDVLLabSystemWebRequest();
                } else {
                    pullRequest = new LabwareSystemWebRequest();
                }
                pullRequest.pullResult(orderIds, manifestOrderIds, manifestToUpdateResults);

            } catch (Exception e) {
                throw new IllegalArgumentException("Lab Results Get: Unable to execute task that pulls lab requests", e);
            } finally {
                Context.closeSession();
            }
        } catch (IOException ioe) {

            try {
                String text = "Lab Results Get: At " + new Date() + " there was an error reported connecting to the internet. The system did not attempt checking for viral load results ";
                log.warn(text);
            }
            catch (Exception e) {
                log.error("Lab Results Get: Failed to check internet connectivity", e);
            }
        }
    }

}
