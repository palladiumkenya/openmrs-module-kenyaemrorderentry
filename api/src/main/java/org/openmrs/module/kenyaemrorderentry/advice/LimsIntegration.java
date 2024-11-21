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
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueueStatus;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Automates the process of generating payload for LIMS-EMR data exchange
 */
public class LimsIntegration implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		String limsIntegrationEnabled = "";
		GlobalProperty enableLimsIntegration = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_ENABLE_LIMS_INTEGRATION);
		limsIntegrationEnabled = enableLimsIntegration.getPropertyValue().trim();
		if (limsIntegrationEnabled.equalsIgnoreCase("false")) {
			return;
		} else if (limsIntegrationEnabled.equalsIgnoreCase("true")) {
			try {
				// Extract the Order object from the arguments
				if (method.getName().equals("saveOrder") && args.length > 0 && args[0] instanceof Order) {
					Order order = (Order) args[0];

					if (order == null) {
						return;
					}

					if (order instanceof TestOrder) {
						// Exclude discontinuation orders as well
						if (order.getAction().equals(Order.Action.DISCONTINUE)
								|| order.getAction().equals(Order.Action.REVISE)
								|| order.getAction().equals(Order.Action.RENEW)) {
							return;
						}

						LimsSystemWebRequest limsSystemWebRequest = new LimsSystemWebRequest();
						JSONObject limsPayload = limsSystemWebRequest.generateLIMSpostPayload(order);

						if (!limsPayload.isEmpty()) {
						KenyaemrOrdersService service = Context.getService(KenyaemrOrdersService.class);
						LimsQueue limsQueue = new LimsQueue();
						limsQueue.setDateSent(new Date());
						limsQueue.setOrder(order);
						limsQueue.setPayload(limsPayload.toJSONString());

							try {
								LimsSystemWebRequest.postLabOrderRequestToLims(limsPayload.toJSONString());
								limsQueue.setStatus(LimsQueueStatus.SUBMITTED);
								service.saveLimsQueue(limsQueue);
							} catch (Exception e) {
								limsQueue.setStatus(LimsQueueStatus.QUEUED);
								service.saveLimsQueue(limsQueue);
								System.out.println(e.getMessage());
							}
						} else {
							System.err.println("LIMS-EMR integration: Could not generate the payload for LIMS data exchange");
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
