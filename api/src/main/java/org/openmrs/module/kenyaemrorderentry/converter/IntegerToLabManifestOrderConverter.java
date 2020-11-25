package org.openmrs.module.kenyaemrorderentry.converter;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.springframework.core.convert.converter.Converter;

public class IntegerToLabManifestOrderConverter implements Converter<Integer, LabManifestOrder> {

    /**
     * @see Converter#convert(Object)
     */
    @Override
    public LabManifestOrder convert(Integer id) {
        if (id == null) {
            return null;
        } else {
            return Context.getService(KenyaemrOrdersService.class).getLabManifestOrderById(id);
        }
    }
}
