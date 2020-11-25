package org.openmrs.module.kenyaemrorderentry.page.controller.manifest;

import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class CreateManifestPageController {


    public void controller(@RequestParam("returnUrl") String url,
                           @RequestParam(value = "manifestId", required = false) LabManifest manifest,
                           PageModel model) {

        model.addAttribute("returnUrl", url);
        model.addAttribute("manifest", manifest);

    }
}

