package org.openmrs.module.kenyaemrorderentry.api.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public interface KenyaemrOrdersService extends OpenmrsService {
    LabManifest saveLabOrderManifest(LabManifest labManifest);
    List<LabManifest> getLabOrderManifest();
    LabManifest getLabOrderManifestById(Integer id);
    List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate);
    void voidLabOrderManifest(Integer id);

    //Methods for manifest orders
    LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder);
    List<LabManifestOrder> getLabManifestOrders();
    LabManifestOrder getLabManifestOrderById(Integer id);
    List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifestOrder);
    void voidLabManifestOrder(Integer id);
}
