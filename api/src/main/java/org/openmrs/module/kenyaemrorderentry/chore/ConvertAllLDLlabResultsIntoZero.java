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

import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * We want to convert all VL LDL results into quantitative zeros
 *
 */
@Component("kemrorder.chore.ConvertAllLDLlabResultsIntoZero")
public class ConvertAllLDLlabResultsIntoZero extends AbstractChore {
    /**
     * @see AbstractChore#perform(PrintWriter)
     */

    @Override
    public void perform(PrintWriter output) {
        System.out.println("Order Entry: Starting the ConvertAllLDLlabResultsIntoZero chore");
        output.println("Order Entry: Starting the ConvertAllLDLlabResultsIntoZero chore");

        try {
            // Load the stored procedure sql script from resources
            String updateProcedureSql = new String(Files.readAllBytes(Paths.get(ConvertAllLDLlabResultsIntoZero.class.getResource("sql/sp_vl_LDL_to_Zero.sql").toURI())));

            // Create the stored procedure
            Context.getAdministrationService().executeSQL(updateProcedureSql, false);

            // Execute the stored procedure
            callLdlToZero();

            // Destroy the stored procedure
            String finishTaskSQL = "drop procedure sp_vl_LDL_to_Zero";
            Context.getAdministrationService().executeSQL(finishTaskSQL, false);

            System.out.println("Completed migrating VL LDL results to zero quantitative");
            output.println("Completed migrating VL LDL results to zero quantitative");
        } catch(Exception ex) {
            System.err.println("Order Entry: ERROR: ConvertAllLDLlabResultsIntoZero chore: " + ex.getMessage());
            output.println("Order Entry: ERROR: ConvertAllLDLlabResultsIntoZero chore: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private SimpleObject callLdlToZero() {
        final SimpleObject ret = new SimpleObject();

        DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
        Transaction tx = null;
        try {
            Context.openSession();
            tx = sf.getHibernateSessionFactory().getCurrentSession().beginTransaction();
            final Transaction finalTx = tx;
            sf.getCurrentSession().doWork(new Work() {

                @Override
                public void execute(Connection connection) throws SQLException {

                    StringBuilder sb = null;
                    sb = new StringBuilder();
                    sb.append("{call `vl_LDL_to_Zero`()}");
                    System.out.println("Order Entry LDL to Zero: currently executing: " + sb);
                    CallableStatement sp = connection.prepareCall(sb.toString());
                    sp.execute();

                    finalTx.commit();

                    System.out.println("Order Entry LDL to Zero: Successfully completed LDL to Zero task ... ");                   
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Order Entry LDL to Zero: Unable to execute query", e);
        } finally {
            Context.closeSession();
        }
        ret.put("data", SimpleObject.create("status", true));

        return ret;
    }

}
