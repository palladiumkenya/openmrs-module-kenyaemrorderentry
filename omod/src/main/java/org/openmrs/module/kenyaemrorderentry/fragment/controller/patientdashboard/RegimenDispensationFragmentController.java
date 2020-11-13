package org.openmrs.module.kenyaemrorderentry.fragment.controller.patientdashboard;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
        JSONParser parser=new JSONParser();
        Object object=parser.parse(payload);
        JSONObject orderContext=(JSONObject)object;
        OrderGroup orderGroup;
        String patientUuid=orderContext.get("patient").toString();
        String providerUuid=orderContext.get("provider").toString();
        String dosingInstructions="";
        if(orderContext.get("regimenDosingInstructions") !=null){
            dosingInstructions=orderContext.get("regimenDosingInstructions").toString();
        }
        Date date=null;
        if(!orderContext.get("orderDate").toString().trim().isEmpty()){
            String orderDate=orderContext.get("orderDate").toString();
            date=new SimpleDateFormat("dd-MMM-yyyy", Locale.US).parse(orderDate);
        }
        else{
            date=new Date();
        }
        JSONArray drugGroupOrder=(JSONArray)orderContext.get("drugs");
        Patient patient = patientService.getPatientByUuid(patientUuid);
        String orderSetId=orderContext.get("orderSetId") != null? orderContext.get("orderSetId").toString() : "";
        Provider provider = providerService.getProviderByUuid(providerUuid);
        if(orderContext.get("activeOrderGroupUuId") !=null){
            String orderGroupUuId=orderContext.get("activeOrderGroupUuId").toString();
            orderGroup=orderService.getOrderGroupByUuid(orderGroupUuId);
            orderGroupExists=true;
        }
        else{
            orderGroup=new OrderGroup();
        }
        Encounter encounter = new Encounter();
        EncounterType encounterType=encounterService.getEncounterTypeByUuid("7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3");
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(date);
        encounter.setDateCreated(date);
        encounterService.saveEncounter(encounter);
        ArrayList<Order> orderList=new ArrayList<Order>();
        for(int i=0; i<drugGroupOrder.size();i++) {
            JSONObject drugOrderJson=(JSONObject)drugGroupOrder.get(i);
            DrugOrder drugOrder;
            String drugId = drugOrderJson.get("drug_id").toString();
            Double dose = Double.parseDouble(drugOrderJson.get("dose").toString());
            int drugDuration = Integer.parseInt(drugOrderJson.get("drugDuration").toString());
            String drugDurationUnitUuid = drugOrderJson.get("drugDurationUnit").toString();
            String doseUnitConceptUuiId = drugOrderJson.get("units_uuid").toString();
            String quantityUnitConceptUuiId = drugOrderJson.get("quantity_units").toString();
            String frequencyUuId = drugOrderJson.get("frequency").toString();
            Double quantity = Double.parseDouble(drugOrderJson.get("quantity").toString());
            if(orderGroupExists){
                drugOrder=(DrugOrder)orderService.getOrder(Integer.valueOf(drugOrderJson.get("order_id").toString())).cloneForRevision();
                drugOrder.setDose(dose);
                drugOrder.setDuration(drugDuration);
                drugOrder.setDurationUnits(conceptService.getConceptByUuid(drugDurationUnitUuid));
                Concept doseUnitConcept = conceptService.getConceptByUuid(doseUnitConceptUuiId);
                Concept quantityUnitConcept=conceptService.getConceptByUuid(quantityUnitConceptUuiId);
                drugOrder.setDoseUnits(doseUnitConcept);
                drugOrder.setQuantity(quantity);
                drugOrder.setQuantityUnits(quantityUnitConcept);
                drugOrder.setInstructions(dosingInstructions);
                drugOrder.setOrderer(provider);
                drugOrder.setEncounter(encounter);
                drugOrder.setDateCreated(date);
                orderList.add(drugOrder);
            }
            else{
                drugOrder = new DrugOrder();
                drugOrder.setPatient(patient);
                drugOrder.setEncounter(encounter);
                drugOrder.setDateCreated(date);
                Drug drug = conceptService.getDrugByNameOrId(drugId);
                drugOrder.setDrug(drug);
                drugOrder.setInstructions(dosingInstructions);
                drugOrder.setOrderer(provider);
                drugOrder.setDose(dose);
                drugOrder.setDuration(drugDuration);
                drugOrder.setDurationUnits(conceptService.getConceptByUuid(drugDurationUnitUuid));
                Concept doseUnitConcept = conceptService.getConceptByUuid(doseUnitConceptUuiId);
                Concept quantityUnitConcept=conceptService.getConceptByUuid(quantityUnitConceptUuiId);
                drugOrder.setDoseUnits(doseUnitConcept);
                drugOrder.setDosingType(SimpleDosingInstructions.class);
                Concept route = conceptService.getConcept(160240);
                drugOrder.setRoute(route);
                OrderFrequency orderFrequency = orderService.getOrderFrequencyByUuid(frequencyUuId);
                drugOrder.setFrequency(orderFrequency);
                CareSetting careSetting = orderService.getCareSetting(1);
                drugOrder.setCareSetting(careSetting);
                drugOrder.setQuantity(quantity);
                drugOrder.setQuantityUnits(quantityUnitConcept);
                drugOrder.setNumRefills(0);
                drugOrder.setOrderGroup(orderGroup);
                orderList.add(drugOrder);
            }

        }
        if(!orderGroupExists){
            orderGroup.setEncounter(encounter);
            orderGroup.setPatient(patient);
            if (orderSetId != null && orderSetId != "") {
                OrderSet orderSet = orderSetService.getOrderSet(Integer.valueOf(orderSetId));
                orderGroup.setOrderSet(orderSet);
            }

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
        JSONParser parser = new JSONParser();
        Object object = parser.parse(payload);
        JSONObject orderContext = (JSONObject) object;
        String patientUuid = orderContext.get("patient").toString();
        String providerUuid = orderContext.get("provider").toString();
        JSONArray drugGroupOrder = (JSONArray) orderContext.get("drugs");
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Provider provider = providerService.getProviderByUuid(providerUuid);
        Date date=null;
        if(!orderContext.get("orderDate").toString().trim().isEmpty()){
            String orderDate=orderContext.get("orderDate").toString();
            date=new SimpleDateFormat("dd-MMM-yyyy", Locale.US).parse(orderDate);
        }
        else{
            date=new Date();
        }
        discontinueOrder(patient,provider,drugGroupOrder,encounterService,orderService,date);
    }
    private void discontinueOrder(Patient patient,Provider provider,JSONArray drugs,
        EncounterService encounterService,OrderService orderService,Date orderDate){
        Encounter encounter = new Encounter();
        EncounterType encounterType=encounterService.getEncounterTypeByUuid("7df67b83-1b84-4fe2-b1b7-794b4e9bfcc3");
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(orderDate);
        encounterService.saveEncounter(encounter);
        ArrayList<Order> orderList=new ArrayList<Order>();
        for(int i=0; i<drugs.size();i++) {
            JSONObject drugOrderJson = (JSONObject) drugs.get(i);
            DrugOrder drugOrder=(DrugOrder)orderService.getOrder(Integer.valueOf(drugOrderJson.get("order_id").toString()));
            DrugOrder orderToDiscontinue = null;
            try {
                orderToDiscontinue = (DrugOrder)orderService.discontinueOrder(drugOrder, "order fulfilled", null, provider, encounter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            orderList.add(orderToDiscontinue);
        }

    }

}
