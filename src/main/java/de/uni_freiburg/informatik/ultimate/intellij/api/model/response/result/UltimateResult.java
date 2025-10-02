package de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UltimateResult {

    @JsonProperty("shortDesc")
    private String shortDescription;

    @JsonProperty("longDesc")
    private String longDescription;

    @JsonProperty("startLNr")
    private int startLine;

    @JsonProperty("endLNr")
    private int endLine;

    @JsonProperty("startCol")
    private int startColumn;

    @JsonProperty("endCol")
    private int endColumn;

    @JsonProperty("logLvl")
    private UltimateResultLevel level;

    @JsonProperty("type")
    private UltimateResultType type;


    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public UltimateResultLevel getLevel() {
        return level;
    }

    public UltimateResultType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UltimateResult{" +
                "shortDescription='" + shortDescription + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", startLine=" + startLine +
                ", endLine=" + endLine +
                ", startColumn=" + startColumn +
                ", endColumn=" + endColumn +
                ", level=" + level +
                ", type=" + type +
                '}';
    }
}
