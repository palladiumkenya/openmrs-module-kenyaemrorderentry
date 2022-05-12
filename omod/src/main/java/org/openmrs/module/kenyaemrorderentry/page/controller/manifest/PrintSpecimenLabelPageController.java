package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.kenyaemrorderentry.api.itext.LabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.itext.PrintSpecimenLabel;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrintSpecimenLabelPageController {


    public void controller(@RequestParam(value = "manifestOrder") LabManifestOrder order, HttpServletResponse response) {

        PrintSpecimenLabel report = new PrintSpecimenLabel(order);
        File generatedManifest = null;
        try {
            generatedManifest = report.downloadSpecimenLabel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (generatedManifest != null) {
            try {
                InputStream is = new FileInputStream(generatedManifest);
                response.setContentType("application/pdf");
                response.addHeader("content-disposition", "inline;filename=" + generatedManifest.getName());
                IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException ex) {
                System.out.println("Error writing file to output stream");
            }
        } else {
            System.out.println("The returned file was null");
        }

    }
}

