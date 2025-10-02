package de.uni_freiburg.informatik.ultimate.intellij.api.model.response.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JobInfo(
        @JsonProperty("requestId") String id,
        @JsonProperty("status") JobStatus status,
        @JsonProperty("results") List<UltimateResult> results
) {
    @Override
    public @NotNull String toString() {
        return "JobInfo{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", results=" + results +
                '}';
    }
}
