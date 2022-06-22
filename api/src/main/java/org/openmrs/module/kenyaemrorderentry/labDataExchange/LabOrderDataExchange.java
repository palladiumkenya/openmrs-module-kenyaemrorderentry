package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.PrivilegeConstants;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openmrs.module.metadatasharing.util.MetadataSharingGlobalPropertyListener.log;

public class LabOrderDataExchange {

    public static final String GP_LAB_SERVER_REQUEST_URL = "chai.viral_load_server_url";
    public static final String GP_LAB_SERVER_RESULT_URL = "chai.viral_load_server_result_url";
    public static final String GP_LAB_SERVER_API_TOKEN = "chai.viral_load_server_api_token";
    public static final String GP_MANIFEST_LAST_PROCESSED = "kemrorder.last_processed_manifest";// used when fetching results from the server
    public static final String GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS = "kemrorder.retry_period_for_incomplete_vl_result";
    public static final String GP_LAB_TAT_FOR_VL_RESULTS = "kemrorder.viral_load_result_tat_in_days";
    public static final String GP_MANIFEST_LAST_UPDATETIME = "kemrorder.manifest_last_update_time";
    public static final String MANIFEST_LAST_UPDATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String LAB_SYSTEM_DATE_PATTERN = "yyyy-MM-dd";
    public static final String GP_LAB_SYSTEM_IN_USE = "kemrorder.labsystem_identifier";

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
        if (StringUtils.isNotBlank(regimenName )) {
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
     *@param sampleType @return
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
        if (StringUtils.isNotBlank(regimenName )) {
            nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
        }

        if (StringUtils.isBlank(nascopCode) && StringUtils.isNotBlank(regimenLine)) {
            nascopCode = RegimenMappingUtils.getNonStandardCodeFromRegimenLine(regimenLine);
        }

        //add to list only if code is found. This is a temp measure to avoid sending messages with null regimen codes
        if (StringUtils.isNotBlank(nascopCode)) {

            test.put(isEidVlLabSystem() ? "mflCode" : "mfl_code", Utils.getDefaultLocationMflCode(null));
            test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            test.put("dob", dob);
            test.put("patient_name", fullName);
            test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
            test.put("sampletype", StringUtils.isNotBlank(sampleType) && isEidVlLabSystem() ? LabOrderDataExchange.getSampleTypeCode(sampleType) : StringUtils.isNotBlank(sampleType) && !isEidVlLabSystem() ? sampleType : "");
            test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("order_no", o.getOrderId().toString());
            test.put("lab", "");
            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");
            test.put("prophylaxis", nascopCode);
            if (patient.getGender().equals("F")) {
                test.put("pmtct", "3");
            }
            test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
            test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");

            if (!isEidVlLabSystem()) { // if labware

                if (patient.getGender().equals("F")) {
                    test.put("female_status", "none");
                }

                test.put("lab", "7");
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

    public static boolean isEidVlLabSystem() {
        GlobalProperty gpLabSystemInUse = Context.getAdministrationService().getGlobalPropertyObject(LabOrderDataExchange.GP_LAB_SYSTEM_IN_USE);
        if (gpLabSystemInUse == null) {
            return false;
        }
        String labSystemName = gpLabSystemInUse.getPropertyValue();
        return "CHAI".toLowerCase().equalsIgnoreCase(labSystemName);
    }


    /**
     * TODO: Get correct mappings for the different regions
     * Returns mapping for testing labs
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
     *1= Routine VL
     2=confirmation of
     treatment failure (repeat VL)
     3= Clinical failure
     4= Single drug
     substitution
     5=Baseline VL (for infants diagnosed through EID)
     6=Confirmation of persistent low level Viremia (PLLV)
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
     * @param manifestId
     * @param startDate
     * @param endDate
     * @return
     */
    public Set<Order> getActiveEidOrdersNotInManifest(Integer manifestId, Date startDate, Date endDate) {
        Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
        Set<Order> activeLabs = new HashSet<Order>();
        String sql = "select o.order_id from orders o\n" +
                "left join kenyaemr_order_entry_lab_manifest_order mo on mo.order_id = o.order_id\n" +
                "where o.order_action='NEW' and o.concept_id = 1030 and o.date_stopped is null and o.voided=0 and mo.order_id is null ";

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
     * @param resultPayload this should be an array
     * @return
     */
    public String processIncomingViralLoadLabResults(String resultPayload) {

        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(resultPayload);

        JsonArray resultsObj = null;
        String statusMsg;
        try {
            if(rootNode.isJsonArray()){
                resultsObj = rootNode.getAsJsonArray();

            } else {
                System.out.println("The payload could not be understood. An array is expected!:::: ");

                statusMsg = "The payload could not be understood. An array is expected!";
                return statusMsg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {

                JsonObject o =  resultsObj.get(i).getAsJsonObject();
                Integer specimenId = o.get("order_number").getAsInt();
                String dateSampleReceived = !o.get("date_received").isJsonNull() ? o.get("date_received").getAsString() : "";
                String dateSampleTested = !o.get("date_tested").isJsonNull() ? o.get("date_tested").getAsString() : "";
                Date sampleReceivedDate = null;
                Date sampleTestedDate = null;

                if (StringUtils.isNotBlank(dateSampleReceived)) {
                    try {
                        sampleReceivedDate = Utils.getSimpleDateFormat(LAB_SYSTEM_DATE_PATTERN).parse(dateSampleReceived);
                    } catch (ParseException e) {
                        //e.printStackTrace();
                    }
                }

                if (StringUtils.isNotBlank(dateSampleTested)) {
                    try {
                        sampleTestedDate = Utils.getSimpleDateFormat(LAB_SYSTEM_DATE_PATTERN).parse(dateSampleTested);
                    } catch (ParseException e) {
                        //e.printStackTrace();
                    }
                }

                String specimenReceivedStatus = o.get("sample_status").getAsString();// Complete, Incomplete, Rejected
                String specimenRejectedReason = o.has("rejected_reason") ? o.get("rejected_reason").getAsString() : "";
                String results = !o.get("result").isJsonNull() ? o.get("result").getAsString() : null; //1 - negative, 2 - positive, 5 - inconclusive
                updateOrder(specimenId, results, specimenReceivedStatus, specimenRejectedReason, sampleReceivedDate, sampleTestedDate);
                // update manifest object to reflect received status
            }
        }
        return "Viral load results pulled and updated successfully in the database";
    }

    /**
     * Updates an active order and sets results if provided
     * @param orderId
     * @param result
     * @param specimenStatus
     * @param rejectedReason
     */
    private void updateOrder(Integer orderId, String result, String specimenStatus, String rejectedReason, Date dateSampleReceived, Date dateSampleTested) {

        Order od = orderService.getOrder(orderId);
        LabManifestOrder manifestOrder = kenyaemrOrdersService.getLabManifestOrderByOrderId(orderService.getOrder(orderId));
        Date orderDiscontinuationDate = null;
        if (dateSampleTested != null) {
            orderDiscontinuationDate = dateSampleTested;
        } else {
            orderDiscontinuationDate = aMomentBefore(new Date());
        }

        if (od != null && od.isActive()) {
            if ((StringUtils.isNotBlank(specimenStatus) && specimenStatus.equals("Rejected")) || StringUtils.isNotBlank(rejectedReason) || (StringUtils.isNotBlank(result) && result.equals("Collect New Sample"))) {
                // Get all active VL orders and discontinue them
                Map<String, Order> ordersToProcess = getOrdersToProcess(od, vlTestConceptQuantitative);
                Order o1 = ordersToProcess.get("orderToRetain");
                Order o2 = ordersToProcess.get("orderToVoid");
                String discontinuationReason = "";
                if (StringUtils.isNotBlank(rejectedReason)){
                    discontinuationReason = rejectedReason;
                } else if (result.equals("Collect New Sample")) {
                    discontinuationReason = "Collect New Sample";
                } else {
                    discontinuationReason = "Rejected specimen";
                }
                try {
                    // discontinue one order, and void the other.
                    // Discontinuing both orders result in one of them remaining active
                    orderService.discontinueOrder(o1, discontinuationReason, orderDiscontinuationDate, o1.getOrderer(),
                            o1.getEncounter());
                    orderService.voidOrder(o2, discontinuationReason);
                } catch (Exception e) {
                    e.printStackTrace();
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

                Concept conceptToRetain = null;
                String lDLResult = "< LDL copies/ml";
                String aboveMillionResult = "> 10,000,000 cp/ml";
                Obs o = new Obs();

                if (result.equals(lDLResult)) {
                    conceptToRetain = vlTestConceptQualitative;
                    o.setValueCoded(LDLConcept);
                } else if (result.equalsIgnoreCase(aboveMillionResult)) {
                    conceptToRetain = vlTestConceptQuantitative;
                    o.setValueNumeric(new Double(10000001));
                } else {
                    conceptToRetain = vlTestConceptQuantitative;
                    o.setValueNumeric(Double.valueOf(result));
                }

                // In order to record results both qualitative (LDL) and quantitative,
                // every vl request saves two orders: one with 856(quantitative) for numeric values and another with 1305(quantitative) for LDL value
                // When recording result, it is therefore prudent to set result for one order and void the other one
                Map<String, Order> ordersToProcess = getOrdersToProcess(od, conceptToRetain);
                Order orderToRetain = ordersToProcess.get("orderToRetain");
                Order orderToVoid = ordersToProcess.get("orderToVoid");


                // logic that picks the right concept id for the result obs
                o.setConcept(conceptToRetain);
                o.setDateCreated(orderDiscontinuationDate);
                o.setCreator(Context.getUserService().getUser(1));
                o.setObsDatetime(orderToRetain.getDateActivated());
                o.setPerson(od.getPatient());
                o.setOrder(orderToRetain);

                Encounter enc = new Encounter();
                enc.setEncounterType(labEncounterType);
                enc.setEncounterDatetime(orderDiscontinuationDate);
                enc.setPatient(od.getPatient());
                enc.setCreator(Context.getUserService().getUser(1));

                enc.addObs(o);

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
                        System.out.println("An error was encountered while updating orders for viral load");
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
                        System.out.println("An error was encountered while updating orders for viral load");
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
                            System.out.println("An error was encountered while updating orders for viral load");
                            e.printStackTrace();
                        }
                    }  else {
                        /**
                         * the result could not be updated in the system
                         * TODO: establish why one order for VL is missing. When a VL request is made, two orders (856 and 1305) are created
                         * sometimes one order just misses and the code cannot find the one to update
                         * We will mark these with errors for a user to manually update in the system.
                         * An alternative is to create a similar order and update results
                         */

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
            } else if (StringUtils.isNotBlank(specimenStatus) && specimenStatus.equalsIgnoreCase("Incomplete")) {

                // indicate the incomplete status
                manifestOrder.setStatus("Incomplete");
                manifestOrder.setResult("");
                manifestOrder.setLastStatusCheckDate(new Date());
                kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
            }
        }

    }

    /**
     * Returns an object indicating the order to retain and that to void
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
