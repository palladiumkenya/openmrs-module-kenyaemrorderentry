package org.openmrs.module.kenyaemrorderentry.util;

import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.kenyaui.KenyaUiConstants;
import org.openmrs.ui.framework.page.PageContext;

public class OrderEntryUIUtils {
    public static final String APP_LAB_ORDER = "kenyaemr.laborder";

    public static void setDrugOrderPageAttributes(PageContext pageContext, String appId) {
        AppDescriptor app = null;
        app = Context.getService(AppFrameworkService.class).getApp(appId);
        pageContext.getRequest().getRequest().setAttribute(KenyaUiConstants.REQUEST_ATTR_CURRENT_APP, app);
        pageContext.getModel().addAttribute(KenyaUiConstants.MODEL_ATTR_CURRENT_APP, app);
    }
}
