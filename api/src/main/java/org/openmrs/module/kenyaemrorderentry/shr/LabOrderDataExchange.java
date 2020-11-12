package org.openmrs.module.kenyaemrorderentry.shr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;

import java.util.*;

public class LabOrderDataExchange {

    PersonService personService = Context.getPersonService();
    PatientService patientService = Context.getPatientService();
    ObsService obsService = Context.getObsService();
    ConceptService conceptService = Context.getConceptService();
    EncounterService encounterService = Context.getEncounterService();
    OrderService orderService = Context.getOrderService();
    LocationService locationService = Context.getLocationService();


    String TEST_ORDER_TYPE_UUID = "1814ee89-2abf-42d7-920b-d138740d56d4";
    String LAB_ENCOUNTER_TYPE_UUID = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";
    String COVID_19_CASE_INVESTIGATION = "a4414aee-6832-11ea-bc55-0242ac130003";
    //Concept covidTestConcept = conceptService.getConcept(856);
    Concept vlTestConcept = conceptService.getConcept(856);
   // Concept covidPosConcept = conceptService.getConcept(703);
   // Concept covidNegConcept = conceptService.getConcept(664);
    //Concept covidIndeterminateConcept = conceptService.getConcept(1138);
    EncounterType labEncounterType = encounterService.getEncounterTypeByUuid(LAB_ENCOUNTER_TYPE_UUID);

    /**
     * Returns a list of active lab requests
     *
     * @param gpLastOrderId
     * @param lastId
     * @return
     */
    public ObjectNode getLabRequests(Integer gpLastOrderId, Integer lastId) {

        JsonNodeFactory factory = OutgoingLabOrderSHR.getJsonNodeFactory();
        ArrayNode activeRequests = factory.arrayNode();
        ObjectNode requestWrapper = factory.objectNode();
        Set<Integer> allPatients = getPatientsWithOrders(gpLastOrderId, lastId);
        Integer patientsFound = 0;
        if (!allPatients.isEmpty()) {
            patientsFound = allPatients.size();
            for (Integer ptId : allPatients) {
                Patient p = patientService.getPatient(ptId);
                activeRequests = getActiveLabRequestForPatient(p, activeRequests);
            }
        }
        System.out.println("Preparing lab requests for " + patientsFound + " cases for the lab system");
        requestWrapper.put("samples", activeRequests);
        return requestWrapper;

    }

    /**
     * Returns active lab requests for a patient
     *
     * @param patient
     * @return
     */
    public ArrayNode getActiveLabRequestForPatient(Patient patient, ArrayNode requests) {

        JsonNodeFactory factory = OutgoingLabOrderSHR.getJsonNodeFactory();
        ObjectNode patientSHR = factory.objectNode();


        if (patient != null) {
            return getActiveLabRequestsForPatient(patient, requests);

        } else {
            return requests;
        }
    }

    /**
     * Returns a patient's address
     *
     * @param patient
     * @return
     */
    public static ObjectNode getPatientAddress(Patient patient) {
        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();
        String postalAddress = "";
        String county = "";
        String sub_county = "";
        String ward = "";
        String landMark = "";

        for (PersonAddress address : addresses) {
            if (address.getAddress1() != null) {
                postalAddress = address.getAddress1();
            }
            if (address.getCountry() != null) {
                county = address.getCountry() != null ? address.getCountry() : "";
            }

            if (address.getCountyDistrict() != null) {
                county = address.getCountyDistrict() != null ? address.getCountyDistrict() : "";
            }

            if (address.getStateProvince() != null) {
                sub_county = address.getStateProvince() != null ? address.getStateProvince() : "";
            }

            if (address.getAddress4() != null) {
                ward = address.getAddress4() != null ? address.getAddress4() : "";
            }
            if (address.getAddress2() != null) {
                landMark = address.getAddress2() != null ? address.getAddress2() : "";
            }

        }

        physicalAddressNode.put("COUNTY", county);
        physicalAddressNode.put("SUB_COUNTY", sub_county);
        physicalAddressNode.put("WARD", ward);
        physicalAddressNode.put("NEAREST_LANDMARK", landMark);

        //combine all addresses
        patientAddressNode.put("PHYSICAL_ADDRESS", physicalAddressNode);
        patientAddressNode.put("POSTAL_ADDRESS", postalAddress);

        return patientAddressNode;
    }

