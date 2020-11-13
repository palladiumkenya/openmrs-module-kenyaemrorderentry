package org.openmrs.module.kenyaemrorderentry.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

/**
 * Created by developer on 10 Nov, 2020
 */
public class PushLabRequestsTask extends AbstractTask {
    private Log log = LogFactory.getLog(getClass());

    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        Context.openSession();
        try {

            if (!Context.isAuthenticated()) {
                authenticate();
            }
            GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject("chai.server_url");
            GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject("chai.api_token");

            String serverUrl = gpServerUrl.getPropertyValue();
            String API_KEY = gpApiToken.getPropertyValue();

            if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
                System.out.println("Please set credentials for posting lab requests to the lab system");
                return;
            }

            GlobalProperty lastOrderEntry = Context.getAdministrationService().getGlobalPropertyObject("lab.lastUpdate");
            String lastOrdersql = "select max(order_id) last_id from orders where voided=0;";
            List<List<Object>> lastOrderId = Context.getAdministrationService().executeSQL(lastOrdersql, true);

            Integer lastId = (Integer) lastOrderId.get(0).get(0);
            lastId = lastId != null ? lastId : 0;

            String lastOrderIdStr = lastOrderEntry != null && lastOrderEntry.getValue() != null ? lastOrderEntry.getValue().toString() : "";
            Integer gpLastOrderId = StringUtils.isNotBlank(lastOrderIdStr) ? Integer.parseInt(lastOrderIdStr) : 0;

            LabOrderDataExchange e = new LabOrderDataExchange();
            ObjectNode samplesWrapper = e.getLabRequests(null, null);
            ArrayNode samples = (ArrayNode) samplesWrapper.get("samples");

            if (samples.size() < 1) {
                System.out.println("Found no lab requests to post. Skipping the post operation");
                return;
            }

            String payload = samplesWrapper.toString();

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try
            {
                //Define a postRequest request
                HttpPost postRequest = new HttpPost(serverUrl);

                //Set the API media type in http content-type header
                postRequest.addHeader("content-type", "application/json");
                postRequest.addHeader("apikey", API_KEY);

                //Set the request post body
                StringEntity userEntity = new StringEntity(payload);
                postRequest.setEntity(userEntity);

                //Send the request; It will immediately return the response in HttpResponse object if any
                HttpResponse response = httpClient.execute(postRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 201)
                {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                }
                System.out.println("Successfully executed the task that pushes lab requests");
                log.info("Successfully executed the task that pushes lab requests");

                lastOrderEntry.setPropertyValue(lastId.toString());
                Context.getAdministrationService().saveGlobalProperty(lastOrderEntry);


            }
            finally
            {
                //Important: Close the connect
                httpClient.close();
            }

        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to execute task that pushes lab requests", e);
        }
    }
}
