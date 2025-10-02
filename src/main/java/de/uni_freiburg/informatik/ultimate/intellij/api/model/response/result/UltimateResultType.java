package de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum UltimateResultType {

    // Results
    INVARIANT("invariant", "Invariant", AllIcons.General.Information),
    COUNTER("counter", "Counterexample", AllIcons.General.ExclMark),
    POSITIVE("positive", "Positive", AllIcons.Status.Success),
    UNPROVABLE("unprovable", "Unprovable", AllIcons.RunConfigurations.TestSkipped),
    UNKNOWN("unknown", "Unknown", AllIcons.General.Information),

    BENCHMARK("benchmark", "Benchmark", AllIcons.General.Information),

    // Tool errors
    EXCEPTION_OR_ERROR("ExceptionOrError", "Exception or Error", AllIcons.General.Error),
    SYNTAX_ERROR("syntaxError", "Syntax Error", AllIcons.General.Error),
    SYNTAX_UNSUPPORTED("syntaxUnsupported", "Unsupported Syntax", AllIcons.General.Error),
    TYPE_ERROR("typeError", "Type Error", AllIcons.General.Error),
    TIMEOUT("timeout", "Timeout", AllIcons.General.Warning),

    INFO("info", "Info (Analysis)", AllIcons.General.Information),
    WARNING("warning", "Warning (Analysis)", AllIcons.General.Warning),
    ERROR("error", "Error (Analysis)", AllIcons.General.Error),

    // Missing results
    NO_RESULT("noResult", "No Result", AllIcons.General.Information),
    UNDEF("UNDEF", "Undefined", AllIcons.General.Information);

    private final String value;
    private final String displayName;
    private final Icon icon;

    UltimateResultType(String value, String displayName, Icon icon) {
        this.value = value;
        this.displayName = displayName;
        this.icon = icon;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    @JsonCreator
    public static UltimateResultType fromString(String type) {
        for (UltimateResultType t : UltimateResultType.values()) {
            if (t.getValue().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown UltimateResultType: " + type);
    }

}
