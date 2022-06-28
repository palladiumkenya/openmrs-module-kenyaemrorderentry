package org.openmrs.module.kenyaemrorderentry.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.util.Utils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The main controller that exposes additional end points for order entry
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrorder")
public class KenyaemrOrderRestController extends BaseRestController {
    protected final Log log = LogFactory.getLog(getClass());

    @RequestMapping(method = RequestMethod.POST, value = "/labresults") // end point for processing individual lab results e,g http://localhost:8080/kenyaemr/ws/rest/v1/kemrorder/labresults
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
            LabOrderDataExchange shr = new LabOrderDataExchange();
            return shr.processIncomingViralLoadLabResults(requestBody);

        }
        return  "The request could not be interpreted by the internal vl result end point";
    }

    /**
     * @see BaseRestController#getNamespace()
     */

    @Override
    public String getNamespace() {
        return "v1/kenyaemrorderentry";
    }
}
