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
    public static final Locale LOCALE = Locale.ENGLISH;
    ConceptService concService = Context.getConceptService();

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
                    orderSetMember.put("drug_id",template[4]);
                    orderSetMember.put("dose",template[1]);
                    orderSetMember.put("units",template[2]);
                  //  orderSetMember.put("units_uuid",template[5]);
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
                               @SpringBean("orderSetService") OrderSetService orderSetService,
                               @RequestParam("payload") String payload) throws ParseException {
        System.out.println("payload++++++++++++++++++++++++++++++++++++++++++++++++++++"+payload);
        OrderGroup orderGroup=new OrderGroup();
        JSONParser parser=new JSONParser();
        Object object=parser.parse(payload);
        JSONObject orderContext=(JSONObject)object;
        String patientUuid=orderContext.get("patient").toString();
        String providerId=orderContext.get("provider").toString();
        JSONArray drugGroupOrder=(JSONArray)orderContext.get("drugs");
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Encounter encounter = new Encounter();
        EncounterType encounterType=encounterService.getEncounterTypeByUuid("7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3");
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        Date today=new Date();
        encounter.setEncounterDatetime(today);
        Provider provider = providerService.getProvider(Integer.valueOf(providerId));
        ArrayList<Order> orderList=new ArrayList<Order>();
        for(int i=0; i<drugGroupOrder.size();i++) {
            DrugOrder drugOrder = new DrugOrder();
            JSONObject drugOrderJson=(JSONObject)drugGroupOrder.get(i);
            String drugId=drugOrderJson.get("drug").toString();
            Double dose=Double.parseDouble(drugOrderJson.get("dose").toString());
            int doseUnitConceptId=Integer.valueOf(drugOrderJson.get("dose_unit").toString());
            int frequencyId=Integer.valueOf(drugOrderJson.get("frequency").toString());
            Double quantity=Double.parseDouble(drugOrderJson.get("quantity").toString());
            int quantityUnitConceptId=Integer.valueOf(drugOrderJson.get("quantity_units").toString());

            drugOrder.setPatient(patient);
            drugOrder.setEncounter(encounter);
            Drug drug = conceptService.getDrugByNameOrId(drugId);
            drugOrder.setDrug(drug);
            drugOrder.setOrderer(provider);
            drugOrder.setDose(dose);
            Concept doseUnitConcept = conceptService.getConcept(doseUnitConceptId);
            drugOrder.setDoseUnits(doseUnitConcept);
            drugOrder.setDosingType(SimpleDosingInstructions.class);
            Concept route = conceptService.getConcept(160240);
            drugOrder.setRoute(route);
            OrderFrequency orderFrequency = orderService.getOrderFrequency(frequencyId);
            drugOrder.setFrequency(orderFrequency);
            CareSetting careSetting = orderService.getCareSetting(1);
            drugOrder.setCareSetting(careSetting);
            drugOrder.setQuantity(quantity);
            Concept quantityUnits = conceptService.getConcept(quantityUnitConceptId);
            drugOrder.setQuantityUnits(quantityUnits);
            drugOrder.setNumRefills(0);
            drugOrder.setOrderGroup(orderGroup);
            orderList.add(drugOrder);
        }

        orderGroup.setEncounter(encounter);
        orderGroup.setPatient(patient);
        OrderSet orderSet=orderSetService.getOrderSet(1);
        orderGroup.setOrderSet(orderSet);
        orderGroup.setOrders(orderList);
        orderService.saveOrderGroup(orderGroup);
    }

}