/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.cashier.api.BillLineItemService;
import org.openmrs.module.kenyaemr.cashier.api.model.BillLineItem;
import org.openmrs.module.kenyaemr.cashier.api.model.BillStatus;
import org.openmrs.module.kenyaemr.cashier.api.search.BillItemSearch;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.util.PrivilegeConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class labsUtils {
	public static String INPATIENT = "a73e2ac6-263b-47fc-99fc-e0f2c09fc914";
	/**
	 * Format gender
	 *
	 * @param input
	 * @return
	 */
	public static String formatGender(String input) {
		String in = input.trim().toLowerCase();
		if (in.equalsIgnoreCase("m"))
			return ("M");
		if (in.equalsIgnoreCase("f"))
			return ("F");
		return ("M");
	}

	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		return new SimpleDateFormat(pattern);
	}

	/**
	 * Creates a node factory
	 *
	 * @return
	 */
	public static JsonNodeFactory getJsonNodeFactory() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		return factory;
	}
	/**
	 * Returns a list of active lab orders IDs
	 *	
	 * @return
	 */
	public static List<Integer> getOrderIdsForActiveOrders(String fetchDate) {		
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		List<Integer> activeLabs = new ArrayList<Integer>();
		String sql = "select o.order_id from orders o\t\n" +
			"           inner join order_type ot on ot.order_type_id = o.order_type_id and ot.uuid = '52a447d3-a64a-11e3-9aeb-50e549534c5e'\n" +
			"where o.order_action='NEW' and o.concept_id not in (856,5497) and o.date_stopped is null and o.voided=0 and o.date_created >= '" + fetchDate + "' ";
		
		System.out.println("LIMS checking Active orders: Now executing SQL: " + sql);
		List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
		if (!activeOrders.isEmpty()) {
			for (List<Object> res : activeOrders) {
				Integer orderId = (Integer) res.get(0);
				activeLabs.add(orderId);
			}
		}
		Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		System.out.println("Active Labs For LIMS: " + activeOrders.size());
		return activeLabs;
	}

	/**
	 * Checks if an order has a bill, and checks the bill status.
	 * If bill status is PENDING and method of payment captured during checkin in not express, then the order is not submitted to LIMS
	 * @param order
	 * @return
	 */
	public static boolean orderHasUnsettledBill(Order order) {
		BillLineItem billItemSearch = new BillLineItem();
		billItemSearch.setOrder(order);
		BillLineItemService billLineItemService = Context.getService(BillLineItemService.class);
		List<BillLineItem> result = billLineItemService.fetchBillItemByOrder(new BillItemSearch(billItemSearch, false));
		BillLineItem lineItem = result != null && !result.isEmpty() ? result.get(0) : null;// default to the first item
		if (lineItem != null && lineItem.getPaymentStatus().equals(BillStatus.PENDING)) {// all other statuses should allow data to move to LIMS
			return true;
		}
		return false;
	}

	/**
	 * Checks the express status of a patient based on check-in details.
	 * Express payment methods should be a configurable global property.
	 * @should return true if a patient is checked in with an express payment method
	 * @should return true if an order is marked as emergency
	 * @should return true if an order's visit is inpatient
	 * @param order
	 * @return
	 */
	public static boolean isOrderForExpressPatient(Order order) {
		Visit activeVisit = order.getEncounter().getVisit();
		if (activeVisit != null && activeVisit.getVisitType().getUuid().equals(INPATIENT)) {
			return true;
		}

		VisitAttribute visitPaymentMethod = activeVisit.getActiveAttributes().stream().filter(attr -> attr.getAttributeType().getUuid().equalsIgnoreCase(ModuleConstants.VISIT_ATTRIBUTE_PAYMENT_METHOD_UUID)).findFirst().orElse(null);
		GlobalProperty expressPaymentMethodsConfig = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EXPRESS_PAYMENT_METHODS);
		String expressPaymentMethodsString = expressPaymentMethodsConfig.getPropertyValue();

		if (visitPaymentMethod != null && !StringUtils.isBlank(expressPaymentMethodsString)) {
			String [] expressPaymentMethodUuids = expressPaymentMethodsString.split(",");
			if (expressPaymentMethodUuids.length > 0) {
				boolean isExpressPatient =  Arrays.stream(expressPaymentMethodUuids).anyMatch(visitPaymentMethod.getValueReference().trim()::equals);
				if (isExpressPatient) {
					return true;
				}
			}
		}
		return false;
	}
}
