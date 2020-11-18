package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
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
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabOrderDataExchange {

    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();
    EncounterService encounterService = Context.getEncounterService();
    OrderService orderService = Context.getOrderService();

    String LAB_ENCOUNTER_TYPE_UUID = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";
    Concept vlTestConcept = conceptService.getConcept(856);
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
     * Returns object lab request for patients
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
        Encounter originalRegimenEncounter = Utils.getFirstEncounterForProgram(patient, "ARV");
        Encounter currentRegimenEncounter = Utils.getLastEncounterForProgram(patient, "ARV");
        SimpleObject regimenDetails = Utils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
        String regimenName = (String) regimenDetails.get("regimenShortDisplay");
        String regimenLine = (String) regimenDetails.get("regimenLine");
        String nascopCode = "";
        System.out.println("Regimen line: " + regimenLine);
        if (StringUtils.isNotBlank(regimenName )) {
            nascopCode = Utils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
        }

        //add to list only if code is found. This is a temp measure to avoid sending messages with null regimen codes
        if (StringUtils.isNotBlank(nascopCode)) {
            ObjectNode test = Utils.getJsonNodeFactory().objectNode();

            test.put("mflCode", Utils.getDefaultLocationMflCode(null));
            test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            test.put("dob", dob);
            test.put("patient_name", fullName);
            test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
            //test.put("sampletype", o.getInstructions() != null ? getSampleTypeCode(o.getInstructions()) : "");
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
    private String getOrderReasonCode(String conceptUuid) {

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
     * processes results from lab     *
     *
     * @param resultPayload this should be an array
     * @return
     */
    public String processIncomingLabResults(String resultPayload) {

        Integer statusCode;
        String statusMsg;
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsObj = null;
        try {
            JsonNode actualObj = mapper.readTree(resultPayload);
            resultsObj = (ArrayNode) actualObj;
        } catch (JsonProcessingException e) {
            statusCode = 400;
            statusMsg = "The payload could not be understood. An array is expected!";
            e.printStackTrace();
            return statusMsg;
        }

        if (resultsObj.size() > 0) {
            for (int i = 0; i < resultsObj.size(); i++) {
                ObjectNode o = (ObjectNode) resultsObj.get(i);
                Integer specimenId = o.get("specimen_id").intValue();
                Integer specimenReceivedStatus = o.get("receivedstatus").intValue();// 1-received, 2-rejected
                String specimenRejectedReason = o.get("rejectedreason").textValue();
                Integer results = o.get("result").intValue(); //1 - negative, 2 - positive, 5 - inconclusive
                updateOrder(specimenId, results, specimenReceivedStatus, specimenRejectedReason);
            }
        }
        return "Results updated successfully";
    }

    private void updateOrder(Integer orderId, Integer result, Integer receivedStatus, String rejectedReason) {

        Order od = Context.getOrderService().getOrder(orderId);
        if (od != null && od.isActive()) {

            if (receivedStatus == 2 || StringUtils.isNotBlank(rejectedReason)) {
                try {
                    orderService.discontinueOrder(od, rejectedReason != null ? rejectedReason : "Rejected order", new Date(), od.getOrderer(),
                            od.getEncounter());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Encounter enc = new Encounter();
                enc.setEncounterType(labEncounterType);
                enc.setEncounterDatetime(new Date());
                enc.setPatient(od.getPatient());
                enc.setCreator(Context.getUserService().getUser(1));

                Obs o = new Obs();
                o.setConcept(vlTestConcept);
                o.setDateCreated(new Date());
                o.setCreator(Context.getUserService().getUser(1));
                o.setObsDatetime(new Date());
                o.setPerson(od.getPatient());
                o.setOrder(od);
                //  o.setValueCoded(result == 1 ? covidNegConcept : result == 2 ? covidPosConcept : covidIndeterminateConcept);
                enc.addObs(o);

                try {
                    encounterService.saveEncounter(enc);
                    orderService.discontinueOrder(od, "Results received", new Date(), od.getOrderer(),
                            od.getEncounter());
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

}
