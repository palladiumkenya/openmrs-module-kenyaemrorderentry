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
 * Previous version of the module used global properties which have since been removed.
 * This chore copies any values from the previous gp then deletes them from the db
 *
 */
@Component("kemrorder.chore.RemoveRedundantGlobalProperties")
public class RemoveRedundantGlobalProperties extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter out) {

        String updateSql4 = "delete from global_property where property in ('chai.viral_load_server_url', 'chai.viral_load_server_result_url', 'chai.viral_load_server_api_token');";
        Context.getAdministrationService().executeSQL(updateSql4, false);
        out.println("Successfully completed setting lab manifest global properties");

    }
}
