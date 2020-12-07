package org.openmrs.module.kenyaemrorderentry.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.List;

/**
 * Prepares payload and performs remote login to CHAI system
 */
public class PushLabRequestsTask extends AbstractTask {
    private Log log = LogFactory.getLog(getClass());
    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        Context.openSession();
        try {

            if (!Context.isAuthenticated()) {
                authenticate();
            }
            GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_REQUEST_URL);
            GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);

            String serverUrl = gpServerUrl.getPropertyValue();
            String API_KEY = gpApiToken.getPropertyValue();

            if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
                System.out.println("Please set credentials for posting lab requests to the lab system");
                return;
            }

            // Get a manifest ready to be sent
            LabManifest readyManifest = kenyaemrOrdersService.getLabOrderManifestByStatus("Ready to send");
            LabManifest currentlyProcessingManifest = kenyaemrOrdersService.getLabOrderManifestByStatus("Sending");


            if (readyManifest == null && currentlyProcessingManifest == null) {
                System.out.println("There are no viral load request to push to the lab system");
                return;
            }

            List<LabManifestOrder> ordersInManifest = null;
            if (readyManifest != null) {
                ordersInManifest = kenyaemrOrdersService.getLabManifestOrdersToSend(readyManifest);
            } else if (currentlyProcessingManifest != null){
                ordersInManifest = kenyaemrOrdersService.getLabManifestOrdersToSend(currentlyProcessingManifest);
            }

            if (ordersInManifest.size() < 1) {
                System.out.println("Found no lab requests to post. Will mark the manifest as complete");
                if (readyManifest != null) {
                    readyManifest.setStatus("Completed");
                    kenyaemrOrdersService.saveLabOrderManifest(readyManifest);
                } else {
                    currentlyProcessingManifest.setStatus("Completed");
                    kenyaemrOrdersService.saveLabOrderManifest(currentlyProcessingManifest);
                }
                return;
            } else {
                System.out.println("No of labs to push: " + ordersInManifest.size());
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try  {
                //Define a postRequest request
                HttpPost postRequest = new HttpPost(serverUrl);

                //Set the API media type in http content-type header
                postRequest.addHeader("content-type", "application/json");
                postRequest.addHeader("apikey", API_KEY);

                for (LabManifestOrder manifestOrder : ordersInManifest) {

                    //Set the request post body
                    String payload = manifestOrder.getPayload();
                    StringEntity userEntity = new StringEntity(payload);
                    postRequest.setEntity(userEntity);

                    //Send the request; It will immediately return the response in HttpResponse object if any
                    HttpResponse response = httpClient.execute(postRequest);

                    //verify the valid error code first
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 201) {
                        JSONParser parser = new JSONParser();
                        JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
                        JSONObject errorObj = (JSONObject) responseObj.get("error");
                        manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
                       // throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
                    } else {
                        manifestOrder.setStatus("Sent");
                        System.out.println("Successfully executed the task that pushes lab requests");
                        log.info("Successfully executed the task that pushes lab requests");
                    }
                    kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                    if (readyManifest != null && !readyManifest.getStatus().equals("Sending")) {
                        readyManifest.setStatus("Sending");
                        kenyaemrOrdersService.saveLabOrderManifest(readyManifest);
                    } else if (currentlyProcessingManifest != null && !currentlyProcessingManifest.getStatus().equals("Sending")){
                        currentlyProcessingManifest.setStatus("Sending");
                        kenyaemrOrdersService.saveLabOrderManifest(currentlyProcessingManifest);
                    }
                }
            } catch (Exception e) {
                log.info("Could not push requests to the lab! " + e.getCause());
                e.printStackTrace();
            }
            finally {
                httpClient.close();
            }

        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to execute task that pushes lab requests", e);
        }
    }
}
