package org.openmrs.module.kenyaemrorderentry.manifest;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Order;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A model for a manifest entry. This is ideally an order in a manifest.
 */
public class LabManifestOrder extends BaseOpenmrsData implements Serializable {
    private Integer id;
    private LabManifest labManifest;
    private Order order;
    private String sampleType;
    private String payload;
    private Date dateSent;
    private String status;
    private String result;
    private Date resultDate;
    private Date sampleCollectionDate;
    private Date sampleSeparationDate;
    private Date lastStatusCheckDate;
    private Date sampleReceivedDate;
    private Date sampleTestedDate;
    private String uuid;

    //Order Type
    public static final String EID_ORDER_TYPE = "1";
    public static final String VL_ORDER_TYPE = "2";

    public LabManifestOrder() {
        prePersist();
    }

    public LabManifestOrder(LabManifest labManifest, Order order, String payload, Date dateSent, String status, String result, Date resultDate, String uuid) {
        this.labManifest = labManifest;
        this.order = order;
        this.payload = payload;
        this.dateSent = dateSent;
        this.status = status;
        this.result = result;
        this.resultDate = resultDate;
        this.uuid = uuid;
    }

    public void prePersist() {

        if (null == getUuid())
            setUuid(UUID.randomUUID().toString());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public LabManifest getLabManifest() {
        return labManifest;
    }

    public void setLabManifest(LabManifest labManifest) {
        this.labManifest = labManifest;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }

    public Date getSampleCollectionDate() {
        return sampleCollectionDate;
    }

    public void setSampleCollectionDate(Date sampleCollectionDate) {
        this.sampleCollectionDate = sampleCollectionDate;
    }

    public Date getSampleSeparationDate() {
        return sampleSeparationDate;
    }

    public void setSampleSeparationDate(Date sampleSeparationDate) {
        this.sampleSeparationDate = sampleSeparationDate;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public Date getLastStatusCheckDate() {
        return lastStatusCheckDate;
    }

    public void setLastStatusCheckDate(Date lastStatusCheckDate) {
        this.lastStatusCheckDate = lastStatusCheckDate;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Date getSampleReceivedDate() {
        return sampleReceivedDate;
    }

    public void setSampleReceivedDate(Date sampleReceivedDate) {
        this.sampleReceivedDate = sampleReceivedDate;
    }

    public Date getSampleTestedDate() {
        return sampleTestedDate;
    }

    public void setSampleTestedDate(Date sampleTestedDate) {
        this.sampleTestedDate = sampleTestedDate;
    }
}