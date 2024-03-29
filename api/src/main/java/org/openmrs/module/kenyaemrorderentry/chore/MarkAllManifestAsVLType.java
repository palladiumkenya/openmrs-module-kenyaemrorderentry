/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.chore;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * The module has been updated to handle both EID and VL.
 * We want to mark all existing manifests to VL type
 * EID = 1
 * VL = 2
 *
 */
@Component("kemrorder.chore.markAllManifestAsVL")
public class MarkAllManifestAsVLType extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter out) {
        String updateSql = "update kenyaemr_order_entry_lab_manifest set manifest_type = 2;";
        Context.getAdministrationService().executeSQL(updateSql, false);
        out.println("Completed executing task that sets manifest type to VL");

    }
}
