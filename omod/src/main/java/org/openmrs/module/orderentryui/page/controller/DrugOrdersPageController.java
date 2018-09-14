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
                    orderSetMember.put("units_uuid",template[5]);
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
        labOrdersConceptPanels( conceptService, sessionContext, ui, model);
        getActiveLabOrders(orderService,conceptService,patient,model);

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
                conceptService.getConcept(163652)


        );

        // Build panels for Blood
        List<Concept> bloodTestPanels = Arrays.asList(
                conceptService.getConcept( 	161430),
                conceptService.getConcept( 	657),
                conceptService.getConcept( 	161487),
                conceptService.getConcept( 	161437),
                conceptService.getConcept( 	163602),
                conceptService.getConcept( 	1010),
                conceptService.getConcept( 	161532)




        );
        sampleTypes.put("Urine", urineTestPanels);
        sampleTypes.put("Blood", bloodTestPanels);



        JSONArray labTestJsonPayload = new JSONArray();

        for (Map.Entry<String, List<Concept>> entry : sampleTypes.entrySet()) {
            JSONObject sampleTypeObject = new JSONObject();

            String testSampleTypeName = entry.getKey();
            List<Concept> panelConcepts = entry.getValue();

            JSONArray sampleTypePanels = new JSONArray();

            for (Concept panelConcept : panelConcepts) {
                JSONObject labTestObj, panelObj;
                JSONArray labTestArray = new JSONArray();
                panelObj=new JSONObject();

                String panelName = panelConcept.getName(LOCALE).getName();

                for(Concept labTest:conceptService.getConceptsByConceptSet(panelConcept)) {
                    labTestObj=new JSONObject();
                    labTestObj.put("concept_id",labTest.getConceptId());
                    labTestObj.put("concept",labTest.getUuid());
                    labTestObj.put("name",conceptService.getConcept(labTest.getConceptId()).getName(LOCALE).getName());
                    labTestArray.add(labTestObj);


                }
                panelObj.put("name",panelName);
                panelObj.put("tests",labTestArray);
                sampleTypePanels.add(panelObj);

            }
            sampleTypeObject.put("name", testSampleTypeName);
            sampleTypeObject.put("panels", sampleTypePanels);
            labTestJsonPayload.add(sampleTypeObject);


        }
       // System.out.println("labTestJsonPayload=========================+++++"+labTestJsonPayload);
        model.put("labTestJsonPayload", labTestJsonPayload.toString());


    }
    public void getActiveLabOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService") ConceptService conceptService,
                                   Patient patient, PageModel model) {
        OrderType labType = orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        List<Order> activeOrders = orderService.getActiveOrders(patient, labType, null, null);

        System.out.println("Active orders =========================+++++" + activeOrders);


        JSONArray panelList = new JSONArray();

        for(Order order : activeOrders) {
            Concept labTestConcept = order.getConcept();
            String inputType = "";
            String orderUuid = order.getUuid();
            Integer orderId = order.getOrderId();

            JSONObject labOrderObject = new JSONObject();
            // develop rendering for result
            JSONArray testResultList = new JSONArray();

            labOrderObject.put("label", labTestConcept.getName(LOCALE).getName());
            labOrderObject.put("concept", labTestConcept.getUuid());
            labOrderObject.put("orderUuid", orderUuid);
            labOrderObject.put("orderId", orderId);

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
        System.out.println("panelList=========================+++++"+panelList.toString());
        model.put("panelList", panelList.toString());

       // return panelList.toString();

    }

    private JSONArray constructBooleanAnswers() {
        JSONArray ansList = new JSONArray();

        for(Integer ans : Arrays.asList(1065, 1066)) {
            Concept concept = concService.getConcept(ans);
            JSONObject testResultObject = new JSONObject();
            testResultObject.put("concept", concept.getUuid());
            testResultObject.put("label", concept.getName(LOCALE).getName());
            ansList.add(testResultObject);
        }
        return ansList;

    }

}