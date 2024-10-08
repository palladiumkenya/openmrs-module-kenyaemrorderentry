package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for mapping lab tests and results between a labware facility-wide implementation and the EMR
 * TODO: abstract the key methods into a parent class and have various lab systems implement the specifics
 */
public class LabwareFacilityWideResultsMapper {
    public static final String LAB_TEST_NAME_PROPERTY = "testName";
    public static final String LAB_TEST_RESULT_SET_PROPERTY = "result";
    String LAB_ENCOUNTER_TYPE_UUID = "e1406e88-e9a9-11e8-9f32-f2801f1b9fd1";

    public LabwareFacilityWideResultsMapper() {
    }

    /**
     * Reads the mapping file into a json object with a hash map structure
     * The object has concept uuid as keys and an object as value.
     * A structure for a simple test i.e. Malaria smear can be as below.
     * <>
     *     "34567AAAAAAAA":{
     *       	"testName":"CD4 count",
     *       }
     * </>
     * For lab sets i.e. complete blood count, the structure should look like the below:
     *
     * {
     * "34567AAAAAAAA":{
     * 	"testName":"Malaria Smear",
     * 	"result": {
     * 		"Positive":"703AAAAAAAAAAA",
     * 		"Negative":"664AAAAAAAAAAA"
     * 	    }
     * },
     * "34588AAAAAAAA":{
     * 	"testName":"Full Blood count",
     * 	"result": {
     * 		"WBC":"7987AAAAAAAAAAA",
     * 		"RDW":"79673AAAAAAAAAAA"
     *    }
     * }
     * }
     * @return
     */
    public ObjectNode readLabTestMappingConfiguration(){
        AdministrationService administrationService = Context.getAdministrationService();
        String limsConfiguration = (administrationService.getGlobalProperty("kenyaemrorderentry.facilitywidelims.mapping"));
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode testConfiguration = null;
        try {
            testConfiguration = (ObjectNode) mapper.readTree(limsConfiguration);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return testConfiguration;
    }

    /**
     * Processes results from LIMS
     * @param resultPayload
     * @return
     */
    public String processResultsFromLims(String resultPayload) {
        JsonElement rootNode = JsonParser.parseString(resultPayload);
        JsonObject resultsObj = null;
        String statusMsg;
        try {
            if (rootNode.isJsonObject()) {
                resultsObj = rootNode.getAsJsonObject();
            } else {
                System.out.println("Lab Results Get Results: The payload could not be understood. An object is expected!:::: ");
                statusMsg = "Lab Results Get Results: The payload could not be understood. An object is expected!";
                return statusMsg;
            }
        } catch (Exception e) {
            System.err.println("Lab Results Get Results: An error occured: " + e.getMessage());
            e.printStackTrace();
        }

        if (resultsObj != null) {
            Map<String,String> resultMap = new HashMap<>();
            JsonArray resultArray = resultsObj.get("data").getAsJsonArray();
            Integer orderId = null;
            for (int i = 0; i < resultArray.size(); i++) {
                try {
                    JsonObject o = resultArray.get(i).getAsJsonObject();
                    orderId = o.get("labRequestId").getAsInt();
                    String testName = !o.isJsonNull() && !o.get("resultName").isJsonNull() ? o.get("resultName").getAsString() : "";
                    String result = !o.isJsonNull() && !o.get("resultValue").isJsonNull() ? o.get("resultValue").getAsString() : "";
                    if (StringUtils.isNotBlank(testName) && StringUtils.isNotBlank(result)) {
                        resultMap.put(testName, result);
                    }
                } catch (Exception ex) {
                    System.err.println("Lab Results Get Results: Unable to update order with results: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            // update results and complete the order
            mapLimsResultsInEmr(orderId, resultMap);
        }
        System.out.println("Lab Results Get Results: LIMS results pulled and updated successfully in the database");
        return "LIMS results pulled and updated successfully in the database";
    }


    /**
     *
     * @param orderId
     * @param limsResult is a map of test and result.
     * For simple test i.e. Malaria smear, the key is the test name
     * For lab sets i.e. complete blood count, the map entry is a test and the value
     * Results for lab sets are handled as grouped observations with a reference to the parent order.
     */
    public void mapLimsResultsInEmr(Integer orderId, Map<String,String> limsResult) {
        if (limsResult == null || limsResult.isEmpty()) {return;}
        EncounterType labEncounterType = Context.getEncounterService().getEncounterTypeByUuid(LAB_ENCOUNTER_TYPE_UUID);
        EncounterService encounterService = Context.getEncounterService();
        ConceptService conceptService = Context.getConceptService();
        OrderService orderService = Context.getOrderService();

        Order order = Context.getOrderService().getOrder(orderId);
        Concept orderConcept = order.getConcept();

        if (orderConcept != null) {

            ObjectNode mapping = readLabTestMappingConfiguration();
            // Get mapping for the test concept
            ObjectNode testConceptMapping = (ObjectNode) mapping.get(orderConcept.getUuid());
            System.out.println("test concept mapping: " + testConceptMapping);

            // setup lab result encounter
            Encounter enc = new Encounter();
            enc.setEncounterType(labEncounterType);
            enc.setEncounterDatetime(order.getDateCreated());
            enc.setPatient(order.getPatient());
            enc.setCreator(Context.getUserService().getUser(1));

            Obs o = constructObs(order);
            o.setConcept(orderConcept);

            String limsTestName = "", limsTestResult = "";
            /**
             * TODO: remove this if it ceases being useful
             * A sample config would look like:
             *
             * "34588AAAAAAAA":{
             * 	"testName":"Full Blood count",
             * 	"result": {
             * 		"WBC":"7987AAAAAAAAAAA",
             * 		"RDW":"79673AAAAAAAAAAA",
             * 		"someQualitativeTest": {
             * 			"Positive":"703AAAAAAAAAAA",
             * 			"Negative":"664AAAAAAAAAAA"
             *      }
             *  }
             * }
             *
             * We expect results to be formed as key:value pair like below.
             * "WBC": 123, "RDW": 679, "someQualitativeTest":"Positive"
             */
            if (orderConcept.isSet() && orderConcept.getSetMembers().size() > 0) {
                ObjectNode resultSet = (ObjectNode) testConceptMapping.get(LAB_TEST_RESULT_SET_PROPERTY);
                // loop through the results and create an obs group
                for (Map.Entry<String, String> entry : limsResult.entrySet()) {
                    limsTestName = entry.getKey();
                    limsTestResult = entry.getValue();

                    if (StringUtils.isBlank(limsTestResult) || StringUtils.isBlank(limsTestName)) {
                        System.err.println("LIMS results: system encountered NULL test or result. Terminating now");
                        return;
                    }
                    String memberConceptUuid = resultSet.get(limsTestName).asText();
                    Concept memberObsConcept = conceptService.getConceptByUuid(memberConceptUuid);
                    Obs memberObs = constructObs(order);
                    if (memberObsConcept != null) {
                        memberObs.setConcept(memberObsConcept);
                        if (memberObsConcept.getDatatype().isNumeric() || memberObsConcept.getDatatype().isText()) {
                            setObsValue(memberObs, memberObsConcept, limsTestResult);
                        } else if (memberObsConcept.getDatatype().isCoded()) {
                            String memberTestConceptUuid = resultSet.get(limsTestName).asText();
                            Concept codedAnswer = Context.getConceptService().getConceptByUuid(memberTestConceptUuid);
                            setObsValue(memberObs, memberObsConcept, codedAnswer);
                        }
                    }
                    o.addGroupMember(memberObs);
                }

            } else { // this is for a non-set test.
                for (Map.Entry<String, String> entry : limsResult.entrySet()) {
                    limsTestName = entry.getKey();
                    limsTestResult = entry.getValue();
                }

                if (StringUtils.isBlank(limsTestResult) || StringUtils.isBlank(limsTestName)) {
                    System.err.println("LIMS results: system encountered NULL test or result. Terminating now");
                    return;
                }

                if (orderConcept.getDatatype().isNumeric() || orderConcept.getDatatype().isText()) {
                    setObsValue(o, orderConcept, limsTestResult);
                } else if (orderConcept.getDatatype().isCoded()) {
                    /*
                    TODO: remove this comment once the logic is well understood
                    "34567AAAAAAAA":{
                        "testName":"Malaria Smear",
                        "result": {
                            "Positive":"703AAAAAAAAAAA",
                            "Negative":"664AAAAAAAAAAA"
                        }
                    }
                    We expect results in the form of "Malaria Smear":"Positive"
                    */
                    ObjectNode resultSet = (ObjectNode) testConceptMapping.get(LAB_TEST_RESULT_SET_PROPERTY);
                    String codedAnswerConceptUuid = resultSet.get(limsTestResult).asText();
                    Concept codedAnswer = Context.getConceptService().getConceptByUuid(codedAnswerConceptUuid);
                    setObsValue(o, orderConcept, codedAnswer);
                }
            }
            try {
                enc.addObs(o);
                encounterService.saveEncounter(enc);
                orderService.discontinueOrder(order, "Results received", new Date(), order.getOrderer(), order.getEncounter());
            } catch (Exception e) {
                System.out.println("Facility Lims Results update: An error was encountered while updating results for " + order.getConcept().getUuid());
                e.printStackTrace();
                //TODO: we should return an appropriate message and status code
            }
        }
    }

    private void setObsValue(Obs obs, Concept concept, String obsValue) {
        if (concept.isNumeric()) {
            obs.setValueNumeric(Double.valueOf(obsValue));
        } else if (concept.getDatatype().isText()) {
            obs.setValueText(obsValue);
        }
    }

    private void setObsValue(Obs obs, Concept concept, Concept obsValue) {
        if (concept.getDatatype().isCoded()) {
            obs.setValueCoded(obsValue);
        }
    }

    private Obs constructObs(Order order){
        Obs o = new Obs();
        o.setDateCreated(order.getDateCreated());
        o.setCreator(Context.getUserService().getUser(1));
        o.setObsDatetime(order.getDateActivated());
        o.setPerson(order.getPatient());
        o.setOrder(order);
        return o;
    }
}
