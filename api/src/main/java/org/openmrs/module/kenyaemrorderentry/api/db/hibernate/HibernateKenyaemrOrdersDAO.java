package org.openmrs.module.kenyaemrorderentry.api.db.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.DataException;
import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.kenyaemrorderentry.api.db.KenyaemrOrdersDAO;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.reporting.common.DurationUnit;

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
        return criteria.list();
    }

    @Override
    public LabManifest getLabOrderManifestById(Integer id) throws DataException {
        return (LabManifest) this.sessionFactory.getCurrentSession().get(LabManifest.class, id);
    }

    @Override
    public LabManifest getLabOrderManifestByManifestType(Integer manifestType) throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("manifestType", manifestType));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return (LabManifest) criteria.uniqueResult();
    }

    @Override
    public LabManifest getLastLabOrderManifest() throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.setMaxResults(1);
        criteria.addOrder(org.hibernate.criterion.Order.desc("id"));
        return (LabManifest) criteria.uniqueResult();
    }

    @Override
    public List<LabManifest> getLabOrderManifestBetweenDates(Date startDate, Date endDate) throws DataException{
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.ge("startDate",startDate));
        criteria.add(Restrictions.le("endDate",endDate));
        criteria.add(Restrictions.eq("voided", false));

        return criteria.list();
    }

    @Override
    public void voidLabOrderManifest(Integer id) throws DAOException {
        LabManifest mf =  (LabManifest) this.sessionFactory.getCurrentSession().get(LabManifest.class, id);
        mf.setVoided(true);
        sessionFactory.getCurrentSession().saveOrUpdate(mf);
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
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public LabManifestOrder getLabManifestOrderById(Integer id) throws DataException {
        return (LabManifestOrder) this.sessionFactory.getCurrentSession().get(LabManifestOrder.class, id);
    }

    @Override
    public LabManifestOrder getLabManifestOrderByOrderType(Integer orderType) throws DataException {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("orderType", orderType));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return (LabManifestOrder) criteria.uniqueResult();
    }

    @Override
    public void voidLabManifestOrder(Integer labManifestOrder) throws DAOException{
        LabManifestOrder mfo =  (LabManifestOrder) this.sessionFactory.getCurrentSession().get(LabManifestOrder.class, labManifestOrder);
        if (mfo != null) {
            sessionFactory.getCurrentSession().delete(mfo);
        }
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
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, String... status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.in("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public LabManifest getLabOrderManifestByStatus(String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.add(Restrictions.eq("voided", false));
        // return the earliest - the first in the list
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
    public List<LabManifestOrder> getLabManifestOrdersToSend(LabManifest labManifest) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifest));
        criteria.add(Restrictions.eq("status", "Pending"));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.add(Restrictions.eq("voided", false));
        criteria.setMaxResults(50);
        return criteria.list();
    }

    @Override
    public Long countLabManifestOrdersToSend(LabManifest labManifest) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("labManifest", labManifest));
        criteria.add(Restrictions.eq("status", "Pending"));
        criteria.add(Restrictions.eq("voided", false));
        criteria.setProjection(Projections.rowCount());
        List rowCount = criteria.list();
        Long count = (Long) rowCount.get(0);
        return(count);
    }

    //Patient contact dimensions methods
    @Override
    public Cohort getPatientContactWithGender(boolean includeMales, boolean includeFemales, boolean includeUnknownGender) {

        if (!includeMales && !includeFemales && !includeUnknownGender) {
            return new Cohort();
        }

        String prefixTerm = "";
        StringBuilder query = new StringBuilder("select c.id from kenyaemr_hiv_testing_patient_contact c where c.voided = 0 and ( ");
        if (includeMales) {
            query.append(" c.sex = 'M' ");
            prefixTerm = " or";
        }
        if (includeFemales) {
            query.append(prefixTerm + " c.sex = 'F'");
            prefixTerm = " or";
        }
        if (includeUnknownGender) {
            query.append(prefixTerm + " c.sex is null or (c.sex != 'M' and c.sex != 'F')");
        }
        query.append(")");
        Query q = sessionFactory.getCurrentSession().createSQLQuery(query.toString());
        q.setCacheMode(CacheMode.IGNORE);
        return new Cohort(q.list());
    }

    @Override
    public Cohort getPatientContactWithAgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, boolean unknownAgeIncluded, Date effectiveDate) {

        if (effectiveDate == null) {
            effectiveDate = new Date();
        }
        if (minAgeUnit == null) {
            minAgeUnit = DurationUnit.YEARS;
        }
        if (maxAgeUnit == null) {
            maxAgeUnit = DurationUnit.YEARS;
        }

        String sql = "select c.id from kenyaemr_hiv_testing_patient_contact c where c.voided = 0 and ";
        Map<String, Date> paramsToSet = new HashMap<String, Date>();

        Date maxBirthFromAge = effectiveDate;
        if (minAge != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(effectiveDate);
            cal.add(minAgeUnit.getCalendarField(), -minAgeUnit.getFieldQuantity()*minAge);
            maxBirthFromAge = cal.getTime();
        }

        String c = "c.birth_date <= :maxBirthFromAge";
        paramsToSet.put("maxBirthFromAge", maxBirthFromAge);

        Date minBirthFromAge = null;
        if (maxAge != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(effectiveDate);
            cal.add(maxAgeUnit.getCalendarField(), -(maxAgeUnit.getFieldQuantity()*maxAge + 1));
            minBirthFromAge = cal.getTime();
            c = "(" + c + " and c.birth_date >= :minBirthFromAge)";
            paramsToSet.put("minBirthFromAge", minBirthFromAge);
        }

        if (unknownAgeIncluded) {
            c = "(c.birth_date is null or " + c + ")";
        }

        sql += c;

        log.debug("Executing: " + sql + " with params: " + paramsToSet);

        Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
        for (Map.Entry<String, Date> entry : paramsToSet.entrySet()) {
            query.setDate(entry.getKey(), entry.getValue());
        }

        return new Cohort(query.list());
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.or(Restrictions.isNull("lastStatusCheckDate"), Restrictions.le("lastStatusCheckDate", lastStatusCheckDate)));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.setMaxResults(50);
        return criteria.list();
    }

    @Override
    public Long countLabManifestOrderByStatusBeforeDate(String status, Date lastStatusCheckDate) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.or(Restrictions.isNull("lastStatusCheckDate"), Restrictions.le("lastStatusCheckDate", lastStatusCheckDate)));
        criteria.setProjection(Projections.rowCount());
        List rowCount = criteria.list();
        Long count = (Long) rowCount.get(0);
        return(count);
    }

    @Override
    public LabManifest getLabOrderManifestByStatus(String status, Date onOrBefore) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.le("dispatchDate", onOrBefore));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.add(Restrictions.eq("voided", false));
        // return the earliest - the first in the list
        if (criteria.list().size() > 0) {
            return (LabManifest) criteria.list().get(0);
        }
        return null;
    }

    @Override
    public List<LabManifest> getLabOrderManifest(String status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifest.class);
        criteria.add(Restrictions.eq("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByNotFoundInLabSystem(Integer... ordersList) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.in("id", ordersList));
        criteria.add(Restrictions.eq("status", "Sent")); // samples marked as sent in the manifest but are not part of the result from lab
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        return criteria.list();
    }

    @Override
    public List<LabManifestOrder> getLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String... status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.le("lastStatusCheckDate", updatedBefore));
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.in("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(org.hibernate.criterion.Order.asc("id"));
        criteria.setMaxResults(50);

        return criteria.list();
    }

    @Override
    public Long countLabManifestOrderByManifestAndStatus(LabManifest labManifestOrder, Date updatedBefore, String... status) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(LabManifestOrder.class);
        criteria.add(Restrictions.le("lastStatusCheckDate", updatedBefore));
        criteria.add(Restrictions.eq("labManifest", labManifestOrder));
        criteria.add(Restrictions.in("status", status));
        criteria.add(Restrictions.eq("voided", false));
        criteria.setProjection(Projections.rowCount());
        List rowCount = criteria.list();
        Long count = (Long) rowCount.get(0);
        return(count);
    }

}
