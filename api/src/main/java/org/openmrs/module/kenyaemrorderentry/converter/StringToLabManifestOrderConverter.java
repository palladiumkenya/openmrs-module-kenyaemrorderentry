package org.openmrs.module.kenyaemrorderentry.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.springframework.core.convert.converter.Converter;

public class StringToLabManifestOrderConverter implements Converter<String, LabManifestOrder> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public LabManifestOrder convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        return Context.getService(KenyaemrOrdersService.class).getLabManifestOrderById(Integer.valueOf(source));
    }
}
