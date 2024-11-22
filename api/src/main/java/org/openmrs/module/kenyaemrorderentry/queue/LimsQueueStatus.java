package org.openmrs.module.kenyaemrorderentry.queue;

public enum LimsQueueStatus {
    QUEUED ("QUEUED"),
    SUBMITTED ("SUBMITTED"),
    ERROR ("ERROR"),
    UPDATED_RESULTS ("UPDATED_RESULTS");
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
