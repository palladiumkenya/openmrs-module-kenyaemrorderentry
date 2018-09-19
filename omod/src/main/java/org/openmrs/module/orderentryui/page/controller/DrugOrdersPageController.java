package org.openmrs.module.orderentryui.page.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

public class DrugOrdersPageController {

    public void get(@RequestParam("patient") Patient patient,
                    @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                    @SpringBean("encounterService") EncounterService encounterService,
                    @SpringBean("orderService") OrderService orderService,
                    UiSessionContext sessionContext,
                    UiUtils ui,
                    PageModel model,
                    @SpringBean("orderSetService") OrderSetService orderSetService,
                    @SpringBean("patientService")PatientService patientService,
                    @SpringBean("conceptService") ConceptService conceptService,
                    @SpringBean("providerService") ProviderService providerService) {

        // HACK
        EncounterType drugOrderEncounterType = encounterService.getAllEncounterTypes(false).get(0);
        EncounterRole encounterRoles = encounterService.getAllEncounterRoles(false).get(0);

        List<CareSetting> careSettings = orderService.getCareSettings(false);

        List<Concept> dosingUnits = orderService.getDrugDosingUnits();
        List<Concept> dispensingUnits = orderService.getDrugDispensingUnits();
        Set<Concept> quantityUnits = new LinkedHashSet<Concept>();
        quantityUnits.addAll(dosingUnits);
        quantityUnits.addAll(dispensingUnits);

        Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
        jsonConfig.put("patient", convertToFull(patient));
        jsonConfig.put("provider", convertToFull(sessionContext.getCurrentProvider()));
        jsonConfig.put("encounterRole", convertToFull(encounterRoles));
        jsonConfig.put("drugOrderEncounterType", convertToFull(drugOrderEncounterType));
        jsonConfig.put("careSettings", convertToFull(careSettings));
        jsonConfig.put("routes", convertToFull(orderService.getDrugRoutes()));
        jsonConfig.put("doseUnits", convertToFull(dosingUnits));
        jsonConfig.put("durationUnits", convertToFull(orderService.getDurationUnits()));
        jsonConfig.put("quantityUnits", convertToFull(dispensingUnits)); // after TRUNK-4524 is fixed, change this to quantityUnits
        jsonConfig.put("frequencies", convertTo(orderService.getOrderFrequencies(false), new NamedRepresentation("fullconcept")));
        if (careSetting != null) {
            jsonConfig.put("intialCareSetting", careSetting.getUuid());
        }

        model.put("patient", patient);
        model.put("jsonConfig", ui.toJson(jsonConfig));

        OrderType drugOrders = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        List<Order> activeDrugOrders = orderService.getActiveOrders(patient, drugOrders, null, null);
        JSONObject orderObj,component;
        JSONArray orderGroupArray=new JSONArray();
        JSONArray orderArray=new JSONArray();
        JSONArray components=new JSONArray();
        int previousOrderGroupId=0;
        for(Order order:activeDrugOrders){
            DrugOrder drugOrder=(DrugOrder)order;
            if(order.getOrderGroup()!=null){
                component=new JSONObject();
                component.put("name", drugOrder.getDrug().getName());
                component.put("dose", drugOrder.getDose().toString());
                component.put("units", drugOrder.getDoseUnits().getUuid());
                component.put("frequency", drugOrder.getFrequency().getUuid());
                component.put("drug_id", drugOrder.getDrug().getDrugId());
                component.put("order_id",order.getOrderId());
                if(order.getOrderGroup().getOrderGroupId()==previousOrderGroupId){
                    components.add(component);
                    continue;
                }
                else{
                    orderObj = new JSONObject();
                    components=new JSONArray();
                    components.add(component);
                    OrderSet orderSet=order.getOrderGroup().getOrderSet();
                    orderObj.put("name",orderSet.getName());
                    orderObj.put("date",order.getDateActivated().toString());
                    orderObj.put("orderGroupUuId",order.getOrderGroup().getUuid());
                    orderObj.put("orderSetId",orderSet.getOrderSetId());
                    orderObj.put("components", components);
                    orderGroupArray.add(orderObj);
                    previousOrderGroupId=order.getOrderGroup().getOrderGroupId();
                }
            }
            else {
                orderObj = new JSONObject();
                orderObj.put("uuid", order.getUuid());
                orderObj.put("orderNumber", order.getOrderNumber());
                orderObj.put("concept", convertToFull(order.getConcept()));
                orderObj.put("careSetting", convertToFull(order.getCareSetting()));
                orderObj.put("dateActivated", order.getDateActivated().toString());
                orderObj.put("encounter", convertToFull(order.getEncounter()));
                orderObj.put("orderer", convertToFull(order.getOrderer()));
                orderObj.put("drug", convertToFull(drugOrder.getDrug()));
                orderObj.put("dosingType", drugOrder.getDosingType());
                orderObj.put("dose", drugOrder.getDose());
                orderObj.put("doseUnits", convertToFull(drugOrder.getDoseUnits()));
                orderObj.put("frequency", convertToFull(drugOrder.getFrequency()));
                orderObj.put("quantity", drugOrder.getQuantity());
                orderObj.put("quantityUnits", convertToFull(drugOrder.getQuantityUnits()));
                orderObj.put("route", convertToFull(drugOrder.getRoute()));
                orderArray.add(orderObj);
            }
        }
        JSONObject activeOrdersResponse=new JSONObject();
        activeOrdersResponse.put("order_groups",orderGroupArray);
        activeOrdersResponse.put("single_drugs",orderArray);
        model.put("activeOrdersResponse",ui.toJson(activeOrdersResponse));
    }

    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }

}