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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.itext.HeiLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.itext.LabManifestLog;
import org.openmrs.module.kenyaemrorderentry.api.itext.PrintSpecimenLabel;
import org.openmrs.module.kenyaemrorderentry.api.itext.ViralLoadLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabwareFacilityWideResultsMapper;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LimsSystemWebRequest;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.labsUtils;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.kenyaemrorderentry.metadata.KenyaemrorderentryAdminSecurityMetadata;
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueue;
import org.openmrs.module.kenyaemrorderentry.queue.LimsQueueStatus;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * The main controller that exposes additional end points for order entry
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrorder")
@Authorized
public class KenyaemrOrderRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * // end point for processing individual lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/labresults
     * @param request
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    @RequestMapping(method = RequestMethod.POST, value = "/limsfacilitywideresults")
    @ResponseBody
    public ResponseEntity<String> processLimsFacilityWideResults(HttpServletRequest request) {
        String requestBody = null;
        try {
            requestBody = Utils.fetchRequestBody(request.getReader());
        } catch (IOException e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error extracting request body" + msg);
        }

        if (requestBody != null) {
            LabwareFacilityWideResultsMapper labOrderDataExchange = new LabwareFacilityWideResultsMapper();
            return labOrderDataExchange.processResultsFromLims(requestBody);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The request could not be interpreted in KenyaEMR");
    }
    /**
     * Gets a list of valid orders for a given manifest (manifest type)
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

        return(null);
    }

    /**
     * Print the manifest given a lab manifest uuid
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

        return(null);
    }

    /**
     * Print the manifest LOG given a lab manifest uuid
     * @param request
     * @param manifestUuid - The manifest uuid
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

        return(null);
    }

    /**
     * Gets the metrics for lab manifests
     * @param request - the request
     * @return
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    @RequestMapping(method = RequestMethod.GET, value = "/manifestmetrics")
    @ResponseBody
    public Object getManifestMerics(HttpServletRequest request) {
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
        SimpleObject model = new SimpleObject();

        // Drafts
        Long drafts = kenyaemrOrdersService.countTotalDraftManifests();
        model.put("manifestsDraft", drafts);

        // On Hold
        Long onHold = kenyaemrOrdersService.countTotalManifestsOnHold();
        model.put("manifestsOnHold", onHold);

        // Ready to send
        Long readyToSend = kenyaemrOrdersService.countTotalReadyToSendManifests();
        model.put("manifestsReadyToSend", readyToSend);

        // Sending
        Long sending = kenyaemrOrdersService.countTotalManifestsOnSending();
        model.put("manifestsSending", sending);

        // Submitted
        Long submitted = kenyaemrOrdersService.countTotalSubmittedManifests();
        model.put("manifestsSubmitted", submitted);

        // Incomplete with Errors
        Long incompleteWithErrors = kenyaemrOrdersService.countTotalManifestsIncompleteWithErrors();
        model.put("manifestsIncompleteWithErrors", incompleteWithErrors);

        // Total Errors on incomplete manifests
        Long errorsOnIncomplete = kenyaemrOrdersService.countTotalErrorsOnIncompleteManifests();
        model.put("errorsOnIncomplete", errorsOnIncomplete);

        // Incomplete
        Long incomplete = kenyaemrOrdersService.countTotalIncompleteManifests();
        model.put("manifestsIncomplete", incomplete);
        
        // Complete with Errors
        Long completeWithErrors = kenyaemrOrdersService.countTotalManifestsCompleteWithErrors();
        model.put("manifestsCompleteWithErrors", completeWithErrors);

        // Total Errors on complete manifests
        Long errorsOnComplete = kenyaemrOrdersService.countTotalErrorsOnCompleteManifests();
        model.put("errorsOnComplete", errorsOnComplete);

        // Complete
        Long complete = kenyaemrOrdersService.countTotalCompleteManifests();
        model.put("manifestsComplete", complete);

        // Graph
        List<SimpleObject> summaryGraph = kenyaemrOrdersService.getLabManifestSummaryGraphSQL();
        model.put("summaryGraph", summaryGraph);

        // Settings
        model.put("userHasSettingsEditRole", (Context.getAuthenticatedUser().containsRole(KenyaemrorderentryAdminSecurityMetadata._Role.API_ROLE_EDIT_SETTINGS) || Context.getAuthenticatedUser().isSuperUser()));

        return(model);
    }

    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    @RequestMapping(method = RequestMethod.GET, value = "/sendteststolims")
    @ResponseBody
    public Object sendQueuedLabTestsToLims(HttpServletRequest request) {
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
        SimpleObject model = new SimpleObject();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -2); // upto two minutes ago to ensure billing information (created through AOP) is carefully evaluated
        Date effectiveDate = cal.getTime();
        List<LimsQueue> queuedLabTests = kenyaemrOrdersService.getLimsQueueEntriesByStatus(LimsQueueStatus.QUEUED, null, effectiveDate, false);

        if (queuedLabTests.isEmpty()) {
            model.put("response", "There are no tests to send to LIMS");
            return model;
        }
        int counter = 0;
        for (LimsQueue limsQueue : queuedLabTests) {
            try {
                if (labsUtils.isOrderForExpressPatient(limsQueue.getOrder()) || !labsUtils.orderHasUnsettledBill(limsQueue.getOrder())) {
                    LimsSystemWebRequest.postLabOrderRequestToLims(limsQueue.getPayload());
                    limsQueue.setStatus(LimsQueueStatus.SUBMITTED);
                    limsQueue.setDateLastChecked(new Date());
                    kenyaemrOrdersService.saveLimsQueue(limsQueue);
                    counter++;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        model.put("numberOfSubmittedTests", counter);
        return(model);
    }

    /**
     * @see BaseRestController#getNamespace()
     */

    @Override
    public String getNamespace() {
        return "v1/kenyaemrorderentry";
    }
}
