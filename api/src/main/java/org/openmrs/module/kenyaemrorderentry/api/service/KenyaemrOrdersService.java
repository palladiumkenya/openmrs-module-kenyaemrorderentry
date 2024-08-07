package org.openmrs.module.kenyaemrorderentry.api.service;

import java.util.Date;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.ui.framework.SimpleObject;

public interface KenyaemrOrdersService extends OpenmrsService {
    void reprocessLabManifest(Integer manifestId);
    LabManifest saveLabOrderManifest(LabManifest labManifest);
    Long getLastManifestID();
    List<LabManifest> getLabOrderManifest();
    List<LabManifest> getLabOrderManifest(String status);
    LabManifest getLabOrderManifestById(Integer id);
    
    LabManifest getLabManifestById(Integer manId);
    LabManifest getLabManifestByUUID(String manUUID);
    String getLabManifestStatusByIdSQL(Integer manID);
    List<SimpleObject> getLabManifestSummaryGraphSQL();

    LabManifest getLabOrderManifestByManifestType(Integer manifestType);
    LabManifest getLastLabOrderManifest();
    LabManifest getLabOrderManifestByStatus(String status);
    LabManifest getLabOrderManifestByStatus(String status, Date onOrBefore);
    List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate);
    void voidLabOrderManifest(Integer id);

    List<LabManifest> getLabManifests(String uuid, String status, String type, String withErrors, Date createdOnOrAfterDate, Date createdOnOrBeforeDate);

    //Methods for manifest orders
    LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder);
    List<LabManifestOrder> getLabManifestOrders();
    LabManifestOrder getLabManifestOrderById(Integer id);
    LabManifestOrder getLabManifestOrderByUUID(String UUID);
    LabManifestOrder getLabManifestOrderByOrderType(Integer orderType);
    List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifestOrder);
    void voidLabManifestOrder(Integer manifestOrder);

    LabManifestOrder getLabManifestOrderByOrderId(Order specimenId);
    List<LabManifestOrder> getLabManifestOrderByStatus(String status);
    List<LabManifestOrder> getLabManifestOrderByNotFoundInLabSystem(Integer... ordersList);
    List<LabManifestOrder> getLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate);
    LabManifest getFirstLabManifestByOrderStatusCheckedBeforeDate(String status, Date lastStatusCheckDate);
    Long countLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate);
    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status);
    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String ... status);
    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String ... status);
    Long countLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String ... status);
    List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifestOrder);
    Long countLabManifestOrdersToSend(LabManifest labManifestOrder);
    Cohort getPatientsWithCadre(boolean includeTroupes, boolean includeCivilians);

    Integer countTotalSamples(LabManifest labManifest);
    Integer countSamplesSuppressed(LabManifest labManifest);
    Integer countSamplesUnsuppressed(LabManifest labManifest);
    Integer countSamplesRejected(LabManifest labManifest);

    // Start cached data for summary form
    Long countTotalDraftManifests();
    Long countTotalManifestsOnHold();
    Long countTotalReadyToSendManifests();
    Long countTotalManifestsOnSending();
    Long countTotalSubmittedManifests();
    Long countTotalIncompleteManifests();
    Long countTotalManifestsIncompleteWithErrors();
    Long countTotalErrorsOnIncompleteManifests();
    Long countTotalCompleteManifests();
    Long countTotalManifestsCompleteWithErrors();
    Long countTotalErrorsOnCompleteManifests();

    //Patient contact dimensions service methods

    Cohort getPatientContactWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender);
    Cohort getPatientContactWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate);

    //End of Patient contact dimensions service methods
}
