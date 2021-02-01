package org.openmrs.module.kenyaemrorderentry.page.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.CareSetting;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderSet;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.kenyaemrorderentry.util.OrderEntryUIUtils;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageContext;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@AppPage("kenyaemr.drugorder")
public class DrugOrdersPageController {
    public static final Locale LOCALE = Locale.ENGLISH;

    public void get(@RequestParam("patientId") Patient patient,
                    @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                    @SpringBean("encounterService") EncounterService encounterService,
                    @SpringBean("orderService") OrderService orderService,
                    UiSessionContext sessionContext,
                    UiUtils ui,
                    PageModel model,
                    PageContext pageContext,
                    @SpringBean("conceptService") ConceptService conceptService,
                    @SpringBean("obsService") ObsService obsService) {



        OrderEntryUIUtils.setDrugOrderPageAttributes(pageContext, OrderEntryUIUtils.APP_DRUG_ORDER);

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

        // Build customized duration units
        List<Concept> customizedDurationUnits = Arrays.asList(
                conceptService.getConcept(1072),
                conceptService.getConcept(1074),
                conceptService.getConcept(1073)
        );
        JSONArray durationUnitsArray = new JSONArray();

        for (Concept durationUnitConcept : customizedDurationUnits) {
            JSONObject duraitonUnitObj = new JSONObject();
            duraitonUnitObj.put("name",conceptService.getConcept(durationUnitConcept.getConceptId()).getName(LOCALE).getName());
            duraitonUnitObj.put("uuid",durationUnitConcept.getUuid());
            duraitonUnitObj.put("concept_id",durationUnitConcept.getConceptId());
            durationUnitsArray.add(duraitonUnitObj);

        }

        JSONObject durationUnitsResponse=new JSONObject();
        durationUnitsResponse.put("durationUnitsResponse",durationUnitsArray);
        model.put("durationUnitsResponse",durationUnitsResponse.toString());


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
                component.put("name", drugOrder.getDrug() != null && drugOrder.getDrug().getConcept() != null ? mapConceptNamesToShortNames(drugOrder.getDrug().getConcept().getUuid()) : "");
                component.put("conceptUuid", drugOrder.getDrug() != null ? drugOrder.getDrug().getConcept().getUuid(): "");
                component.put("dose", String.valueOf(drugOrder.getDose().intValue()));
                component.put("drugDuration", drugOrder.getDuration() != null ? drugOrder.getDuration().toString(): "");
                component.put("units_uuid", drugOrder.getDoseUnits() != null ? drugOrder.getDoseUnits().getUuid(): "");
                component.put("units_name", drugOrder.getDoseUnits()!= null ? drugOrder.getDoseUnits().getName(LOCALE).getName(): "");
                component.put("frequency", drugOrder.getFrequency() != null ? drugOrder.getFrequency().getUuid(): "");
                component.put("frequency_name", drugOrder.getFrequency() != null ? drugOrder.getFrequency().getName(): "");
                component.put("drug_id",  drugOrder.getDrug() != null ? drugOrder.getDrug().getDrugId(): "");
                component.put("order_id",order.getOrderId());
                component.put("quantity",drugOrder.getQuantity()!= null ? drugOrder.getQuantity(): "");
                component.put("quantity_units_name",drugOrder.getQuantityUnits() != null ? drugOrder.getQuantityUnits().getName(LOCALE).getName(): "");
                component.put("quantity_units",drugOrder.getQuantityUnits()!= null ? drugOrder.getQuantityUnits().getUuid(): "");
                component.put("drugDurationUnitName", drugOrder.getDurationUnits() != null ? drugOrder.getDurationUnits().getName(LOCALE).getName() : "");

                if(order.getOrderGroup().getOrderGroupId()==previousOrderGroupId){
                    components.add(component);
                    continue;
                }
                else{
                    orderObj = new JSONObject();
                    components=new JSONArray();
                    components.add(component);
                    OrderSet orderSet=order.getOrderGroup().getOrderSet();
                    orderObj.put("name",orderSet != null ? orderSet.getName(): "");
                    orderObj.put("date",order.getDateCreated().toString());
                    orderObj.put("orderGroupUuId",order.getOrderGroup().getUuid());
                    orderObj.put("orderSetId",orderSet != null ? orderSet.getOrderSetId():"");
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
                orderObj.put("dateActivated", order.getDateActivated().toString());
                orderObj.put("encounter", convertToFull(order.getEncounter()));
                orderObj.put("orderer", convertToFull(order.getOrderer()));
                orderObj.put("drug", convertToFull(drugOrder.getDrug()));
                orderObj.put("dosingType", drugOrder.getDosingType());
                orderObj.put("dose", drugOrder.getDose());
                orderObj.put("drugDuration", drugOrder.getDuration() != null ? drugOrder.getDuration().toString() : "");
                orderObj.put("doseUnits", convertToFull(drugOrder.getDoseUnits()));
                orderObj.put("frequency", convertToFull(drugOrder.getFrequency()));
                orderObj.put("quantity", drugOrder.getQuantity());
                orderObj.put("quantityUnits", convertToFull(drugOrder.getQuantityUnits()));
                orderObj.put("route", convertToFull(drugOrder.getRoute()));
                orderArray.add(orderObj);
            }
        }

