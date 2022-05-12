/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.reporting.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

@Caching(strategy= ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.CadreCohortDefinition")
public class CadreCohortDefinition extends BaseCohortDefinition {

    public static final long serialVersionUID = 1L;

	//***** PROPERTIES *****

	@ConfigurationProperty(group="cadres")
	private Boolean troupeIncluded = Boolean.FALSE;

	@ConfigurationProperty(group="cadres")
	private Boolean civilianIncluded = Boolean.FALSE;

	//***** CONSTRUCTORS *****

	/**
	 * Default constructor
	 */
	public CadreCohortDefinition() {
		super();
	}

	//***** INSTANCE METHODS *****

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();		
		if (isTroupeIncluded() == Boolean.TRUE) {
			buffer.append("Troupe");
		}
		if (isCivilianIncluded() == Boolean.TRUE) {
			buffer.append((buffer.length() > 0 ? "," : "") + "Civilian");
		}
		if (buffer.length() == 0) {
			return "No Patients";
		}
		return buffer.toString();
	}

	//***** PROPERTY ACCESS *****

	/**
	 * @return the troupeIncluded
	 */
	public Boolean isTroupeIncluded() {
		return troupeIncluded;
	}

	/**
	 * @return the civilianIncluded
	 */
	public Boolean isCivilianIncluded() {
		return civilianIncluded;
	}

	public Boolean getTroupeIncluded() {
		return troupeIncluded;
	}

	public void setTroupeIncluded(Boolean troupeIncluded) {
		this.troupeIncluded = troupeIncluded;
	}

	public Boolean getCivilianIncluded() {
		return civilianIncluded;
	}

	public void setCivilianIncluded(Boolean civilianIncluded) {
		this.civilianIncluded = civilianIncluded;
	}
}
