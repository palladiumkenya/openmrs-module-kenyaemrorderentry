package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import org.openmrs.Order;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AppPage("kenyaemr.labmanifest")
public class ManifestOrdersHomePageController {

    public void get(@RequestParam(value = "manifest") LabManifest manifest, @SpringBean KenyaUiUtils kenyaUi,
                    UiUtils ui, PageModel model) {

        // Is DOD
        AdministrationService administrationService = Context.getAdministrationService();
        final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

        List<LabManifestOrder> allOrdersForManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByManifest(manifest);
        PatientIdentifierType pat = Utils.getUniquePatientNumberIdentifierType();
        PatientIdentifierType kat = Utils.getKDODIdentifierType();
        PatientIdentifierType hei = Utils.getHeiNumberIdentifierType();
        LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
        Integer manifestTypeCode = manifest.getManifestType();
        String manifestType = "";
        if (manifestTypeCode == LabManifest.EID_TYPE) {
            manifestType = "EID";
        } else if (manifestTypeCode == LabManifest.VL_TYPE) {
            manifestType = "Viral load";
        } else if (manifestTypeCode == LabManifest.FLU_TYPE) {
            manifestType = "FLU";
        }

        Set<SimpleObject> activeVlOrdersNotInManifest = new HashSet<SimpleObject>();
        Set<SimpleObject> activeEidOrdersNotInManifest = new HashSet<SimpleObject>();
        Set<SimpleObject> activeFluOrdersNotInManifest = new HashSet<SimpleObject>();

        if(manifestTypeCode == LabManifest.VL_TYPE) {
            activeVlOrdersNotInManifest = labOrderDataExchange.getActiveViralLoadOrdersNotInManifest(null, manifest.getStartDate(), manifest.getEndDate());
        } else if(manifestTypeCode == LabManifest.EID_TYPE) {
            activeEidOrdersNotInManifest = labOrderDataExchange.getActiveEidOrdersNotInManifest(null, manifest.getStartDate(), manifest.getEndDate());
        } else if(manifestTypeCode == LabManifest.FLU_TYPE) {
            activeFluOrdersNotInManifest = labOrderDataExchange.getActiveFluOrdersNotInManifest(null, manifest.getStartDate(), manifest.getEndDate());
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


        model.put("eligibleVlOrders", activeVlOrdersNotInManifest );
        model.put("eligibleEidOrders", activeEidOrdersNotInManifest );
        model.put("VLOrders", ui.toJson(VLOrders) );
        model.put("EIDOrders", ui.toJson(EIDOrders) );
        model.put("FLUOrders", ui.toJson(FLUOrders) );
        model.put("manifestType", manifestType);
        model.put("manifest", manifest);
        model.put("manifestOrders", ordersForManifest);
        model.put("allManifestOrders", ui.toJson(manifestOrders));

        model.put("cccNumberType", "");
        model.put("heiNumberType", "");
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            model.put("cccNumberType", kat.getPatientIdentifierTypeId());
            model.put("heiNumberType", kat.getPatientIdentifierTypeId());
        } else {
            model.put("cccNumberType", pat.getPatientIdentifierTypeId());
            model.put("heiNumberType", hei.getPatientIdentifierTypeId());
        }
    }

}
