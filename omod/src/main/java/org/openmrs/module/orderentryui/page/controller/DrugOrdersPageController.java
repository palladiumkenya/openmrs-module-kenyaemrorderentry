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

        JSONObject drugOrderDispensationPayload=new JSONObject();
        JSONArray programs=new JSONArray();

        //start of first program
        JSONObject program=new JSONObject();
        JSONArray regimenLines=new JSONArray();
        String[] regimenNames={"TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)",
                "TDF + 3TC + EFV (300mg OD/150mg BD/200mg BD"};
        JSONObject regimenLine=(JSONObject)createRegimenLine(regimenNames,"Adult first line");
        regimenLines.add(regimenLine);
        String[] regimenNames2={"2nd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)"};
        regimenLine=(JSONObject)createRegimenLine(regimenNames2,"Adult second line");
        regimenLines.add(regimenLine);
        String[] regimenNames3={"3rd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)"};
        regimenLine=(JSONObject)createRegimenLine(regimenNames3,"Adult third line");
        regimenLines.add(regimenLine);
        program.put("name", "HIV");
        program.put("regimen_lines", regimenLines);
        programs.add(program);
        //end of first program

        //start of second program
        program=new JSONObject();
        regimenLines=new JSONArray();
        String[] regimenNames5={"Test for TB"};
        regimenLine=(JSONObject)createRegimenLine(regimenNames5,"Adult first line");
        regimenLines.add(regimenLine);
        program.put("name", "TB");
        program.put("regimen_lines", regimenLines);
        programs.add(program);
        //end of second program
        drugOrderDispensationPayload.put("programs",programs);
        model.put("drugOrderDispensationPayload",drugOrderDispensationPayload.toString());
        model.put("dispensePayload",payload());
    }

    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }
    private Object createRegimenLine(String[] regimenNames,String regimenLineName){
        JSONObject regimenLine=new JSONObject();
        JSONArray regimens=new JSONArray();
        JSONObject regimen=new JSONObject();
        for(int i=0; i<regimenNames.length;i++){
            regimen.put("name",regimenNames[i].toString());
            regimens.add(regimen.toJSONString());
        }
        regimenLine.put("name",regimenLineName);
        regimenLine.put("regimens", regimens);
        return regimenLine;
    }
    private String payload(){
        String payload="{\n" +
                "                                    \"programs\": [\n" +
                "                                      {\n" +
                "                                        \"name\": \"HIV\",\n" +
                "                                        \"regimen_lines\": [\n" +
                "                                          {\n" +
                "                                            \"name\": \"Adult first line\",\n" +
                "                                            \"regimens\": [\n" +
                "                                              {\n" +
                "                                                \"name\": \"TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)\"\n" +
                "                                              },\n" +
                "                                              {\n" +
                "                                                \"name\": \"TDF + 3TC + EFV (300mg OD/150mg BD/200mg BD\"\n" +
                "                                              }\n" +
                "                                            ]\n" +
                "                                          },\n" +
                "                                          {\n" +
                "                                            \"name\": \"Adult second line\",\n" +
                "                                            \"regimens\": [\n" +
                "                                              {\n" +
                "                                                \"name\": \"2nd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)\"\n" +
                "                                              }\n" +
                "                                            ]\n" +
                "                                          },\n" +
                "                                          {\n" +
                "                                            \"name\": \"Adult third line\",\n" +
                "                                            \"regimens\": [\n" +
                "                                              {\n" +
                "                                                \"name\": \"3rd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)\"\n" +
                "                                              }\n" +
                "                                            ]\n" +
                "                                          },\n" +
                "                                          {\n" +
                "                                            \"name\": \"Peds first line\",\n" +
                "                                            \"regimens\": [\n" +
                "                                              {\n" +
                "                                                \"name\": \"3rd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)\"\n" +
                "                                              }\n" +
                "                                            ]\n" +
                "                                          },\n" +
                "                                          {\n" +
                "                                            \"name\": \"Peds second line\",\n" +
                "                                            \"regimens\": [\n" +
                "                                              {\n" +
                "                                                \"name\": \"3rd + 3TC + NVPP (300mg OD/150mg BD/200mg BD)\"\n" +
                "                                              }\n" +
                "                                            ]\n" +
                "                                          }\n" +
                "                                        ]\n" +
                "                                      },\n" +
                "                                      {\n" +
                "                                        \"name\": \"TB\",\n" +
                "                                        \"regimen_lines\": [\n" +
                "                                          {\n" +
                "                                            \"name\": \"Adult first line\",\n" +
                "                                            \"regimens\": []\n" +
                "                                          }\n" +
                "                                        ]\n" +
                "                                      }\n" +
                "                                    ]\n" +
                "                                  }";
        return payload;
    }

}