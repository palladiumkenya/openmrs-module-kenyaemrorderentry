package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.kenyaemrorderentry.api.itext.PrintSpecimenLabel;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class PrintSpecimenLabelPageController {


    public void controller(@RequestParam(value = "manifestOrder") LabManifestOrder order, HttpServletResponse response) {

        PrintSpecimenLabel report = new PrintSpecimenLabel(order);
        File generatedSpecimenLabel = null;
        try {
            generatedSpecimenLabel = report.downloadSpecimenLabel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (generatedSpecimenLabel != null) {
            try {
                InputStream is = new FileInputStream(generatedSpecimenLabel);
                // response.setContentType(MediaType.APPLICATION_PDF);
                response.setContentType("application/pdf");
                // To open PDF in browser
                // response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + generatedSpecimenLabel.getName());
                // To download PDF
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + generatedSpecimenLabel.getName());
                int bytes = IOUtils.copy(is, response.getOutputStream());
                response.setContentLength(bytes);
                response.flushBuffer();
            } catch (IOException ex) {
                System.out.println("Error writing file to output stream");
            }
        } else {
            System.out.println("The returned file was null");
        }

    }
}

