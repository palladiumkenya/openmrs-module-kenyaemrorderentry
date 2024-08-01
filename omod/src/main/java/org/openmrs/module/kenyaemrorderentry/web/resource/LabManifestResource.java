package org.openmrs.module.kenyaemrorderentry.web.resource;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifestOrder;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Resource(name = RestConstants.VERSION_1 +  "/labmanifest", supportedClass = LabManifest.class, supportedOpenmrsVersions = {"2.0.*", "2.1.*", "2.2.*", "2.0 - 2.*"})
public class LabManifestResource extends DataDelegatingCrudResource<LabManifest> {

    @Override
    public LabManifest getByUniqueId(String uniqueId) {
        return Context.getService(KenyaemrOrdersService.class).getLabManifestByUUID(uniqueId);
    }

    @Override
    protected void delete(LabManifest labManifest, String reason, RequestContext context) throws ResponseException {
        labManifest.setVoided(true);
        labManifest.setVoidReason(reason);
        Context.getService(KenyaemrOrdersService.class).saveLabOrderManifest(labManifest);
    }

    @Override
    public LabManifest newDelegate() {
        return new LabManifest();
    }

    @Override
    public LabManifest save(LabManifest labManifest) {
        if (labManifest.getLabManifestOrders() != null) {
            for (LabManifestOrder order : labManifest.getLabManifestOrders()) {
                order.setLabManifest(labManifest); // Set the reference to the parent LabManifest
            }
        }
        return Context.getService(KenyaemrOrdersService.class).saveLabOrderManifest(labManifest);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        
        description.addProperty("startDate");
        description.addProperty("endDate");
        description.addProperty("dispatchDate");
        description.addProperty("courier");
        description.addProperty("courierOfficer");
        description.addProperty("status");

        description.addProperty("county");
        description.addProperty("subCounty");
        description.addProperty("facilityEmail");
        description.addProperty("facilityPhoneContact");
        description.addProperty("clinicianPhoneContact");
        description.addProperty("clinicianName");
        description.addProperty("labPocPhoneNumber");
        description.addProperty("manifestType");

        description.addProperty("labManifestOrders");
        return description;
    }

    @Override
    public void purge(LabManifest labManifest, RequestContext context) throws ResponseException {
        labManifest.setVoided(true);
        Context.getService(KenyaemrOrdersService.class).saveLabOrderManifest(labManifest);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        
        if (representation instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("identifier");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("dispatchDate");
            description.addProperty("courier");
            description.addProperty("courierOfficer");
            description.addProperty("status");
            description.addProperty("county");
            description.addProperty("subCounty");
            description.addProperty("facilityEmail");
            description.addProperty("facilityPhoneContact");
            description.addProperty("clinicianPhoneContact");
            description.addProperty("clinicianName");
            description.addProperty("labPocPhoneNumber");
            description.addProperty("manifestType");
            String customRep = "(uuid,status,result,batchNumber,dateSent,resultDate,sampleType,order:(patient:(id,uuid,identifiers:(identifier,uuid))))";
            Representation rep = new CustomRepresentation(customRep);
            description.addProperty("labManifestOrders", rep);
            description.addSelfLink();
            
            return description;
        } else if (representation instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("identifier");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("dispatchDate");
            description.addProperty("courier");
            description.addProperty("courierOfficer");
            description.addProperty("status");
            description.addProperty("county");
            description.addProperty("subCounty");
            description.addProperty("facilityEmail");
            description.addProperty("facilityPhoneContact");
            description.addProperty("clinicianPhoneContact");
            description.addProperty("clinicianName");
            description.addProperty("labPocPhoneNumber");
            description.addProperty("manifestType");
            description.addProperty("labManifestOrders", Representation.REF);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            
            return description;
        } else if (representation instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("identifier");
            description.addProperty("status");
            description.addProperty("labManifestOrders", Representation.REF);
            description.addSelfLink();

            return description;
        }
        return null;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) {
        return new NeedsPaging<LabManifest>(Context.getService(KenyaemrOrdersService.class).getLabOrderManifest(), context);
    }

    @Override
	protected AlreadyPaged<LabManifest> doSearch(RequestContext context) {
		String uuid = context.getRequest().getParameter("uuid");
		String status = context.getRequest().getParameter("status");
        String type = context.getRequest().getParameter("type");
		String createdOnOrBeforeDateStr = context.getRequest().getParameter("createdOnOrBefore");
		String createdOnOrAfterDateStr = context.getRequest().getParameter("createdOnOrAfter");

		Date createdOnOrBeforeDate = StringUtils.isNotBlank(createdOnOrBeforeDateStr) ? (Date) ConversionUtil.convert(createdOnOrBeforeDateStr, Date.class) : null;
		Date createdOnOrAfterDate = StringUtils.isNotBlank(createdOnOrAfterDateStr) ? (Date) ConversionUtil.convert(createdOnOrAfterDateStr, Date.class) : null;

		KenyaemrOrdersService service = Context.getService(KenyaemrOrdersService.class);

		List<LabManifest> result = service.getLabManifests(uuid, status, type, createdOnOrAfterDate, createdOnOrBeforeDate);
		return new AlreadyPaged<LabManifest>(context, result, false);
	}

}