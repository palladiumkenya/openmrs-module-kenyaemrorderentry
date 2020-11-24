package org.openmrs.module.kenyaemrorderentry.task;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class PullViralLoadLabResultsTask extends AbstractTask {
    private Log log = LogFactory.getLog(getClass());
    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
    LabOrderDataExchange dataExchange = new LabOrderDataExchange();


    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        Context.openSession();
        try {

            if (!Context.isAuthenticated()) {
                authenticate();
            }
            GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_RESULT_URL);
            GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);

            String serverUrl = gpServerUrl.getPropertyValue();
            String API_KEY = gpApiToken.getPropertyValue();

            if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
                System.out.println("Please set credentials for posting lab requests to the lab system");
                return;
            }

            List<LabManifest> allManifest = kenyaemrOrdersService.getLabOrderManifest();

            if (allManifest.size() < 1) {
                System.out.println("There are not manifests yet! No viral load requests are  present to be pushed to CHAI system");
                return;
            }

            List<LabManifestOrder> ordersInManifest = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(allManifest.get(0), "Sent");

            if (ordersInManifest.size() < 1) {
                System.out.println("Found no lab requests to post. Will attempt again in the next schedule");
                return;
            }



            CloseableHttpClient httpClient = HttpClients.createDefault();

            try
            {
                //Define a postRequest request
                HttpPost postRequest = new HttpPost(serverUrl);

                //Set the API media type in http content-type header
                postRequest.addHeader("content-type", "application/json");
                postRequest.addHeader("apikey", API_KEY);

                // we want to create a comma separated list of order id
                List<Integer> orderIds = new ArrayList<Integer>();
                for (LabManifestOrder manifestOrder : ordersInManifest) {
                    orderIds.add(manifestOrder.getOrder().getOrderId());
                }
                ObjectNode request = Utils.getJsonNodeFactory().objectNode();
                request.put("test", "2");
                request.put("facility_code", Utils.getDefaultLocationMflCode(Utils.getDefaultLocation()));
                request.put("order_numbers", StringUtils.join(orderIds, ","));

                if (orderIds.size() < 1) {
                    System.out.println("There are no lab requests awaiting results");
                    return;
                }
                //Set the request post body
                StringEntity userEntity = new StringEntity(request.toString());
                postRequest.setEntity(userEntity);

                //Send the request; It will immediately return the response in HttpResponse object if any
                HttpResponse response = httpClient.execute(postRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                }
                String responseString = EntityUtils.toString(response.getEntity());

                JSONParser parser = new JSONParser();
                Object object = parser.parse(responseString);
                JSONObject responseObject = (JSONObject) object;

                // extract the array with results
                JSONArray resultArray = (JSONArray) responseObject.get("data");

                /*System.out.println("JSONArray:toString: " + resultArray.toString());
                System.out.println("JSONArray:toJSONString: " + resultArray.toJSONString());*/


                if (resultArray != null && !resultArray.isEmpty()) {
                    dataExchange.processIncomingViralLoadLabResults(resultArray.toJSONString());
                }

                System.out.println("Successfully executed the task that pulls lab requests");
                log.info("Successfully executed the task that pulls lab requests");
            }
            finally
            {
                //Important: Close the connect
                httpClient.close();
            }

        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to execute task that pulls lab requests", e);
        }
    }
}
