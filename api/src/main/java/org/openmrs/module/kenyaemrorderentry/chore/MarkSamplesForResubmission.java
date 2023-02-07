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
 * We want to resubmit samples that couldn't be sent due to internal errors in the lab system
 *
 */
@Component("kemrorder.chore.MarkSamplesForResubmission")
public class MarkSamplesForResubmission extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter out) {
        String updateManifestSql = "update kenyaemr_order_entry_lab_manifest m " +
                "inner join kenyaemr_order_entry_lab_manifest_order mo on m.id = mo.manifest_id " +
                "set m.status = 'Ready to send' where m.status = 'Submitted' and mo.status like 'Error - 500%';";

        String updateManifestSamplesSql = "update kenyaemr_order_entry_lab_manifest_order mo " +
                "inner join kenyaemr_order_entry_lab_manifest m on m.id = mo.manifest_id " +
                "set mo.status = 'Pending' where mo.status like 'Error - 500%' and m.status = 'Ready to send';";

        Context.getAdministrationService().executeSQL(updateManifestSql, false);
        Context.getAdministrationService().executeSQL(updateManifestSamplesSql, false);
        out.println("Marked samples for resubmission");

    }
}
