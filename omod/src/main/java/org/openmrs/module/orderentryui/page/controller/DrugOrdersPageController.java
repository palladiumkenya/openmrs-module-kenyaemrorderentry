package org.openmrs.module.orderentryui.page.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.*;
import org.openmrs.api.*;
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

        List<OrderSet> orderSetsList=orderSetService.getOrderSets(false);
        JSONObject orderSetObj,orderSetMember;
        JSONArray orderSetArray=new JSONArray();

        for(OrderSet orderSet:orderSetsList){
            orderSetObj=new JSONObject();
            orderSetObj.put("name", orderSet.getName());
            orderSetObj.put("regimen_line", orderSet.getDescription());
            JSONArray membersArray=new JSONArray();
            for(OrderSetMember member:orderSet.getOrderSetMembers()){
                orderSetMember=new JSONObject();
                if(member !=null){
                    String[] template=member.getOrderTemplate().split(",");
                    orderSetMember.put("name",template[0]);
                    orderSetMember.put("dose",template[1]);
                    orderSetMember.put("units",template[2]);
                    orderSetMember.put("frequency",template[3]);
                    membersArray.add(orderSetMember);
                }
            }
            orderSetObj.put("components", membersArray);
            orderSetArray.add(orderSetObj);
        }
        JSONObject response=new JSONObject();
        response.put("orderSets", orderSetArray);
        model.put("orderSetJson", response.toString());

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
    }

    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
    public void saveOrderGroup(@SpringBean("orderService") OrderService orderService,
                               @SpringBean("PatientService")PatientService patientService,
                               @SpringBean("encounterService") EncounterService encounterService,
                               @SpringBean("conceptService") ConceptService conceptService,
                               @SpringBean("providerService") ProviderService providerService,
                               @SpringBean("orderSetService") OrderSetService orderSetService){
        OrderGroup orderGroup=new OrderGroup();
        DrugOrder drugOrder=new DrugOrder();
        Patient patient=patientService.getPatientByUuid("b03f8c93-ffb1-4c0a-bbae-0e8e27ffbc2b");
        drugOrder.setPatient(patient);
        Encounter encounter=encounterService.getEncounterByUuid("0a9971ef-1d64-47a1-bdd6-1a5578caeb8f");
        drugOrder.setEncounter(encounter);
        Drug drug=conceptService.getDrugByNameOrId("12");
        drugOrder.setDrug(drug);
        Provider provider=providerService.getProvider(32);
        drugOrder.setOrderer(provider);
        drugOrder.setDose(2.0);
        Concept orderUnit=conceptService.getConcept(1513);
        drugOrder.setDoseUnits(orderUnit);
        drugOrder.setDosingType(SimpleDosingInstructions.class);
        Concept route=conceptService.getConcept(160240);
        drugOrder.setRoute(route);
        OrderFrequency orderFrequency=orderService.getOrderFrequency(6);
        drugOrder.setFrequency(orderFrequency);
        CareSetting careSetting=orderService.getCareSetting(1);
        drugOrder.setCareSetting(careSetting);
        drugOrder.setQuantity(10.0);
        Concept quantityUnits=conceptService.getConcept(1513);
        drugOrder.setQuantityUnits(quantityUnits);
        drugOrder.setNumRefills(0);
        ArrayList<Order> orderList=new ArrayList<Order>();
        drugOrder.setOrderGroup(orderGroup);
        orderList.add(drugOrder);

        orderGroup.setEncounter(encounter);
        orderGroup.setPatient(patient);
        OrderSet orderSet=orderSetService.getOrderSet(1);
        orderGroup.setOrderSet(orderSet);
        orderGroup.setOrders(orderList);
        orderService.saveOrderGroup(orderGroup);
    }

}