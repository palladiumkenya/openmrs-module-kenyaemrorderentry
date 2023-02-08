package org.openmrs.module.kenyaemrorderentry;

public class ModuleConstants {
    public static final String GP_CHAI_VL_LAB_SERVER_API_TOKEN = "chai_vl_server_api_token";
    public static final String GP_CHAI_VL_LAB_SERVER_REQUEST_URL = "chai_vl_server_url";
    public static final String GP_CHAI_VL_LAB_SERVER_RESULT_URL = "chai_vl_server_result_url";
    public static final String GP_CHAI_EID_LAB_SERVER_REQUEST_URL = "chai_eid_server_url";
    public static final String GP_CHAI_EID_LAB_SERVER_RESULT_URL = "chai_eid_server_result_url";
    public static final String GP_CHAI_EID_LAB_SERVER_API_TOKEN = "chai_eid_server_api_token";
    public static final String GP_LABWARE_VL_LAB_SERVER_REQUEST_URL = "labware_vl_server_url";
    public static final String GP_LABWARE_VL_LAB_SERVER_RESULT_URL = "labware_vl_server_result_url";
    public static final String GP_LABWARE_VL_LAB_SERVER_API_TOKEN = "labware_vl_server_api_token";
    public static final String GP_LABWARE_EID_LAB_SERVER_REQUEST_URL = "labware_eid_server_url";
    public static final String GP_LABWARE_EID_LAB_SERVER_RESULT_URL = "labware_eid_server_result_url";
    public static final String GP_LABWARE_EID_LAB_SERVER_API_TOKEN = "labware_eid_server_api_token";

    public static final String GP_EDARP_VL_LAB_SERVER_API_TOKEN = "edarp_vl_server_api_token";
    public static final String GP_EDARP_VL_LAB_SERVER_REQUEST_URL = "edarp_vl_server_url";
    public static final String GP_EDARP_VL_LAB_SERVER_RESULT_URL = "edarp_vl_server_result_url";
    public static final String GP_MANIFEST_LAST_PROCESSED = "kemrorder.last_processed_manifest";// used when fetching results from the server
    public static final String GP_RETRY_PERIOD_FOR_ORDERS_WITH_INCOMPLETE_RESULTS = "kemrorder.retry_period_for_incomplete_vl_result";
    public static final String GP_LAB_TAT_FOR_VL_RESULTS = "kemrorder.viral_load_result_tat_in_days";
    public static final String GP_MANIFEST_LAST_UPDATETIME = "kemrorder.manifest_last_update_time";
    public static final String MANIFEST_LAST_UPDATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String LAB_SYSTEM_DATE_PATTERN = "yyyy-MM-dd";
    public static final String GP_LAB_SYSTEM_IN_USE = "kemrorder.labsystem_identifier";

    // System Types e.g CHAI, LABWARE etc

    public static final int NO_SYSTEM_CONFIGURED = 0;
    public static final int CHAI_SYSTEM = 1;
    public static final int LABWARE_SYSTEM = 2;

    public static final int EDARP_SYSTEM = 3;

    public static final Integer DEFAULT_APHL_LAB_CODE = 7;
    public static final String GP_SSL_VERIFICATION_ENABLED = "kemrorder.ssl_verification_enabled";

}
