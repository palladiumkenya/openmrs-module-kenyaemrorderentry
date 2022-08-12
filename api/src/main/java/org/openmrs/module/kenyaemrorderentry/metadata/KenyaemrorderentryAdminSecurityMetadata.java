package org.openmrs.module.kenyaemrorderentry.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.privilege;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.idSet;
import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.role;

/**
 * Implementation of access control to the app.
 */
@Component
public class KenyaemrorderentryAdminSecurityMetadata extends AbstractMetadataBundle{

    public static class _Privilege {
        public static final String APP_LAB_MANIFEST_ADMIN = "App: kenyaemr.labmanifest";
    }

    public static final class _Role {
        public static final String APPLICATION_ORDERS_ADMIN = "Lab Manifest Administration";
        public static final String API_PRIVILEGES_VIEW_AND_EDIT = "API Privileges (View and Edit)";
    }

    /**
     * @see AbstractMetadataBundle#install()
     */
    @Override
    public void install() {

        install(privilege(_Privilege.APP_LAB_MANIFEST_ADMIN, "Able to access Lab Manifest"));
        install(role(_Role.APPLICATION_ORDERS_ADMIN, "Can access Order Entry Admin app", idSet(
                _Role.API_PRIVILEGES_VIEW_AND_EDIT
        ), idSet(
                _Privilege.APP_LAB_MANIFEST_ADMIN
        )));
    }
}
