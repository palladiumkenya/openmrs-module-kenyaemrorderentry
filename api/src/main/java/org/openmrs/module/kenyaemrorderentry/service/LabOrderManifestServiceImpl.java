package org.openmrs.module.kenyaemrorderentry.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.kenyaemrorderentry.LabOrderManifest;
import org.openmrs.module.kenyaemrorderentry.db.LabOrderManifestDaoImpl;

import java.util.Date;
import java.util.List;

/**
 * Created by developer on 09 Nov, 2020
 */
public class LabOrderManifestServiceImpl implements LabOrderManifestService{
    protected final Log log = LogFactory.getLog(this.getClass());

    private LabOrderManifestDaoImpl labOrderManifestDao;

    @Override
    public LabOrderManifest saveLabOrderManifest(LabOrderManifest labOrderManifest) {
        return labOrderManifestDao.saveLabOrderManifest(labOrderManifest);
    }

    @Override
    public List<LabOrderManifest> getLabOrderManifest() {
        return labOrderManifestDao.getLabOrderManifest();
    }

    @Override
    public LabOrderManifest getLabOrderManifestById(Integer id) {
        return labOrderManifestDao.getLabOrderManifestById(id);
    }

    @Override
    public List<LabOrderManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate) {
        return labOrderManifestDao.getLabOrderManifestBetweenDates(startDate,endDate);
    }

    @Override
    public void voidLabOrderManifest(Integer id) {
         labOrderManifestDao.voidLabOrderManifest(id);
    }

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }
}
