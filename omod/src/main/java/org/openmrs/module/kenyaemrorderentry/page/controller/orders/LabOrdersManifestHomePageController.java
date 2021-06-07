package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.ArrayList;
import java.util.List;

@AppPage("kenyaemr.labmanifest")
public class LabOrdersManifestHomePageController {

    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

    public void get(@SpringBean KenyaUiUtils kenyaUi,
                    UiUtils ui, PageModel model) {
        List<LabManifest> allManifests = Context.getService(KenyaemrOrdersService.class).getLabOrderManifest();
        List<SimpleObject> manifestList1 = new ArrayList<SimpleObject>();
        for (LabManifest manifest : allManifests) {

            List<LabManifestOrder> ordersWithIncompleteResult = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, "Incomplete");
            List<LabManifestOrder> collectNewSampleOrders = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, "Collect New Sample");
            List<LabManifestOrder> manualDiscontinuationOrders = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, "Requires manual update in the lab module");
            List<LabManifestOrder> ordersWithMissingPhysicalSamples = kenyaemrOrdersService.getLabManifestOrderByManifestAndStatus(manifest, "Missing Sample ( Physical Sample Missing)");

            SimpleObject m = SimpleObject.create(
                    "id", manifest.getId(),
                    "startDate", ui.formatDatePretty(manifest.getStartDate()),
                    "endDate", ui.formatDatePretty(manifest.getEndDate()),
                    "dispatchDate", ui.formatDatePretty(manifest.getDispatchDate()),
                    "courier", StringUtils.capitalize(manifest.getCourier() != null ? manifest.getCourier().toLowerCase() : ""),
                    "courierOfficer", StringUtils.capitalize(manifest.getCourierOfficer() != null ? manifest.getCourierOfficer().toLowerCase() : ""),
                    "status", manifest.getStatus(),
                    "county", StringUtils.capitalize(manifest.getCounty() != null ? manifest.getCounty().toLowerCase() : ""),
                    "subCounty", StringUtils.capitalize(manifest.getSubCounty() != null ? manifest.getSubCounty().toLowerCase() : ""),
                    "facilityEmail", manifest.getFacilityEmail(),
                    "facilityPhoneContact", manifest.getFacilityPhoneContact(),
                    "clinicianPhoneContact", manifest.getClinicianPhoneContact(),
                    "clinicianName", StringUtils.capitalize(manifest.getClinicianName() != null ? manifest.getClinicianName().toLowerCase() : ""),
                    "labPocPhoneNumber", manifest.getLabPocPhoneNumber()

            );

            SimpleObject o1 = SimpleObject.create(
                    "manifest", m,
                    "collectNewSample", collectNewSampleOrders.size(),
                    "incompleteSample", ordersWithIncompleteResult.size(),
                    "manualUpdates", manualDiscontinuationOrders.size(),
                    "missingPhysicalSample", ordersWithMissingPhysicalSamples.size());
            manifestList1.add(o1);
        }
        model.put("manifestList", ui.toJson(manifestList1));
        model.put("manifestListSize", ui.toJson(manifestList1.size()));
    }

}