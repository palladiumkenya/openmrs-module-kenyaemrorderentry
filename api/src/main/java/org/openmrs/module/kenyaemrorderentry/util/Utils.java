package org.openmrs.module.kenyaemrorderentry.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    public static final String GP_ORDER_ENTRY_CONFIG_DIR = "kenyaemrorderentry.drugsMappingDirectory";

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
}


