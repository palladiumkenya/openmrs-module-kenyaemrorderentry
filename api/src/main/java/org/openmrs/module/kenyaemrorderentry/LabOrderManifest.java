package org.openmrs.module.kenyaemrorderentry;

import org.openmrs.BaseOpenmrsData;

import java.util.Date;
import java.util.UUID;

/**
 * Created by developer on 09 Nov, 2020
 */
public class LabOrderManifest extends BaseOpenmrsData {
    private Integer id;
    private Date startDate;
    private Date endDate;
    private String orderIdList;
    private Integer testType;
    private Date dateCreated;
    private Date dateChanged;
    private Boolean voided;
    private Date dateVoided;
    private String voidReason;
    private String uuid;

    public LabOrderManifest() {
        prePersist();
    }

    public LabOrderManifest(Date startDate, Date endDate, String orderIdList, Integer testType, Date dateCreated, Date dateChanged,Boolean voided, Date dateVoided, String voidReason, String uuid) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderIdList = orderIdList;
        this.testType = testType;
        this.dateCreated = dateCreated;
        this.dateChanged = dateChanged;
        this.voided = voided;
        this.dateVoided = dateVoided;
        this.voidReason = voidReason;
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

    public String getOrderIdList() {
        return orderIdList;
    }

    public void setOrderIdList(String orderIdList) {
        this.orderIdList = orderIdList;
    }

    public Integer getTestType() {
        return testType;
    }

    public void setTestType(Integer testType) {
        this.testType = testType;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public Date getDateChanged() {
        return dateChanged;
    }

    @Override
    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    @Override
    public Boolean getVoided() {
        return voided;
    }

    @Override
    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    @Override
    public Date getDateVoided() {
        return dateVoided;
    }

    @Override
    public void setDateVoided(Date dateVoided) {
        this.dateVoided = dateVoided;
    }

    @Override
    public String getVoidReason() {
        return voidReason;
    }

    @Override
    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
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
