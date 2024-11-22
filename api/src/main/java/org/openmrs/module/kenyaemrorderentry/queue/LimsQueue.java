package org.openmrs.module.kenyaemrorderentry.queue;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Order;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A model for queuing messages to local lab system
 */
public class LimsQueue extends BaseOpenmrsData implements Serializable {
    private Integer id;
    private Order order;
    private String payload;
    private Date dateSent;
    private LimsQueueStatus status;
    private Date dateLastChecked;
    private String uuid;

    public LimsQueue() {
        prePersist();
    }

    public LimsQueue(Order order, String payload, Date dateSent, LimsQueueStatus status, String uuid) {
        this.order = order;
        this.payload = payload;
        this.dateSent = dateSent;
        this.status = status;
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

    public LimsQueueStatus getStatus() {
        return status;
    }

    public void setStatus(LimsQueueStatus status) {
        this.status = status;
    }

    public Date getDateLastChecked() {
        return dateLastChecked;
    }

    public void setDateLastChecked(Date dateLastChecked) {
        this.dateLastChecked = dateLastChecked;
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


    @Override
    public String toString() {
        return "LimsQueue [id=" + id + ", order=" + order
                + ", payload=" + payload + ", dateSent=" + dateSent + ", status=" + status + ", result="
                + ", resultsPulledDate=" + dateLastChecked
                + ", uuid=" + uuid + "]";
    }

}