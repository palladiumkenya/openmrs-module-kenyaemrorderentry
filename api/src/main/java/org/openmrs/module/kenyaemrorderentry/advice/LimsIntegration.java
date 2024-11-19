/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LimsSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueue;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * Automates the process of generating LIMS lab order payload on saving a lab order
 */
public class LimsIntegration implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());
	public static String LIMS_QUEUE_PENDING_STATUS = "PENDING";
	public static String LIMS_QUEUE_SUBMITTED_STATUS = "SUBMITTED";
	public static String LIMS_QUEUE_ERROR_STATUS = "ERROR";

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		String limsIntegrationEnabled = "";
		GlobalProperty enableLimsIntegration = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_ENABLE_LIMS_INTEGRATION);
		limsIntegrationEnabled = enableLimsIntegration.getPropertyValue().trim();
		/*if (limsIntegrationEnabled.equals("false")) {
			return;
		} else */if (true) {//limsIntegrationEnabled.equals("true")
			try {
				// Extract the Order object from the arguments
				if (method.getName().equals("saveOrder") && args.length > 0 && args[0] instanceof Order) {
					Order order = (Order) args[0];

					if (order == null) {
						return;
					}
					// Exclude discontinuation orders as well
					if (order.getAction().equals(Order.Action.DISCONTINUE)
						|| order.getAction().equals(Order.Action.REVISE)
						|| order.getAction().equals(Order.Action.RENEW)) {
						return;
					}
					if (order instanceof TestOrder) {

						Date nextProcessingDate = new Date();
						nextProcessingDate.setTime(System.currentTimeMillis());
						Date startOfDayMidnight = new Date(nextProcessingDate.getTime() - (1000 * 60 * 60 * 24));
						Date midnightDateTime = OpenmrsUtil.getLastMomentOfDay(startOfDayMidnight);

						KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
						List<LimsQueue> ordersWithPendingResultsFromQueue = kenyaemrOrdersService.getLimsQueueEntriesByStatus(LimsIntegration.LIMS_QUEUE_PENDING_STATUS, midnightDateTime, null, true);
						System.out.println("Start of midnight: " + midnightDateTime);
						System.out.println("No of submitted entries: " + ordersWithPendingResultsFromQueue);
						//
						LimsSystemWebRequest limsSystemWebRequest = new LimsSystemWebRequest();
						JSONObject limsPayload = limsSystemWebRequest.generateLIMSpostPayload(order);

						if (!limsPayload.isEmpty()) {
						KenyaemrOrdersService service = Context.getService(KenyaemrOrdersService.class);
						LimsQueue limsQueue = new LimsQueue();
						limsQueue.setDateSent(new Date());
						limsQueue.setOrder(order);
						limsQueue.setPayload(limsPayload.toJSONString());

							try {
								Boolean results = LimsSystemWebRequest.postLabOrderRequestToLims(limsPayload.toJSONString());
								limsQueue.setStatus(LimsIntegration.LIMS_QUEUE_SUBMITTED_STATUS);
								service.saveLimsQueue(limsQueue);
							} catch (Exception e) {
								limsQueue.setStatus(LimsIntegration.LIMS_QUEUE_PENDING_STATUS);
								service.saveLimsQueue(limsQueue);
								System.out.println(e.getMessage());
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Error intercepting order before creation: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
