package org.openmrs.module.kenyaemrorderentry.queue;

public enum LimsQueueStatus {
    QUEUED ("QUEUED"),
    SUBMITTED ("SUBMITTED"),
    ERROR ("ERROR"),
    COMPLETED ("COMPLETED");
    private final String name;

    LimsQueueStatus(String name) {
        this.name = name;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
