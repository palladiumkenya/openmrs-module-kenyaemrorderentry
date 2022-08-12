package org.openmrs.module.kenyaemrorderentry.fragment.controller.patientdashboard;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.CareSetting;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.ChaiSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabwareSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GeneralLabOrdersFragmentController {
    public static final Locale LOCALE = Locale.ENGLISH;
    KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);


    public void controller(FragmentConfiguration config,
                          // @RequestParam("patient") Patient patient,
                           @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                           @SpringBean("encounterService") EncounterService encounterService,
                           @SpringBean("orderService") OrderService orderService,
                           UiSessionContext sessionContext,
                           UiUtils ui,
                           FragmentModel model,
                           @SpringBean("orderSetService") OrderSetService orderSetService,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean("conceptService") ConceptService conceptService,
                           @SpringBean("providerService") ProviderService providerService,
                           @SpringBean("obsService") ObsService obsService) throws Exception {
        config.require("patient|patientId");
        Patient patient;
        Object pt = config.getAttribute("patient");
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        }
        else {
            // in case we are passed a PatientDomainWrapper (but this module doesn't know about emrapi)
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }
        EncounterType labOrderEncounterType = encounterService.getEncounterTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        EncounterRole encounterRoles = encounterService.getAllEncounterRoles(false).get(0);

        List<CareSetting> careSettings = orderService.getCareSettings(false);

        Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
        jsonConfig.put("patient", convertToFull(patient));
        jsonConfig.put("provider", convertToFull(sessionContext.getCurrentProvider()));
        jsonConfig.put("encounterRole", convertToFull(encounterRoles));
        jsonConfig.put("labOrderEncounterType", convertToFull(labOrderEncounterType));
        jsonConfig.put("careSettings", convertToFull(careSettings));

        if (careSetting != null) {
            jsonConfig.put("intialCareSetting", careSetting.getUuid());
        }

        model.put("patient", patient);
        model.put("jsonConfig", ui.toJson(jsonConfig));

    }
    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }

    /**
     * Fragment action method that adds viral load sample to a draft manifest
     * @param manifest
     * @param order
     * @param sampleType
     * @param dateSampleCollected
     * @param dateSampleSeparated
     * @return
     */
    public SimpleObject addOrderToManifest(@RequestParam(value = "manifestId") LabManifest manifest,
                                           @RequestParam(value = "orderId") Order order,
                                           @RequestParam(value = "sampleType") String sampleType,
                                           @RequestParam(value = "dateSampleCollected") Date dateSampleCollected,
                                           @RequestParam(value = "dateSampleSeparated") Date dateSampleSeparated
                                           ) {

        if (manifest != null && order != null) { // check for the configured lab system so that appropriate payload structure is generated

            if (LabOrderDataExchange.getSystemType() == LabOrderDataExchange.NO_SYSTEM_CONFIGURED) {
                System.out.println("The System Type has not been set: Please set it to continue" );
                return SimpleObject.create("status", "Not successful", "cause", "LAB system is not configured! Please configure it to proceed");
            }

            LabManifestOrder labOrder = new LabManifestOrder();

            labOrder.setLabManifest(manifest);
            labOrder.setOrder(order);
            labOrder.setSampleType(sampleType);
            labOrder.setSampleCollectionDate(dateSampleCollected);
            labOrder.setSampleSeparationDate(dateSampleSeparated);

            LabWebRequest payloadGenerator = null;

            if (LabOrderDataExchange.getSystemType() == LabOrderDataExchange.CHAI_SYSTEM) {
                payloadGenerator = new ChaiSystemWebRequest();
            } else if (LabOrderDataExchange.getSystemType() == LabOrderDataExchange.LABWARE_SYSTEM) {
                payloadGenerator = new LabwareSystemWebRequest();
            }

            if (payloadGenerator == null) {
                return SimpleObject.create("status", "Not successful", "cause", "An error occured while adding sample to manifest");
            }

            payloadGenerator.setManifestType(manifest.getManifestType());
            ObjectNode payload = payloadGenerator.completePostPayload(order, dateSampleCollected, dateSampleSeparated, sampleType, manifest.getIdentifier());

            // TODO: check if the payload is not null. Currently, an empty payload is generated if nascop code or ccc number (if VL) or hei number (if HEI) is null
            if (!payload.isEmpty()) {
                labOrder.setPayload(payload.toString());
                labOrder.setStatus("Pending");

                kenyaemrOrdersService.saveLabManifestOrder(labOrder);
                return SimpleObject.create("status", "successful");
            }
        }
        return SimpleObject.create("status", "Not successful", "cause", "Documentation of patient identifier and/or regimen is required");
    }

    /**
     * A fragment action method that removes a viral lab sample from a draft manifest
     * @param manifestOrder
     * @return
     */
    public SimpleObject removeManifestOrder(@RequestParam(value = "manifestOrderId") Integer manifestOrder
    ) {

        if (manifestOrder != null) {
            kenyaemrOrdersService.voidLabManifestOrder(manifestOrder);
            return SimpleObject.create("status", "successful");
        }
        return SimpleObject.create("status", "Not successful");
    }
}
