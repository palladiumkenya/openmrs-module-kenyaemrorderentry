package org.openmrs.module.kenyaemrorderentry.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.*;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Fetch the last date of fetch
		String fetchDate = null;
		GlobalProperty globalPropertyObject = Context.getAdministrationService().getGlobalPropertyObject("kenyaemrorderentry.facilitywidelims.lastFetchDateAndTime");

		try {
			String ts = globalPropertyObject.getValue().toString();
			fetchDate = sd.format(formatter.parse(ts));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Context.openSession();
		System.out.println("Fetch Date ==>" + fetchDate);
		String limsIntegrationEnabled = "";
		GlobalProperty enableLimsIntegration = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_ENABLE_LIMS_INTEGRATION);
		limsIntegrationEnabled = enableLimsIntegration.getPropertyValue().trim();
		if (limsIntegrationEnabled.equals("false")) {
			return;
		} else {
			List<Integer> activeTestOrders = labsUtils.getOrderIdsForActiveOrders(fetchDate);
			System.out.println("List size of active test orders: " + activeTestOrders.size());
			if (activeTestOrders.size() > 0) {
				try {
					URLConnection connection = new URL(url).openConnection();
					connection.connect();
					try {
						// Pull Lab Results
						System.out.println("List size of active test orders is > 1: " + activeTestOrders.size());
						LimsSystemWebRequest.pullFacilityWideLimsLabResult(activeTestOrders);

					} catch (Exception e) {
						throw new IllegalArgumentException("Lab Results Get: Unable to execute task that pulls lab requests", e);
					} finally {
						Context.closeSession();
					}
				} catch (IOException ioe) {

					try {
						String text = "Lab Results Get: At " + new Date() + " there was an error reported connecting to the internet. ";
						log.warn(text);
					} catch (Exception e) {
						log.error("Lab Results Get: Failed to check internet connectivity", e);
					}
				}
			}
			//Set next fetch date start time
			Date nextProcessingDate = new Date();
			nextProcessingDate.setTime(System.currentTimeMillis());
			Date startOfDayMidnight = new Date(nextProcessingDate.getTime() - (1000 * 60 * 60 * 24));
			Date midnightDateTime = OpenmrsUtil.getLastMomentOfDay(startOfDayMidnight);

			globalPropertyObject.setPropertyValue(formatter.format(midnightDateTime));
			Context.getAdministrationService().saveGlobalProperty(globalPropertyObject);
		}
	}

}
