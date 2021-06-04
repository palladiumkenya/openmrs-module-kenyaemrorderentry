package org.openmrs.module.kenyaemrorderentry.api.service;

import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface KenyaemrOrdersService extends OpenmrsService {
    LabManifest saveLabOrderManifest(LabManifest labManifest);
    List<LabManifest> getLabOrderManifest();
    List<LabManifest> getLabOrderManifest(String status);
    LabManifest getLabOrderManifestById(Integer id);
    LabManifest getLabOrderManifestByStatus(String status);
    LabManifest getLabOrderManifestByStatus(String status, Date onOrBefore);
    List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate);
    void voidLabOrderManifest(Integer id);

    //Methods for manifest orders
    LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder);
    List<LabManifestOrder> getLabManifestOrders();
    LabManifestOrder getLabManifestOrderById(Integer id);
    List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifestOrder);
    void voidLabManifestOrder(Integer manifestOrder);

    LabManifestOrder getLabManifestOrderByOrderId(Order specimenId);
    List<LabManifestOrder> getLabManifestOrderByStatus(String status);
    List<LabManifestOrder> getLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate);
    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status);
    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String ... status);
    List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifestOrder);
    Cohort getPatientsWithCadre(boolean includeTroupes, boolean includeCivilians);

    //Patient contact dimensions service methods

    public Cohort getPatientContactWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender);
    public Cohort getPatientContactWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate);

    //End of Patient contact dimensions service methods

}
