package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.apache.commons.io.IOUtils;
import org.openmrs.module.kenyaemrorderentry.api.itext.LabManifestReport;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadManifestPageController {


    public void controller(@RequestParam(value = "manifest") LabManifest manifest, HttpServletResponse response) {

        LabManifestReport report = new LabManifestReport(manifest);
        File generatedManifest = null;
        try {
            generatedManifest = report.generateReport("");
        } catch (IOException ex) {
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

