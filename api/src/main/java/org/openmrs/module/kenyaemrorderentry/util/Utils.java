package org.openmrs.module.kenyaemrorderentry.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Relationship;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.CoreConstants;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class Utils {

    public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    public static final String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
    static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    public static final String MCH_MOTHER_SERVICE_PROGRAM = "b5d9e05f-f5ab-4612-98dd-adb75438ed34";

    /**
     * Gets the PatientIdentifierType for a patient UPN
     * @return
     */
    public static PatientIdentifierType getUniquePatientNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);

    }
    /**
     * Gets the PatientIdentifierType for a patient HEI Number
     * @return
     */
    public static PatientIdentifierType getHeiNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);

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

    /**
     * Borrowed from KenyaEMR
     * Returns the MFL code for a location
     * @param location
     * @return
     */
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


    /**
     * Creates a node factory
     * @return
     */
    public static JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }


    /**
     * Extracts the request body and return it as string
     * @param reader
     * @return
     */
    public static String fetchRequestBody(BufferedReader reader) {
        String requestBodyJsonStr = "";
        try {

            BufferedReader br = new BufferedReader(reader);
            String output = "";
            while ((output = reader.readLine()) != null) {
                requestBodyJsonStr += output;
            }
        } catch (IOException e) {

            System.out.println("IOException: " + e.getMessage());

        }
        return requestBodyJsonStr;
    }

    /**
     * Used to strip off unicode literals from string
     */
    public static void stripUnicodeLiterals () {
        //String fullName = result.get("full_names").getAsString();

                            /*for (int j = 0; j < fullName.length(); j++) {
                                if (Character.UnicodeBlock.of(fullName.charAt(j)) != Character.UnicodeBlock.BASIC_LATIN) {
                                    fullName = "Replaced name";//result.addProperty("full_names", "Replaced Name");
                                    break;
                                    // replace with Y
                                }
                            }

                            String escapeName = StringEscapeUtils.escapeJava(fullName);
                            String stripUnicode = new UnicodeUnescaper().translate(escapeName);

                            String cleanedName = StringEscapeUtils.unescapeJava(stripUnicode);*/
    }

    /**
     * Finds the last encounter during the program enrollment with the given encounter type
     * Picked for Kenyaemr.EmrUtils
     * @param type the encounter type
     *
     * @return the encounter
     */
    public static Encounter lastEncounter(Patient patient, EncounterType type) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }
    /**
     * Check mothers CCC number for an infant from the relationship defined
     * Picked for Kenyaemr.EmrUtils
     * @param patient
     * @return list of mothers
     */

    public static String getMothersUniquePatientNumber(Patient patient) {

        String cccNumber = "";

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            if (relationship.getRelationshipType().getbIsToA().equals("Parent")) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    if (!relationship.getPersonB().isDead()) {

                        Integer personId = relationship.getPersonB().getPersonId();
                        //Patient mother = Context.getPatientService().getPatient(personId);
                        if(Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            PatientIdentifierType pit = MetadataUtils.existing(PatientIdentifierType.class,Utils.getUniquePatientNumberIdentifierType().getUuid());
                            PatientIdentifier cccObject = mother.getPatientIdentifier(pit);
                            cccNumber = cccObject.getIdentifier();
                        }
                    }
                }
            }
            if (relationship.getRelationshipType().getaIsToB().equals("Parent")) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    if (!relationship.getPersonA().isDead()) {

                        Integer personId = relationship.getPersonA().getPersonId();
                        //Patient mother = Context.getPatientService().getPatient(personId);
                        if(Context.getPatientService().getPatient(personId) != null){
                            Patient mother = Context.getPatientService().getPatient(personId);
                            PatientIdentifierType pit = MetadataUtils.existing(PatientIdentifierType.class, Utils.getUniquePatientNumberIdentifierType().getUuid());
                            PatientIdentifier cccObject = mother.getPatientIdentifier(pit);
                            cccNumber = cccObject.getIdentifier();

                        }
                    }
                }
            }
        }
        return cccNumber;
    }

    /**
     * Check mothers  Age for an infant from the relationship defined
     * @param patient
     * @return list of mothers
     */

    public static SimpleObject getHeiMothersAge(Patient patient) {

        Integer mothersAge = null;
        SimpleObject object = null;

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            if (relationship.getRelationshipType().getbIsToA().equals("Parent")) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    if (!relationship.getPersonB().isDead()) {

                        Integer personId = relationship.getPersonB().getPersonId();
                        if(Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            mothersAge = mother.getAge();
                        }
                    }
                }
                break;
            }
            if (relationship.getRelationshipType().getaIsToB().equals("Parent")) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    if (!relationship.getPersonA().isDead()) {

                        Integer personId = relationship.getPersonA().getPersonId();
                        //Patient mother = Context.getPatientService().getPatient(personId);
                        if(Context.getPatientService().getPatient(personId) != null){
                            Patient mother = Context.getPatientService().getPatient(personId);
                            mothersAge = mother.getAge();

                        }
                    }
                }
            }
        }
        object = SimpleObject.create("mothersAge",mothersAge);
        return object;
    }

   /**
     * Check mothers last viral load for an infant from the relationship defined
     * @param patient
     * @return list of mothers
     */

    public static SimpleObject getMothersLastViralLoad(Patient patient) {

        String latestVL = "856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String latestLDL = "1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        SimpleObject object = null;

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            if (relationship.getRelationshipType().getbIsToA().equals("Parent")) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    if (!relationship.getPersonB().isDead()) {

                        Integer personId = relationship.getPersonB().getPersonId();
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            Obs mothersNumericVLObs = getLatestObs(mother, latestVL);
                            Obs mothersLDLObs = getLatestObs(mother, latestLDL);
                            if (mothersNumericVLObs != null && mothersLDLObs == null) {
                                object = SimpleObject.create("lastVl", mothersNumericVLObs.getValueNumeric(), "lastVlDate", mothersNumericVLObs.getObsDatetime());
                            }
                            if (mothersNumericVLObs == null && mothersLDLObs != null) {
                                object = SimpleObject.create("lastVl", "LDL", "lastVlDate", mothersLDLObs.getObsDatetime());
                            }
                            if (mothersNumericVLObs != null && mothersLDLObs != null) {
                                //find the latest of the 2
                                Obs lastViralLoadPicked = null;
                                if (mothersNumericVLObs.getObsDatetime().after(mothersLDLObs.getObsDatetime())) {
                                    lastViralLoadPicked = mothersNumericVLObs;
                                } else {
                                    lastViralLoadPicked = mothersLDLObs;
                                }

                                if (lastViralLoadPicked.getConcept().getConceptId().equals(856)) {
                                    object = SimpleObject.create("lastVl", lastViralLoadPicked.getValueNumeric(), "lastVlDate", mothersNumericVLObs.getObsDatetime());
                                } else {
                                    object = SimpleObject.create("lastVl", "LDL", "lastVlDate", mothersLDLObs.getObsDatetime());
                                }

                            }
                        }
                    }
                }
                break;
            }
            if (relationship.getRelationshipType().getaIsToB().equals("Parent")) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    if (!relationship.getPersonA().isDead()) {

                        Integer personId = relationship.getPersonA().getPersonId();
                        //Patient mother = Context.getPatientService().getPatient(personId);
                        if(Context.getPatientService().getPatient(personId) != null){
                            Patient mother = Context.getPatientService().getPatient(personId);
                            Obs mothersNumericVLObs = getLatestObs(mother, latestVL);
                            Obs mothersLDLObs = getLatestObs(mother, latestLDL);
                            if(mothersNumericVLObs != null && mothersLDLObs == null){
                                object = SimpleObject.create("lastVl", mothersNumericVLObs.getValueNumeric(), "lastVlDate", mothersNumericVLObs.getObsDatetime());
                            }
                            if(mothersNumericVLObs == null && mothersLDLObs != null) {
                                object = SimpleObject.create("lastVl", "LDL", "lastVlDate", mothersLDLObs.getObsDatetime());
                            }
                            if(mothersNumericVLObs != null && mothersLDLObs != null) {
                                //find the latest of the 2
                                Obs lastViralLoadPicked = null;
                                if (mothersNumericVLObs.getObsDatetime().after(mothersLDLObs.getObsDatetime())) {
                                    lastViralLoadPicked = mothersNumericVLObs;
                                } else {
                                    lastViralLoadPicked = mothersLDLObs;
                                }
                                if(lastViralLoadPicked.getConcept().getConceptId().equals(856)) {
                                    object = SimpleObject.create("lastVl", lastViralLoadPicked.getValueNumeric(), "lastVlDate", mothersNumericVLObs.getObsDatetime());
                                }
                                else {
                                    object = SimpleObject.create("lastVl", "LDL", "lastVlDate", mothersLDLObs.getObsDatetime());
                                }

                            }

                        }
                    }
                }
            }
        }
        return object;
    }

    /**
     * Check mothers  current age for an HEI from the relationship defined
     * @param patient
     * @return current mothers regimen
     */

    public static SimpleObject getHeiMothersCurrentRegimen(Patient patient) {

        String mothersCurrentRegimen = "";
        SimpleObject object = null;

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            if (relationship.getRelationshipType().getbIsToA().equals("Parent")) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    if (!relationship.getPersonB().isDead()) {
                        Integer personId = relationship.getPersonB().getPersonId();
                        if(Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            //On ART -- find if mother has active ART
                            Encounter lastDrugRegimenEditorEncounter = getLastEncounterForCategory(mother, "ARV");   //last DRUG_REGIMEN_EDITOR encounter
                            if (lastDrugRegimenEditorEncounter != null) {
                                SimpleObject o = buildRegimenChangeObject(lastDrugRegimenEditorEncounter.getAllObs(), lastDrugRegimenEditorEncounter);
                                mothersCurrentRegimen = o.get("regimenShortDisplay").toString();
                            }
                        }
                    }
                }
                break;
            }
            if (relationship.getRelationshipType().getaIsToB().equals("Parent")) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    if (!relationship.getPersonA().isDead()) {
                        Integer personId = relationship.getPersonA().getPersonId();
                        if(Context.getPatientService().getPatient(personId) != null){
                            Patient mother = Context.getPatientService().getPatient(personId);
                            //On ART -- find if mother has active ART
                            Encounter lastDrugRegimenEditorEncounter = getLastEncounterForCategory(mother, "ARV");   //last DRUG_REGIMEN_EDITOR encounter
                            if (lastDrugRegimenEditorEncounter != null) {
                                SimpleObject o = buildRegimenChangeObject(lastDrugRegimenEditorEncounter.getAllObs(), lastDrugRegimenEditorEncounter);
                                mothersCurrentRegimen = o.get("regimenShortDisplay").toString();
                            }

                        }
                    }
                }
            }
        }
        object = SimpleObject.create("mothersCurrentRegimen",mothersCurrentRegimen);
        return object;
    }

    public static Encounter getLastEncounterForCategory (Patient patient, String category) {

        FormService formService = Context.getFormService();
        EncounterService encounterService = Context.getEncounterService();
        String ARV_TREATMENT_PLAN_EVENT_CONCEPT = "1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String TB_TREATMENT_PLAN_CONCEPT = "1268AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String DRUG_REGIMEN_EDITOR_ENCOUNTER = "7dffc392-13e7-11e9-ab14-d663bd873d93";
        String DRUG_REGIMEN_EDITOR_FORM = "da687480-e197-11e8-9f32-f2801f1b9fd1";
        List<SimpleObject> history = new ArrayList<SimpleObject>();
        String categoryConceptUuid = category.equals("ARV")? ARV_TREATMENT_PLAN_EVENT_CONCEPT : TB_TREATMENT_PLAN_CONCEPT;

        EncounterType et = encounterService.getEncounterTypeByUuid(DRUG_REGIMEN_EDITOR_ENCOUNTER);
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

    public static SimpleObject buildRegimenChangeObject(Set<Obs> obsList, Encounter e) {

        String CURRENT_DRUGS = "1193AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REASON_REGIMEN_STOPPED_CODED = "1252AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REASON_REGIMEN_STOPPED_NON_CODED = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String DATE_REGIMEN_STOPPED = "1191AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String CURRENT_DRUG_NON_STANDARD ="1088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String REGIMEN_LINE_CONCEPT = "163104AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"; // concept should be changed to correct one



        String regimen = null;
        String regimenShort = null;
        String regimenLine = null;
        String regimenUuid = null;
        String endDate = null;
        String startDate = e != null? DATE_FORMAT.format(e.getEncounterDatetime()) : "";
        Set<String> changeReason = new HashSet<String>();

        StringBuilder nonstandardRegimen = new StringBuilder();
        for(Obs obs:obsList) {

            if (obs.getConcept().getUuid().equals(CURRENT_DRUGS) ) {
                regimen = obs.getValueCoded() != null ? obs.getValueCoded().getFullySpecifiedName(CoreConstants.LOCALE).getName() : "Unresolved Regimen name";
                try {
                    regimenShort = getRegimenNameFromRegimensXMLString(obs.getValueCoded().getUuid(), getRegimenConceptJson());
                    //  regimenLine = getRegimenLineFromRegimensXMLString(obs.getValueCoded().getUuid(), getRegimenConceptJson());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                regimenUuid = obs.getValueCoded() != null ? obs.getValueCoded().getUuid() : "";
            } else if (obs.getConcept().getUuid().equals(CURRENT_DRUG_NON_STANDARD) ) {
                nonstandardRegimen.append(obs.getValueCoded().getFullySpecifiedName(CoreConstants.LOCALE).getName().toUpperCase() + "/");
                regimenUuid = obs.getValueCoded() != null ? obs.getValueCoded().getUuid() : "";
            }

            else if (obs.getConcept().getUuid().equals(REASON_REGIMEN_STOPPED_CODED)) {
                String reason = obs.getValueCoded() != null ?  obs.getValueCoded().getName().getName() : "";
                if (reason != null)
                    changeReason.add(reason);
            } else if (obs.getConcept().getUuid().equals(REASON_REGIMEN_STOPPED_NON_CODED)) {
                String reason = obs.getValueText();
                if (reason != null)
                    changeReason.add(reason);
            } else if (obs.getConcept() != null && obs.getConcept().getUuid().equals(DATE_REGIMEN_STOPPED)) {
                if(obs.getValueDatetime() != null){
                    endDate = DATE_FORMAT.format(obs.getValueDatetime());
                }
            } else if (obs.getConcept() != null && obs.getConcept().getUuid().equals(REGIMEN_LINE_CONCEPT)) {
                if(obs.getValueText() != null){
                    if (obs.getValueText().equals("AF")) {
                        regimenLine = "Adult first line";
                    } else if (obs.getValueText().equals("AS")) {
                        regimenLine = "Adult second line";
                    } else if (obs.getValueText().equals("AT")) {
                        regimenLine = "Adult third line";
                    } else if (obs.getValueText().equals("CF")) {
                        regimenLine = "Child first line";
                    } else if (obs.getValueText().equals("CS")) {
                        regimenLine = "Child second line";
                    } else if (obs.getValueText().equals("CT")) {
                        regimenLine = "Child third line";
                    }
                }
            }

        }
        if(nonstandardRegimen.length() > 0) {
            return SimpleObject.create(
                    "startDate", startDate,
                    "endDate", endDate != null? endDate : "",
                    "regimenShortDisplay", (nonstandardRegimen.toString()).substring(0,nonstandardRegimen.length() - 1) ,
                    "regimenLine", regimenLine != null ? regimenLine : "",
                    "regimenLongDisplay", (nonstandardRegimen.toString()).substring(0,nonstandardRegimen.length() - 1),
                    "changeReasons", changeReason,
                    "regimenUuid", regimenUuid,
                    "current",endDate != null ? false : true

            );
        }

        if(regimen != null) {
            return SimpleObject.create(
                    "startDate", startDate,
                    "endDate", endDate != null? endDate : "",
                    "regimenShortDisplay", regimenShort != null ? regimenShort : regimen,
                    "regimenLine", regimenLine != null ? regimenLine : "",
                    "regimenLongDisplay", regimen,
                    "changeReasons", changeReason,
                    "regimenUuid", regimenUuid,
                    "current",endDate != null ? false : true

            );
        }
        return SimpleObject.create(
                "startDate",  "",
                "endDate",  "",
                "regimenShortDisplay", "",
                "regimenLine",  "",
                "regimenLongDisplay", "",
                "changeReasons", "",
                "regimenUuid", "",
                "current",""

        );

        //return null;
    }
    public static String getRegimenNameFromRegimensXMLString(String conceptRef, String regimenJson) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode conf = (ArrayNode) mapper.readTree(regimenJson);

        for (Iterator<JsonNode> it = conf.iterator(); it.hasNext(); ) {
            ObjectNode node = (ObjectNode) it.next();
            if (node.get("conceptRef").asText().equals(conceptRef)) {
                return node.get("name").asText();
            }
        }

        return "Unknown";
    }
    /**
     * Check latest obs values for given obs
     * @param patient
     * @return latest obs
     */
    public static Obs getLatestObs(Patient patient, String conceptIdentifier) {
        Concept concept = Context.getConceptService().getConceptByUuid(conceptIdentifier);
        List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
        if (obs.size() > 0) {
            // these are in reverse chronological order
            return obs.get(0);
        }
        return null;
    }
    public static List<Encounter> AllEncounters(Patient patient, EncounterType type, Form form) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, Collections.singleton(form), Collections.singleton(type), null, null, null, false);
        return encounters;
    }
    public static boolean programEncounterMatching(Set<Obs> obs, String conceptUuidToMatch) {
        for (Obs o : obs) {
            if (o.getConcept().getUuid().equals(conceptUuidToMatch)) {
                return true;
            }
        }
        return false;
    }

    public static int daysBetween(Date date1, Date date2) {
        DateTime d1 = new DateTime(date1.getTime());
        DateTime d2 = new DateTime(date2.getTime());
        return Days.daysBetween(d1, d2).getDays();
    }

    public static String getRegimenConceptJson() {
        String json = "[\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/NVP\",\n" +
                "    \"conceptRef\": \"162565AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/EFV\",\n" +
                "    \"conceptRef\": \"164505AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/NVP\",\n" +
                "    \"conceptRef\": \"1652AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/EFV\",\n" +
                "    \"conceptRef\": \"160124AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/NVP\",\n" +
                "    \"conceptRef\": \"792AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/EFV\",\n" +
                "    \"conceptRef\": \"160104AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/AZT\",\n" +
                "    \"conceptRef\": \"98e38a9c-435d-4a94-9b66-5ca524159d0e\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/DTG\",\n" +
                "    \"conceptRef\": \"6dec7d7d-0fda-4e8d-8295-cb6ef426878d\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG\",\n" +
                "    \"conceptRef\": \"9fb85385-b4fb-468c-b7c1-22f75834b4b0\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/DTG\",\n" +
                "    \"conceptRef\": \"4dc0119b-b2a6-4565-8d90-174b97ba31db\",\n" +
                "    \"regimenLine\": \"adult_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162561AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164511AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162201AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164512AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162560AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"c421d8e7-4f43-43b4-8d2f-c7d4cfb976a4\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETR/RAL/DRV/RTV\",\n" +
                "    \"conceptRef\": \"337b6cfd-9fa7-47dc-82b4-d479c39ef355\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETR/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"7a6c51c4-2b68-4d5a-b5a2-7ba420dde203\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162200AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"dddd9cf2-2b9c-4c52-84b3-38cfe652529a\",\n" +
                "    \"regimenLine\": \"adult_second\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162200AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/NVP\",\n" +
                "    \"conceptRef\": \"162199AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/EFV\",\n" +
                "    \"conceptRef\": \"162563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ABC\",\n" +
                "    \"conceptRef\": \"817AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"D4T/3TC/ABC\",\n" +
                "    \"conceptRef\": \"b9fea00f-e462-4ea5-8d40-cc10e4be697e\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/ABC/LPV/r\",\n" +
                "    \"conceptRef\": \"162562AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/DDI/LPV/r\",\n" +
                "    \"conceptRef\": \"162559AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/TDF/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"077966a6-4fbd-40ce-9807-2d5c2e8eb685\",\n" +
                "    \"regimenLine\": \"child_first\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHZE\",\n" +
                "    \"conceptRef\": \"1675AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHZ\",\n" +
                "    \"conceptRef\": \"768AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"SRHZE\",\n" +
                "    \"conceptRef\": \"1674AAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RfbHZE\",\n" +
                "    \"conceptRef\": \"07c72be8-c575-4e26-af09-9a98624bce67\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RfbHZ\",\n" +
                "    \"conceptRef\": \"9ba203ec-516f-4493-9b2c-4ded6cc318bc\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"SRfbHZE\",\n" +
                "    \"conceptRef\": \"fce8ba26-8524-43d1-b0e1-53d8a3c06c00\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"S (1 gm vial)\",\n" +
                "    \"conceptRef\": \"84360AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"adult_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"E\",\n" +
                "    \"conceptRef\": \"75948AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RH\",\n" +
                "    \"conceptRef\": \"1194AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RHE\",\n" +
                "    \"conceptRef\": \"159851AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"EH\",\n" +
                "    \"conceptRef\": \"1108AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"child_intensive\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"5b8e4955-897a-423b-ab66-7e202b9c304c\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/AZT\",\n" +
                "    \"conceptRef\": \"092604d3-e9cb-4589-824e-9e17e3cb4f5e\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/TDF\",\n" +
                "    \"conceptRef\": \"c6372744-9e06-40cf-83e5-c794c985b6bf\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETV/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"1995c4a1-a625-4449-ab28-aae88d0f80e6\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/LPV/r\",\n" +
                "    \"conceptRef\": \"162561AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"Child (second line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"164511AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"Child (second line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/ATV/r\",\n" +
                "    \"conceptRef\": \"dddd9cf2-2b9c-4c52-84b3-38cfe652529a\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"5b8e4955-897a-423b-ab66-7e202b9c304c\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/AZT\",\n" +
                "    \"conceptRef\": \"092604d3-e9cb-4589-824e-9e17e3cb4f5e\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ETV/3TC/DRV/RTV\",\n" +
                "    \"conceptRef\": \"1995c4a1-a625-4449-ab28-aae88d0f80e6\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"RAL/3TC/DRV/RTV/ABC\",\n" +
                "    \"conceptRef\": \"0e74f7aa-85ab-4e92-9f97-79e76e618689\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"AZT/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"a1183b26-8e87-457c-8d7d-00a96b17e046\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"02302ab5-dcb2-4337-a792-d6cf1082fc1d\",\n" +
                "    \"regimenLine\": \"Child (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/DRV/r\",\n" +
                "    \"conceptRef\": \"5f429c76-2976-4374-a69e-d2d138dd16bf\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/RAL/DRV/r\",\n" +
                "    \"conceptRef\": \"9b9817dd-4c84-4093-95c3-690d65d24b99\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/ATV/r\",\n" +
                "    \"conceptRef\": \"64b63993-1479-4714-9389-312072f26704\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/ETV/DRV/r\",\n" +
                "    \"conceptRef\": \"9de6367e-479b-4d50-a0f9-2a9987c6dce0\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/DTG/DRV/r\",\n" +
                "    \"conceptRef\": \"cc728487-2f54-4d5e-ae0f-22ef617a8cfd\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"TDF/3TC/DTG/EFV/DRV/r\",\n" +
                "    \"conceptRef\": \"f2acaf9b-3da9-4d71-b0cf-fd6af1073c9e\",\n" +
                "    \"regimenLine\": \"Adult (third line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"B/F/TAF\",\n" +
                "    \"conceptRef\": \"167206AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n" +
                "    \"regimenLine\": \"Adult (first line)\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"ABC/3TC/RAL\",\n" +
                "    \"conceptRef\": \"7af7ebbe-99da-4a43-a23a-c3866c5d08db\",\n" +
                "    \"regimenLine\": \"Child (first line)\"\n" +
                "  }\n" +
                "]";
        return json;
    }
}


