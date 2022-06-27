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
import static org.openmrs.module.kenyaemrorderentry.util.Utils.getHeiMothersAge;

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
        test.put("sampletype", manifestType.toString());
        test.put("order_no", o.getOrderId().toString());
        test.put("patient_identifier", cccNumber != null ? cccNumber.getIdentifier() : "");
        test.put("lab", "");

        System.out.println("Lab Results POST: Manifest Type is: " + manifestType);
        if (manifestType == LabManifest.EID_TYPE) { // we are using 1 for EID and 2 for VL
            System.out.println("Lab Results POST: populating payload for EID Type");
            PatientIdentifier heiNumber = patient.getPatientIdentifier(Utils.getHeiNumberIdentifierType());
            SimpleObject heiDetailsObject = getHeiDetailsForEidPostObject(o.getPatient(),o);
            SimpleObject heiMothersAgeObject = getHeiMothersAge(o.getPatient());
            if(heiDetailsObject !=null) {
                test.put("prophylaxis", heiDetailsObject.get("prophylaxisAnswer").toString());
                test.put("pcr_sample_code", heiDetailsObject.get("pcrSampleCodeAnswer").toString());
                test.put("entry_point", heiDetailsObject.get("entryPointAnswer").toString());
                test.put("infant_feeding_code", heiDetailsObject.get("feedingMethodAnswer").toString());
                test.put("pmtct_regimen_of_mother", heiDetailsObject.get("mothersRegimenAnswer").toString());
                test.put("mother_vl_result", heiDetailsObject.get("validMothersVL").toString()); // vl within last 6 months
            }
            test.put("hei_identifier", heiNumber != null ? heiNumber.getIdentifier() : "");
            test.put("age_of_mother", heiMothersAgeObject.get("mothersAge").toString() != null ? heiMothersAgeObject.get("mothersAge").toString() : "" );
            test.put("ccc_number_of_mother", Utils.getMothersUniquePatientNumber(patient));
            test.put("pmtct_regimen_of_mother", heiDetailsObject.get("mothersRegimenAnswer").toString());
            test.put("mother_vl_result", ""); // within the last 6 months

        } else if (manifestType == LabManifest.VL_TYPE) {
            System.out.println("Lab Results POST: populating payload for VL Type");
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
        Integer mothersRegimenQuestion = 1088;
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
                //pmtct_regimen_of_mother
                if (obs.getConcept().getConceptId().equals(mothersRegimenQuestion)) {
                    Integer heiMotherRegimenObsAnswer = obs.getValueCoded().getConceptId();
                    if (heiMotherRegimenObsAnswer.equals(1652)) {
                        mothersRegimenAnswer = "PM3";    //PM3= AZT+3TC+NVP
                    } else if (heiMotherRegimenObsAnswer.equals(160124)) {
                        mothersRegimenAnswer = "PM4";      //AZT+ 3TC+ EFV
                    } else if (heiMotherRegimenObsAnswer.equals(162561)) {
                        mothersRegimenAnswer = "PM5";      //AZT+3TC+ LPV/r
                    } else if (heiMotherRegimenObsAnswer.equals(162565)) {
                        mothersRegimenAnswer = "PM6";     //TDC+3TC+NVP
                    } else if (heiMotherRegimenObsAnswer.equals(164505)) {
                        mothersRegimenAnswer = "PM9";     //TDF+3TC+EFV
                    } else if (heiMotherRegimenObsAnswer.equals(164511)) {
                        mothersRegimenAnswer = "PM10";     //AZT+3TC+ATV/r
                    } else if (heiMotherRegimenObsAnswer.equals(164512)) {
                        mothersRegimenAnswer = "PM11";     //TDF+3TC+ATV/r
                    } else if (heiMotherRegimenObsAnswer.equals(164512)) {
                        mothersRegimenAnswer = "PM11";     //TDF+3TC+ATV/r
                    }
                }
            }
        }

        if (lastHeiCWCFollowupEncounter != null) {
            for (Obs obs : lastHeiEnrollmentEncounter.getObs()) {
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

        object = SimpleObject.create("entryPointAnswer", entryPointAnswer,
                                      "prophylaxisAnswer", prophylaxisAnswer,
                                      "mothersRegimenAnswer", mothersRegimenAnswer,
                                      "pcrSampleCodeAnswer", pcrSampleCodeAnswer,
                                      "feedingMethodAnswer", feedingMethodAnswer,
                                      "validMothersVL", validMothersVL);
        return object;
       }

    }
