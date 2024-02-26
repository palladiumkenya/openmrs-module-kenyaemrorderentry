package org.openmrs.module.kenyaemrorderentry.labDataExchange;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.ModuleConstants;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.SimpleObject;
// import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.api.AdministrationService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange.getOrderReasonCode;

/**
 * A generic class for implementing system specific web requests
 */
public abstract class LabWebRequest {

    protected static final Log log = LogFactory.getLog(LabWebRequest.class);
    private Integer manifestType; // i.e VL or EID
    private AdministrationService administrationService = Context.getAdministrationService();
    final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

    public LabWebRequest() {
    }

    public LabWebRequest(Integer orderType) {
        this.manifestType = orderType;
    }
    public abstract boolean checkRequirements();

    public abstract boolean postSamples(LabManifestOrder manifestOrder, String manifestStatus) throws IOException;

    public abstract void pullResult(List<Integer> orderIds, List<Integer> manifestOrderIds, LabManifest manifestToUpdateResults) throws IOException;

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
        String kdod = "";

        String dob = patient.getBirthdate() != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()) : "";
        PatientIdentifier cccNumber = patient.getPatientIdentifier(Utils.getUniquePatientNumberIdentifierType());
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            PatientIdentifierType pit = MetadataUtils.existing(PatientIdentifierType.class, "b51ffe55-3e76-44f8-89a2-14f5eaf11079");
            PatientIdentifier kdodNumber = patient.getPatientIdentifier(pit);
            if(kdodNumber != null || !StringUtils.isBlank(kdodNumber.getIdentifier())) {
                kdod = kdodNumber.getIdentifier();
            }
        }
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

        if (manifestType == LabManifest.VL_TYPE) {

            if(isKDoD.trim().equalsIgnoreCase("true")) {
                if(StringUtils.isBlank(kdod)) {
                    return test;
                }
            } else {
                if (cccNumber == null || StringUtils.isBlank(cccNumber.getIdentifier())) {
                    return test;
                }
            }
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

            if (StringUtils.isBlank(nascopCode)) {
                return test;
            }

            test.put("dob", dob);
            test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
            test.put("order_no", o.getOrderId().toString());

            if(isKDoD.trim().equalsIgnoreCase("true")) {
                test.put("patient_identifier", kdod != null ? kdod : "");
            } else {
                test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            }
            test.put("sampletype", LabOrderDataExchange.getSampleTypeCode(sampleType));
            test.put("patient_name", fullName);

            test.put("regimenline", regimenLine);
            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");
            test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("prophylaxis", nascopCode);
            test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
            test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");

        } else if (manifestType == LabManifest.EID_TYPE) { // we are using 1 for EID and 2 for VL TODO: this block needs to be reviewed
            PatientIdentifier heiNumber = patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType());
            SimpleObject heiDetailsObject = Utils.getHeiDetailsForEidPostObject(patient,o);
            SimpleObject heiMothersAgeObject = Utils.getHeiMothersAge(patient);

            if (heiNumber == null || StringUtils.isBlank(heiNumber.getIdentifier()) || heiDetailsObject == null) {
                return test;
            }

            if (LabOrderDataExchange.getSystemType() == ModuleConstants.CHAI_SYSTEM || LabOrderDataExchange.getSystemType() == ModuleConstants.EDARP_SYSTEM) {
                System.out.println("Creating payload for CHAI or EDARP EID");
                test.put("dob", dob);
                test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");
                test.put("order_no", o.getOrderId().toString());
                if(isKDoD.trim().equalsIgnoreCase("true")) {
                    test.put("patient_identifier", kdod != null ? kdod : "");
                } else {
                    test.put("patient_identifier", heiNumber != null ? heiNumber.getIdentifier() : "");
                }
                test.put("sampletype", LabOrderDataExchange.getSampleTypeCode(sampleType));
                test.put("patient_name", fullName);
                test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
                test.put("feeding", "yes");
                test.put("pcrtype", heiDetailsObject.get("pcrSampleCodeAnswer") != null ? heiDetailsObject.get("pcrSampleCodeAnswer").toString() : "");
                test.put("regimen", "1");
                test.put("entry_point", heiDetailsObject.get("entryPointAnswer") != null ? heiDetailsObject.get("entryPointAnswer").toString() : "");
                test.put("mother_prophylaxis", heiDetailsObject.get("mothersRegimenAnswer") != null ? heiDetailsObject.get("mothersRegimenAnswer").toString() : "");
                test.put("mother_last_result", heiDetailsObject.get("validMothersVL") != null ? heiDetailsObject.get("validMothersVL").toString() : ""); // vl within last 6 months
                test.put("spots", "");
                test.put("mother_age", (heiMothersAgeObject != null && heiMothersAgeObject.get("mothersAge") != null) ? heiMothersAgeObject.get("mothersAge").toString() : "" );
                test.put("ccc_no", Utils.getMothersUniquePatientNumber(patient) !=null ? Utils.getMothersUniquePatientNumber(patient) : "");

            } else if (LabOrderDataExchange.getSystemType() == ModuleConstants.LABWARE_SYSTEM) {
                System.out.println("Creating payload for labware EID");
                test.put("sample_type", sampleType);
                test.put("pat_name", fullName);
                test.put("patient_name", fullName);
                test.put("order_no", o.getOrderId().toString());
                test.put("dob", dob);
                test.put("sex", patient.getGender().equals("M") ? "1" : patient.getGender().equals("F") ? "2" : "3");

                if(heiDetailsObject !=null) {
                    test.put("infant_prophylaxis", heiDetailsObject.get("prophylaxisAnswer") != null ? heiDetailsObject.get("prophylaxisAnswer").toString() : "");
                    test.put("pcr_code", heiDetailsObject.get("pcrSampleCodeAnswer") != null ? heiDetailsObject.get("pcrSampleCodeAnswer").toString() : "");
                    test.put("pcrtype", heiDetailsObject.get("pcrSampleCodeAnswer") != null ? heiDetailsObject.get("pcrSampleCodeAnswer").toString() : "");
                    test.put("entry_point", heiDetailsObject.get("entryPointAnswer") != null ? heiDetailsObject.get("entryPointAnswer").toString() : "");
                    test.put("infant_feeding", heiDetailsObject.get("feedingMethodAnswer") != null ? heiDetailsObject.get("feedingMethodAnswer").toString() : "");
                    test.put("mother_pmtct", heiDetailsObject.get("mothersRegimenAnswer") != null ? heiDetailsObject.get("mothersRegimenAnswer").toString() : "");
                    test.put("mother_vl_res", heiDetailsObject.get("validMothersVL") != null ? heiDetailsObject.get("validMothersVL").toString() : ""); // vl within last 6 months
                }
                test.put("date_collected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
                test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
                test.put("regimen", "1");
                test.put("feeding", "yes");
                test.put("sample_type", "DBS");
                if(isKDoD.trim().equalsIgnoreCase("true")) {
                    test.put("hei_id", kdod != null ? kdod : "");
                } else {
                    test.put("hei_id", heiNumber != null ? heiNumber.getIdentifier() : "");
                }
                test.put("mother_age", (heiMothersAgeObject != null && heiMothersAgeObject.get("mothersAge") != null) ? heiMothersAgeObject.get("mothersAge").toString() : "" );
                test.put("mother_ccc", Utils.getMothersUniquePatientNumber(patient) !=null ? Utils.getMothersUniquePatientNumber(patient) : "");
                test.put("ccc_no",  cccNumber != null ? cccNumber.getIdentifier() : "");
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
