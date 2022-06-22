package org.openmrs.module.kenyaemrorderentry.manifest;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A model that holds information about lab manifest
 */
public class LabManifest extends BaseOpenmrsData implements Serializable {
    private Integer id;
    private String identifier;
    private Date startDate;
    private Date endDate;
    private Date dispatchDate;
    private String courier;
    private String courierOfficer;
    private String status;
    private String county;
    private String subCounty;
    private String facilityEmail;
    private String facilityPhoneContact;
    private String clinicianPhoneContact;
    private String clinicianName;
    private String labPocPhoneNumber;
    private Integer manifestType;
    private String uuid;

    //Manifest Type and Order Type
    public static final int EID_TYPE = 1;
    public static final int VL_TYPE = 2;

    public LabManifest() {
        prePersist();
    }

    public LabManifest(Date startDate, Date endDate, Date dispatchDate, String courier, String courierOfficer, String status, String uuid) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.dispatchDate = dispatchDate;
        this.courier = courier;
        this.courierOfficer = courierOfficer;
        this.status = status;
        this.uuid = uuid;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
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

    public Date getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(Date dispatchDate) {
        this.dispatchDate = dispatchDate;
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

    public Integer getManifestType() {
        return manifestType;
    }

    public void setManifestType(Integer manifestType) {
        this.manifestType = manifestType;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void prePersist() {

        if (null == getUuid())
            setUuid(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "LabManifest{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", dispatchDate=" + dispatchDate +
                ", courier='" + courier + '\'' +
                ", courierOfficer='" + courierOfficer + '\'' +
                ", status='" + status + '\'' +
                ", county='" + county + '\'' +
                ", subCounty='" + subCounty + '\'' +
                ", facilityEmail='" + facilityEmail + '\'' +
                ", facilityPhoneContact='" + facilityPhoneContact + '\'' +
                ", clinicianPhoneContact='" + clinicianPhoneContact + '\'' +
                ", clinicianName='" + clinicianName + '\'' +
                ", labPocPhoneNumber='" + labPocPhoneNumber + '\'' +
                ", manifestType=" + manifestType +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}