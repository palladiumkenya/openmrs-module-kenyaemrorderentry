package org.openmrs.module.kenyaemrorderentry.fragment.controller.manifest;

import java.util.Collection;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.metadata.KenyaemrorderentryAdminSecurityMetadata;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class SettingsFragmentController {

    SchedulerService schedulerService = Context.getSchedulerService();

    public void controller(@FragmentParam(value = "manifestId", required = false) LabManifest labManifest,
                           @RequestParam(value = "returnUrl") String returnUrl,
                           PageModel model) {
        //
    }

    /**
     * Save the settings details
     * @param payload the json object
     * @param ui UiUtils object
     * @return
     */
    public SimpleObject saveSettings(@RequestParam("payload") String payload, UiUtils ui) {
        SimpleObject ret = SimpleObject.create(
                "success", false
        );
        try {
            System.out.println("Order Entry: Saving settings : Received payload: " + payload);
            // Check if the current user has the privilege
            if (Context.getAuthenticatedUser().containsRole(KenyaemrorderentryAdminSecurityMetadata._Role.API_ROLE_EDIT_SETTINGS) || Context.getAuthenticatedUser().isSuperUser()) {
                
                // Global Properties
                // SYSTEM TYPE
                GlobalProperty gpLabSystemInUse = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LAB_SYSTEM_IN_USE);
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
                GlobalProperty gpEnableEIDFunction = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.ENABLE_EID_FUNCTION);
                GlobalProperty gpEnableFLUFunction = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.ENABLE_FLU_FUNCTION);
                GlobalProperty gpLocalResultEndpoint = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LOCAL_RESULT_ENDPOINT);
                GlobalProperty gpLocalFLUResultEndpoint = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LOCAL_FLU_RESULT_ENDPOINT);
                GlobalProperty gpSchedulerUsername = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SCHEDULER_USERNAME);
                GlobalProperty gpSchedulerPassword = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_SCHEDULER_PASSWORD);
                GlobalProperty gpLabTATForVLResults = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_LAB_TAT_FOR_VL_RESULTS);
                GlobalProperty gpRetryPeriodForIncompleteResults = Context.getAdministrationService().getGlobalPropertyObject(ModuleConstants.GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS);
                
                // User has the privilege
                System.out.println("Order Entry: Saving settings : User has privilege");
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(payload);

                // System Type
                String selSystemType = (String) responseObj.get("selSystemType");
                selSystemType = selSystemType == null ? "" : selSystemType.trim();
                System.out.println("selSystemType: " + selSystemType);
                selSystemType = selSystemType.toUpperCase(Locale.ROOT);
                gpLabSystemInUse.setPropertyValue(selSystemType);
                Context.getAdministrationService().saveGlobalProperty(gpLabSystemInUse);
                
                // EID Enabled
                String chkEIDEnabled = (String) responseObj.get("chkEIDEnabled");
                chkEIDEnabled = chkEIDEnabled == null ? "" : chkEIDEnabled.trim();
                System.out.println("chkEIDEnabled: " + chkEIDEnabled);
                if(chkEIDEnabled.equalsIgnoreCase("on")) {
                    gpEnableEIDFunction.setPropertyValue("true");
                    Context.getAdministrationService().saveGlobalProperty(gpEnableEIDFunction);
                } else if(chkEIDEnabled.equalsIgnoreCase("off")) {
                    gpEnableEIDFunction.setPropertyValue("false");
                    Context.getAdministrationService().saveGlobalProperty(gpEnableEIDFunction);
                }

                // FLU Enabled
                String chkFLUEnabled = (String) responseObj.get("chkFLUEnabled");
                chkFLUEnabled = chkFLUEnabled == null ? "" : chkFLUEnabled.trim();
                System.out.println("chkFLUEnabled: " + chkFLUEnabled);
                if(chkFLUEnabled.equalsIgnoreCase("on")) {
                    gpEnableFLUFunction.setPropertyValue("true");
                    Context.getAdministrationService().saveGlobalProperty(gpEnableFLUFunction);
                } else if(chkFLUEnabled.equalsIgnoreCase("off")) {
                    gpEnableFLUFunction.setPropertyValue("false");
                    Context.getAdministrationService().saveGlobalProperty(gpEnableFLUFunction);
                }

                // EID Token
                String txtEIDToken = (String) responseObj.get("txtEIDToken");
                txtEIDToken = txtEIDToken == null ? "" : txtEIDToken.trim();
                System.out.println("txtEIDToken: " + txtEIDToken);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiEIDApiToken.setPropertyValue(txtEIDToken);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiEIDApiToken);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareEIDApiToken.setPropertyValue(txtEIDToken);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareEIDApiToken);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpEIDApiToken.setPropertyValue(txtEIDToken);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpEIDApiToken);
                }

                // EID Pull URL
                String txtEIDPullURL = (String) responseObj.get("txtEIDPullURL");
                txtEIDPullURL = txtEIDPullURL == null ? "" : txtEIDPullURL.trim();
                System.out.println("txtEIDPullURL: " + txtEIDPullURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiEIDServerPullUrl.setPropertyValue(txtEIDPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiEIDServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareEIDServerPullUrl.setPropertyValue(txtEIDPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareEIDServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpEIDServerPullUrl.setPropertyValue(txtEIDPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpEIDServerPullUrl);
                }

                // EID Push URL
                String txtEIDPushURL = (String) responseObj.get("txtEIDPushURL");
                txtEIDPushURL = txtEIDPushURL == null ? "" : txtEIDPushURL.trim();
                System.out.println("txtEIDPushURL: " + txtEIDPushURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiEIDServerPushUrl.setPropertyValue(txtEIDPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiEIDServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareEIDServerPushUrl.setPropertyValue(txtEIDPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareEIDServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpEIDServerPushUrl.setPropertyValue(txtEIDPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpEIDServerPushUrl);
                }

                // FLU Token
                String txtFLUToken = (String) responseObj.get("txtFLUToken");
                txtFLUToken = txtFLUToken == null ? "" : txtFLUToken.trim();
                System.out.println("txtFLUToken: " + txtFLUToken);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiFLUApiToken.setPropertyValue(txtFLUToken);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiFLUApiToken);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareFLUApiToken.setPropertyValue(txtFLUToken);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareFLUApiToken);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpFLUApiToken.setPropertyValue(txtFLUToken);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpFLUApiToken);
                }

                // FLU Pull URL
                String txtFLUPullURL = (String) responseObj.get("txtFLUPullURL");
                txtFLUPullURL = txtFLUPullURL == null ? "" : txtFLUPullURL.trim();
                System.out.println("txtFLUPullURL: " + txtFLUPullURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiFLUServerPullUrl.setPropertyValue(txtFLUPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiFLUServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareFLUServerPullUrl.setPropertyValue(txtFLUPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareFLUServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpFLUServerPullUrl.setPropertyValue(txtFLUPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpFLUServerPullUrl);
                }

                // FLU Push URL
                String txtFLUPushURL = (String) responseObj.get("txtFLUPushURL");
                txtFLUPushURL = txtFLUPushURL == null ? "" : txtFLUPushURL.trim();
                System.out.println("txtFLUPushURL: " + txtFLUPushURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiFLUServerPushUrl.setPropertyValue(txtFLUPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiFLUServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareFLUServerPushUrl.setPropertyValue(txtFLUPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareFLUServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpFLUServerPushUrl.setPropertyValue(txtFLUPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpFLUServerPushUrl);
                }

                // VL Token
                String txtVLToken = (String) responseObj.get("txtVLToken");
                txtVLToken = txtVLToken == null ? "" : txtVLToken.trim();
                System.out.println("txtVLToken: " + txtVLToken);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiVLApiToken.setPropertyValue(txtVLToken);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiVLApiToken);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareVLApiToken.setPropertyValue(txtVLToken);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareVLApiToken);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpVLApiToken.setPropertyValue(txtVLToken);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpVLApiToken);
                }

                // VL Pull URL
                String txtVLPullURL = (String) responseObj.get("txtVLPullURL");
                txtVLPullURL = txtVLPullURL == null ? "" : txtVLPullURL.trim();
                System.out.println("txtVLPullURL: " + txtVLPullURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiVLServerPullUrl.setPropertyValue(txtVLPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiVLServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareVLServerPullUrl.setPropertyValue(txtVLPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareVLServerPullUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpVLServerPullUrl.setPropertyValue(txtVLPullURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpVLServerPullUrl);
                }

                // VL Push URL
                String txtVLPushURL = (String) responseObj.get("txtVLPushURL");
                txtVLPushURL = txtVLPushURL == null ? "" : txtVLPushURL.trim();
                System.out.println("txtVLPushURL: " + txtVLPushURL);
                if(selSystemType.equalsIgnoreCase("CHAI")) {
                    gpChaiVLServerPushUrl.setPropertyValue(txtVLPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpChaiVLServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("LABWARE")) {
                    gpLabwareVLServerPushUrl.setPropertyValue(txtVLPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpLabwareVLServerPushUrl);
                } else if(selSystemType.equalsIgnoreCase("EDARP")) {
                    gpEdarpVLServerPushUrl.setPropertyValue(txtVLPushURL);
                    Context.getAdministrationService().saveGlobalProperty(gpEdarpVLServerPushUrl);
                }

                // Local Result Endpoint
                String txtLocalResultEndpoint = (String) responseObj.get("txtLocalResultEndpoint");
                txtLocalResultEndpoint = txtLocalResultEndpoint == null ? "" : txtLocalResultEndpoint.trim();
                System.out.println("txtLocalResultEndpoint: " + txtLocalResultEndpoint);
                gpLocalResultEndpoint.setPropertyValue(txtLocalResultEndpoint);
                Context.getAdministrationService().saveGlobalProperty(gpLocalResultEndpoint);

                // Local FLU Result Endpoint
                String txtLocalFLUResultEndpoint = (String) responseObj.get("txtLocalFLUResultEndpoint");
                txtLocalFLUResultEndpoint = txtLocalFLUResultEndpoint == null ? "" : txtLocalFLUResultEndpoint.trim();
                System.out.println("txtLocalResultEndpoint: " + txtLocalFLUResultEndpoint);
                gpLocalFLUResultEndpoint.setPropertyValue(txtLocalFLUResultEndpoint);
                Context.getAdministrationService().saveGlobalProperty(gpLocalFLUResultEndpoint);

                // Scheduler Username
                String txtSchedulerUsername = (String) responseObj.get("txtSchedulerUsername");
                txtSchedulerUsername = txtSchedulerUsername == null ? "" : txtSchedulerUsername.trim();
                System.out.println("txtSchedulerUsername: " + txtSchedulerUsername);
                gpSchedulerUsername.setPropertyValue(txtSchedulerUsername);
                Context.getAdministrationService().saveGlobalProperty(gpSchedulerUsername);

                // Scheduler Password
                String txtSchedulerPassword = (String) responseObj.get("txtSchedulerPassword");
                txtSchedulerPassword = txtSchedulerPassword == null ? "" : txtSchedulerPassword.trim();
                System.out.println("txtSchedulerPassword: " + txtSchedulerPassword);
                gpSchedulerPassword.setPropertyValue(txtSchedulerPassword);
                Context.getAdministrationService().saveGlobalProperty(gpSchedulerPassword);

                // SSL Verification Enabled
                String chkSSLVerificationEnabled = (String) responseObj.get("chkSSLVerificationEnabled");
                chkSSLVerificationEnabled = chkSSLVerificationEnabled == null ? "" : chkSSLVerificationEnabled.trim();
                System.out.println("chkSSLVerificationEnabled: " + chkSSLVerificationEnabled);
                if(chkSSLVerificationEnabled.equalsIgnoreCase("on")) {
                    gpSslVerification.setPropertyValue("true");
                    Context.getAdministrationService().saveGlobalProperty(gpSslVerification);
                } else if(chkSSLVerificationEnabled.equalsIgnoreCase("off")) {
                    gpSslVerification.setPropertyValue("false");
                    Context.getAdministrationService().saveGlobalProperty(gpSslVerification);
                }

                // Lab TAT For VL Results
                String txtLabTATForVLResults = (String) responseObj.get("txtLabTATForVLResults");
                txtLabTATForVLResults = txtLabTATForVLResults == null ? "" : txtLabTATForVLResults.trim();
                System.out.println("txtLabTATForVLResults: " + txtLabTATForVLResults);
                gpLabTATForVLResults.setPropertyValue(txtLabTATForVLResults);
                Context.getAdministrationService().saveGlobalProperty(gpLabTATForVLResults);

                // Retry Period For Incomplete Results
                String txtRetryPeriodForIncompleteResults = (String) responseObj.get("txtRetryPeriodForIncompleteResults");
                txtRetryPeriodForIncompleteResults = txtRetryPeriodForIncompleteResults == null ? "" : txtRetryPeriodForIncompleteResults.trim();
                System.out.println("txtRetryPeriodForIncompleteResults: " + txtRetryPeriodForIncompleteResults);
                gpRetryPeriodForIncompleteResults.setPropertyValue(txtRetryPeriodForIncompleteResults);
                Context.getAdministrationService().saveGlobalProperty(gpRetryPeriodForIncompleteResults);

                TaskDefinition pullTaskDefinition = getTaskByUuid(ModuleConstants.PULL_SCHEDULER_UUID);
                TaskDefinition pushTaskDefinition = getTaskByUuid(ModuleConstants.PUSH_SCHEDULER_UUID);

                // Pull Scheduler Interval
                String txtPullSchedulerInterval = (String) responseObj.get("txtPullSchedulerInterval");
                txtPullSchedulerInterval = txtPullSchedulerInterval == null ? "" : txtPullSchedulerInterval.trim();
                System.out.println("txtPullSchedulerInterval: " + txtPullSchedulerInterval);
                long lnPullSchedulerInterval = Utils.getLongValue(txtPullSchedulerInterval);
                lnPullSchedulerInterval = lnPullSchedulerInterval * 60;
                pullTaskDefinition.setRepeatInterval(lnPullSchedulerInterval);

                // Pull Scheduler Enabled
                String chkPullSchedulerEnabled = (String) responseObj.get("chkPullSchedulerEnabled");
                chkPullSchedulerEnabled = chkPullSchedulerEnabled == null ? "" : chkPullSchedulerEnabled.trim();
                System.out.println("chkPullSchedulerEnabled: " + chkPullSchedulerEnabled);
                if(chkPullSchedulerEnabled.equalsIgnoreCase("on")) {
                    pullTaskDefinition.setStartOnStartup(true);
                } else if(chkPullSchedulerEnabled.equalsIgnoreCase("off")) {
                    pullTaskDefinition.setStartOnStartup(false);
                }

                // Push Scheduler Interval
                String txtPushSchedulerInterval = (String) responseObj.get("txtPushSchedulerInterval");
                txtPushSchedulerInterval = txtPushSchedulerInterval == null ? "" : txtPushSchedulerInterval.trim();
                System.out.println("txtPushSchedulerInterval: " + txtPushSchedulerInterval);
                long lnPushSchedulerInterval = Utils.getLongValue(txtPushSchedulerInterval);
                lnPushSchedulerInterval = lnPushSchedulerInterval * 60;
                pushTaskDefinition.setRepeatInterval(lnPushSchedulerInterval);

                // Push Scheduler Enabled
                String chkPushSchedulerEnabled = (String) responseObj.get("chkPushSchedulerEnabled");
                chkPushSchedulerEnabled = chkPushSchedulerEnabled == null ? "" : chkPushSchedulerEnabled.trim();
                System.out.println("chkPushSchedulerEnabled: " + chkPushSchedulerEnabled);
                if(chkPushSchedulerEnabled.equalsIgnoreCase("on")) {
                    pushTaskDefinition.setStartOnStartup(true);
                } else if(chkPushSchedulerEnabled.equalsIgnoreCase("off")) {
                    pushTaskDefinition.setStartOnStartup(false);
                }

                ret.put("success", true);
                ret.put("message", "Success saving settings");
            } else {
                // User has NO privilege
                System.out.println("Order Entry: Saving settings : User has NO privilege");
                ret.put("success", false);
                ret.put("message", "User not authorized to edit settings");
            }
        } catch(Exception ex) {
            ret.put("success", false);
            ret.put("message", "An error occured: " + ex.getMessage());
        }
        return(ret);
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



