package org.openmrs.module.kenyaemrorderentry.api.db;

import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;

import java.util.Date;
import java.util.List;

public interface KenyaemrOrdersDAO {
    LabManifest saveLabOrderManifest(LabManifest labManifest);
    List<LabManifest> getLabOrderManifest();
    LabManifest getLabOrderManifestById(Integer id);
    LabManifest getLabOrderManifestByManifestType(Integer manifestType);
    List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate);
    void voidLabOrderManifest(Integer id);

    //Lab manifest order methods
    LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder);
    List<LabManifestOrder> getLabManifestOrders();
    LabManifestOrder getLabManifestOrderById(Integer id);
    LabManifestOrder getLabManifestOrderByOrderType(Integer orderType);
    List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifestOrder);
    void voidLabManifestOrder(Integer labManifestOrder);

    LabManifestOrder getLabManifestOrderByManifest(Order specimenId);

    List<LabManifestOrder> getLabManifestOrderByStatus(String status);

    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status);

    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String ... status);


    LabManifest getLabOrderManifestByStatus(String status);
    public Cohort getPatientsWithCadre(boolean includeTroupes, boolean includeCivilians);

    List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifestOrder);

    //Patient Contact dimensions methods
    public Cohort getPatientContactWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender);

    public Cohort getPatientContactWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate);

    List<LabManifestOrder> getLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate);

    LabManifest getLabOrderManifestByStatus(String status, Date onOrBefore);

    List<LabManifest> getLabOrderManifest(String status);

    List<LabManifestOrder> getLabManifestOrderByNotFoundInLabSystem(Integer... ordersList);

    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String... status);

    //End of Patient Contact dimensions methods
}
