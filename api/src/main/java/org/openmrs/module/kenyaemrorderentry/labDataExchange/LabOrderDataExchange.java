package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.PrivilegeConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.openmrs.module.metadatasharing.util.MetadataSharingGlobalPropertyListener.log;

public class LabOrderDataExchange {

    public static final String GP_CHAI_VL_LAB_SERVER_REQUEST_URL = "chai_vl_server_url";
    public static final String GP_CHAI_VL_LAB_SERVER_RESULT_URL = "chai_vl_server_result_url";
    public static final String GP_CHAI_VL_LAB_SERVER_API_TOKEN = "chai_vl_server_api_token";
    public static final String GP_CHAI_EID_LAB_SERVER_REQUEST_URL = "chai_eid_server_url";
    public static final String GP_CHAI_EID_LAB_SERVER_RESULT_URL = "chai_eid_server_result_url";
    public static final String GP_CHAI_EID_LAB_SERVER_API_TOKEN = "chai_eid_server_api_token";
    public static final String GP_LABWARE_VL_LAB_SERVER_REQUEST_URL = "labware_vl_server_url";
    public static final String GP_LABWARE_VL_LAB_SERVER_RESULT_URL = "labware_vl_server_result_url";
    public static final String GP_LABWARE_VL_LAB_SERVER_API_TOKEN = "labware_vl_server_api_token";
    public static final String GP_LABWARE_EID_LAB_SERVER_REQUEST_URL = "labware_eid_server_url";
    public static final String GP_LABWARE_EID_LAB_SERVER_RESULT_URL = "labware_eid_server_result_url";
    public static final String GP_LABWARE_EID_LAB_SERVER_API_TOKEN = "labware_eid_server_api_token";
    public static final String GP_MANIFEST_LAST_PROCESSED = "kemrorder.last_processed_manifest";// used when fetching results from the server
    public static final String GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS = "kemrorder.retry_period_for_incomplete_vl_result";
    public static final String GP_LAB_TAT_FOR_VL_RESULTS = "kemrorder.viral_load_result_tat_in_days";
    public static final String GP_MANIFEST_LAST_UPDATETIME = "kemrorder.manifest_last_update_time";
    public static final String MANIFEST_LAST_UPDATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String LAB_SYSTEM_DATE_PATTERN = "yyyy-MM-dd";
    public static final String GP_LAB_SYSTEM_IN_USE = "kemrorder.labsystem_identifier";

    // System Types e.g CHAI, LABWARE etc

    public static final int NO_SYSTEM_CONFIGURED = 0;
    public static final int CHAI_SYSTEM = 1;
    public static final int LABWARE_SYSTEM = 2;
    public static final Integer DEFAULT_APHL_LAB_CODE = 7;
    public static final String GP_SSL_VERIFICATION_ENABLED = "kemrorder.ssl_verification_enabled";

