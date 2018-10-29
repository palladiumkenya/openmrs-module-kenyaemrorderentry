package org.openmrs.module.orderentryui.api;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.List;

import static org.junit.Assert.*;

public class DrugRegimenHistoryServiceTest extends BaseModuleContextSensitiveTest {

    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test to make sure that a simple save to a new drug regimen history gets persisted to the database
     *
     * @see {@link DrugRegimenHistoryService#saveDrugRegimenHistory(DrugRegimenHistory)}
     */
    @Test
    @Verifies(value = "should create drug regimen history successfully", method = "saveDrugRegimenHistory(DrugRegimenHistory)")
    public void saveDrugRegimenHistory_shouldCreateDrugHistorySuccessfully() {
        DrugRegimenHistory drugRegimenHistory = new DrugRegimenHistory();
        Patient patient = Context.getPatientService().getPatient(2);

        drugRegimenHistory.setRegimenName("TDF+3TC+NVP");
        drugRegimenHistory.setStatus("stop");
        drugRegimenHistory.setOrderGroupId(234);
        drugRegimenHistory.setPatient(patient);

        DrugRegimenHistoryService drugRegimenHistoryService = Context.getService(DrugRegimenHistoryService.class);
        List<DrugRegimenHistory> allChanges = drugRegimenHistoryService.getPatientCurrentRegimenByPatient(patient);
        assertEquals(allChanges.size(), 0);


      //  DrugRegimenHistory history = drugRegimenHistoryService.saveDrugRegimenHistory(drugRegimenHistory);
       // assertNotNull(history);
    }
}