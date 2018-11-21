package org.openmrs.module.kenyaemrorderentry.fragment.controller.patientdashboard;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

public class LabOrdersResultsFragmentController {
    public static final Locale LOCALE = Locale.ENGLISH;
    ConceptService concService = Context.getConceptService();

    public void controller(FragmentConfiguration config,
                          // @RequestParam("patient") Patient patient,
                           @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                           @SpringBean("encounterService") EncounterService encounterService,
                           @SpringBean("orderService") OrderService orderService,
                           UiSessionContext sessionContext,
                           UiUtils ui,
                           FragmentModel model,
                           @SpringBean("orderSetService") OrderSetService orderSetService,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean("conceptService") ConceptService conceptService,
                           @SpringBean("providerService") ProviderService providerService,
                           @SpringBean("obsService") ObsService obsService) throws Exception {
        config.require("patient|patientId");
        Patient patient;
        Object pt = config.getAttribute("patient");
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        }
        else {
            // in case we are passed a PatientDomainWrapper (but this module doesn't know about emrapi)
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }
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
        getActiveLabOrders(orderService, conceptService, patient, model);

    }
    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }

    public void getActiveLabOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService")
            ConceptService conceptService,
                                   Patient patient, FragmentModel model) {
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





}
