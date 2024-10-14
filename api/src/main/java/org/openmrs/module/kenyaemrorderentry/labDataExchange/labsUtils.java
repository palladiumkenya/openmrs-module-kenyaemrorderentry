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
	public static List<Integer> getOrderIdsForActiveOrders() {		
		Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		List<Integer> activeLabs = new ArrayList<Integer>();
		String sql = "select o.order_id from orders o\t\n" +
			"           inner join order_type ot on ot.order_type_id = o.order_type_id and ot.uuid = '52a447d3-a64a-11e3-9aeb-50e549534c5e'\n" +
			"where o.order_action='NEW' and o.date_stopped is null and o.voided=0 ";
		
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
	 * Mappings for Lab Test Codes and concepts
	 *
	 * @return
	 */
	static String limsLabTestIdCodesConverter(Concept key) {
		Map<Concept, String> labTestsIdCodes = new HashMap<Concept, String>();
		labTestsIdCodes.put(conceptService.getConcept(161500), "596");
		labTestsIdCodes.put(conceptService.getConcept(848), "609");
		labTestsIdCodes.put(conceptService.getConcept(785), "3748");		
		labTestsIdCodes.put(conceptService.getConcept(300), "513");
		labTestsIdCodes.put(conceptService.getConcept(161478), "607");
		labTestsIdCodes.put(conceptService.getConcept(161154), "1803");
		labTestsIdCodes.put(conceptService.getConcept(166038), "1165");
		labTestsIdCodes.put(conceptService.getConcept(730), "589");
		labTestsIdCodes.put(conceptService.getConcept(790), "595");
		labTestsIdCodes.put(conceptService.getConcept(161233), "865");
		labTestsIdCodes.put(conceptService.getConcept(159647), "683");
		labTestsIdCodes.put(conceptService.getConcept(159607), "624");
		labTestsIdCodes.put(conceptService.getConcept(855), "619");
		labTestsIdCodes.put(conceptService.getConcept(160912), "1957");
		labTestsIdCodes.put(conceptService.getConcept(1019), "3845");
		labTestsIdCodes.put(conceptService.getConcept(159829), "2235");
		labTestsIdCodes.put(conceptService.getConcept(163620), "1860");
		labTestsIdCodes.put(conceptService.getConcept(21), "1625");
		labTestsIdCodes.put(conceptService.getConcept(159644), "3059");
		labTestsIdCodes.put(conceptService.getConcept(159430), "587");
		labTestsIdCodes.put(conceptService.getConcept(167810), "944");
		labTestsIdCodes.put(conceptService.getConcept(163722), "585");
		labTestsIdCodes.put(conceptService.getConcept(165552), "515");
		labTestsIdCodes.put(conceptService.getConcept(159606), "625");
		labTestsIdCodes.put(conceptService.getConcept(654), "1620");
		labTestsIdCodes.put(conceptService.getConcept(1006), "684");
		labTestsIdCodes.put(conceptService.getConcept(159362), "629");
		labTestsIdCodes.put(conceptService.getConcept(163594), "615");
		labTestsIdCodes.put(conceptService.getConcept(1000071), "1188");
		labTestsIdCodes.put(conceptService.getConcept(45), "1394");
		labTestsIdCodes.put(conceptService.getConcept(161154), "658");
		labTestsIdCodes.put(conceptService.getConcept(160916), "1889");
		labTestsIdCodes.put(conceptService.getConcept(1000443), "511");
		labTestsIdCodes.put(conceptService.getConcept(161470), "606");
		labTestsIdCodes.put(conceptService.getConcept(161467), "630");
		labTestsIdCodes.put(conceptService.getConcept(160225), "622");
		labTestsIdCodes.put(conceptService.getConcept(1000451), "3764");
		labTestsIdCodes.put(conceptService.getConcept(161503), "3889");
		labTestsIdCodes.put(conceptService.getConcept(790), "1551");
		labTestsIdCodes.put(conceptService.getConcept(163699), "2202");
		labTestsIdCodes.put(conceptService.getConcept(655), "642");
		labTestsIdCodes.put(conceptService.getConcept(163680), "1583");
		labTestsIdCodes.put(conceptService.getConcept(1133), "613");
		labTestsIdCodes.put(conceptService.getConcept(161504), "588");
		labTestsIdCodes.put(conceptService.getConcept(160922), "2237");
		labTestsIdCodes.put(conceptService.getConcept(161454), "638");
		labTestsIdCodes.put(conceptService.getConcept(167207), "634");
		labTestsIdCodes.put(conceptService.getConcept(161453), "644");
		labTestsIdCodes.put(conceptService.getConcept(159607), "1236");
		
		return labTestsIdCodes.get(key);
	}

}
