package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.ui.framework.SimpleObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange.getOrderReasonCode;

/**
 * A generic class for implementing system specific web requests
 */
public abstract class LabWebRequest {


    private Integer manifestType; // i.e VL or EID

    public LabWebRequest() {
    }

    public LabWebRequest(Integer orderType) {
        this.manifestType = orderType;
    }
    public abstract boolean checkRequirements();

    public abstract void postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException;

    public abstract void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds) throws IOException;

    /**
     * Generate cross cutting object for post request
     * @param o
     * @param dateSampleCollected
     * @param dateSampleSeparated
     * @param sampleType
     * @param manifestID
     * @return
     */
    public ObjectNode baselinePostRequestPayload(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID) {
        Patient patient = o.getPatient();
        ObjectNode test = Utils.getJsonNodeFactory().objectNode();

        String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";
        PatientIdentifier cccNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType());

        String fullName = "";

        if (patient.getGivenName() != null) {
            fullName += patient.getGivenName();
        }

        if (patient.getMiddleName() != null) {
            fullName += " " + patient.getMiddleName();
        }

        if (patient.getFamilyName() != null) {
            fullName += " " + patient.getFamilyName();
        }

        test.put("dob", dob);
        test.put("patient_name", fullName);
        test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
        test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
        test.put("order_no", o.getOrderId().toString());
        test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
        test.put("lab", "");


        if (manifestType == 1) { // we are using 1 for EID and 2 for VL
            PatientIdentifier heiNumber = patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType());
            test.put("hei_identifier", heiNumber != null ? heiNumber.getIdentifier() : "");
            test.put("prophylaxis", "nascopCode");
            test.put("pcr_sample_code", "nascopCode");
            test.put("entry_point", "nascopCode");
            test.put("infant_feeding_code", "nascopCode");
            test.put("age_of_mother", "nascopCode");
            test.put("ccc_number_of_mother", "nascopCode");
            test.put("pmtct_regimen_of_mother", "nascopCode");
            test.put("mother_vl_result", "nascopCode"); // within the last 6 months

        } else if (manifestType == 2) {
            Encounter originalRegimenEncounter = RegimenMappingUtils.getFirstEncounterForProgram(patient, "ARV");
            Encounter currentRegimenEncounter = RegimenMappingUtils.getLastEncounterForProgram(patient, "ARV");
            if (currentRegimenEncounter == null) {
                return test;
            }

            SimpleObject regimenDetails = RegimenMappingUtils.buildRegimenChangeObject(currentRegimenEncounter.getObs(), currentRegimenEncounter);
            String regimenName = (String) regimenDetails.get("regimenShortDisplay");
            String regimenLine = (String) regimenDetails.get("regimenLine");
            String nascopCode = "";
            if (StringUtils.isNotBlank(regimenName )) {
                nascopCode = RegimenMappingUtils.getDrugNascopCodeByDrugNameAndRegimenLine(regimenName, regimenLine);
            }

            if (StringUtils.isBlank(nascopCode) && StringUtils.isNotBlank(regimenLine)) {
                nascopCode = RegimenMappingUtils.getNonStandardCodeFromRegimenLine(regimenLine);
            }

            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");

            //add to list only if code is found. This is a temp measure to avoid sending messages with null regimen codes
            if (StringUtils.isNotBlank(nascopCode)) {

                test.put("prophylaxis", nascopCode);
                if (patient.getGender().equals("F")) {
                    test.put("pmtct", "3");
                }
                test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
                test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");

            }
        }

        return test;
    }

    public abstract ObjectNode completePostPayload(Order o, Date dateSampleCollected, Date dateSampleSeparated, String sampleType, String manifestID);

    public Integer getManifestType() {
        return manifestType;
    }

    public void setManifestType(Integer manifestType) {
        this.manifestType = manifestType;
    }
}