    /**
     * Returns patient name
     *
     * @param patient
     * @return
     */
    private ObjectNode getPatientName(Patient patient) {
        PersonName pn = patient.getPersonName();
        ObjectNode nameNode = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();
        nameNode.put("FIRST_NAME", pn.getGivenName());
        nameNode.put("MIDDLE_NAME", pn.getMiddleName());
        nameNode.put("LAST_NAME", pn.getFamilyName());
        return nameNode;
    }

    private ObjectNode getPatientIdentifier(Patient patient) {

        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
        PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
        PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);
        PatientIdentifierType CASE_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PATIENT_CLINIC_NUMBER);
        PatientIdentifierType OPENMRS_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.MEDICAL_RECORD_NUMBER);

        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);

        ObjectNode patientIdentifiers = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();
        String pIdentifier = null;
        Integer idCode = null;

        for (PatientIdentifier identifier : identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();

            if (identifierType.equals(NATIONAL_ID_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 1;
                patientIdentifiers.put("type", 1);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 3;
                patientIdentifiers.put("type", 3);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;


            } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                pIdentifier = identifier.getIdentifier();
                idCode = 2;
                patientIdentifiers.put("type", 2);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;

            } else if (identifierType.equals(CASE_ID_TYPE)) { // use this to track those with no documented identifier
                pIdentifier = identifier.getIdentifier();
                idCode = 4;
                patientIdentifiers.put("type", 4);
                patientIdentifiers.put("identifier", identifier.getIdentifier());
                return patientIdentifiers;
            }

        }

        if (idCode == null || pIdentifier == null) {
            PatientIdentifier openmrsId = patient.getPatientIdentifier(OPENMRS_ID_TYPE);
            pIdentifier = openmrsId.getIdentifier();
            idCode = 4;
        }

        patientIdentifiers.put("type", idCode);
        patientIdentifiers.put("identifier", pIdentifier);
        return patientIdentifiers;
    }

    /**
     * Returns object lab request for patients
     *
     * @param patient
     * @return
     */
    protected ArrayNode getActiveLabRequestsForPatient(Patient patient, ArrayNode labTests) {

        ObjectNode cifInfo = getCovidEnrollmentDetails(patient);
        //ObjectNode address = getPatientAddress(patient);
        ArrayNode blankArray = OutgoingLabOrderSHR.getJsonNodeFactory().arrayNode();
        OrderService orderService = Context.getOrderService();
        ObjectNode idMap = getPatientIdentifier(patient);
        //Check whether client has active VL order
        OrderType patientLabOrders = orderService.getOrderTypeByUuid(TEST_ORDER_TYPE_UUID);
        String dob = patient.getBirthdate() != null ? OutgoingLabOrderSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";
        //String deathDate = patient.getDeathDate() != null ? OutgoingLabOrderSHR.getSimpleDateFormat("yyyy-MM-dd").format(patient.getDeathDate()) : "";

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

        if (patientLabOrders != null) {
            //Get active lab orders
            List<Order> activeVLTestOrders = orderService.getActiveOrders(patient, patientLabOrders, null, null);
            if (activeVLTestOrders.size() > 0) {
                for (Order o : activeVLTestOrders) {
                    ObjectNode test = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();
                    //test.put("patient_id", patient.getPatientId());
                    //test.put("identifier_type", idMap.get("type"));
                    test.put("patient_identifier", idMap.get("identifier"));
                    test.put("dob", dob);
                    test.put("patient_name", fullName);
                    test.put("sex", patient.getGender());
                    test.put("order_no", o.getOrderId());
                    test.put("lab", getRequestLab(o.getCommentToFulfiller()));
                    test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getConceptId()) : "");
                    test.put("sampletype", o.getInstructions() != null ? getSampleTypeCode(o.getInstructions()) : "");
                    labTests.add(test);
                }
            }
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
     *
     * @param orderReason
     * @return
     */
    private String getOrderReasonCode(Integer orderReason) {

        if (orderReason == null)
            return "";

        Integer code = null;
        if (orderReason.equals(162080)) { // baseline
            code = 1;
        } else if (orderReason.equals(162081)) { // 1st followup
            code = 2;
        } else if (orderReason.equals(164142)) { // 2nd followup
            code = 3;
        } else if (orderReason.equals(159490)) { // 3rd followup
            code = 4;
        } else if (orderReason.equals(159489)) { // 4th followup
            code = 5;
        } else if (orderReason.equals(161893)) { // 5th followup
            code = 6;
        }
        return code != null ? code.toString() : "";
    }


    private ObjectNode getCovidEnrollmentDetails(Patient patient) {

        Concept countyConcept = conceptService.getConcept(165197);
        Concept subCountyConcept = conceptService.getConcept(161551);
        Concept healthStatusConcept = conceptService.getConcept(159640);
        Concept tempConcept = conceptService.getConcept(5088);
        ObjectNode enrollmentObj = OutgoingLabOrderSHR.getJsonNodeFactory().objectNode();


        String county = "", subCounty = "";
        Integer healthStatus = null;
        Double temp = null;


        EncounterType covid_enc_type = encounterService.getEncounterTypeByUuid(COVID_19_CASE_INVESTIGATION);
        Encounter lastEncounter = lastEncounter(patient, covid_enc_type);

        List<Concept> questionConcepts = Arrays.asList(countyConcept, subCountyConcept, healthStatusConcept, tempConcept);
        List<Obs> enrollmentData = obsService.getObservations(
                Collections.singletonList(patient.getPerson()),
                Collections.singletonList(lastEncounter),
                questionConcepts,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        for (Obs o : enrollmentData) {
            if (o.getConcept().equals(countyConcept)) {
                county = o.getValueText();
            } else if (o.getConcept().equals(subCountyConcept)) {
                subCounty = o.getValueText();
            } else if (o.getConcept().equals(healthStatusConcept)) {
                if (o.getValueCoded().getConceptId().equals(159405)) {
                    //healthStatus = "Stable";
                    healthStatus = 1;
                } else if (o.getValueCoded().getConceptId().equals(159407)) {
                    //healthStatus = "Severely ill";
                    healthStatus = 2;

                } else if (o.getValueCoded().getConceptId().equals(160432)) {
                    //healthStatus = "Dead";
                    healthStatus = 3;
                } else if (o.getValueCoded().getConceptId().equals(1067)) {
                    //healthStatus = "Unknown";
                    healthStatus = 4;
                }
            } else if (o.getConcept().equals(tempConcept)) {
                temp = o.getValueNumeric();
            }
        }

        enrollmentObj.put("county", county);
        enrollmentObj.put("subCounty", subCounty);
        enrollmentObj.put("healthStatus", healthStatus != null ? healthStatus.toString() : "");
        enrollmentObj.put("temp", temp != null ? temp.toString() : "");
        return enrollmentObj;
    }

    /**
     * Finds the last encounter during the program enrollment with the given encounter type
     *
     * @param type the encounter type
     * @return the encounter
     */
    public Encounter lastEncounter(Patient patient, EncounterType type) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }

    /**
     * Returns a list of patients with active lab orders
     *
     * @param lastLabEntry
     * @param lastId
     * @return
     */
    protected Set<Integer> getPatientsWithOrders(Integer lastLabEntry, Integer lastId) {

        Set<Integer> patientWithActiveLabs = new HashSet<Integer>();
        String sql = "";
        if (lastLabEntry != null && lastLabEntry > 0) {
            sql = "select patient_id from orders where order_id >" + lastLabEntry + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0;";
        } else {
            sql = "select patient_id from orders where order_id <= " + lastId + " and order_action='NEW' and instructions is not null and comment_to_fulfiller is not null and voided=0;";

        }

        List<List<Object>> activeOrders = Context.getAdministrationService().executeSQL(sql, true);
        if (!activeOrders.isEmpty()) {
            for (List<Object> res : activeOrders) {
                Integer patientId = (Integer) res.get(0);
                patientWithActiveLabs.add(patientId);
            }
        }

        return patientWithActiveLabs;
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
