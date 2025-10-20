package org.openmrs.module.kenyaemrorderentry.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LimsSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueue;
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueueStatus;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Prepares payload and performs remote login to Labware system
 */
public class PullLimsFacilityWideLabResultsTask extends AbstractTask {

	private static final Logger log = LoggerFactory.getLogger(PullLimsFacilityWideLabResultsTask.class);
	private String url = "http://www.google.com:80/index.html";


	KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		System.out.println("PULL Lims Lab Results and persist to EMR: PULL TASK Starting");
		Context.openSession();
		// check first if there is internet connectivity before pushing
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.connect();
			try {

				String limsIntegrationEnabled = "";
				GlobalProperty enableLimsIntegration = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_ENABLE_LIMS_INTEGRATION);
				limsIntegrationEnabled = enableLimsIntegration.getPropertyValue().trim();
				if (limsIntegrationEnabled.equals("false")) {
					return;
				} else {
					Date currentDate = new Date();
					currentDate.setTime(System.currentTimeMillis());
					Date startOfDayMidnight = new Date(currentDate.getTime() - (1000 * 60 * 60 * 24));
					Date midnightDateTime = OpenmrsUtil.getLastMomentOfDay(startOfDayMidnight);

					KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
					List<LimsQueue> testsWithPendingResults = kenyaemrOrdersService.getLimsQueueEntriesByStatus(LimsQueueStatus.SUBMITTED, midnightDateTime, null, false);
					List<Integer> activeTestOrders = new ArrayList<>();
					System.out.println("Lab tests pending results from LIMS: " + activeTestOrders.size());
					if (testsWithPendingResults.size() > 0) {

						for (LimsQueue queueEntry : testsWithPendingResults) {
							activeTestOrders.add(queueEntry.getOrder().getOrderId());
						}
						if (activeTestOrders.size() < 1) {
							System.out.println("LIMS-EMR integration: there are no lab tests with pending results");
							return;
						}

					}
				}

			} catch (Exception e) {
				throw new IllegalArgumentException("Lab Results PULL: Unable to execute task that pulls viral load lab manifest", e);
			} finally {
				Context.closeSession();
			}
		} catch (IOException ioe) {

			try {
				String text = "Lims Lab Results PULL: At " + new Date() + " there was an error reported connecting to the internet. Will not attempt pulling labware lab tests ";
				System.err.println(text);
				log.warn(text);
			} catch (Exception e) {
				System.err.println("Lims Lab Results PULL: Failed to check internet connectivity" + e.getMessage());
				log.error("Lims lab Results POST: Failed to check internet connectivity", e);
			}
		}
	}
}
