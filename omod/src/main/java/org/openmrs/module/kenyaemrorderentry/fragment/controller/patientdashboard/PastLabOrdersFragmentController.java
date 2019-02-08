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

public class PastLabOrdersFragmentController {
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
        getLabOrders(orderService, conceptService,careSetting, patient, model,obsService);

    }
    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }

    public void getLabOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService")
            ConceptService conceptService,
                             @SpringBean("careSetting")
                                     CareSetting careSetting,
                             Patient patient, FragmentModel model,@SpringBean("obsService") ObsService obsService) {
        OrderType labType = orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        CareSetting careset = orderService.getCareSetting(1);
        List<Order> labOrders = orderService.getOrders(patient, careset, labType, false);

        JSONArray labOrdersList = new JSONArray();

        for (Order order : labOrders) {
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
                                    obsObj.put("OrderId", obs.getOrder().getOrderId());
                                    if(order.getOrderReason() != null) {
                                        obsObj.put("orderReasonCoded", order.getOrderReason().getUuid());
                                    }

                                    obsObj.put("orderReasonNonCoded", order.getOrderReasonNonCoded());
                                    obsObj.put("obsUuid", obs.getUuid());
                                    obsObj.put("concept", obs.getConcept().getUuid());
                                    obsObj.put("concept_id", obs.getConcept().getId());
                                    obsObj.put("dateActivated", orderService.getOrder(orderId).getDateActivated().toString());
                                    obsObj.put("resultDate", obs.getObsDatetime().toString());
                                    labOrdersList.add(obsObj);

                                }
                            }


                        }
                    }

                }
            }
        }
        model.put("pastLabOrdersResults", labOrdersList.toString());
    }

}
