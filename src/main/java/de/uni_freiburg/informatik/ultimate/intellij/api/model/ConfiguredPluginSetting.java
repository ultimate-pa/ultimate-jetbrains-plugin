package de.uni_freiburg.informatik.ultimate.intellij.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.PluginSetting;

public class ConfiguredPluginSetting extends PluginSetting {

    public ConfiguredPluginSetting(PluginSetting base, Object value) {
        super(base);
        this.value = value;
    }

    @JsonProperty("value")
    public Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ConfiguredPluginSetting{" +
                "pluginId='" + getPluginId() + '\'' +
                ", defaultValue=" + getDefaultValue() +
                ", visible=" + isVisible() +
                ", name='" + getName() + '\'' +
                ", options=" + getOptions() +
                ", id='" + getId() + '\'' +
                ", type='" + getType() + '\'' +
                ", key='" + getKey() + '\'' +
                ", value=" + value +
                '}';
    }
}
