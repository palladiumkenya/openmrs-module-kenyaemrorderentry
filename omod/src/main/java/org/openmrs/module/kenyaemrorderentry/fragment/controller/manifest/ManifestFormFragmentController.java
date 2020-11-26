package org.openmrs.module.kenyaemrorderentry.fragment.controller.manifest;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemrorderentry.api.service.KenyaemrOrdersService;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ManifestFormFragmentController {
    public void controller(@FragmentParam(value = "manifestId", required = false) LabManifest labManifest,
                           @RequestParam(value = "returnUrl") String returnUrl,
                           PageModel model) {

        LabManifest exists = labManifest != null ? labManifest : null;
        model.addAttribute("labManifest", labManifest);
        model.addAttribute("command", newEditManifestForm(exists));
        model.addAttribute("manifestStatusOptions", manifestStatus());
        model.addAttribute("returnUrl", returnUrl);


    }

    private List<String> manifestStatus() {
        return Arrays.asList(
                new String("Open"),
                new String("Ready to send"),
                new String("Sending"),
                new String("Sent")
                );
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
        private Date startDate;
        private  Date endDate;
        private String courier;
        private String courierOfficer;
        private String status;
        private  Date dispatchDate;

        public EditManifestForm() {
        }

        public EditManifestForm(LabManifest manifest) {
            this.original = manifest;
            this.startDate = manifest.getStartDate();
            this.status = manifest.getStatus();
            this.endDate = manifest.getEndDate();
            this.courier = manifest.getCourier();
            this.courierOfficer = manifest.getCourierOfficer();
            this.dispatchDate = manifest.getDispatchDate();

        }
        public LabManifest save(){
            LabManifest toSave;
            if (original !=null){

                toSave = original;
            }
            else{
                toSave = new LabManifest();
            }
            toSave.setStartDate(startDate);
            toSave.setEndDate(endDate);
            toSave.setDispatchDate(dispatchDate);
            toSave.setStatus(status);
            toSave.setCourier(courier);
            toSave.setCourierOfficer(courierOfficer);

            LabManifest lManifest = Context.getService(KenyaemrOrdersService.class).saveLabOrderManifest(toSave);
            return lManifest;

        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "startDate");
            require(errors, "endDate");
            require(errors, "status");

            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);
            System.out.println("now: " + new Date());

            if (startDate != null) {
                if (startDate.after(new Date())) {
                    errors.rejectValue("startDate", "Cannot be in the future");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (startDate.before(calendar.getTime())) {
                        errors.rejectValue("startDate", " Invalid date");
                    }
                }
            }
            if (endDate != null) {
                if (endDate.before(new Date())) {
                    errors.rejectValue("endDate", "Cannot be in the past");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (endDate.before(calendar.getTime())) {
                        errors.rejectValue("endDate", " Invalid date");
                    }
                }
            }
        }

        public LabManifest getOriginal() {
            return original;
        }

        public void setOriginal(LabManifest original) {
            this.original = original;
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

        public Date getDispatchDate() {
            return dispatchDate;
        }

        public void setDispatchDate(Date dispatchDate) {
            this.dispatchDate = dispatchDate;
        }
    }


}



