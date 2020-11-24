package org.openmrs.module.kenyaemrorderentry.api.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrorderentry.api.db.hibernate.HibernateKenyaemrOrdersDAO;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;

import java.util.Date;
import java.util.List;

public class KenyaemrOrdersServiceImpl extends BaseOpenmrsService implements KenyaemrOrdersService {
    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernateKenyaemrOrdersDAO dao;

    @Override
    public LabManifest saveLabOrderManifest(LabManifest labManifest) {
        return dao.saveLabOrderManifest(labManifest);
    }

    @Override
    public List<LabManifest> getLabOrderManifest() {
        return dao.getLabOrderManifest();
    }

    @Override
    public LabManifest getLabOrderManifestById(Integer id) {
        return dao.getLabOrderManifestById(id);
    }

    @Override
    public LabManifest getLabOrderManifestByStatus(String status) {
        return dao.getLabOrderManifestByStatus(status);
    }

    @Override
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
    public List<LabManifestOrder> getLabManifestOrders() {
        return dao.getLabManifestOrders();
    }

    @Override
    public LabManifestOrder getLabManifestOrderById(Integer id) {
        return dao.getLabManifestOrderById(id);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifest) {
        return dao.getLabManifestOrderByManifest(labManifest);
    }

    @Override
    public void voidLabManifestOrder(Integer id) {
        dao.voidLabManifestOrder(id);
    }

    @Override
    public LabManifestOrder getLabManifestOrderByOrderId(Order specimenId) {
        return dao.getLabManifestOrderByManifest(specimenId);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByStatus(String status) {
        return dao.getLabManifestOrderByStatus(status);
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status) {
        return dao.getLabManifestOrderByManifestAndStatus(labManifestOrder, status);
    }

    public HibernateKenyaemrOrdersDAO getDao() {
        return dao;
    }

    public void setDao(HibernateKenyaemrOrdersDAO dao) {
        this.dao = dao;
    }

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }
}
