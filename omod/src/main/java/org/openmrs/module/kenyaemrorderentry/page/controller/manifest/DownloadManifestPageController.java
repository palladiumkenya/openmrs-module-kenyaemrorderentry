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

public class DownloadManifestPageController {


    public void controller(@RequestParam(value = "manifest") LabManifest manifest, HttpServletResponse response) {

        File generatedManifest = null;

        if (manifest.getManifestType().intValue() == 1) {
            HeiLabManifestReport report = new HeiLabManifestReport(manifest);
            try {
                generatedManifest = report.generateReport("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
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

