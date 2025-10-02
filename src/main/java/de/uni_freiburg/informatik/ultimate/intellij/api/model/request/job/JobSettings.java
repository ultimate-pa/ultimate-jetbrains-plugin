package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.ConfiguredPluginSetting;

import java.util.List;

public class JobSettings {

    @JsonProperty("user_settings")
    public List<ConfiguredPluginSetting> pluginSettings;

    // Required for jackson
    public JobSettings() {

    }

    public JobSettings(List<ConfiguredPluginSetting> settings) {
        this.pluginSettings = settings;
    }

    @Override
    public String toString() {
        return "JobSettings{" +
                "pluginSettings=" + pluginSettings +
                '}';
    }
}
