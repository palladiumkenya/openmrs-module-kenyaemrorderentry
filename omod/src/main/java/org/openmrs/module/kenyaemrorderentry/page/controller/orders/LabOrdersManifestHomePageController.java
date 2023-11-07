package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.metadata.KenyaemrorderentryAdminSecurityMetadata;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

@AppPage("kenyaemr.labmanifest")
public class LabOrdersManifestHomePageController {

    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);

    public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {

        // Drafts
        Long drafts = kenyaemrOrdersService.countTotalDraftManifests();
        model.put("manifestsDraft", ui.toJson(drafts));

        // On Hold
        Long onHold = kenyaemrOrdersService.countTotalManifestsOnHold();
        model.put("manifestsOnHold", ui.toJson(onHold));

        // Ready to send
        Long readyToSend = kenyaemrOrdersService.countTotalReadyToSendManifests();
        model.put("manifestsReadyToSend", ui.toJson(readyToSend));

        // Sending
        Long sending = kenyaemrOrdersService.countTotalManifestsOnSending();
        model.put("manifestsSending", ui.toJson(sending));

        // Submitted
        Long submitted = kenyaemrOrdersService.countTotalSubmittedManifests();
        model.put("manifestsSubmitted", ui.toJson(submitted));

        // Incomplete with Errors
        Long incompleteWithErrors = kenyaemrOrdersService.countTotalManifestsIncompleteWithErrors();
        model.put("manifestsIncompleteWithErrors", ui.toJson(incompleteWithErrors));

        // Total Errors on incomplete manifests
        Long errorsOnIncomplete = kenyaemrOrdersService.countTotalErrorsOnIncompleteManifests();
        model.put("errorsOnIncomplete", ui.toJson(errorsOnIncomplete));

        // Incomplete
        Long incomplete = kenyaemrOrdersService.countTotalIncompleteManifests();
        model.put("manifestsIncomplete", ui.toJson(incomplete));
        
        // Complete with Errors
        Long completeWithErrors = kenyaemrOrdersService.countTotalManifestsCompleteWithErrors();
        model.put("manifestsCompleteWithErrors", ui.toJson(completeWithErrors));

        // Total Errors on complete manifests
        Long errorsOnComplete = kenyaemrOrdersService.countTotalErrorsOnCompleteManifests();
        model.put("errorsOnComplete", ui.toJson(errorsOnComplete));

        // Complete
        Long complete = kenyaemrOrdersService.countTotalCompleteManifests();
        model.put("manifestsComplete", ui.toJson(complete));

        // Graph
        List<SimpleObject> summaryGraph = kenyaemrOrdersService.getLabManifestSummaryGraphSQL();
        model.put("summaryGraph", ui.toJson(summaryGraph));

        // Settings
        model.put("userHasSettingsEditRole", (Context.getAuthenticatedUser().containsRole(KenyaemrorderentryAdminSecurityMetadata._Role.API_ROLE_EDIT_SETTINGS) || Context.getAuthenticatedUser().isSuperUser()));
    }

}