package org.openmrs.module.kenyaemrorderentry.shr;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.openmrs.*;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;


import java.text.SimpleDateFormat;
import java.util.*;

public class OutgoingLabOrderSHR {
    LocationService locationService = Context.getLocationService();

    private Integer patientID;
    private Patient patient;
    private PersonService personService;
    private PatientService patientService;
    private String patientIdentifier;
    String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
    String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";

    public OutgoingLabOrderSHR() {
    }

    public OutgoingLabOrderSHR(Integer patientID) {
        this.patientID = patientID;
        this.patientService = Context.getPatientService();
        this.patient = patientService.getPatient(patientID);
        this.personService = Context.getPersonService();
    }

    public OutgoingLabOrderSHR(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
        this.patientService = Context.getPatientService();
        this.personService = Context.getPersonService();
        setPatientUsingIdentifier();
    }

    public static JsonNodeFactory getJsonNodeFactory() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        return factory;
    }

    private ObjectNode getPatientName() {
        PersonName pn = patient.getPersonName();
        ObjectNode nameNode = getJsonNodeFactory().objectNode();
        nameNode.put("FIRST_NAME", pn.getGivenName());
        nameNode.put("MIDDLE_NAME", pn.getMiddleName());
        nameNode.put("LAST_NAME", pn.getFamilyName());
        return nameNode;
    }

    public static String getSHRDateFormat() {
        return "yyyyMMdd";
    }

    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    public void setPatientUsingIdentifier() {

        if (patientIdentifier != null) {
            PatientIdentifierType HEI_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(HEI_UNIQUE_NUMBER);
            PatientIdentifierType CCC_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(UNIQUE_PATIENT_NUMBER);

            List<Patient> patientsListWithIdentifier = patientService.getPatients(null, patientIdentifier.trim(),
                    Arrays.asList(HEI_NUMBER_TYPE, CCC_NUMBER_TYPE), false);
            if (patientsListWithIdentifier.size() > 0) {
                this.patient = patientsListWithIdentifier.get(0);
            }

        }
    }
    private ObjectNode getPatientAddress() {

        /**
         * county: personAddress.country
         * sub-county: personAddress.stateProvince
         * ward: personAddress.address4
         * landmark: personAddress.address2
         * postal address: personAddress.address1
         */

        Set<PersonAddress> addresses = patient.getAddresses();
        //patient address
        ObjectNode patientAddressNode = getJsonNodeFactory().objectNode();
        ObjectNode physicalAddressNode = getJsonNodeFactory().objectNode();
        String postalAddress = "";
        String county = "";
        String sub_county = "";
        String ward = "";
        String landMark = "";

        for (PersonAddress address : addresses) {
            if (address.getAddress1() != null) {
                postalAddress = address.getAddress1();
            }
            if (address.getCountry() != null) {
                county = address.getCountry() != null ? address.getCountry() : "";
            }

            if (address.getCountyDistrict() != null) {
                county = address.getCountyDistrict() != null ? address.getCountyDistrict() : "";
            }

            if (address.getStateProvince() != null) {
                sub_county = address.getStateProvince() != null ? address.getStateProvince() : "";
            }

            if (address.getAddress4() != null) {
                ward = address.getAddress4() != null ? address.getAddress4() : "";
            }
            if (address.getAddress2() != null) {
                landMark = address.getAddress2() != null ? address.getAddress2() : "";
            }

        }

        physicalAddressNode.put("COUNTY", county);
        physicalAddressNode.put("SUB_COUNTY", sub_county);
        physicalAddressNode.put("WARD", ward);
        physicalAddressNode.put("NEAREST_LANDMARK", landMark);

        //combine all addresses
        patientAddressNode.put("PHYSICAL_ADDRESS", physicalAddressNode);
        patientAddressNode.put("POSTAL_ADDRESS", postalAddress);

        return patientAddressNode;
    }

    public ObjectNode patientIdentification() {

        JsonNodeFactory factory = getJsonNodeFactory();
        ObjectNode patientSHR = factory.objectNode();
        if (patient != null) {

            PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
            PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
            PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);
            List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);

            Map<String, String> patientIdentifiers = new HashMap<String, String>();

            ObjectNode patientIdentificationNode = factory.objectNode();
            ArrayNode internalIdentifiers = factory.arrayNode();
            ObjectNode externalIdentifiers = factory.objectNode();

            for (PatientIdentifier identifier : identifierList) {
                PatientIdentifierType identifierType = identifier.getIdentifierType();

                ObjectNode element = factory.objectNode();
                if (identifierType.equals(NATIONAL_ID_TYPE)) {
                    patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
                } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                    patientIdentifiers.put("ALIEN_NUMBER", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "ALIEN_NUMBER");

                } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                    patientIdentifiers.put("PASSPORT_NUMBER", identifier.getIdentifier());
                    element.put("ID", identifier.getIdentifier());
                    element.put("IDENTIFIER_TYPE", "PASSPORT_NUMBER");
                }
                if (!element.isEmpty(null)) {
                    internalIdentifiers.add(element);
                }
            }

            // get other patient details

            String dob = getSimpleDateFormat(getSHRDateFormat()).format(this.patient.getBirthdate());
            String dobPrecision = patient.getBirthdateEstimated() ? "ESTIMATED" : "EXACT";
            String sex = patient.getGender();

            patientIdentificationNode.put("INTERNAL_PATIENT_ID", internalIdentifiers);
            patientIdentificationNode.put("PATIENT_NAME", getPatientName());
            patientIdentificationNode.put("DATE_OF_BIRTH", dob);
            patientIdentificationNode.put("DATE_OF_BIRTH_PRECISION", dobPrecision);
            patientIdentificationNode.put("SEX", sex);
            patientIdentificationNode.put("PATIENT_ADDRESS", getPatientAddress());
                        patientSHR.put("VERSION", "1.0.0");
            patientSHR.put("PATIENT_IDENTIFICATION", patientIdentificationNode);
            return patientSHR;
        } else {
            return patientSHR;
        }
    }

    private ObjectNode getMotherDetails() {
        // get relationships
        // mother name
        String motherName = "";
        ObjectNode mothersNameNode = getJsonNodeFactory().objectNode();
        ObjectNode motherDetails = getJsonNodeFactory().objectNode();
        ArrayNode motherIdenfierNode = getJsonNodeFactory().arrayNode();
        RelationshipType type = getParentChildType();

        List<Relationship> parentChildRel = personService.getRelationships(null, patient, getParentChildType());
        if (parentChildRel.isEmpty() && parentChildRel.size() == 0) {
            // try getting this from person attribute
            if (patient.getAttribute(4) != null) {
                motherName = patient.getAttribute(4).getValue();
                mothersNameNode.put("FIRST_NAME", motherName);
                mothersNameNode.put("MIDDLE_NAME", "");
                mothersNameNode.put("LAST_NAME", "");
            } else {
                mothersNameNode.put("FIRST_NAME", "");
                mothersNameNode.put("MIDDLE_NAME", "");
                mothersNameNode.put("LAST_NAME", "");
            }

        }

        // check if it is mothers
        Person mother = null;
        // a_is_to_b = 'Parent' and b_is_to_a = 'Child'
        for (Relationship relationship : parentChildRel) {

            if (patient.equals(relationship.getPersonB())) {
                if (relationship.getPersonA().getGender().equals("F")) {
                    mother = relationship.getPersonA();
                    break;
                }
            } else if (patient.equals(relationship.getPersonA())) {
                if (relationship.getPersonB().getGender().equals("F")) {
                    mother = relationship.getPersonB();
                    break;
                }
            }
        }
        if (mother != null) {
            //get mother name
            mothersNameNode.put("FIRST_NAME", mother.getGivenName());
            mothersNameNode.put("MIDDLE_NAME", mother.getMiddleName());
            mothersNameNode.put("LAST_NAME", mother.getFamilyName());

            // get identifiers
            motherIdenfierNode = getMotherIdentifiers(patientService.getPatient(mother.getPersonId()));
        }

        motherDetails.put("MOTHER_NAME", mothersNameNode);
        motherDetails.put("MOTHER_IDENTIFIER", motherIdenfierNode);

        return motherDetails;
    }

    protected RelationshipType getParentChildType() {
        return personService.getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f");

    }

    public ArrayNode getMotherIdentifiers(Patient patient) {

        PatientIdentifierType NATIONAL_ID_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.NATIONAL_ID);
        PatientIdentifierType ALIEN_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.ALIEN_NUMBER);
        PatientIdentifierType PASSPORT_NUMBER_TYPE = patientService.getPatientIdentifierTypeByUuid(SHRConstants.PASSPORT_NUMBER);

        List<PatientIdentifier> identifierList = patientService.getPatientIdentifiers(null, Arrays.asList(NATIONAL_ID_TYPE, NATIONAL_ID_TYPE, ALIEN_NUMBER_TYPE, PASSPORT_NUMBER_TYPE), null, Arrays.asList(patient), null);
        Map<String, String> patientIdentifiers = new HashMap<String, String>();

        JsonNodeFactory factory = getJsonNodeFactory();
        ArrayNode internalIdentifiers = factory.arrayNode();

        for (PatientIdentifier identifier : identifierList) {
            PatientIdentifierType identifierType = identifier.getIdentifierType();
            ObjectNode element = factory.objectNode();

            if (identifierType.equals(NATIONAL_ID_TYPE)) {
                patientIdentifiers.put("NATIONAL_ID", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "NATIONAL_ID");
            } else if (identifierType.equals(ALIEN_NUMBER_TYPE)) {
                patientIdentifiers.put("ALIEN_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "ALIEN_NUMBER");

            } else if (identifierType.equals(PASSPORT_NUMBER_TYPE)) {
                patientIdentifiers.put("PASSPORT_NUMBER", identifier.getIdentifier());
                element.put("ID", identifier.getIdentifier());
                element.put("IDENTIFIER_TYPE", "PASSPORT_NUMBER");
            }

            internalIdentifiers.add(element);

        }

        return internalIdentifiers;
    }
    private JSONPObject getPatientIdentifiers() {
        return null;
    }

    private JSONPObject getHivTestDetails() {
        return null;
    }

    private JSONPObject getImmunizationDetails() {
        return null;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }


}
