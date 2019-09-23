package org.openmrs.module.kenyaemrorderentry.page.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Obs;
import org.openmrs.Encounter;
import org.openmrs.api.ConceptService;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.Order;

import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.kenyaemrorderentry.util.OrderEntryUIUtils;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageContext;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class LabOrdersPageController {
    public static final Locale LOCALE = Locale.ENGLISH;
    ConceptService concService = Context.getConceptService();
    public void get(@RequestParam("patientId") Patient patient,
                    @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                    @SpringBean("encounterService") EncounterService encounterService,
                    @SpringBean("orderService") OrderService orderService,
                    UiSessionContext sessionContext,
                    UiUtils ui,
                    PageModel model,
                    PageContext pageContext,
                    @SpringBean("conceptService") ConceptService conceptService,
                    @SpringBean("obsService") ObsService obsService) {

        OrderEntryUIUtils.setDrugOrderPageAttributes(pageContext, OrderEntryUIUtils.APP_LAB_ORDER);

        EncounterType labOrderEncounterType = encounterService.getEncounterTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        EncounterRole encounterRoles = encounterService.getAllEncounterRoles(false).get(0);

        List<CareSetting> careSettings = orderService.getCareSettings(false);

        Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
        jsonConfig.put("patient", convertToFull(patient));
        jsonConfig.put("provider", convertToFull(sessionContext.getCurrentProvider()));
        jsonConfig.put("encounterRole", convertToFull(encounterRoles));
        jsonConfig.put("labOrderEncounterType", convertToFull(labOrderEncounterType));
        jsonConfig.put("careSettings", convertToFull(careSettings));

        if (careSetting != null) {
            jsonConfig.put("intialCareSetting", careSetting.getUuid());
        }


        model.put("patient", patient);
        model.put("jsonConfig", ui.toJson(jsonConfig));
        labOrdersConceptPanels(conceptService, sessionContext, ui, model);
        getActiveLabOrders(orderService, conceptService, patient, model);
        getPastLabOrders(orderService, conceptService,careSetting, patient, model,obsService);

    }

    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
    public void labOrdersConceptPanels(@SpringBean("conceptService") ConceptService conceptService,
                                       UiSessionContext sessionContext,
                                       UiUtils ui,
                                       PageModel model) {

        // Define sample type
        Map<String, List<Concept>> sampleTypes = new HashMap<String, List<Concept>>();

        // Build panels for urine
        List<Concept> urineTestPanels = Arrays.asList(
                conceptService.getConcept(163697),
                conceptService.getConcept(161488),
                conceptService.getConcept(163652),
                conceptService.getConcept(161446)


        );

        // Build panels for Blood
        List<Concept> bloodTestPanels = Arrays.asList(
                conceptService.getConcept(161430),
                // conceptService.getConcept(657),
                conceptService.getConcept(161487),
                conceptService.getConcept(161437),
                conceptService.getConcept(163602),
                conceptService.getConcept(1010),
                conceptService.getConcept(161532),
                conceptService.getConcept(1019),
                conceptService.getConcept(161483),
                conceptService.getConcept(161488),
                conceptService.getConcept(953),
                conceptService.getConcept(161476),
                conceptService.getConcept(161426),
                conceptService.getConcept(161431),
                conceptService.getConcept(161475)


        );
        // Build panels for Stool
        List<Concept> stoolTestPanels = Arrays.asList(
                conceptService.getConcept(161451)


        );
        // Build panels for Histology
        List<Concept> histologyTestPanels = Arrays.asList(
                conceptService.getConcept(159645)


        );
        List<Concept> sputumTestPanels = Arrays.asList(
                // conceptService.getConcept(159645)


        );
        sampleTypes.put("Urine", urineTestPanels);
        sampleTypes.put("Blood", bloodTestPanels);
        sampleTypes.put("Stool", stoolTestPanels);
        sampleTypes.put("Histology/Cytology", histologyTestPanels);
        sampleTypes.put("Sputum", sputumTestPanels);



        JSONArray labTestJsonPayload = new JSONArray();

        for (Map.Entry<String, List<Concept>> entry : sampleTypes.entrySet()) {
            JSONObject sampleTypeObject = new JSONObject();

            String testSampleTypeName = entry.getKey();
            List<Concept> panelConcepts = entry.getValue();

            JSONArray sampleTypePanels = new JSONArray();

            for (Concept panelConcept : panelConcepts) {
                JSONObject labTestObj, panelObj;
                JSONArray labTestArray = new JSONArray();
                panelObj = new JSONObject();

                String panelName = panelConcept.getName(LOCALE).getName();

                for (Concept labTest : conceptService.getConceptsByConceptSet(panelConcept)) {
                    labTestObj = new JSONObject();
                    labTestObj.put("concept_id", labTest.getConceptId());
                    labTestObj.put("concept", labTest.getUuid());
                    labTestObj.put("dataType", labTest.getDatatype().getName());
                    labTestObj.put("name", conceptService.getConcept(labTest.getConceptId()).getName(LOCALE).getName());
                    labTestArray.add(labTestObj);


                }
                panelObj.put("name", panelName);
                panelObj.put("tests", labTestArray);
                sampleTypePanels.add(panelObj);

            }
            sampleTypeObject.put("name", testSampleTypeName);
            sampleTypeObject.put("panels", sampleTypePanels);
            labTestJsonPayload.add(sampleTypeObject);


        }

        // Add panels with no concept ids
        JSONArray finalJsonPayloadArray = buildTestPanelWithoutPanelConcept("Blood", labTestJsonPayload,
                "GROUPING AND CROSSMATCH", Arrays.asList(
                        concService.getConcept(161233), // blood cross matching
                        concService.getConcept(160232), // rhesus type
                        concService.getConcept(163126) // blood group
                ));
        JSONArray hivMonitoring = buildTestPanelWithoutPanelConcept("Blood", labTestJsonPayload,
                "HIV MONITORING", Arrays.asList(
                        concService.getConcept(856), // hiv viral load
                        //   concService.getConcept(1305), // hiv viral load Qualitative
                        concService.getConcept(5497), // cd4 counts
                        concService.getConcept(730), // cd4%
                        concService.getConcept(163722), // hiv rapid test
                        concService.getConcept(1030) // DNA PCR
                ));

        JSONArray tbMonitoring = buildTestPanelWithoutPanelConcept("Blood", labTestJsonPayload,
                "TB MONITORING", Arrays.asList(
                        concService.getConcept(162202) // GeneXpert MTB/RIF
                ));
        JSONArray sputumTb = buildTestPanelWithoutPanelConcept("Sputum", labTestJsonPayload,
                "TB MONITORING", Arrays.asList(
                        concService.getConcept(307),
                        concService.getConcept(1465),
                        concService.getConcept(162202) // GeneXpert MTB/RIF
                ));

        model.put("labTestJsonPayload", labTestJsonPayload.toString());



    }

    public void getActiveLabOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService")
            ConceptService conceptService,
                                   Patient patient, PageModel model) {
        OrderType labType = orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        List<Order> activeOrders = orderService.getActiveOrders(patient, labType, null, null);

        JSONArray panelList = new JSONArray();

        for (Order order : activeOrders) {
            Concept labTestConcept = order.getConcept();
            String inputType = "";
            String orderUuid = order.getUuid();
            Integer orderId = order.getOrderId();
            Encounter labTestEncounter = order.getEncounter();


            JSONObject labOrderObject = new JSONObject();
            // develop rendering for result
            JSONArray testResultList = new JSONArray();

            labOrderObject.put("label", labTestConcept.getName(LOCALE).getName());
            labOrderObject.put("concept", labTestConcept.getUuid());
            labOrderObject.put("orderUuid", orderUuid);
            labOrderObject.put("orderId", orderId);
            labOrderObject.put("encounter", labTestEncounter.getUuid());
            labOrderObject.put("dateActivated", order.getDateActivated().toString());

            if (labTestConcept.getDatatype().isCoded()) {
                inputType = "select";
                for (ConceptAnswer ans : labTestConcept.getAnswers()) {
                    JSONObject testResultObject = new JSONObject();
                    testResultObject.put("concept", ans.getAnswerConcept().getUuid());
                    testResultObject.put("label", ans.getAnswerConcept().getName(LOCALE).getName());
                    testResultList.add(testResultObject);
                }

                labOrderObject.put("answers", testResultList);


            } else if (labTestConcept.getDatatype().isNumeric()) {
                inputType = "inputnumeric";
            } else if (labTestConcept.getDatatype().isText()) {
                inputType = "inputtext";
            } else if (labTestConcept.getDatatype().isBoolean()) {
                inputType = "select";
                labOrderObject.put("answers", constructBooleanAnswers());
            }
            labOrderObject.put("rendering", inputType);

            panelList.add(labOrderObject);

        }
        model.put("enterLabOrderResults", panelList.toString());
    }

    public void getPastLabOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService")
            ConceptService conceptService,
                             @SpringBean("careSetting")
                                     CareSetting careSetting,
                             Patient patient, PageModel model,@SpringBean("obsService") ObsService obsService) {
        OrderType labType = orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        CareSetting careset = orderService.getCareSetting(1);
        List<Order> labOrders = orderService.getOrders(patient, careset, labType, false);

        JSONArray labOrdersList = new JSONArray();


        for (Order order : labOrders) {
            if (order.getDateStopped() != null) {

            Concept labConcept = order.getConcept();
            if (order.getDateStopped() != null) {
                List<Concept> labConcepts = new ArrayList<Concept>();
                labConcepts.add(labConcept);
                for (Concept con : labConcepts) {

                    List<Obs> pastOrders = obsService.getObservationsByPersonAndConcept(patient, conceptService.getConcept(con.getConceptId()));

                    if (pastOrders != null) {

                        for (Obs obs : pastOrders) {
                            JSONObject obsObj = new JSONObject();
                            if (obs.getOrder() != null) {
                                if (obs.getOrder().getOrderId() != null) {
                                    Integer orderId = obs.getOrder().getOrderId();

                                    if (obs.getValueCoded() != null) {
                                        obsObj.put("valueCoded", obs.getValueCoded().getName(LOCALE).getName());
                                    }
                                    obsObj.put("valueNumeric", obs.getValueNumeric());
                                    obsObj.put("valueText", obs.getValueText());
                                    obsObj.put("name", con.getName(LOCALE).getName());
                                    obsObj.put("obsId", obs.getId());
                                    obsObj.put("OrderId", orderId);
                                    obsObj.put("orderUuid", obs.getOrder().getUuid());
                                    obsObj.put("encounter", obs.getOrder().getEncounter().getUuid());
                                    if(obs.getOrder() !=null && obs.getOrder().getOrderReason() != null) {
                                        obsObj.put("orderReasonCoded", obs.getOrder().getOrderReason().getUuid());
                                        obsObj.put("orderReason", obs.getOrder().getOrderReason().getUuid());

                                    }
                                    obsObj.put("orderReasonNonCoded", obs.getOrder().getOrderReasonNonCoded());
                                    obsObj.put("obsUuid", obs.getUuid());
                                    obsObj.put("concept", obs.getConcept().getUuid());
                                    obsObj.put("concept_id", obs.getConcept().getId());
                                    obsObj.put("dateActivated", obs.getOrder().getDateActivated().toString());
                                    obsObj.put("resultDate", obs.getObsDatetime().toString());
                                    labOrdersList.add(obsObj);


                                }
                            }


                        }


                    }

                }
            }
        }
        }
        model.put("pastLabOrdersResults", labOrdersList.toString());
    }


    private JSONArray constructBooleanAnswers() {
        JSONArray ansList = new JSONArray();

        for (Integer ans : Arrays.asList(1065, 1066)) {
            Concept concept = concService.getConcept(ans);
            JSONObject testResultObject = new JSONObject();
            testResultObject.put("concept", concept.getUuid());
            testResultObject.put("label", concept.getName(LOCALE).getName());
            ansList.add(testResultObject);
        }
        return ansList;

    }
    private JSONArray buildTestPanelWithoutPanelConcept(String sampleType, JSONArray sampleTypeArray, String panelName,
                                                        List<Concept> testConcepts) {
        if (null == panelName || panelName.equals("") || null == testConcepts || testConcepts.size() == 0)
            return null ;

        JSONObject labTestObj, panelObj;
        JSONArray labTestArray = new JSONArray();
        panelObj = new JSONObject();

        for (Concept labTest : testConcepts) {
            labTestObj = new JSONObject();
            labTestObj.put("concept_id", labTest.getConceptId());
            labTestObj.put("concept", labTest.getUuid());
            labTestObj.put("name", concService.getConcept(labTest.getConceptId()).getName(LOCALE).getName());
            labTestArray.add(labTestObj);


        }
        panelObj.put("name", panelName);
        panelObj.put("tests", labTestArray);

        JSONArray typePanels = getSampleTypePanels(sampleTypeArray, sampleType);
        if (typePanels != null)
            typePanels.add(panelObj);


        return typePanels;
    }

    private JSONArray getSampleTypePanels(JSONArray array, String key)
    {
        JSONArray value = null;
        for (Object o : array)
        {
            JSONObject item = (JSONObject) o;
            if (key.equals(item.get("name"))) {
                value = (JSONArray) item.get("panels");
                break;
            }
        }    return value;
    }


}