    ConceptService conceptService = Context.getConceptService();
    EncounterService encounterService = Context.getEncounterService();
    OrderService orderService = Context.getOrderService();
    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);


    String LAB_ENCOUNTER_TYPE_UUID = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";
    Concept vlTestConceptQualitative = conceptService.getConcept(1305);
    Concept LDLConcept = conceptService.getConcept(1302);
    Concept vlTestConceptQuantitative = conceptService.getConcept(856);
    EncounterType labEncounterType = encounterService.getEncounterTypeByUuid(LAB_ENCOUNTER_TYPE_UUID);


    /**
     * Returns an array of active lab request
     *
     * @param o
     * @return
     */
    protected ArrayNode generateActiveVLPayload(Order o, ArrayNode labTests) {
        Patient patient = o.getPatient();

        String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";

        String fullName = "";

        if (patient.getGivenName() != null) {
            fullName += patient.getGivenName();
        }

        if (patient.getMiddleName() != null) {
            fullName += " " + patient.getMiddleName();
        }

        if (patient.getFamilyName() != null) {
            fullName += " " + patient.getFamilyName();
        }

        PatientIdentifier cccNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType());
        Encounter originalRegimenEncounter = RegimenMappingUtils.getFirstEncounterForProgram(patient, "ARV");
        Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(patient, "ARV");
        SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
        String regimenName = (String) regimenDetails.get("regimenShortDisplay");
        String regimenLine = (String) regimenDetails.get("regimenLine");
        String nascopCode = "";
        if (StringUtils.isNotBlank(regimenName)) {
            nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
        }

        if (StringUtils.isBlank(nascopCode) && StringUtils.isNotBlank(regimenLine)) {
            nascopCode = RegimenMappingUtils.getNonStandardCodeFromRegimenLine(regimenLine);
        }

        //add to list only if code is found. This is a temp measure to avoid sending messages with null regimen codes
        if (StringUtils.isNotBlank(nascopCode)) {
            ObjectNode test = Utils.getJsonNodeFactory().objectNode();

            test.put("mflCode", Utils.getDefaultLocationMflCode(null));
            test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            test.put("dob", dob);
            test.put("patient_name", fullName);
            test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
            test.put("sampletype", "1");
            test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(o.getDateActivated()));
            test.put("datereceived", Utils.getSimpleDateFormat("yyyy-MM-dd").format(o.getDateActivated()));
            test.put("order_no", o.getOrderId().toString());
            test.put("lab", "");
            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");
            //test.put("justification", "1");
            test.put("prophylaxis", nascopCode);
            if (patient.getGender().equals("F")) {
                test.put("pmtct", "3");
            }
            test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
            test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");
            labTests.add(test);
        }
        return labTests;
    }

    /**
     * Returns a single object for an active lab order
     *
     * @param o
     * @param dateSampleCollected
     * @param sampleType          @return
     */
    public ObjectNode generatePayloadForLabOrder(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        Patient patient = o.getPatient();
        ObjectNode test = Utils.getJsonNodeFactory().objectNode();

        String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";

        String fullName = "";

        if (patient.getGivenName() != null) {
            fullName += patient.getGivenName();
        }

        if (patient.getMiddleName() != null) {
            fullName += " " + patient.getMiddleName();
        }

        if (patient.getFamilyName() != null) {
            fullName += " " + patient.getFamilyName();
        }

        PatientIdentifier cccNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType());
        Encounter originalRegimenEncounter = RegimenMappingUtils.getFirstEncounterForProgram(patient, "ARV");
        Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(patient, "ARV");
        if (currentRegimenEncounter == null) {
            return test;
        }
        SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
        String regimenName = (String) regimenDetails.get("regimenShortDisplay");
        String regimenLine = (String) regimenDetails.get("regimenLine");
        String nascopCode = "";
        if (StringUtils.isNotBlank(regimenName)) {
            nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
        }

        if (StringUtils.isBlank(nascopCode) && StringUtils.isNotBlank(regimenLine)) {
            nascopCode = RegimenMappingUtils.getNonStandardCodeFromRegimenLine(regimenLine);
        }

        //add to list only if code is found. This is a temp measure to avoid sending messages with null regimen codes
        if (StringUtils.isNotBlank(nascopCode)) {

            test.put(getSystemType() == CHAI_SYSTEM ? "mflCode" : "mfl_code", Utils.getDefaultLocationMflCode(null));
            test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            test.put("dob", dob);
            test.put("patient_name", fullName);
            test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
            test.put("sampletype", StringUtils.isNotBlank(sampleType) && getSystemType() == CHAI_SYSTEM ? LabOrderDataExchange.getSampleTypeCode(sampleType) : StringUtils.isNotBlank(sampleType) && getSystemType() == LABWARE_SYSTEM ? sampleType : "");
            test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("datereceived", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("order_no", o.getOrderId().toString());
            test.put("lab", "");
            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");
            test.put("prophylaxis", nascopCode);
            if (patient.getGender().equals("F")) {
                test.put("pmtct", "3");
            }
            test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
            test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");

            if (getSystemType() == LABWARE_SYSTEM) { // if labware

                if (patient.getGender().equals("F")) {
                    test.put("female_status", "none");
                }

                test.put("lab", DEFAULT_APHL_LAB_CODE.toString());
                test.put("facility_email", "");
                test.put("recency_id", "");
                test.put("emr_shipment", StringUtils.isNotBlank(manifestID) ? manifestID : "");
                test.put("date_separated", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleSeparated));

            }
        }
        return test;
    }

    public static void updateAfterPost(HttpResponse response, CloseableHttpClient httpClient, LabManifestOrder manifestOrder) throws IOException, org.json.simple.parser.ParseException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 429) { // too many requests. just terminate
            System.out.println("The push lab scheduler has been configured to run at very short intervals. Please change this to at least 30min");
            //log.warn("The push scheduler has been configured to run at very short intervals. Please change this to at least 30min");
            return;
        }

        if (statusCode != 201 && statusCode != 200 && statusCode != 422 && statusCode != 403) { // skip for status code 422: unprocessable entity, and status code 403 for forbidden response
            JSONParser parser = new JSONParser();
            JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
            JSONObject errorObj = (JSONObject) responseObj.get("error");
            manifestOrder.setStatus("Error - " + statusCode + ". Msg" + errorObj.get("message"));
            System.out.println("There was an error sending lab id = " + manifestOrder.getId());
            log.warn("There was an error sending lab id = " + manifestOrder.getId());
            // throw new RuntimeException("Failed with HTTP error code : " + statusCode + ". Error msg: " + errorObj.get("message"));
        } else if (statusCode == 201 || statusCode == 200) {
            manifestOrder.setStatus("Sent");
            log.info("Successfully pushed a VL lab test id " + manifestOrder.getId());
        } else if (statusCode == 403 || statusCode == 422) {
            JSONParser parser = new JSONParser();
            JSONObject responseObj = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
            JSONObject errorObj = (JSONObject) responseObj.get("error");
            System.out.println("Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
            log.error("Error while submitting manifest sample. " + "Error - " + statusCode + ". Msg" + errorObj.get("message"));
        }
    }

    /**
     * Give the kind of lab system configured i.e CHAI or LABWARE
     *
     * @return int system type LABWARE_SYSTEM or CHAI_SYSTEM
     */
    public static int getSystemType() {
        String systemType = "";
        GlobalProperty gpLabSystemInUse = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SYSTEM_IN_USE);
        if (gpLabSystemInUse == null) {
            return NO_SYSTEM_CONFIGURED; // return 0 if not set
        } else {
            systemType = gpLabSystemInUse.getPropertyValue();
        }

        if (StringUtils.isBlank(systemType)) {
            return NO_SYSTEM_CONFIGURED;
        }

        if (systemType.equalsIgnoreCase("CHAI")) {
            return CHAI_SYSTEM;
        } else if (systemType.equalsIgnoreCase("LABWARE")) {
            return LABWARE_SYSTEM;
        } else {
            return NO_SYSTEM_CONFIGURED; // The default if empty string or another string
        }
    }


    /**
     * TODO: Get correct mappings for the different regions
     * Returns mapping for testing labs
     *
     * @param lab
     * @return
     */
    private String getRequestLab(String lab) {

        if (lab == null) {
            return "";
        }
        Integer code = null;
        if (lab.equals("KEMRI Nairobi")) {
            code = 1;
        } else if (lab.equals("KEMRI CDC Kisumu")) {
            code = 2;
        } else if (lab.equals("KEMRI Alupe HIV Lab")) {
            code = 3;
        } else if (lab.equals("KEMRI Walter Reed Kericho")) {
            code = 4;
        } else if (lab.equals("AMPATH Care Lab Eldoret")) {
            code = 5;
        } else if (lab.equals("Coast Provincial General Hospital Molecular Lab")) {
            code = 6;
        } else if (lab.equals("NPHL")) {
            code = 7;
        } else if (lab.equals("Nyumbani Diagnostic Lab")) {
            code = 8;
        } else if (lab.equals("Kenyatta National Hospial Lab Nairobi")) {
            code = 9;
        } else if (lab.equals("EDARP Nairobi")) {
            code = 10;
        } else if (lab.equals("NIC")) {
            code = 11;
        } else if (lab.equals("KEMRI Kilifi")) {
            code = 12;
        } else if (lab.equals("Aga Khan")) {
            code = 13;
        } else if (lab.equals("Lancet")) {
            code = 14;
        }

        return code.toString();
    }

    /**
     * TODO: add correct mappings for the different specimen types
     * Returns mapping for specimen types
     *
     * @param type
     * @return
     */
    public static String getSampleTypeCode(String type) {

        if (type == null) {
            return "";
        }

        Integer code = null;
        if (type.equals("Frozen plasma")) {
            code = 1;
        } else if (type.equals("Whole Blood")) {
            code = 2;
        } else if (type.equals("DBS")) {
            code = 3;
        }
        return code.toString();
    }

    /**
     * Converter for concept to lab system code
     * 1= Routine VL
     * 2=confirmation of
     * treatment failure (repeat VL)
     * 3= Clinical failure
     * 4= Single drug
     * substitution
     * 5=Baseline VL (for infants diagnosed through EID)
     * 6=Confirmation of persistent low level Viremia (PLLV)
     *
     * @param conceptUuid
     * @return
     */
    public static String getOrderReasonCode(String conceptUuid) {

        if (conceptUuid == null)
            return "";

        Integer code = null;
        if (conceptUuid.equals("843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // Confirmation of treatment failure (repeat VL)
            code = 2;
        } else if (conceptUuid.equals("1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // pregnancy
            code = 1;
        } else if (conceptUuid.equals("162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // baseline VL
            code = 5;
        } else if (conceptUuid.equals("1259AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // single drug substitution
            code = 4;
        } else if (conceptUuid.equals("159882AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // breastfeeding
            code = 1;
        } else if (conceptUuid.equals("163523AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // clinical failure
            code = 3;
        } else if (conceptUuid.equals("161236AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // routine
            code = 1;
        } else if (conceptUuid.equals("160032AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) { // confirmation of persistent low viremia
            code = 6;
        }
        return code != null ? code.toString() : "";
    }


    /**
     * Returns a list of active VL lab orders
     *
     * @return a list of order_id
     */
    protected Set<Integer> getActiveViralLoadOrders() {
        Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        Set<Integer> activeLabs = new HashSet<Integer>();
        String sql = "select order_id from orders where order_action='NEW' and concept_id = 856 and date_stopped is null and voided=0;";

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer orderId = (Integer) res.get(0);
                activeLabs.add(orderId);
            }
        }
        Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        return activeLabs;
    }

    /**
     * Returns active orders which have not been added to any manifest
     *
     * @param manifestId
     * @param startDate
     * @param endDate
     * @return
     */
    public Set<Order> getActiveOrdersNotInManifest(Integer manifestId, Date startDate, Date endDate) {
        Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        Set<Order> activeLabs = new HashSet<Order>();
        String sql = "select o.order_id from orders o\n" +
                "left join kenyaemr_order_entry_lab_manifest_order mo on mo.order_id = o.order_id\n" +
                "where o.order_action='NEW' and o.concept_id in (856,1030) and o.date_stopped is null and o.voided=0 and mo.order_id is null ";

        if (startDate != null && endDate != null) {
            sql = sql + " and date(o.date_activated) between ':startDate' and ':endDate' ";
            String pStartDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(startDate);
            String pEndDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(endDate);

            sql = sql.replace(":startDate", pStartDate);
            sql = sql.replace(":endDate", pEndDate);
        }

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer orderId = (Integer) res.get(0);
                Order o = orderService.getOrder(orderId);
                if (o != null) {
                    activeLabs.add(o);
                }
            }
        }
        Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        return activeLabs;
    }

    /**
     * Returns active vl orders which have not been added to any manifest
     *
     * @param manifestId
     * @param startDate
     * @param endDate
     * @return
     */
    public Set<Order> getActiveViralLoadOrdersNotInManifest(Integer manifestId, Date startDate, Date endDate) {
        Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        Set<Order> activeLabs = new HashSet<Order>();
        String sql = "select o.order_id from orders o\n" +
                "left join kenyaemr_order_entry_lab_manifest_order mo on mo.order_id = o.order_id\n" +
                "where o.order_action='NEW' and o.concept_id = 856 and o.date_stopped is null and o.voided=0 and mo.order_id is null ";

        if (startDate != null && endDate != null) {
            sql = sql + " and date(o.date_activated) between ':startDate' and ':endDate' ";
            String pStartDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(startDate);
            String pEndDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(endDate);

            sql = sql.replace(":startDate", pStartDate);
            sql = sql.replace(":endDate", pEndDate);
        }

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer orderId = (Integer) res.get(0);
                Order o = orderService.getOrder(orderId);
                if (o != null) {
                    activeLabs.add(o);
                }
            }
        }
        Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        return activeLabs;
    }

    /**
     * Returns active Eid orders which have not been added to any manifest
     *
     * @param manifestId
     * @param startDate
     * @param endDate
     * @return
     */
    public Set<Order> getActiveEidOrdersNotInManifest(Integer manifestId, Date startDate, Date endDate) {
        Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        Set<Order> activeLabs = new HashSet<Order>();
        String sql = "select o.order_id from orders o\n" +
                "left join kenyaemr_order_entry_lab_manifest_order mo on mo.order_id = o.order_id left join patient_identifier k on o.patient_id = k.patient_id left join patient_identifier_type r on k.identifier_type = r.patient_identifier_type_id \n" +
                "where o.order_action='NEW' and o.concept_id = 1030 and o.date_stopped is null and o.voided=0 and mo.order_id is null and r.uuid='0691f522-dd67-4eeb-92c8-af5083baf338' and k.identifier is not null ";

        if (startDate != null && endDate != null) {
            sql = sql + " and date(o.date_activated) between ':startDate' and ':endDate' ";
            String pStartDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(startDate);
            String pEndDate = Utils.getSimpleDateFormat("yyyy-MM-dd").format(endDate);

            sql = sql.replace(":startDate", pStartDate);
            sql = sql.replace(":endDate", pEndDate);
        }

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer orderId = (Integer) res.get(0);
                Order o = orderService.getOrder(orderId);
                if (o != null) {
                    activeLabs.add(o);
                }
            }
        }
        Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        return activeLabs;
    }


    /**
     * processes results from lab     *
     *
     * @param resultPayload this should be a JSON array
     * @return
     */
    public String processIncomingViralLoadLabResults(String resultPayload) {

        JsonElement rootNode = JsonParser.parseString(resultPayload);

        JsonArray resultsObj = null;
        String statusMsg;
        try {
            if (rootNode.isJsonArray()) {
                resultsObj = rootNode.getAsJsonArray();
            } else {
                System.out.println("Lab Results Get Results: The payload could not be understood. An array is expected!:::: ");
                statusMsg = "Lab Results Get Results: The payload could not be understood. An array is expected!";
                return statusMsg;
            }
        } catch (Exception e) {
            System.err.println("Lab Results Get Results: An error occured: " + e.getMessage());
            e.printStackTrace();
        }

        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {

                JsonObject o = resultsObj.get(i).getAsJsonObject();
                Integer orderId = o.get("order_number").getAsInt();
                Date sampleReceivedDate = null;
                Date sampleTestedDate = null;
                String dateSampleReceived = "";
                String dateSampleTested = "";
                String specimenRejectedReason = "";

                if (getSystemType() == LABWARE_SYSTEM) {
                    try {
                        JsonObject dateReceivedObject = o.get("date_received").getAsJsonObject();
                        dateSampleReceived = dateReceivedObject.get("date").getAsString().trim();
                    } catch (Exception ex) {
                    }
                    try {
                        JsonObject dateTestedObject = o.get("date_tested").getAsJsonObject();
                        dateSampleTested = dateTestedObject.get("date").getAsString().trim();
                    } catch (Exception ex) {
                    }
                } else if (getSystemType() == CHAI_SYSTEM) {
                    try {
                        dateSampleReceived = o.get("date_received").getAsString().trim();
                    } catch (Exception ex) {
                    }
                    try {
                        dateSampleTested = o.get("date_tested").getAsString().trim();
                    } catch (Exception ex) {
                    }
                }

                specimenRejectedReason = (o.has("rejected_reason") && o.get("rejected_reason") != null && o.get("rejected_reason").getAsString() != null) ? o.get("rejected_reason").getAsString().trim() : "";

                if (StringUtils.isNotBlank(dateSampleReceived)) {
                    try {
                        sampleReceivedDate = Utils.getSimpleDateFormat(LAB_SYSTEM_DATE_PATTERN).parse(dateSampleReceived);
                    } catch (ParseException e) {
                        System.err.println("Lab Results Get Results: Unable to get sample receive date" + e.getMessage());
                        e.printStackTrace();
                    }
                }

                if (StringUtils.isNotBlank(dateSampleTested)) {
                    try {
                        sampleTestedDate = Utils.getSimpleDateFormat(LAB_SYSTEM_DATE_PATTERN).parse(dateSampleTested);
                    } catch (ParseException e) {
                        System.err.println("Lab Results Get Results: Unable to get sample tested date" + e.getMessage());
                        e.printStackTrace();
                    }
                }

                String specimenReceivedStatus = (o.has("sample_status") && o.get("sample_status") != null && o.get("sample_status").getAsString() != null) ? o.get("sample_status").getAsString().trim() : "";
                String result = !o.isJsonNull() && !o.get("result").isJsonNull() ? o.get("result").getAsString() : "";

                // update manifest object to reflect received status
                updateOrder(orderId, result, specimenReceivedStatus, specimenRejectedReason, sampleReceivedDate, sampleTestedDate);
            }
        }
        System.out.println("Lab Results Get Results: Viral load results pulled and updated successfully in the database");
        return "Viral load results pulled and updated successfully in the database";
    }

    /**
     * Updates an active order and sets results if provided
     *
     * @param orderId
     * @param result
     * @param specimenStatus
     * @param specimenRejectedReason
     */
    private void updateOrder(Integer orderId, String result, String specimenStatus, String specimenRejectedReason, Date dateSampleReceived, Date dateSampleTested) {
        Order od = orderService.getOrder(orderId);
        LabManifestOrder manifestOrder = kenyaemrOrdersService.getLabManifestOrderByOrderId(od);

        System.out.println("Order ID: " + od.getOrderId() + ", manifest order: " + manifestOrder.getId() + ", manifest id: " + manifestOrder.getLabManifest().getId() + ", isActive: " + od.isActive());

        Date orderDiscontinuationDate = null;
        if (dateSampleTested != null) {
            orderDiscontinuationDate = dateSampleTested;
        } else {
            orderDiscontinuationDate = aMomentBefore(new Date());
        }

        int manifestType = manifestOrder.getLabManifest().getManifestType();

        if (od != null && od.isActive()) {
            if ((StringUtils.isNotBlank(specimenStatus) && specimenStatus.equals("Rejected")) || (StringUtils.isNotBlank(result) && result.equals("Collect New Sample"))) {

                String discontinuationReason = "";
                if (StringUtils.isNotBlank(specimenRejectedReason)) {
                    discontinuationReason = specimenRejectedReason;
                } else if (result.equalsIgnoreCase("Collect New Sample")) {
                    discontinuationReason = "Collect New Sample";
                } else {
                    discontinuationReason = "Rejected specimen";
                }

                if (manifestType == LabManifest.EID_TYPE) {
                    try {
                        orderService.discontinueOrder(od, discontinuationReason, orderDiscontinuationDate, od.getOrderer(),
                                od.getEncounter());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (manifestType == LabManifest.VL_TYPE) {
                    // Get all active VL orders and discontinue them
                    Map<String, Order> ordersToProcess = getOrdersToProcess(od, vlTestConceptQuantitative);
                    Order o1 = ordersToProcess.get("orderToRetain");
                    Order o2 = ordersToProcess.get("orderToVoid");

                    try {
                        // discontinue one order, and void the other.
                        // Discontinuing both orders result in one of them remaining active
                        orderService.discontinueOrder(o1, discontinuationReason, orderDiscontinuationDate, o1.getOrderer(),
                                o1.getEncounter());
                        orderService.voidOrder(o2, discontinuationReason);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                manifestOrder.setStatus(discontinuationReason);
                manifestOrder.setResult(result);
                manifestOrder.setResultDate(orderDiscontinuationDate);
                if (dateSampleReceived != null) {
                    manifestOrder.setSampleReceivedDate(dateSampleReceived);
                }

                if (dateSampleTested != null) {
                    manifestOrder.setSampleTestedDate(dateSampleTested);
                }
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
            } else if (StringUtils.isNotBlank(specimenStatus) && specimenStatus.equalsIgnoreCase("Complete") && StringUtils.isNotBlank(result)) {

                Encounter enc = new Encounter();
                enc.setEncounterType(labEncounterType);
                enc.setEncounterDatetime(orderDiscontinuationDate);
                enc.setPatient(od.getPatient());
                enc.setCreator(Context.getUserService().getUser(1));

                if (manifestType == LabManifest.EID_TYPE) {
                    Obs o = new Obs();
                    String eidNegative = "Negative";
                    String eidPositive = "Positive";

                    Concept eidNegativeConcept = conceptService.getConcept(664);
                    Concept eidPositiveConcept = conceptService.getConcept(703);
                    /*Concept eidIndeterminateConcept = conceptService.getConcept(1138);
                    Concept eidPoorSampleConcept = conceptService.getConcept(1304);*/

                    if (result.equalsIgnoreCase(eidNegative)) {
                        o.setValueCoded(eidNegativeConcept);
                    } else if (result.equalsIgnoreCase(eidPositive)) {
                        o.setValueCoded(eidPositiveConcept);
                    }

                    o.setDateCreated(orderDiscontinuationDate);
                    o.setCreator(Context.getUserService().getUser(1));
                    o.setObsDatetime(od.getDateActivated());
                    o.setPerson(od.getPatient());
                    o.setOrder(od);

                    try {
                        enc.addObs(o);
                        encounterService.saveEncounter(enc);

                        orderService.discontinueOrder(od, "Results received", orderDiscontinuationDate, od.getOrderer(), od.getEncounter());

                        manifestOrder.setStatus("Complete");
                        manifestOrder.setResult(result);
                        manifestOrder.setResultDate(orderDiscontinuationDate);
                        if (dateSampleReceived != null) {
                            manifestOrder.setSampleReceivedDate(dateSampleReceived);
                        }

                        if (dateSampleTested != null) {
                            manifestOrder.setSampleTestedDate(dateSampleTested);
                        }
                        kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                    } catch (Exception e) {
                        System.out.println("Lab Results Get Results: An error was encountered while updating orders for EID");
                        e.printStackTrace();
                    }
                } else if (manifestType == LabManifest.VL_TYPE) {
                    Concept conceptToRetain = null;
                    String lDLResult = "< LDL copies/ml";
                    String labwarelDLResult = "<LDL";
                    String aboveMillionResult = "> 10,000,000 cp/ml";
                    Obs o = new Obs();

                    if (result.equalsIgnoreCase(lDLResult) || result.equalsIgnoreCase(labwarelDLResult) || result.contains("LDL")) {
                        conceptToRetain = vlTestConceptQualitative;
                        o.setValueCoded(LDLConcept);
                    } else if (result.equalsIgnoreCase(aboveMillionResult)) {
                        conceptToRetain = vlTestConceptQuantitative;
                        o.setValueNumeric(new Double(10000001));
                    } else {
                        conceptToRetain = vlTestConceptQuantitative;
                        Double vlVal = NumberUtils.toDouble(result);
                        o.setValueNumeric(vlVal);
                    }

                    // In order to record results both qualitative (LDL) and quantitative,
                    // every vl request saves two orders: one with 856(quantitative) for numeric values and another with 1305(quantitative) for LDL value
                    // When recording result, it is therefore prudent to set result for one order and void the other one
                    Map<String, Order> ordersToProcess = getOrdersToProcess(od, conceptToRetain);
                    Order orderToRetain = ordersToProcess.get("orderToRetain");
                    Order orderToVoid = ordersToProcess.get("orderToVoid");

                    // logic that picks the right concept id for the result obs
                    o.setConcept(conceptToRetain);
                    o.setDateCreated(new Date());
                    o.setCreator(Context.getUserService().getUser(1));
                    o.setObsDatetime(orderToRetain.getDateActivated());
                    o.setPerson(od.getPatient());
                    o.setOrder(orderToRetain);

                    enc.addObs(o);

                    // For a VL type order
                    if (orderToRetain != null && orderToRetain.isActive() && orderToVoid != null) {

                        try {

                            encounterService.saveEncounter(enc);
                            orderService.discontinueOrder(orderToRetain, "Results received", orderDiscontinuationDate, orderToRetain.getOrderer(),
                                    orderToRetain.getEncounter());
                            orderService.voidOrder(orderToVoid, "Duplicate VL order");
                            // this is really a hack to ensure that order date_stopped is filled, otherwise the order will remain active
                            // the issue here is that even though disc order is created, the original order is not stopped
                            // an alternative is to discontinue this order via REST which works well

                            manifestOrder.setStatus("Complete");
                            manifestOrder.setResult(result);
                            manifestOrder.setResultDate(orderDiscontinuationDate);
                            if (dateSampleReceived != null) {
                                manifestOrder.setSampleReceivedDate(dateSampleReceived);
                            }

                            if (dateSampleTested != null) {
                                manifestOrder.setSampleTestedDate(dateSampleTested);
                            }
                            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                        } catch (Exception e) {
                            System.out.println("Lab Results Get Results: An error was encountered while updating orders for viral load");
                            e.printStackTrace();
                        }
                    } else if ((orderToRetain != null && orderToRetain.isActive()) && (orderToVoid == null || !orderToVoid.isActive())) {
                        // this use case has been observed in facility dbs.
                        // until a lasting solution is found, this block will handle the use case
                        try {

                            encounterService.saveEncounter(enc);
                            orderService.discontinueOrder(orderToRetain, "Results received", orderDiscontinuationDate, orderToRetain.getOrderer(),
                                    orderToRetain.getEncounter());
                            // this is really a hack to ensure that order date_stopped is filled, otherwise the order will remain active
                            // the issue here is that even though disc order is created, the original order is not stopped
                            // an alternative is to discontinue this order via REST which works well

                            manifestOrder.setStatus("Complete");
                            manifestOrder.setResult(result);
                            manifestOrder.setResultDate(orderDiscontinuationDate);
                            if (dateSampleReceived != null) {
                                manifestOrder.setSampleReceivedDate(dateSampleReceived);
                            }

                            if (dateSampleTested != null) {
                                manifestOrder.setSampleTestedDate(dateSampleTested);
                            }
                            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                        } catch (Exception e) {
                            System.out.println("Lab Results Get Results: An error was encountered while updating orders for viral load");
                            e.printStackTrace();
                        }

                    } else {
                        // we should create a new order and discontinue it

                        if ((orderToRetain == null || !orderToRetain.isActive()) && (orderToVoid != null && orderToVoid.isActive())) {
                            TestOrder order = new TestOrder();
                            order.setAction(Order.Action.NEW);
                            order.setCareSetting(orderToVoid.getCareSetting());
                            order.setConcept(orderToVoid.getConcept().equals(vlTestConceptQualitative) ? vlTestConceptQuantitative : vlTestConceptQualitative);
                            order.setPatient(orderToVoid.getPatient());
                            order.setOrderType(orderToVoid.getOrderType());
                            order.setOrderer(orderToVoid.getOrderer());
                            order.setInstructions(orderToVoid.getInstructions());
                            order.setUrgency(orderToVoid.getUrgency());
                            order.setCommentToFulfiller(orderToVoid.getCommentToFulfiller());
                            order.setOrderReason(orderToVoid.getOrderReason());
                            order.setOrderReasonNonCoded(orderToVoid.getOrderReasonNonCoded());
                            order.setDateActivated(orderToVoid.getDateActivated());
                            order.setCreator(orderToVoid.getCreator());
                            order.setEncounter(orderToVoid.getEncounter());
                            Order savedOrder = orderService.saveOrder(order, null);

                            try {

                                encounterService.saveEncounter(enc);
                                orderService.discontinueOrder(savedOrder, "Results received", orderDiscontinuationDate, savedOrder.getOrderer(),
                                        savedOrder.getEncounter());
                                // this is really a hack to ensure that order date_stopped is filled, otherwise the order will remain active
                                // the issue here is that even though disc order is created, the original order is not stopped
                                // an alternative is to discontinue this order via REST which works well

                                manifestOrder.setStatus("Complete");
                                manifestOrder.setResult(result);
                                manifestOrder.setResultDate(orderDiscontinuationDate);
                                if (dateSampleReceived != null) {
                                    manifestOrder.setSampleReceivedDate(dateSampleReceived);
                                }

                                if (dateSampleTested != null) {
                                    manifestOrder.setSampleTestedDate(dateSampleTested);
                                }
                                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                            } catch (Exception e) {
                                System.out.println("Lab Results Get Results: An error was encountered while updating orders for viral load");
                                e.printStackTrace();
                            }
                        } else {
                            /**
                             * the result could not be updated in the system
                             * TODO: establish why one order for VL is missing. When a VL request is made, two orders (856 and 1305) are created
                             * sometimes one order just misses and the code cannot find the one to update
                             * We will mark these with errors for a user to manually update in the system.
                             * An alternative is to create a similar order and update results
                             */
                            System.out.println("Lab Results Get Results: Unable to discontinue the order. Manual action is required");
                            manifestOrder.setStatus("Requires manual update in the lab module");
                            manifestOrder.setResult(result);
                            manifestOrder.setResultDate(orderDiscontinuationDate);

                            if (dateSampleReceived != null) {
                                manifestOrder.setSampleReceivedDate(dateSampleReceived);
                            }

                            if (dateSampleTested != null) {
                                manifestOrder.setSampleTestedDate(dateSampleTested);
                            }

                            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                        }
                    }
                }

            } else if (StringUtils.isNotBlank(specimenStatus) && specimenStatus.equalsIgnoreCase("Incomplete")) {
                // indicate the incomplete status
                manifestOrder.setStatus("Incomplete");
                manifestOrder.setResult("");
                manifestOrder.setLastStatusCheckDate(new Date());
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
            }
        } else /*if (!od.isActive() || od.getVoided())*/ {
            manifestOrder.setStatus("Inactive");
            manifestOrder.setResult(result);
            manifestOrder.setResultDate(new Date());
            kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
        }

    }

    /**
     * Returns an object indicating the order to retain and that to void
     *
     * @param referenceOrder
     * @param conceptToRetain
     * @return
     */
    private Map<String, Order> getOrdersToProcess(Order referenceOrder, Concept conceptToRetain) {

        Map<String, Order> listToProcess = new HashMap<String, Order>();
        Concept conceptToVoid = conceptToRetain.equals(vlTestConceptQualitative) ? vlTestConceptQuantitative : vlTestConceptQualitative;
        List<Order> ordersOnSameDay = orderService.getActiveOrders(referenceOrder.getPatient(), referenceOrder.getOrderType(), referenceOrder.getCareSetting(), referenceOrder.getDateActivated());

        for (Order order : ordersOnSameDay) {
            if (order.getConcept().equals(conceptToVoid)) {
                listToProcess.put("orderToVoid", order);
            } else if (order.getConcept().equals(conceptToRetain)) {
                listToProcess.put("orderToRetain", order);
            }
        }
        return listToProcess;
    }

    /**
     * Checks if a string is numeric
     *
     * @param feed the string
     * @return true if numeric and false if not
     */
    private boolean checkNumeric(String feed) {
        try {
            Long l = Long.parseLong(feed);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
            return false;
        }
    }

    /**
     * Generate a unique manifest ID
     * V<YY>-MFLCODE-<incrementalNo>
     * Where V is for Viral Load and E is for EID
     * <YY> is 2 final digits of current year
     * MFLCODE is Facility Code
     * <incrementalNo> is an incremental number - add 1 to the last manifest created
     * @param manifestType - The manifest type (V is for Viral Load and E is for EID)
     * @return String - the manifest ID
     */
    public String generateUniqueManifestID(String manifestType) {
        String ret = "";
        try {
            String facilityCode = Utils.getDefaultLocationMflCode(Utils.getDefaultLocation());
            System.out.println("Got facility as: " + facilityCode);
            Calendar cal = Calendar.getInstance();
            String currentYear = String.valueOf(cal.get(Calendar.YEAR));
            currentYear = currentYear.substring(Math.max(currentYear.length() - 2, 0)); // gets the last 2 digits
            System.out.println("Got current year as: " + currentYear);
            //Get the last manifest ID and increment by one
            long lastManifestID = kenyaemrOrdersService.getLastManifestID();
            System.out.println("Got last manifest ID as: " + lastManifestID);
            lastManifestID += 1; // adding one to last ID
            String rawManifestID = String.valueOf(lastManifestID);
            String newManifestID = String.format("%1$" + 6 + "s", rawManifestID).replace(' ', '0'); // padding the ID with zeros (6 chars)
            ret = manifestType + currentYear + "-" + facilityCode + "-" + newManifestID;
            System.out.println("Got new manifest ID as: " + newManifestID);
        } catch(Exception er) {
            System.err.println("Error getting new manifest ID: " + er.getMessage());
            er.printStackTrace();
        }
        return(ret);
    }

    /**
     * Borrowed from OpenMRS core
     * To support MySQL datetime values (which are only precise to the second) we subtract one
     * second. Eventually we may move this method and enhance it to subtract the smallest moment the
     * underlying database will represent.
     *
     * @param date
     * @return one moment before date
     */
    private Date aMomentBefore(Date date) {
        return DateUtils.addSeconds(date, -1);
    }

}
