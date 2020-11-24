package org.openmrs.module.kenyaemrorderentry.api.db;

import org.openmrs.Order;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;

import java.util.Date;
import java.util.List;

public interface KenyaemrOrdersDAO {
    LabManifest saveLabOrderManifest(LabManifest labManifest);
    List<LabManifest> getLabOrderManifest();
    LabManifest getLabOrderManifestById(Integer id);
    List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate);
    void voidLabOrderManifest(Integer id);

    //Lab manifest order methods
    LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder);
    List<LabManifestOrder> getLabManifestOrders();
    LabManifestOrder getLabManifestOrderById(Integer id);
    List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifestOrder);
    void voidLabManifestOrder(Integer id);

    LabManifestOrder getLabManifestOrderByManifest(Order specimenId);

    List<LabManifestOrder> getLabManifestOrderByStatus(String status);

    List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status);

    LabManifest getLabOrderManifestByStatus(String status);
}
