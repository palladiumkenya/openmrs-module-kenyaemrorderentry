package org.openmrs.module.kenyaemrorderentry.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Obs;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.PrivilegeConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class Utils {

    public static final String GP_ORDER_ENTRY_CONFIG_DIR = "kenyaemrorderentry.drugsMappingDirectory";
    public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    public static final String DRUG_REGIMEN_EDITOR_FORM = "da687480-e197-11e8-9f32-f2801f1b9fd1";
    public static final String DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE = "7dffc392-13e7-11e9-ab14-d663bd873d93";
    public static final String TEST_ORDER_TYPE_UUID = "1814ee89-2abf-42d7-920b-d138740d56d4";





    /**
     * Gets mappings for KenyaEMR-Nascop codes drug mapping
     * The mapping file is a json array with the following structure:
     * {
     *  "nascop_code": "AF1A",
     *  "drug_name": "TDF+3TC+EFV",
     *  "concept_id": 1234,
     *  "drug_type":"G",
     *  "regimen_line":"First Line"
     * }
     * @return json array
     */
    public static JSONArray getNacopCodesMapping() {

        File configFile = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService().getGlobalProperty(GP_ORDER_ENTRY_CONFIG_DIR));
        String fullFilePath = configFile.getPath() + File.separator + "KenyaEMR_Nascop_Codes_Drugs_Map.json";
        JSONParser jsonParser = new JSONParser();
        try {
            //Read JSON file
            FileReader reader = new FileReader(fullFilePath);
            Object obj = jsonParser.parse(reader);
            JSONArray drugsMap = (JSONArray) obj;

            return drugsMap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Iterates through the mapping document and returns an item matching on key and value
     * @param key to match
     * @param value to match
     * @return JSONObject
     */
    public static JSONObject getDrugEntryByKeyAndValue(String key, String value) {

        JSONArray config = getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if (o.get(key).toString().equals(value)) {
                    return o;
                }
            }
        }
        return null;
    }


    /**
     * Helper method for getting a config entry by concept_id
     * @param conceptId
     * @return
     */
    public static JSONObject getDrugEntryByConceptId(int conceptId) {

        JSONArray config = getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if ((Integer) o.get("concept_id") == conceptId) {
                    return o;
                }
            }
        }
        return null;
    }

    /**
     * Helper method for getting a config entry by nascop code
     * @param drugName
     * @return
     */
    public static JSONObject getDrugEntryByNascopCode(String drugName) {

        JSONArray config = getNacopCodesMapping();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                JSONObject o = (JSONObject) config.get(i);
                if (o.get("nascop_code").toString().equals(drugName)) {
                    return o;
                }
            }
        }
        return null;
    }

    public static PatientIdentifierType getUniquePatientNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);

    }

    /**
     * gets default location from global property
     * @return
     */
    public static Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            return gp != null ? ((Location) gp.getValue()) : null;
        }
        finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
        }

    }


    public static String getDefaultLocationMflCode(Location location) {
        String MASTER_FACILITY_CODE = "8a845a89-6aa5-4111-81d3-0af31c45c002";

        if(location == null) {
            location = getDefaultLocation();
        }
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
            for (LocationAttribute attr : location.getAttributes()) {
                if (attr.getAttributeType().getUuid().equals(MASTER_FACILITY_CODE) && !attr.isVoided()) {
                    return attr.getValueReference();
                }
            }
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_GLOBAL_PROPERTIES);
        }
        return null;
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    public static Encounter getLastEncounterForProgram (Patient patient, String category) {

        FormService formService = Context.getFormService();
        EncounterService encounterService = Context.getEncounterService();
        String ARV_TREATMENT_PLAN_EVENT_CONCEPT = "1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String TB_TREATMENT_PLAN_CONCEPT = "1268AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        List<SimpleObject> history = new ArrayList<SimpleObject>();
        String categoryConceptUuid = category.equals("ARV")? ARV_TREATMENT_PLAN_EVENT_CONCEPT : TB_TREATMENT_PLAN_CONCEPT;

        EncounterType et = encounterService.getEncounterTypeByUuid(DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE);
        Form form = formService.getFormByUuid(DRUG_REGIMEN_EDITOR_FORM);

        List<Encounter> encs = AllEncounters(patient, et, form);
        NavigableMap<Date, Encounter> programEncs = new TreeMap<Date, Encounter>();
        for (Encounter e : encs) {
            if (e != null) {
                Set<Obs> obs = e.getObs();
                if (programEncounterMatching(obs, categoryConceptUuid)) {
                    programEncs.put(e.getEncounterDatetime(), e);
                }
            }
        }
        if (!programEncs.isEmpty()) {
            return programEncs.lastEntry().getValue();
        }
        return null;
    }

    public static Encounter getFirstEncounterForProgram (Patient patient, String category) {

        FormService formService = Context.getFormService();
        EncounterService encounterService = Context.getEncounterService();
        String ARV_TREATMENT_PLAN_EVENT_CONCEPT = "1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String TB_TREATMENT_PLAN_CONCEPT = "1268AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        List<SimpleObject> history = new ArrayList<SimpleObject>();
        String categoryConceptUuid = category.equals("ARV")? ARV_TREATMENT_PLAN_EVENT_CONCEPT : TB_TREATMENT_PLAN_CONCEPT;

        EncounterType et = encounterService.getEncounterTypeByUuid(DRUG_REGIMEN_EDITOR_ENCOUNTER_TYPE);
        Form form = formService.getFormByUuid(DRUG_REGIMEN_EDITOR_FORM);

        List<Encounter> encs = AllEncounters(patient, et, form);
        NavigableMap<Date, Encounter> programEncs = new TreeMap<Date, Encounter>();
        for (Encounter e : encs) {
            if (e != null) {
                Set<Obs> obs = e.getObs();
                if (programEncounterMatching(obs, categoryConceptUuid)) {
                    programEncs.put(e.getEncounterDatetime(), e);
                }
            }
        }
        if (!programEncs.isEmpty()) {
            return programEncs.firstEntry().getValue();
        }
        return null;
    }

    public static boolean programEncounterMatching(Set<Obs> obs, String conceptUuidToMatch) {
        for (Obs o : obs) {
            if (o.getConcept().getUuid().equals(conceptUuidToMatch)) {
                return true;
            }
        }
        return false;
    }

    public static List<Encounter> AllEncounters(Patient patient, EncounterType type, Form form) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, Collections.singleton(form), Collections.singleton(type), null, null, null, false);
        return encounters;
    }

    public static OrderType getLabOrderType() {
        return Context.getOrderService().getOrderTypeByUuid(TEST_ORDER_TYPE_UUID);
    }

    public static JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

}


