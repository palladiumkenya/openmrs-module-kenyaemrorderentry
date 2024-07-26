package org.openmrs.module.kenyaemrorderentry.api.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.util.OpenmrsUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * A search template class for the {@link Bill} model.
 */
public class ManifestSearch extends BaseDataTemplateSearch<LabManifest> {
	private Date createdOnOrBefore;
	private Date createdOnOrAfter;
	public ManifestSearch() {
		this(new LabManifest(), false);
	}

	public ManifestSearch(LabManifest template) {
		this(template, false);
	}

	public ManifestSearch(LabManifest template, Boolean includeRetired) {
		super(template, includeRetired);
	}

	public ManifestSearch(LabManifest template, Date createdOnOrAfter, Date createdOnOrBefore, Boolean includeRetired) {
		super(template, includeRetired);
		this.createdOnOrAfter = createdOnOrAfter;
		this.createdOnOrBefore = createdOnOrBefore;
	}

	@Override
	public void updateCriteria(Criteria criteria) {
		super.updateCriteria(criteria);

		LabManifest labManifest = getTemplate();
		if (labManifest.getUuid() != null) {
			criteria.add(Restrictions.eq("uuid", labManifest.getUuid()));
		}
		if (labManifest.getStatus() != null) {
			criteria.add(Restrictions.eq("status", labManifest.getStatus()));
		}
		if (labManifest.getManifestType() != null) {
			criteria.add(Restrictions.eq("manifest_type", labManifest.getManifestType()));
		}

		if (getCreatedOnOrBefore() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(getCreatedOnOrBefore());
			criteria.add(Restrictions.le("dateCreated", OpenmrsUtil.getLastMomentOfDay(cal.getTime())));
		}
		if (getCreatedOnOrAfter() != null) {
			// set the date's time to 00:00:00.000
			Calendar cal = Calendar.getInstance();
			cal.setTime(getCreatedOnOrAfter());
			criteria.add(Restrictions.ge("dateCreated", OpenmrsUtil.firstSecondOfDay(cal.getTime())));
		}
	}

	public Date getCreatedOnOrBefore() {
		return createdOnOrBefore;
	}

	public void setCreatedOnOrBefore(Date createdOnOrBefore) {
		this.createdOnOrBefore = createdOnOrBefore;
	}

	public Date getCreatedOnOrAfter() {
		return createdOnOrAfter;
	}

	public void setCreatedOnOrAfter(Date createdOnOrAfter) {
		this.createdOnOrAfter = createdOnOrAfter;
	}
}
