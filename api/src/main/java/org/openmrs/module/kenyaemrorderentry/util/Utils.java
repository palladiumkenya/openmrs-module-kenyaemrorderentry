package org.openmrs.module.kenyaemrorderentry.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

public class Utils {

    public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    public static final String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";

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

    public static Integer getMothersAge(Patient patient) {

        Integer mothersAge = null;

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
        return mothersAge;
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
    public static int daysBetween(Date date1, Date date2) {
        DateTime d1 = new DateTime(date1.getTime());
        DateTime d2 = new DateTime(date2.getTime());
        return Days.daysBetween(d1, d2).getDays();
    }
}


