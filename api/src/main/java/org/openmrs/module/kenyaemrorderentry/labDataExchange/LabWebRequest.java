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
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.RegimenMappingUtils;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
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

    protected static final Log log = LogFactory.getLog(LabWebRequest.class);
    private Integer manifestType; // i.e VL or EID

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

        if (manifestType == LabManifest.VL_TYPE) {

            if (cccNumber == null || StringUtils.isBlank(cccNumber.getIdentifier())) {
                return test;
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
            test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
            test.put("sampletype", sampleType);
            test.put("datereceived", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("patient_name", fullName);

            test.put("regimenline", regimenLine);
            test.put("justification", o.getOrderReason() != null ? getOrderReasonCode(o.getOrderReason().getUuid()) : "");
            test.put("datecollected", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("sampletype", manifestType.toString());
            test.put("prophylaxis", nascopCode);
            test.put("initiation_date", originalRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(originalRegimenEncounter.getEncounterDatetime()) : "");
            test.put("dateinitiatedonregimen", currentRegimenEncounter != null ? Utils.getSimpleDateFormat("yyyy-MM-dd").format(currentRegimenEncounter.getEncounterDatetime()) : "");

        } else if (manifestType == LabManifest.EID_TYPE) { // we are using 1 for EID and 2 for VL TODO: this block needs to be reviewed
            PatientIdentifier heiNumber = patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType());
            SimpleObject heiDetailsObject = getHeiDetailsForEidPostObject(patient,o);
            SimpleObject heiMothersAgeObject = Utils.getHeiMothersAge(patient);

            if (heiNumber == null || StringUtils.isBlank(heiNumber.getIdentifier()) || heiDetailsObject == null) {
                return test;
            }
            //API differences
            test.put("sample_type", sampleType);
            test.put("date_received", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("datereceived", Utils.getSimpleDateFormat("yyyy-MM-dd").format(dateSampleCollected));
            test.put("pat_name", fullName);
            test.put("patient_name", fullName);

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
            test.put("hei_id", heiNumber != null ? heiNumber.getIdentifier() : "");
            test.put("mother_age", heiMothersAgeObject != null ? heiMothersAgeObject.get("mothersAge").toString() : "" );
            test.put("mother_ccc", Utils.getMothersUniquePatientNumber(patient) !=null ? Utils.getMothersUniquePatientNumber(patient) : "");
            test.put("ccc_no",  cccNumber != null ? cccNumber.getIdentifier() : "");
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

    /**
     * Retrieve HEI details required for a successful post
     * @param patient
     * @return HEI details
     */

    public static SimpleObject getHeiDetailsForEidPostObject(Patient patient,Order o) {
        SimpleObject object = null;
        String entryPointAnswer = "";
        Integer entryPointQuestion = 160540;
        String prophylaxisAnswer = "";
        Integer prophylaxisQuestion = 1282;
        String mothersRegimenAnswer = "";
        String pcrSampleCodeAnswer = "";
        String feedingMethodAnswer = "";
        Integer feedingMethodQuestion = 1151;

        //pcr sample code from lab orders
        Integer orderReason = o.getOrderReason().getConceptId();
        if (orderReason.equals(1040)) {
            pcrSampleCodeAnswer = "1";    //Initial PCR (6week or first contact)
        }else if (orderReason.equals(1326)) {
            pcrSampleCodeAnswer = "2";    //2nd PCR (6 months)
        }else if (orderReason.equals(164860)) {
            pcrSampleCodeAnswer = "3";    //3rd PCR (12months)
        }else if (orderReason.equals(162082)) {
            pcrSampleCodeAnswer = "3";    //Confirmatory PCR and Baseline VL
        }
        //Get encounter based variables from hei enrollment and followup
        Encounter lastHeiEnrollmentEncounter = Utils.lastEncounter(Context.getPatientService().getPatient(o.getPatient().getPatientId()), Context.getEncounterService().getEncounterTypeByUuid("415f5136-ca4a-49a8-8db3-f994187c3af6"));   //last Hei Enrollement encounter
        Encounter lastHeiCWCFollowupEncounter = Utils.lastEncounter(Context.getPatientService().getPatient(o.getPatient().getPatientId()), Context.getEncounterService().getEncounterTypeByUuid("bcc6da85-72f2-4291-b206-789b8186a021"));   //last Hei CWC Folowup encounter
        if (lastHeiEnrollmentEncounter != null) {
            //Entry point
            for (Obs obs : lastHeiEnrollmentEncounter.getObs()) {
                if (obs.getConcept().getConceptId().equals(entryPointQuestion)) {
                    Integer heitEntryPointObsAnswer = obs.getValueCoded().getConceptId();
                    if (heitEntryPointObsAnswer.equals(160542)) {
                        entryPointAnswer = "2";    //OPD
                    } else if (heitEntryPointObsAnswer.equals(160456)) {
                        entryPointAnswer = "3";      //Maternity
                    } else if (heitEntryPointObsAnswer.equals(162050)) {
                        entryPointAnswer = "4";      //CCC
                    } else if (heitEntryPointObsAnswer.equals(160538)) {
                        entryPointAnswer = "5";      //MCH/PMTCT
                    } else if (heitEntryPointObsAnswer.equals(5622)) {
                        entryPointAnswer = "6";      //Other
                    }
                }
                //Prophylaxis
                if (obs.getConcept().getConceptId().equals(prophylaxisQuestion)) {
                    Integer heiProphylaxisObsAnswer = obs.getValueCoded().getConceptId();
                    if (heiProphylaxisObsAnswer.equals(80586)) {
                        prophylaxisAnswer = "1";    //AZT for 6 weeks + NVP for 12 weeks
                    } else if (heiProphylaxisObsAnswer.equals(1652)) {
                        prophylaxisAnswer = "2";      //AZT for 6 weeks + NVP for >12 weeks
                    } else if (heiProphylaxisObsAnswer.equals(1149)) {
                        prophylaxisAnswer = "3";      //None
                    } else if (heiProphylaxisObsAnswer.equals(1107)) {
                        prophylaxisAnswer = "4";      //Other
                    }
                }

            }
        }

        if (lastHeiCWCFollowupEncounter != null) {
            for (Obs obs : lastHeiCWCFollowupEncounter.getObs()) {
                // Baby feeding method
                if (obs.getConcept().getConceptId().equals(feedingMethodQuestion)) {
                    Integer heiBabyFeedingObsAnswer = obs.getValueCoded().getConceptId();
                    if (heiBabyFeedingObsAnswer.equals(5526)) {
                        feedingMethodAnswer = "EBF";    //Exclusive Breast Feeding
                    } else if (heiBabyFeedingObsAnswer.equals(1595)) {
                        feedingMethodAnswer = "ERF";      //Exclusive Replacement Feeding
                    } else if (heiBabyFeedingObsAnswer.equals(6046)) {
                        feedingMethodAnswer = "MF";      //MF= Mixed Feeding
                    }
                }
            }
        }

        SimpleObject vlObject = Utils.getMothersLastViralLoad(o.getPatient());
        String validMothersVL = "";
        if(vlObject !=null){
            Date lastVLResultDate = (Date) vlObject.get("lastVlDate");
            if (Utils.daysBetween(lastVLResultDate, new Date()) <= 183) {
                validMothersVL = vlObject.get("lastVl").toString();
            }
        }

        //pmtct_regimen_of_mother
        SimpleObject mothersRegimenObject = Utils.getHeiMothersCurrentRegimen(o.getPatient());
        String currentMothersRegimen = "";
        if(mothersRegimenObject !=null){
            currentMothersRegimen = mothersRegimenObject.get("mothersCurrentRegimen").toString();
            if (currentMothersRegimen.equals("AZT/3TC/NVP")) {
                mothersRegimenAnswer = "PM3";    //PM3= AZT+3TC+NVP
            } else if (currentMothersRegimen.equals("AZT/3TC/EFV")) {
                mothersRegimenAnswer = "PM4";      //AZT+ 3TC+ EFV
            } else if (currentMothersRegimen.equals("AZT/3TC/LPV/r")) {
                mothersRegimenAnswer = "PM5";      //AZT+3TC+ LPV/r
            } else if (currentMothersRegimen.equals("TDF/3TC/NVP")) {
                mothersRegimenAnswer = "PM6";     //TDC+3TC+NVP
            } else if (currentMothersRegimen.equals("TDF/3TC/EFV")) {
                mothersRegimenAnswer = "PM9";     //TDF+3TC+EFV
            } else if (currentMothersRegimen.equals("AZT/3TC/ATV/r")) {
                mothersRegimenAnswer = "PM10";     //AZT+3TC+ATV/r
            } else if (currentMothersRegimen.equals("TDF/3TC/ATV/r")) {
                mothersRegimenAnswer = "PM11";     //TDF+3TC+ATV/r
            } else if (currentMothersRegimen.equals("TDF/3TC/ATV/r")) {
                mothersRegimenAnswer = "PM11";     //TDF+3TC+ATV/r
            }
        }

        object = SimpleObject.create("entryPointAnswer", entryPointAnswer,
                                      "prophylaxisAnswer", prophylaxisAnswer,
                                      "mothersRegimenAnswer", mothersRegimenAnswer,
                                      "pcrSampleCodeAnswer", pcrSampleCodeAnswer,
                                      "feedingMethodAnswer", feedingMethodAnswer,
                                      "validMothersVL", validMothersVL);
        return object;
       }

    }
