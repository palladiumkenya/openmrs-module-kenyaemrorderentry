package org.openmrs.module.kenyaemrorderentry.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.DataException;
import org.openmrs.Order;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.kenyaemrorderentry.api.db.KenyaemrOrdersDAO;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;

import java.util.Date;
import java.util.List;

public class HibernateKenyaemrOrdersDAO implements KenyaemrOrdersDAO {
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
    public LabManifest saveLabOrderManifest(LabManifest labManifest) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(labManifest);
        return labManifest;
    }

    @Override
    public List<LabManifest> getLabOrderManifest() throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.eq("voided", false));
        return criteria.list();
    }

    @Override
    public LabManifest getLabOrderManifestById(Integer id) throws DataException {
        return (LabManifest) this.sessionFactory.getCurrentSession().get(LabManifest.class, id);
    }

    @Override
    public List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate) throws DataException{
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.ge("startDate",startDate));
        criteria.add(Restrictions.le("endDate",endDate));
        return criteria.list();
    }

    @Override
    public void voidLabOrderManifest(Integer id) throws DAOException{
        sessionFactory.getCurrentSession().saveOrUpdate(id);
    }

    //Lab minifest orders methods
    @Override
    public LabManifestOrder saveLabManifestOrder(LabManifestOrder labManifestOrder) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(labManifestOrder);
        return labManifestOrder;
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrders() throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("voided", false));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifest(LabManifest labManifest) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifest));
        return criteria.list();
    }

    @Override
    public LabManifestOrder getLabManifestOrderById(Integer id) throws DataException {
        return (LabManifestOrder) this.sessionFactory.getCurrentSession().get(LabManifestOrder.class, id);
    }

    @Override
    public void voidLabManifestOrder(Integer id) throws DAOException{
        sessionFactory.getCurrentSession().saveOrUpdate(id);
    }

    @Override
    public LabManifestOrder getLabManifestOrderByManifest(Order specimenId) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("order", specimenId));
        if (criteria.list().size() > 0) {
            return (LabManifestOrder) criteria.list().get(0);
        }
        return null;
    }
}
