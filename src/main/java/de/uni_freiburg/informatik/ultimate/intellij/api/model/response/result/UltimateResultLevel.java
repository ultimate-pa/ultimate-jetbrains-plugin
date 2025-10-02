package de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum UltimateResultLevel {

    INFO("Info", AllIcons.General.Information),
    WARNING("Warning", AllIcons.General.Warning),
    ERROR("Error", AllIcons.General.Error);

    private final String displayName;
    private final Icon icon;

    UltimateResultLevel(String displayName, Icon icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    @JsonCreator
    public static UltimateResultLevel fromString(String key) {
        return UltimateResultLevel.valueOf(key.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

}
