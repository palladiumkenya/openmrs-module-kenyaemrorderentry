package org.openmrs.module.kenyaemrorderentry.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Resource(name = RestConstants.VERSION_1 +  "/labmanifestorder", supportedClass = LabManifestOrder.class, supportedOpenmrsVersions = {"2.0.*", "2.1.*", "2.2.*", "2.0 - 2.*"})
public class LabManifestOrderResource extends DataDelegatingCrudResource<LabManifestOrder> {

    @Override
    public LabManifestOrder getByUniqueId(String uniqueId) {
        return Context.getService(KenyaemrOrdersService.class).getLabManifestOrderByUUID(uniqueId);
    }

    @Override
    protected void delete(LabManifestOrder labManifest, String reason, RequestContext context) throws ResponseException {
        labManifest.setVoided(true);
        labManifest.setVoidReason(reason);
        Context.getService(KenyaemrOrdersService.class).saveLabManifestOrder(labManifest);
    }

    @Override
    public LabManifestOrder newDelegate() {
        return new LabManifestOrder();
    }

    @Override
    public LabManifestOrder save(LabManifestOrder labManifestOrder) {
        System.out.println("Saving a new manifest order: " + labManifestOrder.toString());
        return Context.getService(KenyaemrOrdersService.class).saveLabManifestOrder(labManifestOrder);
    }

    @Override
    public void purge(LabManifestOrder labManifestOrder, RequestContext context) throws ResponseException {
        labManifestOrder.setVoided(true);
        Context.getService(KenyaemrOrdersService.class).saveLabManifestOrder(labManifestOrder);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        
        if (representation instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("id");
            description.addProperty("labManifest", Representation.REF);
            description.addProperty("order", Representation.REF);
            description.addProperty("sampleType");
            description.addProperty("payload");
            description.addProperty("dateSent");
            description.addProperty("status");
            description.addProperty("result");
            description.addProperty("resultDate");
            description.addProperty("sampleCollectionDate");
            description.addProperty("sampleSeparationDate");
            description.addProperty("lastStatusCheckDate");
            description.addProperty("sampleReceivedDate");
            description.addProperty("sampleTestedDate");
            description.addProperty("resultsPulledDate");
            description.addProperty("resultsDispatchDate");
            description.addProperty("orderType");
            description.addProperty("batchNumber");
            description.addSelfLink();

            return description;
        } else if (representation instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("id");
            description.addProperty("labManifest", Representation.REF);
            description.addProperty("order", Representation.REF);
            description.addProperty("sampleType");
            description.addProperty("payload");
            description.addProperty("dateSent");
            description.addProperty("status");
            description.addProperty("result");
            description.addProperty("resultDate");
            description.addProperty("sampleCollectionDate");
            description.addProperty("sampleSeparationDate");
            description.addProperty("lastStatusCheckDate");
            description.addProperty("sampleReceivedDate");
            description.addProperty("sampleTestedDate");
            description.addProperty("resultsPulledDate");
            description.addProperty("resultsDispatchDate");
            description.addProperty("orderType");
            description.addProperty("batchNumber");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);

            return description;
        } else if (representation instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("id");
            description.addProperty("sampleType");
            description.addProperty("status");
            description.addSelfLink();

            return description;
        }
        
        return null;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        return new NeedsPaging<LabManifestOrder>(Context.getService(KenyaemrOrdersService.class).getLabManifestOrders(), context);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("sampleType");
        description.addProperty("sampleCollectionDate");
        description.addProperty("sampleSeparationDate");
        description.addProperty("order");
        description.addProperty("labManifest");
        description.addProperty("payload");
        description.addProperty("status");
        return description;
    }

}