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
                               @RequestParam("payload") String payload) throws Exception {
        boolean orderGroupExists=false;
        System.out.println("payload++++++++++++++++++++++++++++++"+payload.toString());
        JSONParser parser=new JSONParser();
        Object object=parser.parse(payload);
        JSONObject orderContext=(JSONObject)object;
        OrderGroup orderGroup;
        if(orderContext.get("activeOrderGroupUuId") !=null) {
            String orderGroupUuId=orderContext.get("activeOrderGroupUuId").toString();
            orderGroup=orderService.getOrderGroupByUuid(orderGroupUuId);
            orderGroupExists=true;
        }
        else{
            orderGroup=new OrderGroup();
        }
        String patientUuid=orderContext.get("patient").toString();
        String providerUuid=orderContext.get("provider").toString();
        JSONArray drugGroupOrder=(JSONArray)orderContext.get("drugs");
        Patient patient = patientService.getPatientByUuid(patientUuid);
        String orderSetId=orderContext.get("orderSetId").toString();

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
            JSONObject drugOrderJson=(JSONObject)drugGroupOrder.get(i);
            DrugOrder drugOrder;
            String drugId=drugOrderJson.get("drug_id").toString();
            Double dose=Double.parseDouble(drugOrderJson.get("dose").toString());
            String doseUnitConceptUuiId=drugOrderJson.get("units").toString();
            String frequencyUuId=drugOrderJson.get("frequency").toString();
            Double quantity=Double.parseDouble(drugOrderJson.get("quantity").toString());
            if(orderGroupExists){
                drugOrder=(DrugOrder)orderService.getOrder(Integer.valueOf(drugOrderJson.get("order_id").toString())).cloneForRevision();
                drugOrder.setDose(dose);
                Concept doseUnitConcept = conceptService.getConceptByUuid(doseUnitConceptUuiId);
                drugOrder.setDoseUnits(doseUnitConcept);
                //OrderFrequency orderFrequency = orderService.getOrderFrequencyByUuid(frequencyUuId);
                //drugOrder.setFrequency(orderFrequency);
                drugOrder.setQuantity(quantity);
                drugOrder.setQuantityUnits(doseUnitConcept);
                drugOrder.setInstructions("Take after a meal");
                drugOrder.setOrderer(provider);
                drugOrder.setEncounter(encounter);
                orderList.add(drugOrder);
            }
            else{
                drugOrder=new DrugOrder();
                drugOrder.setPatient(patient);
                drugOrder.setEncounter(encounter);
                Drug drug = conceptService.getDrugByNameOrId(drugId);
                drugOrder.setDrug(drug);
                drugOrder.setOrderer(provider);
                drugOrder.setDose(dose);
                Concept doseUnitConcept = conceptService.getConceptByUuid(doseUnitConceptUuiId);
                drugOrder.setDoseUnits(doseUnitConcept);
                drugOrder.setDosingType(SimpleDosingInstructions.class);
                Concept route = conceptService.getConcept(160240);
                drugOrder.setRoute(route);
                OrderFrequency orderFrequency = orderService.getOrderFrequencyByUuid(frequencyUuId);
                drugOrder.setFrequency(orderFrequency);
                CareSetting careSetting = orderService.getCareSetting(1);
                drugOrder.setCareSetting(careSetting);
                drugOrder.setQuantity(quantity);
                drugOrder.setQuantityUnits(doseUnitConcept);
                drugOrder.setNumRefills(0);
                drugOrder.setOrderGroup(orderGroup);
                orderList.add(drugOrder);
            }

        }
        if(!orderGroupExists){
            orderGroup.setEncounter(encounter);
            orderGroup.setPatient(patient);
            OrderSet orderSet=orderSetService.getOrderSet(Integer.valueOf(orderSetId));
            orderGroup.setOrderSet(orderSet);
        }
        orderGroup.setOrders(orderList);
        orderService.saveOrderGroup(orderGroup);
    }
    public void discontintueOrderGroup(@SpringBean("orderService") OrderService orderService,
                               @SpringBean("patientService")PatientService patientService,
                               @SpringBean("encounterService") EncounterService encounterService,
                               @SpringBean("conceptService") ConceptService conceptService,
                               @SpringBean("providerService") ProviderService providerService,
                               @SpringBean("orderSetService") OrderSetService orderSetService,
                               @RequestParam("payload") String payload) throws Exception {
        System.out.println("payload++++++++++++++++++++++++++++++"+payload.toString());
        JSONParser parser=new JSONParser();
        Object object=parser.parse(payload);
        JSONObject orderContext=(JSONObject)object;
        if(orderContext.get("discontinueOrderUuId") !=null){

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
                JSONObject drugOrderJson = (JSONObject) drugGroupOrder.get(i);
                DrugOrder drugOrder=(DrugOrder)orderService.getOrder(Integer.valueOf(drugOrderJson.get("order_id").toString()));
                DrugOrder orderToDiscontinue = (DrugOrder)orderService.discontinueOrder(drugOrder, "order fulfilled", null, provider, encounter);
                orderList.add(orderToDiscontinue);
            }
        }
    }

}
