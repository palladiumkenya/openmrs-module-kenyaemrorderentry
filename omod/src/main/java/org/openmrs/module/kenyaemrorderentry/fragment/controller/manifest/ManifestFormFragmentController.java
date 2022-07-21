package org.openmrs.module.kenyaemrorderentry.fragment.controller.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.kenyaemrorderentry.manifest.LabManifest;
import org.openmrs.module.kenyaui.form.AbstractWebForm;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;

public class ManifestFormFragmentController {
    public void controller(@FragmentParam(value = "manifestId", required = false) LabManifest labManifest,
                           @RequestParam(value = "returnUrl") String returnUrl,
                           PageModel model) {
        KenyaemrOrdersService kenyaemrOrdersService = Context.getService(KenyaemrOrdersService.class);
        LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
        LabManifest exists = labManifest != null ? labManifest : null;
        model.addAttribute("labManifest", labManifest);
        model.addAttribute("manifestTypeOptions", manifestTypeOptions());
        model.addAttribute("command", newEditManifestForm(exists));
        model.addAttribute("manifestStatusOptions", manifestStatus());

        //prepopulations
        model.addAttribute("lastCounty", kenyaemrOrdersService.getLastLabOrderManifest().getCounty() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getCounty() : "");
        model.addAttribute("lastSubCounty", kenyaemrOrdersService.getLastLabOrderManifest().getSubCounty() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getSubCounty() : "");
        model.addAttribute("lastFacilityEmail", kenyaemrOrdersService.getLastLabOrderManifest().getFacilityEmail() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getFacilityEmail() : "");
        model.addAttribute("lastFacilityPhoneContact", kenyaemrOrdersService.getLastLabOrderManifest().getFacilityPhoneContact() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getFacilityPhoneContact() : "");
        model.addAttribute("lastFacilityClinicianName", kenyaemrOrdersService.getLastLabOrderManifest().getClinicianName() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getClinicianName() : "");
        model.addAttribute("lastFacilityClinicianPhone", kenyaemrOrdersService.getLastLabOrderManifest().getClinicianPhoneContact() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getClinicianPhoneContact() : "");
        model.addAttribute("lastFacilityLabPhone", kenyaemrOrdersService.getLastLabOrderManifest().getLabPocPhoneNumber() !=null ? kenyaemrOrdersService.getLastLabOrderManifest().getLabPocPhoneNumber() : "");

             // create list of counties
        List<String> countyList = new ArrayList<String>();
        List<Location> locationList = Context.getLocationService().getAllLocations();
        for(Location loc: locationList) {
            String locationCounty = loc.getCountyDistrict();
            if(!StringUtils.isEmpty(locationCounty) && !StringUtils.isBlank(locationCounty)) {
                countyList.add(locationCounty);
            }
        }
        Set<String> uniqueCountyList = new HashSet<String>(countyList);
        model.addAttribute("countyList", uniqueCountyList);
        model.addAttribute("isAnEdit", exists == null ? false : true);
        model.addAttribute("returnUrl", returnUrl);
    }

    private List<String> manifestStatus() {
        return Arrays.asList(
                new String("Draft"),
                new String("Ready to send"),
                new String("On hold")
                );
    }

    protected List<SimpleObject> manifestTypeOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();
        for (Map.Entry<Integer, String> option : createManifestTypeOptions().entrySet())
            options.add(SimpleObject.create("value", option.getKey(), "label", option.getValue()));

