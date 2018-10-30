package org.openmrs.module.kenyaemrorderentry.page.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.kenyaemrorderentry.api.DrugRegimenHistory;
import org.openmrs.module.kenyaemrorderentry.api.DrugRegimenHistoryService;
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
                component.put("units_uuid", drugOrder.getDoseUnits().getUuid());
                component.put("frequency", drugOrder.getFrequency().getUuid());
                component.put("drug_id", drugOrder.getDrug().getDrugId());
                component.put("order_id",order.getOrderId());
                component.put("quantity",drugOrder.getQuantity());
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
                    orderObj.put("date",order.getDateCreated().toString());
                    orderObj.put("orderGroupUuId",order.getOrderGroup().getUuid());
                    orderObj.put("orderSetId",orderSet.getOrderSetId());
                    orderObj.put("instructions",order.getInstructions());
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
                orderObj.put("dateActivated", order.getDateCreated().toString());
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
       //saveDrugRegimenHistorys(patient);
        model.put("activeOrdersResponse",ui.toJson(activeOrdersResponse));
        model.put("currentRegimens",ui.toJson(computeCurrentRegimen(patient)));

    }

    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
    private JSONObject computeCurrentRegimen(Patient patient){
        DrugRegimenHistoryService currentRegService = Context.getService(DrugRegimenHistoryService.class);
        List<DrugRegimenHistory> regimenList = currentRegService.getPatientCurrentRegimenByPatient(patient);
        JSONObject patientRegimen=new JSONObject();
        JSONArray regimens=new JSONArray();
        JSONObject regimen;
        for (DrugRegimenHistory reg : regimenList) {
            regimen=new JSONObject();
            regimen.put("name",reg.getRegimenName());
            regimen.put("program",reg.getProgram());
            regimens.add(regimen);
        }
        patientRegimen.put("patientregimens", regimens);
        return patientRegimen;
    }
    private  void saveDrugRegimenHistorys(Patient patient) {
        DrugRegimenHistory drugRegimenHistory = new DrugRegimenHistory();
        drugRegimenHistory.setRegimenName("TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)");
        drugRegimenHistory.setPatient(patient);
        drugRegimenHistory.setOrderGroupId(23);
        drugRegimenHistory.setStatus("active");
        drugRegimenHistory.setProgram("HIV");
        drugRegimenHistory.setOrderSetId(22);

        DrugRegimenHistoryService drugRegService = Context.getService(DrugRegimenHistoryService.class);
        drugRegService.saveDrugRegimenHistory(drugRegimenHistory);


    }
}