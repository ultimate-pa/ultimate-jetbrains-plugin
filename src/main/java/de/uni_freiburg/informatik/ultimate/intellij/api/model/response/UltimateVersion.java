package de.uni_freiburg.informatik.ultimate.intellij.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UltimateVersion {

    @JsonProperty("ultimate_version")
    private String version;

    @JsonProperty("status")
    private String status;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
