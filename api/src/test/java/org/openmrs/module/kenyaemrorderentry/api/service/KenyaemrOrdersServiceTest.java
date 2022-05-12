package org.openmrs.module.kenyaemrorderentry.api.service;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import static org.junit.Assert.*;

public class KenyaemrOrdersServiceTest extends BaseModuleContextSensitiveTest {

    @Test
    public void shouldSetupContext() {
        assertNotNull(Context.getService(KenyaemrOrdersService.class));
    }
}