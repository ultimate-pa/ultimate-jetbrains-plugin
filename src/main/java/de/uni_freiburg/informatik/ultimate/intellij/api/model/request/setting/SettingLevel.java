package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SettingLevel {

    @JsonProperty("BASIC") BASIC("Basic", "Settings for defining output and assumptions about the input.\nAll combinations of settings are compatible to each other."),
    @JsonProperty("EXPERT") EXPERT("Expert", "Settings that often have an effect of the performance of a tool and might reduce the soundness.\nCombinations of different values may not always make sense."),
    @JsonProperty("EXPERIMENTAL") EXPERIMENTAL("Experimental", "Settings which enable features that have not yet been tested sufficiently, may be immature and might get removed.");

    private final String display;
    private final String description;

    SettingLevel(String display, String description) {
        this.display = display;
        this.description = description;
    }

    public String getDisplay() {
        return display;
    }

    public String getDescription() {
        return description;
    }
}
