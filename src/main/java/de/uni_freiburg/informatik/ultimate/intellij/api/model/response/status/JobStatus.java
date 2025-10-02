package de.uni_freiburg.informatik.ultimate.intellij.api.model.response.status;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JobStatus {

    SCHEDULED("scheduled"),
    DONE("done"),
    ERROR("ERROR");

    private final String status;

    JobStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @JsonCreator
    public static JobStatus fromString(String status) {
        for(JobStatus s : JobStatus.values()) {
            if(s.getStatus().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown JobStatus: " + status);
    }
}
