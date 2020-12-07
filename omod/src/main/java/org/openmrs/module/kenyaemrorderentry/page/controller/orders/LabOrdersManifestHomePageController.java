package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.List;

@AppPage("kenyaemr.labmanifest")
public class LabOrdersManifestHomePageController {

    public void get(@SpringBean KenyaUiUtils kenyaUi,
                    UiUtils ui, PageModel model) {
        List<LabManifest> allManifests = Context.getService(KenyaemrOrdersService.class).getLabOrderManifest();
        model.put("allManifest", allManifests);
    }

}