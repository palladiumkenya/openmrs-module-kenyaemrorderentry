package org.openmrs.module.kenyaemrorderentry.web.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.itext.HeiLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.itext.LabManifestLog;
import org.openmrs.module.kenyaemrorderentry.api.itext.PrintSpecimenLabel;
import org.openmrs.module.kenyaemrorderentry.api.itext.ViralLoadLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The main controller that exposes additional end points for order entry
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrorder")
public class KenyaemrOrderRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * // end point for processing individual lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/labresults
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/labresults") 
    @ResponseBody
    public Object processIncomingViralLoadResults(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return "Error extracting request body" + msg;
        }

        if (requestBody != null) {
            LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
            return labOrderDataExchange.processIncomingViralLoadLabResults(requestBody);
        }
        return  "The request could not be interpreted by the internal vl result end point";
    }

    /**
     * // end point for processing individual FLU lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/flulabresults
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/flulabresults") 
    @ResponseBody
    public Object processIncomingFLUResults(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return "Error extracting request body" + msg;
        }

        if (requestBody != null) {
            LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
            return labOrderDataExchange.processIncomingFLULabResults(requestBody);
        }
        return  "The request could not be interpreted by the internal flu result end point";
    }

    /**
     * Gets a list of valid orders for a given manifest (manifest type)
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/validorders")
    @ResponseBody
    public Object getValidOrdersForManifest(HttpServletRequest request, @RequestParam("manifestUuid") String manifestUuid) {
        Set<SimpleObject> activeOrdersNotInManifest = new HashSet<SimpleObject>();
        LabManifest labManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestByUUID(manifestUuid);
        PersonService personService = Context.getPersonService();

        // Is DOD
        AdministrationService administrationService = Context.getAdministrationService();
        final String isKDoD = (administrationService.getGlobalProperty("kenyaemr.isKDoD"));

        PatientIdentifierType pat = Utils.getUniquePatientNumberIdentifierType();
        PatientIdentifierType kat = Utils.getKDODIdentifierType();
        PatientIdentifierType hei = Utils.getHeiNumberIdentifierType();
        LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
        Integer manifestTypeCode = labManifest.getManifestType();
        
        if (manifestTypeCode == LabManifest.EID_TYPE) {
            activeOrdersNotInManifest = labOrderDataExchange.getActiveEidOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        } else if (manifestTypeCode == LabManifest.VL_TYPE) {
            activeOrdersNotInManifest = labOrderDataExchange.getActiveViralLoadOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        } else if (manifestTypeCode == LabManifest.FLU_TYPE) {
            activeOrdersNotInManifest = labOrderDataExchange.getActiveFluOrdersNotInManifest(null, labManifest.getStartDate(), labManifest.getEndDate());
        }

        // orders
        List<SimpleObject> Orders = new ArrayList<SimpleObject>();
        for(SimpleObject load : activeOrdersNotInManifest){
            SimpleObject so = new SimpleObject();
            Order order = (Order) load.get("order");
            so.put("orderId", order.getId());
            so.put("orderUuid", order.getUuid());
            Patient patient = order.getPatient();

            // Patient identifiers
            so.put("patientId", patient.getId());
            so.put("patientUuid", patient.getUuid());

            // Patient name
            StringBuilder fullName = new StringBuilder();
            String middleName = personService.getPerson(patient.getId()).getMiddleName() != null ? personService
                .getPerson(patient.getId()).getMiddleName().toUpperCase() : "";
            String lastName = personService.getPerson(patient.getId()).getFamilyName().toUpperCase();
            String firstName = personService.getPerson(patient.getId()).getGivenName().toUpperCase();
            if(firstName != null && !firstName.isEmpty()) {
                fullName.append(firstName);
            }
            if(middleName != null && !middleName.isEmpty()) {
                fullName.append(" " + middleName);
            }
            if(lastName != null && !lastName.isEmpty()) {
                fullName.append(" " + lastName);
            }
            so.put("patientName", fullName.toString());

            //Patient ccc/kdod
            String ccc = "";
            if(isKDoD.trim().equalsIgnoreCase("true")) {
                PatientIdentifier kdodNumber = patient.getPatientIdentifier(kat);
                ccc = (kdodNumber == null || StringUtils.isBlank(kdodNumber.getIdentifier())) ? "" : kdodNumber.getIdentifier();
            } else {
                if(patient.getAge() > 2) {
                    PatientIdentifier cccNumber = patient.getPatientIdentifier(pat);
                    ccc = (cccNumber == null || StringUtils.isBlank(cccNumber.getIdentifier())) ? "" : cccNumber.getIdentifier();
                } else {
                    PatientIdentifier heiNumber = patient.getPatientIdentifier(hei);
                    ccc = (heiNumber == null || StringUtils.isBlank(heiNumber.getIdentifier())) ? "" : heiNumber.getIdentifier();
                }
            }
            so.put("cccKdod", ccc);

            so.put("dateRequested", Utils.getSimpleDateFormat("dd-MM-yyyy").format(order.getDateCreated()));
            so.put("payload", "test");
            so.put("hasProblem", load.get("hasProblem"));
            Orders.add(so);
        }

        SimpleObject model = new SimpleObject();

        model.put("Orders", Orders );

        model.put("cccNumberType", "");
        model.put("heiNumberType", "");
        if(isKDoD.trim().equalsIgnoreCase("true")) {
            model.put("cccNumberType", kat.getPatientIdentifierTypeId());
            model.put("heiNumberType", kat.getPatientIdentifierTypeId());
        } else {
            model.put("cccNumberType", pat.getPatientIdentifierTypeId());
            model.put("heiNumberType", hei.getPatientIdentifierTypeId());
        }

        return model;
    }

    /**
     * Print the specimen label given a lab manifest order
     * @param request
     * @param manifestOrderUuid - The manifest order uuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/printspecimenlabel")
    @ResponseBody
    public Object printSpecimenLabel(HttpServletRequest request, HttpServletResponse response, @RequestParam("manifestOrderUuid") String manifestOrderUuid) {
        LabManifestOrder labManifestOrder = Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByUUID(manifestOrderUuid);

        PrintSpecimenLabel report = new PrintSpecimenLabel(labManifestOrder);
        File generatedSpecimenLabel = null;
        try {
            generatedSpecimenLabel = report.downloadSpecimenLabel();
        } catch (Exception ex) {
            System.err.println("Lab Manifest: Error generating specimen label: " + ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<String>("Lab Manifest: Error generating specimen label: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (generatedSpecimenLabel != null) {
            try {
                InputStream inputStream = new FileInputStream(generatedSpecimenLabel);
                response.setContentType("application/pdf");
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + generatedSpecimenLabel.getName());
                int bytes = IOUtils.copy(inputStream, response.getOutputStream());
                response.setContentLength(bytes);
                response.flushBuffer();
            } catch (Exception ex) {
                System.out.println("Lab Manifest: Error writing file to output stream: " + ex.getMessage());
                ex.printStackTrace();
                return new ResponseEntity<String>("Lab Manifest: Error writing file to output stream: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            System.out.println("Lab Manifest: The returned file was null");
            return new ResponseEntity<String>("Lab Manifest: The returned file was null: ", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return(response);
    }

    /**
     * Print the manifest given a lab manifest uuid
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/printmanifest")
    @ResponseBody
    public Object printManifest(HttpServletRequest request, HttpServletResponse response, @RequestParam("manifestUuid") String manifestUuid) {
        LabManifest labManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestByUUID(manifestUuid);

        File generatedManifest = null;

        if (labManifest.getManifestType().intValue() == LabManifest.EID_TYPE) {
            HeiLabManifestReport report = new HeiLabManifestReport(labManifest);
            try {
                generatedManifest = report.generateReport("");
            } catch (Exception ex) {
                System.err.println("Lab Manifest: Failed to generate manifest printout" + ex.getMessage());
                ex.printStackTrace();
                return new ResponseEntity<String>("Lab Manifest: Failed to generate manifest printout: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else if (labManifest.getManifestType().intValue() == LabManifest.VL_TYPE) {
            ViralLoadLabManifestReport report = new ViralLoadLabManifestReport(labManifest);
            try {
                generatedManifest = report.generateReport("");
            } catch (Exception ex) {
                System.err.println("Lab Manifest: Failed to generate manifest printout" + ex.getMessage());
                ex.printStackTrace();
                return new ResponseEntity<String>("Lab Manifest: Failed to generate manifest printout: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (generatedManifest != null) {
            try {
                InputStream is = new FileInputStream(generatedManifest);
                response.setContentType("application/pdf");
                response.addHeader("content-disposition", "inline;filename=" + generatedManifest.getName());
                int bytes = IOUtils.copy(is, response.getOutputStream());
                response.setContentLength(bytes);
                response.flushBuffer();
            } catch (IOException ex) {
                System.err.println("Lab Manifest: Error writing file to output stream");
                ex.printStackTrace();
                return new ResponseEntity<String>("Lab Manifest: Error writing file to output stream: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            System.err.println("Lab Manifest: The returned file was null");
            return new ResponseEntity<String>("Lab Manifest: The returned file was null", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return(response);
    }

    /**
     * Print the manifest LOG given a lab manifest uuid
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/printmanifestlog")
    @ResponseBody
    public Object printManifestLog(HttpServletRequest request, HttpServletResponse response, @RequestParam("manifestUuid") String manifestUuid) {
        LabManifest labManifest = Context.getService(KenyaemrOrdersService.class).getLabManifestByUUID(manifestUuid);

        File generatedLog = null;

        LabManifestLog report = new LabManifestLog(labManifest);
        try {
            generatedLog = report.generateReport("");
        } catch (Exception ex) {
            System.err.println("Lab Manifest: Failed to generate manifest LOG printout" + ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<String>("Lab Manifest: Failed to generate manifest LOG printout: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (generatedLog != null) {
            try {
                InputStream is = new FileInputStream(generatedLog);
                // response.setContentType(MediaType.APPLICATION_PDF);
                response.setContentType("application/pdf");
                // To open PDF in browser
                // To download PDF
                response.addHeader("content-disposition", "inline;filename=" + generatedLog.getName());
                int bytes = IOUtils.copy(is, response.getOutputStream());
                response.setContentLength(bytes);
                response.flushBuffer();
            } catch (Exception ex) {
                System.err.println("Lab Manifest: Error writing file to output stream");
                ex.printStackTrace();
                return new ResponseEntity<String>("Lab Manifest: Error writing file to output stream: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            System.err.println("Lab Manifest: The returned file was null");
            return new ResponseEntity<String>("Lab Manifest: The returned file was null", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return(response);
    }

    /**
     * @see BaseRestController#getNamespace()
     */

    @Override
    public String getNamespace() {
        return "v1/kenyaemrorderentry";
    }
}
