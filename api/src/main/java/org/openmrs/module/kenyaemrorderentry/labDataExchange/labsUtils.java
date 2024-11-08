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
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.PrivilegeConstants;

import java.text.SimpleDateFormat;
import java.util.*;

public class labsUtils {
	static ConceptService conceptService = Context.getConceptService();
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
}
