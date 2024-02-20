package org.openmrs.module.kenyaemrorderentry.page.controller.orders;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.metadata.KenyaemrorderentryAdminSecurityMetadata;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

@AppPage("kenyaemr.labmanifest")
public class MaintenancePageController {

    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
    SchedulerService schedulerService = Context.getSchedulerService();

    public void get(@SpringBean KenyaUiUtils kenyaUi, UiUtils ui, PageModel model) {
        model.put("userHasSettingsEditRole", (Context.getAuthenticatedUser().containsRole(KenyaemrorderentryAdminSecurityMetadata._Role.API_ROLE_EDIT_SETTINGS) || Context.getAuthenticatedUser().isSuperUser()));
    }

}