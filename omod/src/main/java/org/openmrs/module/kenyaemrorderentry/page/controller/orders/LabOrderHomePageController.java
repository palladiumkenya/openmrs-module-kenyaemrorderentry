/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import org.openmrs.Patient;
import org.openmrs.module.kenyaemrorderentry.util.OrderEntryUIUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageContext;
import org.openmrs.ui.framework.page.PageModel;

/**
 * Homepage for the drug order app
 */
@AppPage("kenyaemr.drugorder")
public class LabOrderHomePageController {

	public String controller(UiUtils ui, PageModel model, PageContext pageContext) {

		Patient patient = (Patient) model.getAttribute("currentPatient");
		OrderEntryUIUtils.setDrugOrderPageAttributes(pageContext, OrderEntryUIUtils.APP_LAB_ORDER);

		if (patient != null) {
			return "redirect:" + ui.pageLink("kenyaemrorderentry", "labOrders", SimpleObject.create("patientId", patient.getPatientId(), "currentApp","kenyaemr.laborder"));
		} else {
			return null;
		}
	}
}