package org.openmrs.module.kenyaemrorderentry.manifest;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Order;

import java.util.Date;
import java.util.UUID;

/**
 * A model for a manifest entry. This is ideally an order in a manifest.
 */
public class LabManifestOrder extends BaseOpenmrsData {
    private Integer id;
    private LabManifest labManifest;
    private Order order;
    private String payload;
    private Date dateSent;
    private String status;
    private String result;
    private Date resultDate;
    private String uuid;

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

    public String getTestType() {
        return payload;
    }

    public void setTestType(String payload) {
        this.payload = payload;
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
}