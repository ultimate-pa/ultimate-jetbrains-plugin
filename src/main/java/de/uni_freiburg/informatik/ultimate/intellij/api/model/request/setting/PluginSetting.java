package de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.ConfiguredPluginSetting;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginSetting {

    @JsonProperty("plugin_id")
    private String pluginId;

    @JsonProperty("default")
    private Object defaultValue;

    @JsonProperty("visible")
    private boolean visible;

    @JsonProperty("level")
    private SettingLevel level;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("options")
    private List<String> options;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private SettingType type;

    @JsonProperty("key")
    private String key;

    // Required for jackson
    public PluginSetting() {

    }

    protected PluginSetting(PluginSetting other) {
        this.pluginId = other.pluginId;
        this.defaultValue = other.defaultValue;
        this.visible = other.visible;
        this.level = other.level;
        this.name = other.name;
        this.description = other.description;
        this.options = other.options;
        this.id = other.id;
        this.type = other.type;
        this.key = other.key;
    }

    public ConfiguredPluginSetting toConfigured(Object value) {
        return new ConfiguredPluginSetting(this, value);
    }

    public String getPluginId() {
        return pluginId;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isVisible() {
        return visible;
    }

    public SettingLevel getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getId() {
        return id;
    }

    public SettingType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "PluginSetting{" +
                "pluginId='" + pluginId + '\'' +
                ", defaultValue=" + defaultValue +
                ", visible=" + visible +
                ", level=" + level +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                ", id='" + id + '\'' +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
