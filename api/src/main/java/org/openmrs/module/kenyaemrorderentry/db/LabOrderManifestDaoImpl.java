package org.openmrs.module.kenyaemrorderentry.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.DataException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.kenyaemrorderentry.LabOrderManifest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by developer on 09 Nov, 2020
 */
public class LabOrderManifestDaoImpl implements LabOrderManifestDao {

    protected final Log log = LogFactory.getLog(this.getClass());

    private SessionFactory sessionFactory;
    /**
     * @Autowired private LabOrderManifestDao labOrderManifestDao;
     */
    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public LabOrderManifest saveLabOrderManifest(LabOrderManifest labOrderManifest) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(labOrderManifest);
        return labOrderManifest;
    }

    @Override
    public List<LabOrderManifest> getLabOrderManifest() throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabOrderManifest.class);
        criteria.add(Restrictions.eq("voided", false));
        return criteria.list();
    }

    @Override
    public LabOrderManifest getLabOrderManifestById(Integer id) throws DataException {
        return (LabOrderManifest) this.sessionFactory.getCurrentSession().get(LabOrderManifest.class, id);
    }

    @Override
    public List<LabOrderManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate) throws DataException{
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabOrderManifest.class);
        criteria.add(Restrictions.ge("startDate",startDate));
        criteria.add(Restrictions.le("endDate",endDate));
        return criteria.list();
    }

    @Override
    public void voidLabOrderManifest(Integer id) throws DAOException{
        sessionFactory.getCurrentSession().saveOrUpdate(id);
    }
}
