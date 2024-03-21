package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import java.util.Collection;

import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.metadata.KenyaemrorderentryAdminSecurityMetadata;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

@AppPage("kenyaemr.labmanifest")
public class SettingsPageController {

    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
    SchedulerService schedulerService = Context.getSchedulerService();

    public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {

        // 16 Settings to get LabManifest working

        // SYSTEM TYPE
        GlobalProperty gpLabSystemInUse = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LAB_SYSTEM_IN_USE);

        model.put("gpSystemType", ModuleConstants.GP_LAB_SYSTEM_IN_USE);

        // CHAI SYSTEM
        GlobalProperty gpChaiEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpChaiEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpChaiEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
        GlobalProperty gpChaiVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpChaiVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpChaiVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
        GlobalProperty gpChaiFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpChaiFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_RESULT_URL);
        GlobalProperty gpChaiFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_CHAI_FLU_LAB_SERVER_API_TOKEN);
        
        // LABWARE SYSTEM
        GlobalProperty gpLabwareEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpLabwareEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpLabwareEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_EID_LAB_SERVER_API_TOKEN);
        GlobalProperty gpLabwareVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpLabwareVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpLabwareVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_VL_LAB_SERVER_API_TOKEN);
        GlobalProperty gpLabwareFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpLabwareFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_RESULT_URL);
        GlobalProperty gpLabwareFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_API_TOKEN);
        
        // EDARP SYSTEM
        GlobalProperty gpEdarpEIDServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_EID_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEdarpEIDServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_EID_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEdarpEIDApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_EID_LAB_SERVER_API_TOKEN);
        GlobalProperty gpEdarpVLServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_VL_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEdarpVLServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_VL_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEdarpVLApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_VL_LAB_SERVER_API_TOKEN);
        GlobalProperty gpEdarpFLUServerPushUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_FLU_LAB_SERVER_REQUEST_URL);
        GlobalProperty gpEdarpFLUServerPullUrl = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_FLU_LAB_SERVER_RESULT_URL);
        GlobalProperty gpEdarpFLUApiToken = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_EDARP_FLU_LAB_SERVER_API_TOKEN);
        
        GlobalProperty gpSslVerification = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

        model.put("SSLVerification", gpSslVerification != null ? gpSslVerification.getPropertyValue() : "");
        model.put("gpSSLVerification", ModuleConstants.GP_SSL_VERIFICATION_ENABLED);

        if (gpLabSystemInUse != null) {
            String systemType = gpLabSystemInUse.getPropertyValue();
            model.put("SystemType", systemType);
            
            if (systemType.trim().equalsIgnoreCase("CHAI")) {
                model.put("EIDPushURL", (gpChaiEIDServerPushUrl != null && gpChaiEIDServerPushUrl.getPropertyValue() != null) ? gpChaiEIDServerPushUrl.getPropertyValue() : "");
                model.put("EIDPullURL", (gpChaiEIDServerPullUrl != null && gpChaiEIDServerPullUrl.getPropertyValue() != null) ? gpChaiEIDServerPullUrl.getPropertyValue() : "");
                model.put("EIDToken", (gpChaiEIDApiToken != null && gpChaiEIDApiToken.getPropertyValue() != null) ? gpChaiEIDApiToken.getPropertyValue() : "");
                model.put("VLPushURL", (gpChaiVLServerPushUrl != null && gpChaiVLServerPushUrl.getPropertyValue() != null) ? gpChaiVLServerPushUrl.getPropertyValue() : "");
                model.put("VLPullURL", (gpChaiVLServerPullUrl != null && gpChaiVLServerPullUrl.getPropertyValue() != null) ? gpChaiVLServerPullUrl.getPropertyValue() : "");
                model.put("VLToken", (gpChaiVLApiToken != null && gpChaiVLApiToken.getPropertyValue() != null) ? gpChaiVLApiToken.getPropertyValue() : "");
                model.put("FLUPushURL", (gpChaiFLUServerPushUrl != null && gpChaiFLUServerPushUrl.getPropertyValue() != null) ? gpChaiFLUServerPushUrl.getPropertyValue() : "");
                model.put("FLUPullURL", (gpChaiFLUServerPullUrl != null && gpChaiFLUServerPullUrl.getPropertyValue() != null) ? gpChaiFLUServerPullUrl.getPropertyValue() : "");
                model.put("FLUToken", (gpChaiFLUApiToken != null && gpChaiFLUApiToken.getPropertyValue() != null) ? gpChaiFLUApiToken.getPropertyValue() : "");

                model.put("gpEIDPushURL", ModuleConstants.GP_CHAI_EID_LAB_SERVER_REQUEST_URL);
                model.put("gpEIDPullURL", ModuleConstants.GP_CHAI_EID_LAB_SERVER_RESULT_URL);
                model.put("gpEIDToken", ModuleConstants.GP_CHAI_EID_LAB_SERVER_API_TOKEN);
                model.put("gpVLPushURL", ModuleConstants.GP_CHAI_VL_LAB_SERVER_REQUEST_URL);
                model.put("gpVLPullURL", ModuleConstants.GP_CHAI_VL_LAB_SERVER_RESULT_URL);
                model.put("gpVLToken", ModuleConstants.GP_CHAI_VL_LAB_SERVER_API_TOKEN);
                model.put("gpFLUPushURL", ModuleConstants.GP_CHAI_FLU_LAB_SERVER_REQUEST_URL);
                model.put("gpFLUPullURL", ModuleConstants.GP_CHAI_FLU_LAB_SERVER_RESULT_URL);
                model.put("gpFLUToken", ModuleConstants.GP_CHAI_FLU_LAB_SERVER_API_TOKEN);
            } else if (systemType.trim().equalsIgnoreCase("LABWARE")) {
                model.put("EIDPushURL", (gpLabwareEIDServerPushUrl != null && gpLabwareEIDServerPushUrl.getPropertyValue() != null) ? gpLabwareEIDServerPushUrl.getPropertyValue() : "");
                model.put("EIDPullURL", (gpLabwareEIDServerPullUrl != null && gpLabwareEIDServerPullUrl.getPropertyValue() != null) ? gpLabwareEIDServerPullUrl.getPropertyValue() : "");
                model.put("EIDToken", (gpLabwareEIDApiToken != null && gpLabwareEIDApiToken.getPropertyValue() != null) ? gpLabwareEIDApiToken.getPropertyValue() : "");
                model.put("VLPushURL", (gpLabwareVLServerPushUrl != null && gpLabwareVLServerPushUrl.getPropertyValue() != null) ? gpLabwareVLServerPushUrl.getPropertyValue() : "");
                model.put("VLPullURL", (gpLabwareVLServerPullUrl != null && gpLabwareVLServerPullUrl.getPropertyValue() != null) ? gpLabwareVLServerPullUrl.getPropertyValue() : "");
                model.put("VLToken", (gpLabwareVLApiToken != null && gpLabwareVLApiToken.getPropertyValue() != null) ? gpLabwareVLApiToken.getPropertyValue() : "");
                model.put("FLUPushURL", (gpLabwareFLUServerPushUrl != null && gpLabwareFLUServerPushUrl.getPropertyValue() != null) ? gpLabwareFLUServerPushUrl.getPropertyValue() : "");
                model.put("FLUPullURL", (gpLabwareFLUServerPullUrl != null && gpLabwareFLUServerPullUrl.getPropertyValue() != null) ? gpLabwareFLUServerPullUrl.getPropertyValue() : "");
                model.put("FLUToken", (gpLabwareFLUApiToken != null && gpLabwareFLUApiToken.getPropertyValue() != null) ? gpLabwareFLUApiToken.getPropertyValue() : "");

                model.put("gpEIDPushURL", ModuleConstants.GP_LABWARE_EID_LAB_SERVER_REQUEST_URL);
                model.put("gpEIDPullURL", ModuleConstants.GP_LABWARE_EID_LAB_SERVER_RESULT_URL);
                model.put("gpEIDToken", ModuleConstants.GP_LABWARE_EID_LAB_SERVER_API_TOKEN);
                model.put("gpVLPushURL", ModuleConstants.GP_LABWARE_VL_LAB_SERVER_REQUEST_URL);
                model.put("gpVLPullURL", ModuleConstants.GP_LABWARE_VL_LAB_SERVER_RESULT_URL);
                model.put("gpVLToken", ModuleConstants.GP_LABWARE_VL_LAB_SERVER_API_TOKEN);
                model.put("gpFLUPushURL", ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_REQUEST_URL);
                model.put("gpFLUPullURL", ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_RESULT_URL);
                model.put("gpFLUToken", ModuleConstants.GP_LABWARE_FLU_LAB_SERVER_API_TOKEN);
            } else if (systemType.trim().equalsIgnoreCase("EDARP")) {
                model.put("EIDPushURL", (gpEdarpEIDServerPushUrl != null && gpEdarpEIDServerPushUrl.getPropertyValue() != null) ? gpEdarpEIDServerPushUrl.getPropertyValue() : "");
                model.put("EIDPullURL", (gpEdarpEIDServerPullUrl != null && gpEdarpEIDServerPullUrl.getPropertyValue() != null) ? gpEdarpEIDServerPullUrl.getPropertyValue() : "");
                model.put("EIDToken", (gpEdarpEIDApiToken != null && gpEdarpEIDApiToken.getPropertyValue() != null) ? gpEdarpEIDApiToken.getPropertyValue() : "");
                model.put("VLPushURL", (gpEdarpVLServerPushUrl != null && gpEdarpVLServerPushUrl.getPropertyValue() != null) ? gpEdarpVLServerPushUrl.getPropertyValue() : "");
                model.put("VLPullURL", (gpEdarpVLServerPullUrl != null && gpEdarpVLServerPullUrl.getPropertyValue() != null) ? gpEdarpVLServerPullUrl.getPropertyValue() : "");
                model.put("VLToken", (gpEdarpVLApiToken != null && gpEdarpVLApiToken.getPropertyValue() != null) ? gpEdarpVLApiToken.getPropertyValue() : "");
                model.put("FLUPushURL", (gpEdarpFLUServerPushUrl != null && gpEdarpFLUServerPushUrl.getPropertyValue() != null) ? gpEdarpFLUServerPushUrl.getPropertyValue() : "");
                model.put("FLUPullURL", (gpEdarpFLUServerPullUrl != null && gpEdarpFLUServerPullUrl.getPropertyValue() != null) ? gpEdarpFLUServerPullUrl.getPropertyValue() : "");
                model.put("FLUToken", (gpEdarpFLUApiToken != null && gpEdarpFLUApiToken.getPropertyValue() != null) ? gpEdarpFLUApiToken.getPropertyValue() : "");

                model.put("gpEIDPushURL", ModuleConstants.GP_EDARP_EID_LAB_SERVER_REQUEST_URL);
                model.put("gpEIDPullURL", ModuleConstants.GP_EDARP_EID_LAB_SERVER_RESULT_URL);
                model.put("gpEIDToken", ModuleConstants.GP_EDARP_EID_LAB_SERVER_API_TOKEN);
                model.put("gpVLPushURL", ModuleConstants.GP_EDARP_VL_LAB_SERVER_REQUEST_URL);
                model.put("gpVLPullURL", ModuleConstants.GP_EDARP_VL_LAB_SERVER_RESULT_URL);
                model.put("gpVLToken", ModuleConstants.GP_EDARP_VL_LAB_SERVER_API_TOKEN);
                model.put("gpFLUPushURL", ModuleConstants.GP_EDARP_FLU_LAB_SERVER_REQUEST_URL);
                model.put("gpFLUPullURL", ModuleConstants.GP_EDARP_FLU_LAB_SERVER_RESULT_URL);
                model.put("gpFLUToken", ModuleConstants.GP_EDARP_FLU_LAB_SERVER_API_TOKEN);
            } else {
                model.put("SystemType", "");
                model.put("EIDPushURL", "");
                model.put("EIDPullURL", "");
                model.put("EIDToken", "");
                model.put("VLPushURL", "");
                model.put("VLPullURL", "");
                model.put("VLToken", "");
                model.put("FLUPushURL", "");
                model.put("FLUPullURL", "");
                model.put("FLUToken", "");

                model.put("gpEIDPushURL", "");
                model.put("gpEIDPullURL", "");
                model.put("gpEIDToken", "");
                model.put("gpVLPushURL", "");
                model.put("gpVLPullURL", "");
                model.put("gpVLToken", "");
                model.put("gpFLUPushURL", "");
                model.put("gpFLUPullURL", "");
                model.put("gpFLUToken", "");
            }
        } else {
            model.put("SystemType", "");
            model.put("EIDPushURL", "");
            model.put("EIDPullURL", "");
            model.put("EIDToken", "");
            model.put("VLPushURL", "");
            model.put("VLPullURL", "");
            model.put("VLToken", "");
            model.put("FLUPushURL", "");
            model.put("FLUPullURL", "");
            model.put("FLUToken", "");

            model.put("gpEIDPushURL", "");
            model.put("gpEIDPullURL", "");
            model.put("gpEIDToken", "");
            model.put("gpVLPushURL", "");
            model.put("gpVLPullURL", "");
            model.put("gpVLToken", "");
            model.put("gpFLUPushURL", "");
            model.put("gpFLUPullURL", "");
            model.put("gpFLUToken", "");
        }

        GlobalProperty gpEnableEIDFunction = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.ENABLE_EID_FUNCTION);

        model.put("EnableEIDFunction", gpEnableEIDFunction != null ? gpEnableEIDFunction.getPropertyValue() : "");
        model.put("gpEnableEIDFunction", ModuleConstants.ENABLE_EID_FUNCTION);

        GlobalProperty gpEnableFLUFunction = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.ENABLE_FLU_FUNCTION);

        model.put("EnableFLUFunction", gpEnableFLUFunction != null ? gpEnableFLUFunction.getPropertyValue() : "");
        model.put("gpEnableFLUFunction", ModuleConstants.ENABLE_FLU_FUNCTION);

        GlobalProperty gpLocalResultEndpoint = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LOCAL_RESULT_ENDPOINT);

        model.put("LocalResultEndpoint", gpLocalResultEndpoint != null ? gpLocalResultEndpoint.getPropertyValue() : "");
        model.put("gpLocalResultEndpoint", ModuleConstants.GP_LOCAL_RESULT_ENDPOINT);

        GlobalProperty gpSchedulerUsername = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SCHEDULER_USERNAME);

        model.put("SchedulerUsername", gpSchedulerUsername != null ? gpSchedulerUsername.getPropertyValue() : "");
        model.put("gpSchedulerUsername", ModuleConstants.GP_SCHEDULER_USERNAME);

        GlobalProperty gpSchedulerPassword = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SCHEDULER_PASSWORD);

        model.put("SchedulerPassword", gpSchedulerPassword != null ? gpSchedulerPassword.getPropertyValue() : "");
        model.put("gpSchedulerPassword", ModuleConstants.GP_SCHEDULER_PASSWORD);

        GlobalProperty gpLabTATForVLResults = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LAB_TAT_FOR_VL_RESULTS);

        model.put("LabTATForVLResults", gpLabTATForVLResults != null ? gpLabTATForVLResults.getPropertyValue() : "");
        model.put("gpLabTATForVLResults", ModuleConstants.GP_LAB_TAT_FOR_VL_RESULTS);

        GlobalProperty gpRetryPeriodForIncompleteResults = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS);

        model.put("RetryPeriodForIncompleteResults", gpRetryPeriodForIncompleteResults != null ? gpRetryPeriodForIncompleteResults.getPropertyValue() : "");
        model.put("gpRetryPeriodForIncompleteResults", ModuleConstants.GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS);

        TaskDefinition pullTaskDefinition = getTaskByUuid(ModuleConstants.PULL_SCHEDULER_UUID);
        TaskDefinition pushTaskDefinition = getTaskByUuid(ModuleConstants.PUSH_SCHEDULER_UUID);

        if(pullTaskDefinition != null) {
            model.put("PullTaskClass", pullTaskDefinition.getTaskClass());
            model.put("PullTaskStarted", pullTaskDefinition.getStarted());
            model.put("PullTaskStartOnStartup", pullTaskDefinition.getStartOnStartup());
            model.put("PullTaskInterval", Math.abs(pullTaskDefinition.getRepeatInterval() / 60));
            model.put("PullTaskIntervalNum", pullTaskDefinition.getRepeatInterval());
        } else {
            model.put("PullTaskClass", "");
            model.put("PullTaskStarted", "");
            model.put("PullTaskStartOnStartup", "");
            model.put("PullTaskInterval", "");
            model.put("PullTaskIntervalNum", 0);
        }

        if(pushTaskDefinition != null) {
            model.put("PushTaskClass", pushTaskDefinition.getTaskClass());
            model.put("PushTaskStarted", pushTaskDefinition.getStarted());
            model.put("PushTaskStartOnStartup", pushTaskDefinition.getStartOnStartup());
            model.put("PushTaskInterval", Math.abs(pushTaskDefinition.getRepeatInterval() / 60));
            model.put("PushTaskIntervalNum", pushTaskDefinition.getRepeatInterval());
        } else {
            model.put("PushTaskClass", "");
            model.put("PushTaskStarted", "");
            model.put("PushTaskStartOnStartup", "");
            model.put("PushTaskInterval", "");
            model.put("PushTaskIntervalNum", 0);
        }

        model.put("userHasSettingsEditRole", (Context.getAuthenticatedUser().containsRole(KenyaemrorderentryAdminSecurityMetadata._Role.API_ROLE_EDIT_SETTINGS) || Context.getAuthenticatedUser().isSuperUser()));
    }

    /**
     * Get a task given its uuid
     * 
     * @param uuid - the tasks uuid
     * @return taskdefinition
     */
    public TaskDefinition getTaskByUuid(String uuid) {
        Collection<TaskDefinition> tasks = schedulerService.getRegisteredTasks();
        for(TaskDefinition task : tasks) {
            if(task.getUuid().equalsIgnoreCase(uuid)) {
                return(task);
            }
        }
        return(null);
    }

}