        //AppDescriptor app = new AppDescriptor("kenyaemr.drugorder", null, "Drug Order", "kenyaemrorderentry/orders/drugOrderHome.page", null, null, 350, null, null);

        JSONObject activeOrdersResponse=new JSONObject();
        activeOrdersResponse.put("order_groups",orderGroupArray);
        activeOrdersResponse.put("single_drugs",orderArray);
        model.put("activeOrdersResponse",ui.toJson(activeOrdersResponse));
        model.put("hasActiveOrders",activeDrugOrders.size() > 0 ? true : false);
        //model.addAttribute("appHomepageUrl", app.getUrl());
        getPastDrugOrders(orderService, conceptService,careSetting,ui, patient, model,obsService);

    }

    public void getPastDrugOrders(@SpringBean("orderService") OrderService orderService, @SpringBean("conceptService")
            ConceptService conceptService,
                             @SpringBean("careSetting")
                                     CareSetting careSetting,
                                  UiUtils ui,
                             Patient patient, PageModel model,@SpringBean("obsService") ObsService obsService) {
        OrderType drugType = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        CareSetting careset = orderService.getCareSetting(1);
        List<Order> pastOrders = orderService.getOrders(patient, careset, drugType, false);

        JSONObject orderObj,component;
        JSONArray orderGroupArray = new JSONArray();
        JSONArray orderArray = new JSONArray();
        JSONArray components =  new JSONArray();
        int previousOrderGroupId=0;
        for(Order order:pastOrders){

            if(order.getOrderGroup()!= null) {
                if(order.getDateStopped() != null) {
                    DrugOrder drugOrder=(DrugOrder)order.cloneForRevision();

                component = new JSONObject();
                component.put("name", mapConceptNamesToShortNames(drugOrder.getDrug().getConcept().getUuid()));
              //  component.put("name", drugOrder.getDrug().getConcept().getShortNameInLocale(LOCALE) != null ? drugOrder.getDrug().getConcept().getShortNameInLocale(LOCALE).getName() : drugOrder.getDrug().getConcept().getName(LOCALE).getName());
                if(drugOrder.getDose() != null || drugOrder.getDoseUnits() != null || drugOrder.getFrequency() != null || drugOrder.getDuration() != null) {
                    component.put("dose", String.valueOf(drugOrder.getDose().intValue()));
                    component.put("drugDuration", drugOrder.getDuration() != null ? drugOrder.getDuration().toString(): "");
                    component.put("units_uuid", drugOrder.getDoseUnits().getUuid());
                    component.put("units_name", drugOrder.getDoseUnits().getName(LOCALE).getName());
                    component.put("frequency", drugOrder.getFrequency().getUuid());
                    component.put("frequency_name", drugOrder.getFrequency().getName());
                    component.put("drugDurationUnitName", drugOrder.getDurationUnits() != null ? drugOrder.getDurationUnits().getName(LOCALE).getName():"");

                }

                if(drugOrder.getQuantity() != null || drugOrder.getQuantityUnits()!= null) {
                    component.put("quantity", drugOrder.getQuantity());
                    component.put("quantity_units_name", drugOrder.getQuantityUnits().getName(LOCALE).getName());
                    component.put("quantity_units",drugOrder.getQuantityUnits().getUuid());


                }




                component.put("drug_id", drugOrder.getDrug().getDrugId());
                component.put("dateActivated", order.getDateCreated().toString());
                component.put("dateStopped", order.getDateStopped().toString());
                component.put("order_group_id", order.getOrderGroup().getOrderGroupId());




                    if (order.getOrderGroup().getOrderGroupId() == previousOrderGroupId) {
                    components.add(component);
                    continue;
                } else {
                    orderObj = new JSONObject();
                    components = new JSONArray();
                    components.add(component);
                    OrderSet orderSet = order.getOrderGroup().getOrderSet();
                    orderObj.put("name", orderSet != null ? orderSet.getName() : "");
                    orderObj.put("date", order.getDateCreated().toString());
                    orderObj.put("dateStopped", order.getDateStopped().toString());
                    orderObj.put("orderSetId",orderSet != null ? orderSet.getOrderSetId() : "");
                    orderObj.put("instructions", order.getInstructions());
                    orderObj.put("components", components);
                    orderGroupArray.add(orderObj);
                     previousOrderGroupId=order.getOrderGroup().getOrderGroupId();
                }
            }
            }
            else {
                if(order.getDateStopped() != null) {
                    DrugOrder drugOrder=(DrugOrder)order.cloneForRevision();
                    orderObj = new JSONObject();
                    orderObj.put("uuid", order.getUuid());
                    orderObj.put("concept", order.getConcept());
                    orderObj.put("dateActivated", order.getDateCreated().toString());
                    orderObj.put("dateStopped", order.getDateStopped().toString());

                    if((drugOrder.getDose()!= null || drugOrder.getDuration() != null || drugOrder.getQuantity() != null || drugOrder.getDoseUnits() != null
                    || drugOrder.getFrequency() != null || drugOrder.getQuantityUnits() != null || drugOrder.getRoute()!= null
                    ) && drugOrder.getDrug() != null ) {
                        orderObj.put("dose", drugOrder.getDose());
                        orderObj.put("drugDuration", drugOrder.getDuration());
                        orderObj.put("quantity", drugOrder.getQuantity());
                        orderObj.put("doseUnits", drugOrder.getDoseUnits().getName(LOCALE).getName());
                        orderObj.put("frequency", drugOrder.getFrequency().toString());
                        orderObj.put("drug", drugOrder.getDrug().getFullName(LOCALE));
                        orderObj.put("quantityUnits", drugOrder.getQuantityUnits().getName(LOCALE).getName());
                        orderObj.put("route", drugOrder.getRoute().getName(LOCALE).getName());

                    }
                    orderArray.add(orderObj);
                }

            }
        }
        JSONObject pastDrugOrders=new JSONObject();
        pastDrugOrders.put("pastOrder_groups",orderGroupArray);
        pastDrugOrders.put("pastSingle_drugs",orderArray);
        model.put("pastDrugOrdersPayload",pastDrugOrders.toString() );
    }


    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
    public  String name = null;
    private String mapConceptNamesToShortNames(String conceptUuid) {

        if(conceptUuid.equalsIgnoreCase("84797AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "TDF";
        }
       else if(conceptUuid.equalsIgnoreCase("84309AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "D4T";
        }
       else if(conceptUuid.equalsIgnoreCase("86663AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "AZT";
        }
      else   if(conceptUuid.equalsIgnoreCase("78643AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "3TC";
        }
      else   if(conceptUuid.equalsIgnoreCase("70057AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "ABC";
        }
      else   if(conceptUuid.equalsIgnoreCase("75628AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "FTC";
        }
      else   if(conceptUuid.equalsIgnoreCase("74807AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "DDI";
        }
      else   if(conceptUuid.equalsIgnoreCase("80586AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "NVP";
        }
      else   if(conceptUuid.equalsIgnoreCase("75523AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "EFV";
        }
       else if(conceptUuid.equalsIgnoreCase("794AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "LPV";
        }
       else if(conceptUuid.equalsIgnoreCase("83412AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "RTV";
        }
       else if(conceptUuid.equalsIgnoreCase("71648AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "ATV";
        }
       else if(conceptUuid.equalsIgnoreCase("159810AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "ETR";
        }
       else if(conceptUuid.equalsIgnoreCase("154378AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "RAL";
        }
       else if(conceptUuid.equalsIgnoreCase("74258AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) {
            name = "DRV";
        }
       else if(conceptUuid.equalsIgnoreCase("d1fd0e18-e0b9-46ae-ac0e-0452a927a94b")) {
            name = "DTG";
        }
        return name;

    }
}
