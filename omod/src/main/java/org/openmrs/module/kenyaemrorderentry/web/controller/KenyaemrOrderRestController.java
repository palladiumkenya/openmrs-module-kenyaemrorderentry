package org.openmrs.module.kenyaemrorderentry.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The main controller that exposes additional end points for order entry
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrorder")
public class KenyaemrOrderRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * // end point for processing individual lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/labresults
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/labresults") 
    @ResponseBody
    public Object processIncomingViralLoadResults(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return "Error extracting request body" + msg;
        }

        if (requestBody != null) {
            LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
            return labOrderDataExchange.processIncomingViralLoadLabResults(requestBody);
        }
        return  "The request could not be interpreted by the internal vl result end point";
    }

    /**
     * // end point for processing individual FLU lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/flulabresults
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/flulabresults") 
    @ResponseBody
    public Object processIncomingFLUResults(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return "Error extracting request body" + msg;
        }

        if (requestBody != null) {
            LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
            return labOrderDataExchange.processIncomingFLULabResults(requestBody);
        }
        return  "The request could not be interpreted by the internal flu result end point";
    }

    /**
     * Gets a list of valid orders for a given manifest (manifest type)
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/validorders") // gets all visit forms for a patient
    @ResponseBody
    public Object getValidOrdersForManifest(HttpServletRequest request, @RequestParam("manifestUuid") String manifestUuid) {
        Set<SimpleObject> activeVlOrdersNotInManifest = new HashSet<SimpleObject>();
        Set<SimpleObject> activeEidOrdersNotInManifest = new HashSet<SimpleObject>();
        Set<SimpleObject> activeFluOrdersNotInManifest = new HashSet<SimpleObject>();

        LabManifest labManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestByUUID(manifestUuid);

        // Is DOD
        AdministrationService administrationService = Context.getAdministrationService();
        final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

        List<LabManifestOrder> allOrdersForManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByManifest(labManifest);
        PatientIdentifierType pat = Utils.getUniquePatientNumberIdentifierType();
        PatientIdentifierType kat = Utils.getKDODIdentifierType();
        PatientIdentifierType hei = Utils.getHeiNumberIdentifierType();
        LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
        Integer manifestTypeCode = labManifest.getManifestType();
        String manifestType = "";
        if (manifestTypeCode == LabManifest.EID_TYPE) {
            manifestType = "EID";
            activeEidOrdersNotInManifest = labOrderDataExchange.getActiveEidOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        } else if (manifestTypeCode == LabManifest.VL_TYPE) {
            manifestType = "Viral load";
            activeVlOrdersNotInManifest = labOrderDataExchange.getActiveViralLoadOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        } else if (manifestTypeCode == LabManifest.FLU_TYPE) {
            manifestType = "FLU";
            activeFluOrdersNotInManifest = labOrderDataExchange.getActiveFluOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        }

        //Temporary fix to remove special chars from lab results
        List<LabManifestOrder> ordersForManifest = new ArrayList<LabManifestOrder>();
        for(LabManifestOrder m : allOrdersForManifest) {
            if(m != null) {
                try {
                    String result = m.getResult();
                    if(result != null) {
                        result = result.replaceAll("[^a-zA-Z0-9]"," ");
                        result = result.trim();
                        m.setResult(result);
                    }
                } catch(Exception ex) {}
                ordersForManifest.add(m);
            }
        }

        // For javascript processing

        // VL orders
        List<SimpleObject> VLOrders = new ArrayList<SimpleObject>();
        for(SimpleObject load : activeVlOrdersNotInManifest){
            SimpleObject so = new SimpleObject();
            Order order = (Order) load.get("order");
            so.put("orderId", order.getId());
            VLOrders.add(so);
        }

        // EID orders
        List<SimpleObject> EIDOrders = new ArrayList<SimpleObject>();
        for(SimpleObject load : activeEidOrdersNotInManifest){
            SimpleObject so = new SimpleObject();
            Order order = (Order) load.get("order");
            so.put("orderId", order.getId());
            EIDOrders.add(so);
        }

        // FLU orders
        List<SimpleObject> FLUOrders = new ArrayList<SimpleObject>();
        for(SimpleObject load : activeFluOrdersNotInManifest){
            SimpleObject so = new SimpleObject();
            Order order = (Order) load.get("order");
            so.put("orderId", order.getId());
            FLUOrders.add(so);
        }

        // Manifest orders
        List<SimpleObject> manifestOrders = new ArrayList<SimpleObject>();
        for(LabManifestOrder order : ordersForManifest){
            SimpleObject so = new SimpleObject();
            so.put("orderId", order.getId());
            manifestOrders.add(so);
        }

        SimpleObject model = new SimpleObject();
        model.put("eligibleVlOrders", activeVlOrdersNotInManifest );
        model.put("eligibleEidOrders", activeEidOrdersNotInManifest );
        model.put("eligibleFLUOrders", activeFluOrdersNotInManifest );
        model.put("VLOrders", VLOrders ); // to json?
        model.put("EIDOrders", EIDOrders ); // to json?
        model.put("FLUOrders", FLUOrders ); // to json?
        model.put("manifestType", manifestType);
        model.put("manifest", labManifest);
        model.put("manifestOrders", ordersForManifest);
        model.put("allManifestOrders", manifestOrders); // to json?

        model.put("cccNumberType", "");
        model.put("heiNumberType", "");
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            model.put("cccNumberType", kat.getPatientIdentifierTypeId());
            model.put("heiNumberType", kat.getPatientIdentifierTypeId());
        } else {
            model.put("cccNumberType", pat.getPatientIdentifierTypeId());
            model.put("heiNumberType", hei.getPatientIdentifierTypeId());
        }

        return model;
    }

    /**
     * @see BaseRestController#getNamespace()
     */

    @Override
    public String getNamespace() {
        return "v1/kenyaemrorderentry";
    }
}
