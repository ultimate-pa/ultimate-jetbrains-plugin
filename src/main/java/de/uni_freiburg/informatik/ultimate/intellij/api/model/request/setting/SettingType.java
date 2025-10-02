package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SettingType {

    @JsonProperty("string") STRING,
    @JsonProperty("int") INT,
    @JsonProperty("bool") BOOL

}
