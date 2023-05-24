package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.kenyaemrorderentry.api.itext.HeiLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.api.itext.ViralLoadLabManifestReport;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
public class DownloadManifestPageController {


    public void controller(@RequestParam(value = "manifest") LabManifest manifest, HttpServletResponse response) {

        File generatedManifest = null;

        if (manifest.getManifestType().intValue() == LabManifest.EID_TYPE) {
            HeiLabManifestReport report = new HeiLabManifestReport(manifest);
            try {
                generatedManifest = report.generateReport("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (manifest.getManifestType().intValue() == LabManifest.VL_TYPE) {
            ViralLoadLabManifestReport report = new ViralLoadLabManifestReport(manifest);
            try {
                generatedManifest = report.generateReport("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        if (generatedManifest != null) {
            try {
                InputStream is = new FileInputStream(generatedManifest);
                // response.setContentType(MediaType.APPLICATION_PDF);
                response.setContentType("application/pdf");
                // To open PDF in browser
                // To download PDF
                response.addHeader("content-disposition", "inline;filename=" + generatedManifest.getName());
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

