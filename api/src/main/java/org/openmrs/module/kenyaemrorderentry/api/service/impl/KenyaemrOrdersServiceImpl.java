package org.openmrs.module.kenyaemrorderentry.api.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrorderentry.api.db.hibernate.HibernateKenyaemrOrdersDAO;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class KenyaemrOrdersServiceImpl extends BaseOpenmrsService implements KenyaemrOrdersService {
    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernateKenyaemrOrdersDAO dao;

    @Override
    public LabManifest saveLabOrderManifest(LabManifest labManifest) {
        return dao.saveLabOrderManifest(labManifest);
    }

    @Override
    public Long getLastManifestID() {
        return dao.getLastManifestID();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifest> getLabOrderManifest() {
        return dao.getLabOrderManifest();
    }

    @Override
    public List<LabManifest> getLabOrderManifest(String status) {
        return dao.getLabOrderManifest(status);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifest getLabOrderManifestById(Integer id) {
        return dao.getLabOrderManifestById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifest getLabOrderManifestByManifestType(Integer manifestType) {
        return dao.getLabOrderManifestByManifestType(manifestType);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifest getLastLabOrderManifest() {
        return dao.getLastLabOrderManifest();
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifest getLabOrderManifestByStatus(String status) {
        return dao.getLabOrderManifestByStatus(status);
    }

    @Override
    public LabManifest getLabOrderManifestByStatus(String status, Date onOrBefore) {
        return dao.getLabOrderManifestByStatus(status, onOrBefore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate) {
        return dao.getLabOrderManifestBetweenDates(startDate,endDate);
    }

    @Override
    public void voidLabOrderManifest(Integer id) {
        dao.voidLabOrderManifest(id);
    }


    //Methods for lab manifest orders
    @Override
    public LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder) {
        return dao.saveLabManifestOrder(labManifestOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifestOrder> getLabManifestOrders() {
        return dao.getLabManifestOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifestOrder getLabManifestOrderById(Integer id) {
        return dao.getLabManifestOrderById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifestOrder getLabManifestOrderByOrderType(Integer orderType) {
        return dao.getLabManifestOrderByOrderType(orderType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifest) {
        return dao.getLabManifestOrderByManifest(labManifest);
    }

    @Override
    public void voidLabManifestOrder(Integer labManifestOrder) {
        dao.voidLabManifestOrder(labManifestOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifestOrder getLabManifestOrderByOrderId(Order specimenId) {
        return dao.getLabManifestOrderByOrderId(specimenId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifestOrder> getLabManifestOrderByStatus(String status) {
        return dao.getLabManifestOrderByStatus(status);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByNotFoundInLabSystem(Integer... ordersList) {
        return dao.getLabManifestOrderByNotFoundInLabSystem(ordersList);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate) {
        return dao.getLabManifestOrderByStatusBeforeDate(status, lastStatusCheckDate);
    }

    @Override
    public LabManifest getFirstLabManifestByOrderStatusCheckedBeforeDate(String status, Date lastStatusCheckDate) {
        return dao.getFirstLabManifestByOrderStatusCheckedBeforeDate(status, lastStatusCheckDate);
    }

    @Override
    public Long countLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate) {
        return dao.countLabManifestOrderByStatusBeforeDate(status, lastStatusCheckDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status) {
        return dao.getLabManifestOrderByManifestAndStatus(labManifestOrder, status);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String... status) {
        return dao.getLabManifestOrderByManifestAndStatus(labManifestOrder, status);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String... status) {
        return dao.getLabManifestOrderByManifestAndStatus(labManifestOrder, updatedBefore, status);
    }

    @Override
    public Long countLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String... status) {
        return dao.countLabManifestOrderByManifestAndStatus(labManifestOrder, updatedBefore, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Cohort getPatientsWithCadre(boolean includeTroupes, boolean includeCivilians) {
        return dao.getPatientsWithCadre(includeTroupes, includeCivilians);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifestOrder) {
        return dao.getLabManifestOrdersToSend(labManifestOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countLabManifestOrdersToSend(LabManifest labManifestOrder) {
        return dao.countLabManifestOrdersToSend(labManifestOrder);
    }

    public HibernateKenyaemrOrdersDAO getDao() {
        return dao;
    }

    public void setDao(HibernateKenyaemrOrdersDAO dao) {
        this.dao = dao;
    }

    //Patient contact dimensions service methods
    @Override
    public Cohort getPatientContactWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender) {
        return dao.getPatientContactWithGender(includeMales, includeFemales, includeUnknownGender);
    }

    @Override
    public Cohort getPatientContactWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate) {
        return dao.getPatientContactWithAgeRange(minAge, minAgeUnit, maxAge, maxAgeUnit, unknownAgeIncluded, effectiveDate);
    }
    //End of Patient contact dimensions service methods

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }
}
