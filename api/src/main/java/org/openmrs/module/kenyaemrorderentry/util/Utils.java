package org.openmrs.module.kenyaemrorderentry.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.util.PrivilegeConstants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Utils {

    public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
    public static final String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
    static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    public static final String MCH_MOTHER_SERVICE_PROGRAM = "b5d9e05f-f5ab-4612-98dd-adb75438ed34";

    /**
     * Gets the PatientIdentifierType for a patient UPN
     *
     * @return
     */
    public static PatientIdentifierType getUniquePatientNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);

    }

    /**
     * Gets the PatientIdentifierType for a patient HEI Number
     *
     * @return
     */
    public static PatientIdentifierType getHeiNumberIdentifierType() {
        return Context.getPatientService().getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);

    }

    /**
     * gets default location from global property
     *
     * @return
     */
    public static Location getDefaultLocation() {
        try {
            Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
            String GP_DEFAULT_LOCATION = "kenyaemr.defaultLocation";
            GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(GP_DEFAULT_LOCATION);
            return gp != null ? ((Location) gp.getValue()) : null;
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }

    }

    /**
     * Borrowed from KenyaEMR
     * Returns the MFL code for a location
     *
     * @param location
     * @return
     */
    public static String getDefaultLocationMflCode(Location location) {
        String MASTER_FACILITY_CODE = "8a845a89-6aa5-4111-81d3-0af31c45c002";

        if (location == null) {
            location = getDefaultLocation();
        }
        try {
            Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
            for (LocationAttribute attr : location.getAttributes()) {
                if (attr.getAttributeType().getUuid().equals(MASTER_FACILITY_CODE) && !attr.isVoided()) {
                    return attr.getValueReference();
                }
            }
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }
        return null;
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }


    /**
     * Creates a node factory
     *
     * @return
     */
    public static JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }


    /**
     * Extracts the request body and return it as string
     *
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
    public static void stripUnicodeLiterals() {
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
     *
     * @param type the encounter type
     * @return the encounter
     */
    public static Encounter lastEncounter(Patient patient, EncounterType type) {
        List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, Collections.singleton(type), null, null, null, false);
        return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
    }

    /**
     * Check mothers CCC number for an infant from the relationship defined
     * Picked for Kenyaemr.EmrUtils
     *
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
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            PatientIdentifierType pit = MetadataUtils.existing(PatientIdentifierType.class, Utils.getUniquePatientNumberIdentifierType().getUuid());
                            PatientIdentifier cccObject = mother.getPatientIdentifier(pit);
                            if (cccObject != null) {
                                cccNumber = cccObject.getIdentifier();
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
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            PatientIdentifierType pit = MetadataUtils.existing(PatientIdentifierType.class, Utils.getUniquePatientNumberIdentifierType().getUuid());
                            PatientIdentifier cccObject = mother.getPatientIdentifier(pit);
                            if (cccObject != null) {
                                cccNumber = cccObject.getIdentifier();
                            }

                        }
                    }
                }
            }
        }
        return cccNumber;
    }

    /**
     * Check mothers  Age for an infant from the relationship defined
     *
     * @param patient
     * @return list of mothers
     */

    public static SimpleObject getHeiMothersAge(Patient patient) {

        Integer mothersAge = null;
        SimpleObject mothersAgeObject = null;

        for (Relationship relationship : Context.getPersonService().getRelationshipsByPerson(patient)) {

            if (relationship.getRelationshipType().getbIsToA().equals("Parent")) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    if (!relationship.getPersonB().isDead()) {

                        Integer personId = relationship.getPersonB().getPersonId();
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            if (mother != null) {
                                mothersAge = mother.getAge();
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
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            if (mother != null) {
                                mothersAge = mother.getAge();
                            }

                        }
                    }
                }
            }
        }
        mothersAgeObject = SimpleObject.create("mothersAge", mothersAge);
        return mothersAgeObject;
    }

    /**
     * Check mothers last viral load for an infant from the relationship defined
     *
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
        }
        return object;
    }

    /**
     * Check mothers  current age for an HEI from the relationship defined
     *
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
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            //On ART -- find if mother has active ART
                            Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(mother, "ARV");
                            if (currentRegimenEncounter != null) {
                                SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
                                mothersCurrentRegimen = (String) regimenDetails.get("regimenShortDisplay");
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
                        if (Context.getPatientService().getPatient(personId) != null) {
                            Patient mother = Context.getPatientService().getPatient(personId);
                            //On ART -- find if mother has active ART
                            Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(mother, "ARV");
                            if (currentRegimenEncounter != null) {
                                SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
                                mothersCurrentRegimen = (String) regimenDetails.get("regimenShortDisplay");
                            }
                        }
                    }
                }
            }
        }
        object = SimpleObject.create("mothersCurrentRegimen", mothersCurrentRegimen);
        return object;
    }

    /**
     * Check latest obs values for given obs
     *
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

    /**
     * Builds an SSL context for disabling/bypassing SSL verification
     *
     * @return
     */
    public static SSLConnectionSocketFactory sslConnectionSocketFactoryWithDisabledSSLVerification() {
        SSLContextBuilder builder = SSLContexts.custom();
        try {
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        SSLContext sslContext = null;
        try {
            sslContext = builder.build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext, new X509HostnameVerifier() {
            @Override
            public void verify(String host, SSLSocket ssl)
                    throws IOException {
            }

            @Override
            public void verify(String host, X509Certificate cert)
                    throws SSLException {
            }

            @Override
            public void verify(String host, String[] cns,
                               String[] subjectAlts) throws SSLException {
            }

            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        return sslsf;
    }

    /**
     * Default SSL context
     *
     * @return
     */
    public static SSLConnectionSocketFactory sslConnectionSocketFactoryDefault() {
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        return sslsf;
    }
}