        return options;
    }
    private Map<Integer, String> createManifestTypeOptions() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(LabManifest.VL_TYPE, "Viral Load");
        options.put(LabManifest.EID_TYPE, "EID");
        return options;
    }


    public SimpleObject saveManifest(@MethodParam("newEditManifestForm") @BindParams EditManifestForm
                                                form,
                                        UiUtils ui) {
        ui.validate(form, form, null);
        LabManifest labManifest = form.save();
        return SimpleObject.create(
                "manifestId", labManifest.getId()
        );
    }

    public EditManifestForm newEditManifestForm(@RequestParam(value = "manifestId", required = false) LabManifest labManifest) {
        if (labManifest !=null){

            return new EditManifestForm(labManifest);
        }
        else {
            return new EditManifestForm();
        }
    }

    public class EditManifestForm extends AbstractWebForm {
        private LabManifest original;
        private String identifier;
        private Date startDate;
        private Date endDate;
        private String courier;
        private String courierOfficer;
        private String status;
        private Integer manifestType;
        private Date dispatchDate;
        private String county;
        private String subCounty;
        private String facilityEmail;
        private String facilityPhoneContact;
        private String clinicianPhoneContact;
        private String clinicianName;
        private String labPocPhoneNumber;

        public EditManifestForm() {
        }

        public EditManifestForm(LabManifest manifest) {
            this.original = manifest;
            this.identifier = manifest.getIdentifier();
            this.startDate = manifest.getStartDate();
            this.status = manifest.getStatus();
            this.manifestType = manifest.getManifestType();
            this.endDate = manifest.getEndDate();
            this.courier = manifest.getCourier();
            this.courierOfficer = manifest.getCourierOfficer();
            this.dispatchDate = manifest.getDispatchDate();
            this.county = manifest.getCounty();
            this.subCounty = manifest.getSubCounty();
            this.facilityEmail = manifest.getFacilityEmail();
            this.facilityPhoneContact = manifest.getFacilityPhoneContact();
            this.clinicianName = manifest.getClinicianName();
            this.clinicianPhoneContact = manifest.getClinicianPhoneContact();
            this.labPocPhoneNumber = manifest.getLabPocPhoneNumber();

        }
        public LabManifest save(){
            LabManifest toSave;

            //Check if it is labware system and if so generate a manifest ID
            //Avoid doing this if it is an edit
            if(original == null) {
                //This is a save
                toSave = new LabManifest();
                LabOrderDataExchange labOrderDataExchange = new LabOrderDataExchange();
                if(LabOrderDataExchange.getSystemType() == LabOrderDataExchange.LABWARE_SYSTEM) {
                    String mType = "E";
                    if(manifestType == LabManifest.EID_TYPE) {
                        mType = "E";
                    } else if(manifestType == LabManifest.VL_TYPE) {
                        mType = "V";
                    }
                    toSave.setIdentifier(labOrderDataExchange.generateUniqueManifestID(mType));
                }
            } else {
                //This is an edit
                toSave = original;
                toSave.setIdentifier(identifier);
            }
            
            toSave.setStartDate(startDate);
            toSave.setEndDate(endDate);
            toSave.setDispatchDate(dispatchDate);
            toSave.setStatus(status);
            toSave.setManifestType(manifestType);
            toSave.setCourier(courier);
            toSave.setCourierOfficer(courierOfficer);
            toSave.setCounty(county);
            toSave.setSubCounty(subCounty);
            toSave.setFacilityEmail(facilityEmail);
            toSave.setFacilityPhoneContact(facilityPhoneContact);
            toSave.setClinicianName(clinicianName);
            toSave.setClinicianPhoneContact(clinicianPhoneContact);
            toSave.setLabPocPhoneNumber(labPocPhoneNumber);

            LabManifest lManifest = Context.getService(KenyaemrOrdersService.class).saveLabOrderManifest(toSave);
            return lManifest;

        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "startDate");
            require(errors, "endDate");
            require(errors, "status");
            require(errors, "manifestType");
        }

        public LabManifest getOriginal() {
            return original;
        }

        public void setOriginal(LabManifest original) {
            this.original = original;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getCourier() {
            return courier;
        }

        public void setCourier(String courier) {
            this.courier = courier;
        }

        public String getCourierOfficer() {
            return courierOfficer;
        }

        public void setCourierOfficer(String courierOfficer) {
            this.courierOfficer = courierOfficer;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getManifestType() {
            return manifestType;
        }

        public void setManifestType(Integer manifestType) {
            this.manifestType = manifestType;
        }

        public Date getDispatchDate() {
            return dispatchDate;
        }

        public void setDispatchDate(Date dispatchDate) {
            this.dispatchDate = dispatchDate;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getSubCounty() {
            return subCounty;
        }

        public void setSubCounty(String subCounty) {
            this.subCounty = subCounty;
        }

        public String getFacilityEmail() {
            return facilityEmail;
        }

        public void setFacilityEmail(String facilityEmail) {
            this.facilityEmail = facilityEmail;
        }

        public String getFacilityPhoneContact() {
            return facilityPhoneContact;
        }

        public void setFacilityPhoneContact(String facilityPhoneContact) {
            this.facilityPhoneContact = facilityPhoneContact;
        }

        public String getClinicianPhoneContact() {
            return clinicianPhoneContact;
        }

        public void setClinicianPhoneContact(String clinicianPhoneContact) {
            this.clinicianPhoneContact = clinicianPhoneContact;
        }

        public String getClinicianName() {
            return clinicianName;
        }

        public void setClinicianName(String clinicianName) {
            this.clinicianName = clinicianName;
        }

        public String getLabPocPhoneNumber() {
            return labPocPhoneNumber;
        }

        public void setLabPocPhoneNumber(String labPocPhoneNumber) {
            this.labPocPhoneNumber = labPocPhoneNumber;
        }
    }
}



