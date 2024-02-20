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

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.hibernate.jdbc.Work;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.stereotype.Component;

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
        InputStream is = null;

        try {
            // Load the stored procedure sql script from resources
            is = OpenmrsClassLoader.getInstance().getResourceAsStream("sql/sp_vl_LDL_to_Zero.sql");
			String updateProcedureSql = IOUtils.toString(is, "UTF-8");
            
            // Refresh or Drop the stored procedure
            String refreshTaskSQL = "DROP PROCEDURE IF EXISTS sp_vl_LDL_to_Zero";
            Context.getAdministrationService().executeSQL(refreshTaskSQL, false);

            // Execute the stored procedure
            callLdlToZero(output, updateProcedureSql);

            // Destroy the stored procedure
            Context.getAdministrationService().executeSQL(refreshTaskSQL, false);

            System.out.println("Completed migrating VL LDL results to zero quantitative");
            output.println("Completed migrating VL LDL results to zero quantitative");
        } catch(Exception ex) {
            System.err.println("Order Entry: ERROR: ConvertAllLDLlabResultsIntoZero chore: " + ex.getMessage());
            output.println("Order Entry: ERROR: ConvertAllLDLlabResultsIntoZero chore: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private SimpleObject callLdlToZero(PrintWriter output, String procedure) {
        final SimpleObject ret = new SimpleObject();
        final PrintWriter display = output;
        final String storedProcedure = procedure;

        DbSessionFactory sf = Context.getRegisteredComponents(DbSessionFactory.class).get(0);
        
        try {
            Context.openSession();
            sf.getCurrentSession().doWork(new Work() {

                @Override
                public void execute(Connection connection) throws SQLException {

                    // Create the stored procedure
                    // CallableStatement createSP = connection.prepareCall(storedProcedure);
                    // createSP.execute();
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(storedProcedure);
                    statement.close();

                    String sb = "{call `sp_vl_LDL_to_Zero`()}";
                    System.out.println("Order Entry LDL to Zero: currently executing: " + sb);
                    display.println("Order Entry LDL to Zero: currently executing: " + sb);
                    CallableStatement sp = connection.prepareCall(sb);
                    sp.execute();

                    System.out.println("Order Entry LDL to Zero: Successfully completed LDL to Zero task ... ");
                    display.println("Order Entry LDL to Zero: Successfully completed LDL to Zero task ... ");
                }
            });
        } catch (Exception ex) {
            System.err.println("Order Entry LDL to Zero: ERROR: " + ex.getMessage());
            display.println("Order Entry LDL to Zero: ERROR: " + ex.getMessage());
            ex.printStackTrace();
            throw new IllegalArgumentException("Order Entry LDL to Zero: Unable to execute query", ex);
        } finally {
            Context.closeSession();
        }
        ret.put("data", SimpleObject.create("status", true));

        return ret;
    }

}
