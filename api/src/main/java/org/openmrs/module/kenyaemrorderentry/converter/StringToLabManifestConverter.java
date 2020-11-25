package org.openmrs.module.kenyaemrorderentry.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.springframework.core.convert.converter.Converter;

public class StringToLabManifestConverter implements Converter<String, LabManifest> {

    /**
     * @see org.springframework.core.convert.converter.Converter#convert(Object)
     */
    @Override
    public LabManifest convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        return Context.getService(KenyaemrOrdersService.class).getLabOrderManifestById(Integer.valueOf(source));
    }
}
