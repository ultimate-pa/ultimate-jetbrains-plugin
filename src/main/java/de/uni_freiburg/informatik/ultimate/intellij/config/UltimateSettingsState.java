package de.uni_freiburg.informatik.ultimate.intellij.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service
@State(name = "UltimateSettings", storages = @Storage("UltimateSettings.xml"))
public final class UltimateSettingsState implements PersistentStateComponent<UltimateSettingsState> {

    private String apiUrl = "https://www.ultimate-pa.org/api"; // default url unless changed by user
    private boolean showHidden = false;
    private boolean showDebug = false;
    private Map<String, String> settingValues = new HashMap<>();

    @Override
    public @NotNull UltimateSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull UltimateSettingsState state) {
        this.apiUrl = state.apiUrl;
        this.showHidden = state.showHidden;
        this.settingValues = new HashMap<>(state.settingValues);
        this.showDebug = state.showDebug;
    }

    public static UltimateSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(UltimateSettingsState.class);
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public boolean isShowDebug() {
        return showDebug;
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public Map<String, String> getSettingValues() {
        return settingValues;
    }

    public void setSettingValues(Map<String, String> settingValues) {
        this.settingValues = settingValues;
    }
}
