/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.orderentryui.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.orderentryui.api.PatientCurrentRegimen;
import org.openmrs.module.orderentryui.api.PatientCurrentRegimenService;
import org.openmrs.module.orderentryui.api.db.hibernate.HibernatePatientCurrentRegimenDAO;

import java.util.List;
/**
 * It is a default implementation of {@link PatientCurrentRegimenService}.
 */


public class PatientCurrentRegimenServiceImpl extends BaseOpenmrsService implements PatientCurrentRegimenService {

    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernatePatientCurrentRegimenDAO patientCurrentRegimenDAO;



    @Override
    public List<PatientCurrentRegimen> getPatientCurrentRegimenByPatient(Patient patient) {
        return patientCurrentRegimenDAO.getPatientCurrentRegimenByPatient(patient);
    }

    public void setPatientContactDAO(HibernatePatientCurrentRegimenDAO patientCurrentRegimenDAO) {
        this.patientCurrentRegimenDAO = patientCurrentRegimenDAO;
    }

    public HibernatePatientCurrentRegimenDAO getPatientContactDAO() {
        return patientCurrentRegimenDAO;
    }

    public HibernatePatientCurrentRegimenDAO getDao() {
        return patientCurrentRegimenDAO;
    }




}
