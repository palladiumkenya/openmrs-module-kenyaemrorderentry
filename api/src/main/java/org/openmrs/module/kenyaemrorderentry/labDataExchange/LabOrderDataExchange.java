package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabOrderDataExchange {

    public static final String GP_LAB_SERVER_REQUEST_URL = "chai.viral_load_server_url";
    public static final String GP_LAB_SERVER_RESULT_URL = "chai.viral_load_server_result_url";
    public static final String GP_LAB_SERVER_API_TOKEN = "chai.viral_load_server_api_token";

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
     * Returns a list of active lab requests
     *
     * @param gpLastOrderId
     * @param lastId
     * @return
     */
    public ObjectNode getLabRequests(Integer gpLastOrderId, Integer lastId) {

        JsonNodeFactory factory = Utils.getJsonNodeFactory();
        ArrayNode activeRequests = factory.arrayNode();
        ObjectNode requestWrapper = factory.objectNode();
        Set<Integer> allActiveVLOrders = getActiveViralLoadOrders();
        Integer ordersFound = 0;
        if (!allActiveVLOrders.isEmpty()) {
            ordersFound = allActiveVLOrders.size();
            for (Integer orderId : allActiveVLOrders) {
                Order order = orderService.getOrder(orderId);
                activeRequests = generateActiveVLPayload(order, activeRequests);
            }
        }
        System.out.println("Preparing lab requests for " + ordersFound + " VL orders");
        requestWrapper.put("samples", activeRequests);
        return requestWrapper;

    }

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
     * @return
     */
    public ObjectNode generatePayloadForLabOrder(Order o) {
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
        }
        return test;
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
    private String getSampleTypeCode(String type) {

        if (type == null) {
            return "";
        }
        Integer code = null;
        if (type.equals("Blood")) {
            code = 3;
        } else if (type.equals("OP Swab")) {
            code = 2;
        } else if (type.equals("Tracheal Aspirate")) {
            code = 5;
        } else if (type.equals("Sputum")) {
            code = 4;
        } else if (type.equals("OP and NP Swabs")) {
            code = 1;
        } else {
            code = 6;
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

        Set<Integer> activeLabs = new HashSet<Integer>();
        String sql = "select order_id from orders where order_action='NEW' and concept_id = 856 and date_stopped is null and voided=0;";

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer orderId = (Integer) res.get(0);
                activeLabs.add(orderId);
            }
        }

        return activeLabs;
    }

    /**
     * Returns active orders which have not been added to any manifest
     * @param manifestId
     * @param startDate
     * @param endDate
     * @return
     */
    public Set<Order> getActiveViralLoadOrdersNotInManifest(Integer manifestId, Date startDate, Date endDate) {

        Set<Order> activeLabs = new HashSet<Order>();
        String sql = "select o.order_id from orders o\n" +
                "left join kenyaemr_order_entry_lab_manifest_order mo on mo.order_id = o.order_id\n" +
                "where o.order_action='NEW' and o.concept_id = 856 and o.date_stopped is null and o.voided=0 and mo.order_id is null;";

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

        return activeLabs;
    }

    /**
     * processes results from lab     *
     *
     * @param resultPayload this should be an array
     * @return
     */
    public String processIncomingViralLoadLabResults(String resultPayload) {

        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsObj = null;
        try {
            JsonNode actualObj = mapper.readTree(resultPayload);
            resultsObj = (ArrayNode) actualObj;
        } catch (JsonProcessingException e) {
            statusMsg = "The payload could not be understood. An array is expected!";
            e.printStackTrace();
            return statusMsg;
        }

        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {
                ObjectNode o = (ObjectNode) resultsObj.get(i);
                Integer specimenId = o.get("order_number").asInt();
                String patientIdentifier = o.get("patient").textValue(); // holds CCC number
                String specimenReceivedStatus = o.get("sample_status").textValue();// Complete, Incomplete, Rejected

                String specimenRejectedReason = o.has("rejected_reason") ? o.get("rejected_reason").textValue() : "";
                String results = o.get("result").textValue(); //1 - negative, 2 - positive, 5 - inconclusive
                updateOrder(specimenId, results, specimenReceivedStatus, specimenRejectedReason);
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
    private void updateOrder(Integer orderId, String result, String specimenStatus, String rejectedReason) {

        Order od = orderService.getOrder(orderId);
        LabManifestOrder manifestOrder = kenyaemrOrdersService.getLabManifestOrderByOrderId(orderService.getOrder(orderId));
        Date orderDiscontinuationDate = aMomentBefore(new Date());

        SimpleDateFormat df = Utils.getSimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formatedDiscDate = df.format(orderDiscontinuationDate);

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
                manifestOrder.setResultDate(orderDiscontinuationDate);
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
                o.setObsDatetime(orderDiscontinuationDate);
                o.setPerson(od.getPatient());
                o.setOrder(orderToRetain);

                Encounter enc = new Encounter();
                enc.setEncounterType(labEncounterType);
                enc.setEncounterDatetime(orderDiscontinuationDate);
                enc.setPatient(od.getPatient());
                enc.setCreator(Context.getUserService().getUser(1));

                enc.addObs(o);
                if (orderToRetain != null && orderToVoid != null) {

                    try {

                        encounterService.saveEncounter(enc);
                        orderService.discontinueOrder(orderToRetain, "Results received", orderDiscontinuationDate, orderToRetain.getOrderer(),
                                orderToRetain.getEncounter());
                        orderService.voidOrder(orderToVoid, "Duplicate VL order");
                        // this is really a hack to ensure that order date_stopped is filled, otherwise the order will remain active
                        // the issue here is that even though disc order is created, the original order is not stopped
                        // an alternative is to discontinue this order via REST which works well
                        //Context.getAdministrationService().executeSQL("update orders o1 inner join orders o2 on o1.order_id=o2.previous_order_id and o1.date_stopped is null set o1.date_stopped = '" +  formatedDiscDate + "' where o1.order_id = " + orderToRetain.getOrderId().intValue() , false);
                    } catch (Exception e) {
                        System.out.println("An error was encountered while updating orders for viral load");
                        e.printStackTrace();
                    }
                    manifestOrder.setStatus("Result received");
                    manifestOrder.setResultDate(orderDiscontinuationDate);
                    kenyaemrOrdersService.saveLabManifestOrder(manifestOrder);
                }
            } else if (StringUtils.isNotBlank(specimenStatus) && specimenStatus.equalsIgnoreCase("Incomplete")) {
                System.out.println("Status for " + orderId + " sample not yet ready");
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
