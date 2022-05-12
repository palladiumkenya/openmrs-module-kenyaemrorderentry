/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemrorderentry.reporting.cohort.definition.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.reporting.cohort.definition.CadreCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;

/**
 * Evaluates an PatientCharacteristicCohortDefinition and produces a Cohort
 */
@Handler(supports={CadreCohortDefinition.class})
public class CadreCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

	/**
	 * Default Constructor
	 */
	public CadreCohortDefinitionEvaluator() {}
	
	/**
     * @see CohortDefinitionEvaluator#evaluate(CohortDefinition, EvaluationContext)
     * 
     * @should return all non voided patients when all are included
     * @should return Troupe patients when Troupes are included
     * @should return Civilian patients when civilians are included
     * @should return no patients when none are included
     */
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
		CadreCohortDefinition cadreCohortDefinition = (CadreCohortDefinition) cohortDefinition;
		KenyaemrOrdersService cqs = Context.getService(KenyaemrOrdersService.class);
    	Cohort c = cqs.getPatientsWithCadre(cadreCohortDefinition.isTroupeIncluded(), cadreCohortDefinition.isCivilianIncluded());
    	return new EvaluatedCohort(c, cohortDefinition, context);
    }
}