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
 * We want to fill blank dispatch date from manifest end date if it's missing
 *
 */
@Component("kemrorder.chore.FillManifestDispatchDateFromEndDateIfMissing")
public class FillManifestDispatchDateFromEndDateIfMissing extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter out) {
        String updateManifestSql = "update kenyaemr_order_entry_lab_manifest m " +
                "set m.dispatch_date = date(m.end_date) where m.status = 'Submitted' and m.dispatch_date is null;";

        Context.getAdministrationService().executeSQL(updateManifestSql, false);

        out.println("Completed filling missing dispatch date for manifests");

    }
}
