package org.openmrs.module.orderentryui.fragment.controller.patientdashboard;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.*;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.PatientService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CurrentRegimenFragmentController {

    public void controller(FragmentConfiguration config,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean("orderService") OrderService orderService,
                           FragmentModel model,
                           UiUtils ui) throws Exception {
        // unfortunately in OpenMRS 2.1 the coreapps patient page only gives us a patientId for this extension point
        // (not a patient) but I assume we'll fix this to pass patient, so I'll code defensively
        config.require("patient|patientId");
        Patient patient;
        Object pt = config.getAttribute("patient");
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        }
        else {
            // in case we are passed a PatientDomainWrapper (but this module doesn't know about emrapi)
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }

        OrderType drugOrders = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        List<Order> activeDrugOrders = orderService.getActiveOrders(patient, drugOrders, null, null);

        JSONArray patientOrdersArray=new JSONArray();
        JSONObject patientOrdersResponse=new JSONObject();
        /*for(Order order:activeDrugOrders){
            DrugOrder drugOrder=(DrugOrder)order;
            JSONObject patientOrder=new JSONObject();
            patientOrder.put("groupId",(order.getOrderGroup() !=null) ? order.getOrderGroup().getId():null);
            patientOrder.put("drugId", drugOrder.getDrug().getId());
            patientOrder.put("drugName", drugOrder.getDrug().getName());
            patientOrder.put("dose", drugOrder.getFrequency());
            patientOrdersArray.add(patientOrder);
        }
        patientOrdersResponse.put("response",patientOrdersArray);*/
        model.put("patientOrdersResponse", patientOrdersResponse.toString());

    }

}
