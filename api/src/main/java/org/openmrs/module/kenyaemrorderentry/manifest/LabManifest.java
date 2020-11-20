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
    private Date startDate;
    private Date endDate;
    private Date dispatchDate;
    private String courier;
    private String courierOfficer;
    private String status;
    private String uuid;

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

}