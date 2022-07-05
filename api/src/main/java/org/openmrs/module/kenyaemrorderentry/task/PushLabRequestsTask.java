package org.openmrs.module.kenyaemrorderentry.task;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.ChaiSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabwareSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

/**
 * Prepares payload and performs remote login to CHAI system
 */
public class PushLabRequestsTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(PushLabRequestsTask.class);
    private String url = "http://www.google.com:80/index.html";


    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

    /**
     * @see AbstractTask#execute()
     */
    public void execute() {
        System.out.println("Push Lab Results: PUSH TASK Starting");
        Context.openSession();

        // check first if there is internet connectivity before pushing

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            try {
                // GlobalProperty gpServerUrl = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_REQUEST_URL);
                // GlobalProperty gpApiToken = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SERVER_API_TOKEN);

                // String serverUrl = gpServerUrl.getPropertyValue();
                // String API_KEY = gpApiToken.getPropertyValue();

                // if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(API_KEY)) {
                //     System.out.println("Lab Results POST: Please set credentials for posting lab requests to the lab system");
                //     return;
                // }

                // Get a manifest ready to be sent
                LabManifest toProcess = null;
                String manifestStatus = "";
                toProcess = kenyaemrOrdersService.getLabOrderManifestByStatus("Currently submitting");
                if (toProcess == null) {
                    toProcess = kenyaemrOrdersService.getLabOrderManifestByStatus("Submit");
                    if (toProcess != null) {
                        manifestStatus = "Submit";
                    }
                } else {
                    manifestStatus = "Currently submitting";
                }

                if (toProcess == null) {
                    System.out.println("Lab Results POST: There are no viral load requests to push to the lab system");
                    return;
                }

                List<LabManifestOrder> ordersInManifest = kenyaemrOrdersService.getLabManifestOrdersToSend(toProcess);
                
                if (ordersInManifest.size() < 1) {
                    System.out.println("Lab Results POST: Found no lab requests to post.");
                    // System.out.println("Lab Results POST: Found no lab requests to post. Will mark the manifest as complete");
                    // if (toProcess != null) {
                    //     toProcess.setStatus("Completed");
                    //     kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                    // }
                    return;
                } else {
                    System.out.println("Lab Results POST: Number of lab requests to push: " + ordersInManifest.size());
                    if (toProcess != null) {
                        toProcess.setStatus("Sending");
                        kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                    }
                }

                boolean checkIfSent = true;
                for (LabManifestOrder manifestOrder : ordersInManifest) {

                    LabWebRequest postRequest;

                    if (LabOrderDataExchange.getSystemType() == LabOrderDataExchange.CHAI_SYSTEM) {
                        postRequest = new ChaiSystemWebRequest();
                    } else {
                        postRequest = new LabwareSystemWebRequest();
                    }
                    if(!postRequest.postSamples(manifestOrder, manifestStatus)) {
                        checkIfSent = false;
                    }
                }

                if(checkIfSent) {
                    System.out.println("Lab Results POST: All orders were sent. Marking manifest as submitted");
                    if (toProcess != null) {
                        toProcess.setStatus("Submitted");
                        toProcess.setDispatchDate(new Date()); // set dispatch date to today
                        kenyaemrOrdersService.saveLabOrderManifest(toProcess);
                    }
                }

            } catch (Exception e) {
                throw new IllegalArgumentException("Lab Results POST: Unable to execute task that pushes viral load lab manifest", e);
            } finally {
                Context.closeSession();
            }
        } catch (IOException ioe) {

            try {
                String text = "Lab Results POST: At " + new Date() + " there was an error reported connecting to the internet. Will not attempt pushing viral load manifest ";
                System.err.println(text);
                log.warn(text);
            } catch (Exception e) {
                System.err.println("Lab Results POST: Failed to check internet connectivity" + e.getMessage());
                log.error("Lab Results POST: Failed to check internet connectivity", e);
            }
        }
    }
}
