package org.openmrs.module.kenyaemrorderentry.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.springframework.core.convert.converter.Converter;

public class IntegerToLabManifestConverter implements Converter<Integer, LabManifest> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public LabManifest convert(Integer id) {
        if (id == null) {
            return null;
        } else {
            return Context.getService(KenyaemrOrdersService.class).getLabOrderManifestById(id);
        }
    }
}
