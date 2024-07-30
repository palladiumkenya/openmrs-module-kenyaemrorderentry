package org.openmrs.module.kenyaemrorderentry.api.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrorderentry.api.db.hibernate.HibernateKenyaemrOrdersDAO;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class KenyaemrOrdersServiceImpl extends BaseOpenmrsService implements KenyaemrOrdersService {
    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernateKenyaemrOrdersDAO dao;

    @Override
    public LabManifest saveLabOrderManifest(LabManifest labManifest) {
        CacheManager cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);

        try {
            Integer manifestId = labManifest.getId();
            if(manifestId != null) {
                String curLabManifest = getLabManifestStatusByIdSQL(manifestId);
                if(curLabManifest != null) {
                    // check if status has changed
                    String oldStatus = curLabManifest;
                    String updatedStatus = labManifest.getStatus().trim();
                    if(!updatedStatus.equalsIgnoreCase(oldStatus)) {
                        // Status has changed
                        if(oldStatus != null) {
                            if(oldStatus.equalsIgnoreCase("Draft")) {
                                cacheManager.getCache("countTotalDraftManifests").clear();
                            } else if(oldStatus.equalsIgnoreCase("On Hold")) {
                                cacheManager.getCache("countTotalManifestsOnHold").clear();
                            } else if(oldStatus.equalsIgnoreCase("Ready to send")) {
                                cacheManager.getCache("countTotalReadyToSendManifests").clear();
                            } else if(oldStatus.equalsIgnoreCase("Sending")) {
                                cacheManager.getCache("countTotalManifestsOnSending").clear();
                            } else if(oldStatus.equalsIgnoreCase("Submitted")) {
                                cacheManager.getCache("countTotalSubmittedManifests").clear();
                            } else if(oldStatus.equalsIgnoreCase("Incomplete")) {
                                cacheManager.getCache("countTotalIncompleteManifests").clear();
                            } else if(oldStatus.equalsIgnoreCase("Complete")) {
                                cacheManager.getCache("countTotalCompleteManifests").clear();
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {}
        
        try {
            String newStatus = labManifest.getStatus();
            if(newStatus != null) {
                newStatus = newStatus.trim();
                if(newStatus.equalsIgnoreCase("Draft")) {
                    cacheManager.getCache("countTotalDraftManifests").clear();
                } else if(newStatus.equalsIgnoreCase("On Hold")) {
                    cacheManager.getCache("countTotalManifestsOnHold").clear();
                } else if(newStatus.equalsIgnoreCase("Ready to send")) {
                    cacheManager.getCache("countTotalReadyToSendManifests").clear();
                } else if(newStatus.equalsIgnoreCase("Sending")) {
                    cacheManager.getCache("countTotalManifestsOnSending").clear();
                } else if(newStatus.equalsIgnoreCase("Submitted")) {
                    cacheManager.getCache("countTotalSubmittedManifests").clear();
                } else if(newStatus.equalsIgnoreCase("Incomplete")) {
                    cacheManager.getCache("countTotalIncompleteManifests").clear();
                } else if(newStatus.equalsIgnoreCase("Complete")) {
                    cacheManager.getCache("countTotalCompleteManifests").clear();
                }
            }
        } catch(Exception ex) {}

        // Save or update manifest
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
    public LabManifest getLabManifestById(Integer manId) {
        return dao.getLabManifestById(manId);
    }

    @Override
    @Transactional(readOnly = true)
    public LabManifest getLabManifestByUUID(String manUUID) {
        return dao.getLabManifestByUUID(manUUID);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLabManifestStatusByIdSQL(Integer manID) {
        return dao.getLabManifestStatusByIdSQL(manID);
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
        // Clear the summary graph cache
        try {
            CacheManager cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
            cacheManager.getCache("getSummaryGraph").clear();
        } catch(Exception ex) {}
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
    public LabManifestOrder getLabManifestOrderByUUID(String UUID) {
        return dao.getLabManifestOrderByUUID(UUID);
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
    public List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifest) {
        return dao.getLabManifestOrdersToSend(labManifest);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countLabManifestOrdersToSend(LabManifest labManifest) {
        return dao.countLabManifestOrdersToSend(labManifest);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countTotalSamples(LabManifest labManifest) {
        return dao.countTotalSamples(labManifest);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countSamplesSuppressed(LabManifest labManifest) {
        return dao.countSamplesSuppressed(labManifest);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countSamplesUnsuppressed(LabManifest labManifest) {
        return dao.countSamplesUnsuppressed(labManifest);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countSamplesRejected(LabManifest labManifest) {
        return dao.countSamplesRejected(labManifest);
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

    @Override
    @Transactional(readOnly = false)
    public void reprocessLabManifest(Integer manifestId) {
        dao.reprocessLabManifest(manifestId);
    }

    // Start cached methods for the summary page

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalDraftManifests")
    public Long countTotalDraftManifests() {
        Long ret = 0L;
        ret = dao.countTotalDraftManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalManifestsOnHold")
    public Long countTotalManifestsOnHold() {
        Long ret = 0L;
        ret = dao.countTotalManifestsOnHold();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalReadyToSendManifests")
    public Long countTotalReadyToSendManifests() {
        Long ret = 0L;
        ret = dao.countTotalReadyToSendManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalManifestsOnSending")
    public Long countTotalManifestsOnSending() {
        Long ret = 0L;
        ret = dao.countTotalManifestsOnSending();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalSubmittedManifests")
    public Long countTotalSubmittedManifests() {
        Long ret = 0L;
        ret = dao.countTotalSubmittedManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalIncompleteManifests")
    public Long countTotalIncompleteManifests() {
        Long ret = 0L;
        ret = dao.countTotalIncompleteManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalManifestsIncompleteWithErrors")
    public Long countTotalManifestsIncompleteWithErrors() {
        Long ret = 0L;
        ret = dao.countTotalManifestsIncompleteWithErrors();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalErrorsOnIncompleteManifests")
    public Long countTotalErrorsOnIncompleteManifests() {
        Long ret = 0L;
        ret = dao.countTotalErrorsOnIncompleteManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalCompleteManifests")
    public Long countTotalCompleteManifests() {
        Long ret = 0L;
        ret = dao.countTotalCompleteManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalManifestsCompleteWithErrors")
    public Long countTotalManifestsCompleteWithErrors() {
        Long ret = 0L;
        ret = dao.countTotalManifestsCompleteWithErrors();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "countTotalErrorsOnCompleteManifests")
    public Long countTotalErrorsOnCompleteManifests() {
        Long ret = 0L;
        ret = dao.countTotalErrorsOnCompleteManifests();
        return(ret);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "getSummaryGraph")
    public List<SimpleObject> getLabManifestSummaryGraphSQL() {
        return dao.getLabManifestSummaryGraphSQL();
    }

	@Override
	public List<LabManifest> getLabManifests(String uuid, String status, String type, Date createdOnOrAfterDate, Date createdOnOrBeforeDate) {
        return dao.getLabManifests(uuid, status, type, createdOnOrAfterDate, createdOnOrBeforeDate);
	}

}
