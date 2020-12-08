package org.openmrs.module.kenyaemrorderentry.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.DataException;
import org.openmrs.Cohort;
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
        criteria.addOrder(org.hibernate.criterion.Order.desc("id"));
        criteria.setMaxResults(25);
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

    @Override
    public List<LabManifestOrder> getLabManifestOrderByStatus(String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("status", status));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.eq("status", status));
        return criteria.list();
    }

    @Override
    public LabManifest getLabOrderManifestByStatus(String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        if (criteria.list().size() > 0) {
            return (LabManifest) criteria.list().get(0);
        }
        return null;
    }

    public Cohort getPatientsWithCadre(boolean includeTroupes, boolean includeCivilians) {

        if (!includeTroupes && !includeCivilians) {
            return new Cohort();
        }

        String prefixTerm = "";
        StringBuilder query = new StringBuilder("select p.personId from Person p,PersonAttribute pa where p.personId = pa.person.personId and p.voided = false and ( ");
        if (includeTroupes) {
            query.append(" pa.value  = 'Troupe' ");
            prefixTerm = " or";
        }
        if (includeCivilians) {
            query.append(prefixTerm + " pa.value = 'Civilian'");
        }

        query.append(")");
        Query q = sessionFactory.getCurrentSession().createQuery(query.toString());
        q.setCacheMode(CacheMode.IGNORE);
        return new Cohort(q.list());
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifestOrder) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.eq("status", "Pending"));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.setMaxResults(30);
        return criteria.list();
    }
}
