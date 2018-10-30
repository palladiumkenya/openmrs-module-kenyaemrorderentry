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
package org.openmrs.module.kenyaemrorderentry.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.kenyaemrorderentry.api.DrugRegimenHistory;
import org.openmrs.module.kenyaemrorderentry.api.DrugRegimenHistoryService;
import org.openmrs.module.kenyaemrorderentry.api.db.hibernate.HibernateDrugRegimenHistoryDAO;

import java.util.List;
/**
 * It is a default implementation of {@link DrugRegimenHistoryService}.
 */


public class DrugRegimenHistoryServiceImpl extends BaseOpenmrsService implements DrugRegimenHistoryService {

    protected final Log log = LogFactory.getLog(this.getClass());

    private HibernateDrugRegimenHistoryDAO drugRegimenHistoryDAO;



    @Override
    public List<DrugRegimenHistory> getPatientCurrentRegimenByPatient(Patient patient) {
        return drugRegimenHistoryDAO.getPatientCurrentRegimenByPatient(patient);
    }
    @Override
    public DrugRegimenHistory saveDrugRegimenHistory(DrugRegimenHistory drugRegimenHistory) {
        return drugRegimenHistoryDAO.saveDrugRegimenHistory(drugRegimenHistory);
    }

    public void setDrugRegimenHistoryDAO(HibernateDrugRegimenHistoryDAO patientCurrentRegimenDAO) {
        this.drugRegimenHistoryDAO = patientCurrentRegimenDAO;
    }


}
