package org.openmrs.module.orderentryui.fragment.controller.patientdashboard;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegimenDispensationFragmentController {

    public void controller() throws Exception {

    }

    public void saveOrderGroup(@SpringBean("orderService") OrderService orderService,
                               @SpringBean("patientService")PatientService patientService,
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
        String providerUuid=orderContext.get("provider").toString();
        JSONArray drugGroupOrder=(JSONArray)orderContext.get("drugs");
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Encounter encounter = new Encounter();
        EncounterType encounterType=encounterService.getEncounterTypeByUuid("7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3");
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        Date today=new Date();
        encounter.setEncounterDatetime(today);
        encounterService.saveEncounter(encounter);
        Provider provider = providerService.getProviderByUuid(providerUuid);
        ArrayList<Order> orderList=new ArrayList<Order>();
        for(int i=0; i<drugGroupOrder.size();i++) {
            DrugOrder drugOrder = new DrugOrder();
            JSONObject drugOrderJson=(JSONObject)drugGroupOrder.get(i);
            String drugId=drugOrderJson.get("drug").toString();
            Double dose=Double.parseDouble(drugOrderJson.get("dose").toString());
            int doseUnitConceptId=Integer.valueOf(drugOrderJson.get("dose_unit").toString());
            int frequencyId=Integer.valueOf(drugOrderJson.get("frequency").toString());
            Double quantity=Double.parseDouble(drugOrderJson.get("quantity").toString());
            int quantityUnitConceptId=Integer.valueOf(drugOrderJson.get("quantity_unit").toString());

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
