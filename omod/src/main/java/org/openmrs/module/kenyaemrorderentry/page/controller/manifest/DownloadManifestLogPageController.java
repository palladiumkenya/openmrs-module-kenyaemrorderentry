package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.kenyaemrorderentry.api.itext.LabManifestLog;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
public class DownloadManifestLogPageController {


    public void controller(@RequestParam(value = "manifest") LabManifest manifest, HttpServletResponse response) {

        File generatedLog = null;

        LabManifestLog report = new LabManifestLog(manifest);
        try {
            generatedLog = report.generateReport("");
        } catch (IOException ex) {
            ex.printStackTrace();
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
            } catch (IOException ex) {
                System.out.println("Error writing file to output stream");
            }
        } else {
            System.out.println("The returned file was null");
        }

    }
}

