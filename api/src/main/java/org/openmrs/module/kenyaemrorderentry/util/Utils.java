package org.openmrs.module.kenyaemrorderentry.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {

    public static final String GP_ORDER_ENTRY_CONFIG_DIR = "kenyaemrorderentry.drugsMappingDirectory";
    public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    public static final String DRUG_REGIMEN_EDITOR_FORM = "da687480-e197-11e8-9f32-f2801f1b9fd1";
    public static final String DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE = "7dffc392-13e7-11e9-ab14-d663bd873d93";
    public static final String TEST_ORDER_TYPE_UUID = "1814ee89-2abf-42d7-920b-d138740d56d4";
    public static final Locale LOCALE = Locale.ENGLISH;
    public static final String GP_IL_CONFIG_DIR = "kenyaemrIL.drugsMappingDirectory";
    public static final String MCH_MOTHER_SERVICE_PROGRAM = "b5d9e05f-f5ab-4612-98dd-adb75438ed34";


    public static PatientIdentifierType getUniquePatientNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);

    }

    /**
     * gets default location from global property
     * @return
     */
    public static Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            return gp != null ? ((Location) gp.getValue()) : null;
        }
        finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
        }

    }


    public static String getDefaultLocationMflCode(Location location) {
        String MASTER_FACILITY_CODE = "8a845a89-6aa5-4111-81d3-0af31c45c002";

        if(location == null) {
            location = getDefaultLocation();
        }
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            for (LocationAttribute attr : location.getAttributes()) {
                if (attr.getAttributeType().getUuid().equals(MASTER_FACILITY_CODE) && !attr.isVoided()) {
                    return attr.getValueReference();
                }
            }
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
        }
        return null;
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }



    public static OrderType getLabOrderType() {
        return Context.getOrderService().getOrderTypeByUuid(TEST_ORDER_TYPE_UUID);
    }

    public static JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }



    public static String fetchRequestBody(BufferedReader reader) {
        String requestBodyJsonStr = "";
        try {

            BufferedReader br = new BufferedReader(reader);
            String output = "";
            while ((output = reader.readLine()) != null) {
                requestBodyJsonStr += output;
            }
        } catch (IOException e) {

            System.out.println("IOException: " + e.getMessage());

        }
        return requestBodyJsonStr;
    }
}


