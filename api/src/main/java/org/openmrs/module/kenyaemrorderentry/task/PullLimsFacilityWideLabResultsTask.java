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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This task pulls Lims lab results from the lims lab system.
 */
public class PullLimsFacilityWideLabResultsTask extends AbstractTask {
	private Log log = LogFactory.getLog(getClass());
	private String url = "http://www.google.com:80/index.html";

	KenyaemrOrdersService orderService = Context.getService(KenyaemrOrdersService.class);

	/**
	 * @see AbstractTask#execute()
	 */
	public void execute() {
		System.out.println("Get Lims Lab Results: PULL TASK Starting");

		Context.openSession();
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
				try {
					URLConnection connection = new URL(url).openConnection();
					connection.connect();
					try {
						System.out.println("LIMS-EMR integration: lab tests with pending results:  " + activeTestOrders.size());
						LimsSystemWebRequest.pullFacilityWideLimsLabResult(activeTestOrders);

					} catch (Exception e) {
						throw new IllegalArgumentException("Lab Results Get: Unable to execute task that pulls lab requests", e);
					} finally {
						Context.closeSession();
					}
				} catch (IOException ioe) {

					try {
						String text = "LIMS-EMR integration: At " + new Date() + " there was an error reported connecting to the internet. ";
						log.warn(text);
					} catch (Exception e) {
						log.error("LIMS-EMR integration: Failed to check internet connectivity", e);
					}
				}
			}
		}
	}

}
