package de.uni_freiburg.informatik.ultimate.intellij.config;

import com.intellij.icons.AllIcons;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import de.uni_freiburg.informatik.ultimate.intellij.UltimatePlugin;
import de.uni_freiburg.informatik.ultimate.intellij.api.UltimateApiService;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.PluginSetting;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.SettingLevel;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.request.setting.SettingType;
import de.uni_freiburg.informatik.ultimate.intellij.window.WindowFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class UltimateConfigurable implements Configurable {

    private UltimateApiService api;
    private List<PluginSetting> settings;
    private JPanel rootPanel;

    private JBTextField apiUrlField;
    private JButton apiCheckButton;
    private JLabel apiCheckStatus;

    private JCheckBox allSettingsCheckbox;
    private JCheckBox debugConsoleCheckbox;

    private final Map<String, JComponent> components = new HashMap<>();

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Ultimate Automizer";
    }

    @Override
    public @Nullable JComponent createComponent() {
        api = ApplicationManager.getApplication().getService(UltimatePlugin.class).getApiService();

        UltimateSettingsState state = UltimateSettingsState.getInstance();
        settings = List.of(api.getDefaultSettings());

        apiUrlField = new JBTextField(state.getApiUrl(), 20);
        apiUrlField.setMaximumSize(apiUrlField.getPreferredSize());

        rootPanel = new JBPanel<>(new BorderLayout());

        apiCheckStatus = new JLabel();
        apiCheckStatus.setVisible(false);

        apiCheckButton = new JButton("Check");
        apiCheckButton.addActionListener(e -> {
            apiCheckStatus.setVisible(false);
            apiCheckButton.setEnabled(false);

            String url = apiUrlField.getText();

            if (url == null || url.isBlank()) {
                updateStatus(false, "Failed to connect: URL is empty");
                return;
            }

            try {
                URI u = URI.create(url);
                String scheme = u.getScheme();
                if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || u.getHost() == null) {
                    throw new IllegalArgumentException("Invalid HTTP URL");
                }
            } catch (Exception ex) {
                updateStatus(false, "Failed to connect: Invalid URL");
                return;
            }

            api.getVersion(url).thenAccept(version -> {
                boolean success = version != null;
                String message = success ? "Found server with version: " + version.getVersion() : "Found server with malformed response.";
                updateStatus(success, message);
            }).exceptionally(throwable -> {
                updateStatus(false, "Failed to connect: " + throwable.getMessage());
                return null;
            });
        });

        allSettingsCheckbox = new JBCheckBox("Show all settings", state.isShowHidden());
        allSettingsCheckbox.addActionListener(e -> rebuildForm());

        debugConsoleCheckbox = new JBCheckBox("Show debug console", state.isShowDebug());
        debugConsoleCheckbox.addActionListener(e -> rebuildForm());

        rebuildForm();

        return rootPanel;
    }

    private void updateStatus(boolean success, String message) {
        apiCheckStatus.setVisible(true);
        apiCheckButton.setEnabled(true);
        apiCheckStatus.setText(message);
        apiCheckStatus.setForeground(success ? Color.GREEN : Color.RED);
    }

    private void rebuildForm() {
        components.clear();
        rootPanel.removeAll();

        FormBuilder formBuilder = FormBuilder.createFormBuilder();

        JPanel apiPanel = new JPanel();
        apiPanel.setLayout(new BoxLayout(apiPanel, BoxLayout.X_AXIS));

        JLabel apiLabel = new JLabel("Ultimate API URL:");
        apiLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        apiPanel.add(apiLabel);
        apiPanel.add(apiUrlField);
        apiPanel.add(apiCheckButton);
        apiPanel.add(Box.createHorizontalGlue());
        formBuilder.addComponent(apiPanel);

        formBuilder.addComponent(apiCheckStatus);

        formBuilder.addComponent(debugConsoleCheckbox);
        formBuilder.addComponent(allSettingsCheckbox);

        JButton resetDefaultsButton = new JButton("Reset settings to defaults");
        resetDefaultsButton.addActionListener(e -> resetToDefaults(true));

        formBuilder.addVerticalGap(8);
        formBuilder.addComponent(resetDefaultsButton);
        formBuilder.addVerticalGap(8);
        formBuilder.addSeparator();
        formBuilder.addVerticalGap(8);

        // Group settings by level and sort by name
        Map<SettingLevel, List<PluginSetting>> grouped = new EnumMap<>(SettingLevel.class);
        grouped.put(SettingLevel.BASIC, new ArrayList<>());
        grouped.put(SettingLevel.EXPERT, new ArrayList<>());
        grouped.put(SettingLevel.EXPERIMENTAL, new ArrayList<>());

        for (PluginSetting setting : settings) {
            grouped.get(setting.getLevel()).add(setting);
        }

        // Sort settings by name
        Comparator<PluginSetting> byName = Comparator.comparing(PluginSetting::getName, String.CASE_INSENSITIVE_ORDER);

        // Render each category with a title, description, list of settings, and separator
        for(Map.Entry<SettingLevel, List<PluginSetting>> entry : grouped.entrySet()) {
            SettingLevel level = entry.getKey();
            List<PluginSetting> sorted = entry.getValue().stream().filter(setting -> allSettingsCheckbox.isSelected() || setting.isVisible()).sorted(byName).toList();

            if(!sorted.isEmpty()) {
                addCategory(formBuilder, level.getDisplay(), level.getDescription(), sorted);
                formBuilder.addVerticalGap(8);
                formBuilder.addSeparator();
                formBuilder.addVerticalGap(8);
            }
        }

        rootPanel.add(formBuilder.getPanel(), BorderLayout.NORTH);
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    @Override
    public boolean isModified() {
        UltimateSettingsState state = UltimateSettingsState.getInstance();

        if (!Objects.equals(apiUrlField.getText(), state.getApiUrl())) return true;
        if (allSettingsCheckbox.isSelected() != state.isShowHidden()) return true;
        if (debugConsoleCheckbox.isSelected() != state.isShowDebug()) return true;

        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            String value = getValueFromComponent(entry.getValue());

            if (!Objects.equals(state.getSettingValues().get(entry.getKey()), value)) return true;
        }

        return false;
    }

    @Override
    public void apply() {
        UltimateSettingsState state = UltimateSettingsState.getInstance();

        state.setApiUrl(apiUrlField.getText());
        api.setBaseUrl(apiUrlField.getText());

        state.setShowHidden(allSettingsCheckbox.isSelected());
        state.setShowDebug(debugConsoleCheckbox.isSelected());

        for (Map.Entry<String, JComponent> entry : components.entrySet()) {
            String key = entry.getKey();
            JComponent comp = entry.getValue();
            String value = getValueFromComponent(comp);
            state.getSettingValues().put(key, value);
        }

        WindowFactory.update();
    }

    @Override
    public void reset() {
        resetToDefaults(false);
    }

    private void addCategory(FormBuilder formBuilder, String title, String description, List<PluginSetting> settingsInCategory) {
        if (settingsInCategory.isEmpty()) {
            return;
        }

        // Category title
        JLabel sectionLabel = new JBLabel(title);
        sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        formBuilder.addComponent(sectionLabel);

        if (description != null && !description.isBlank()) {
            JBLabel subtitle = new JBLabel("<html>" + description.replaceAll("\n", "<br/>") + "</html>");
            subtitle.setForeground(JBColor.GRAY);
            Font f = subtitle.getFont();
            subtitle.setFont(f.deriveFont(Math.max(10f, f.getSize2D() - 1f)));
            subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

            formBuilder.addComponent(subtitle);
        }

        for (PluginSetting setting : settingsInCategory) {
            JComponent control = createComponentForSetting(setting);
            components.put(setting.getId(), control);

            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

            if (setting.getType() == SettingType.BOOL && control instanceof JCheckBox cb) {
                cb.setText(setting.getName());
                cb.setAlignmentY(Component.CENTER_ALIGNMENT);
                new HelpTooltip().setDescription("<html>Plugin ID: " + setting.getPluginId() + "<br /><br />Setting ID: " + setting.getId() + "</html>").installOn(cb);
                row.add(cb);
                row.add(Box.createHorizontalStrut(4));
                addHelp(row, setting);
            } else { // string and integer setting types
                JLabel label = new JLabel(setting.getName());
                new HelpTooltip().setDescription("<html>Plugin ID: " + setting.getPluginId() + "<br /><br />Setting ID: " + setting.getId() + "</html>").installOn(label);

                row.add(Box.createHorizontalStrut(4));
                row.add(label);
                addHelp(row, setting);
                row.add(Box.createHorizontalStrut(4));
                row.add(control);
            }

            row.add(Box.createHorizontalGlue());
            formBuilder.addComponent(row);
        }
    }

    private void addHelp(JPanel container, PluginSetting setting) {
        String description = setting.getDescription();
        if (description != null && !description.isBlank()) {
            JLabel helpIcon = new JLabel(AllIcons.General.ContextHelp);
            helpIcon.setFocusable(false);
            helpIcon.setOpaque(false);
            new HelpTooltip().setDescription(description).installOn(helpIcon);
            container.add(Box.createHorizontalStrut(4));
            container.add(helpIcon);
        }
    }

    private String getValueFromComponent(JComponent comp) {
        if (comp instanceof JComboBox) {
            return Objects.toString(((JComboBox<?>) comp).getSelectedItem(), "");
        } else if (comp instanceof JCheckBox) {
            return Boolean.toString(((JCheckBox) comp).isSelected());
        } else if (comp instanceof JSpinner) {
            return Objects.toString(((JSpinner) comp).getValue(), "");
        } else if (comp instanceof JTextField) {
            return ((JTextField) comp).getText();
        }
        return "";
    }

    private JComponent createComponentForSetting(PluginSetting setting) {
        Object defaultVal = setting.getDefaultValue();
        String savedVal = UltimateSettingsState.getInstance().getSettingValues().get(setting.getKey());

        return switch (setting.getType()) {
            case STRING:
                if (setting.getOptions() != null && !setting.getOptions().isEmpty()) {
                    JComboBox<String> comboBox = new ComboBox<>(setting.getOptions().toArray(new String[0]));
                    comboBox.setSelectedItem(savedVal != null ? savedVal : defaultVal);
                    comboBox.setMaximumSize(comboBox.getPreferredSize()); // TODO: sometimes gives bad dimensions. Frequently too small to actually display the full length name of settings.
                    yield comboBox;
                } else {
                    JTextField textField = new JBTextField(20);
                    textField.setText(savedVal != null ? savedVal : (String) defaultVal);
                    textField.setMaximumSize(textField.getPreferredSize());
                    yield textField;
                }

            case BOOL:
                yield new JBCheckBox(setting.getName(), savedVal != null ? Boolean.parseBoolean(savedVal) : (Boolean) defaultVal);

            case INT:
                int initial = (savedVal != null) ? Integer.parseInt(savedVal) : ((Integer) defaultVal);
                yield new JSpinner(new SpinnerNumberModel(initial, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

        };
    }

    private void resetToDefaults(boolean pluginDefault) {
        UltimateSettingsState state = UltimateSettingsState.getInstance();

        apiUrlField.setText(state.getApiUrl());

        for (PluginSetting setting : settings) {
            JComponent comp = components.get(setting.getId());
            if (comp == null) continue;

            String savedValue = state.getSettingValues().get(setting.getId());
            Object defaultVal = setting.getDefaultValue();

            Object appliedValue;
            if(pluginDefault) {
                appliedValue = defaultVal;
            } else {
                appliedValue = savedValue == null ? defaultVal : savedValue;
            }

            switch (setting.getType()) {
                case STRING:
                    if (comp instanceof JComboBox<?> comboBox) {
                        comboBox.setSelectedItem(appliedValue);
                    } else if (comp instanceof JTextField textField) {
                        textField.setText((String) appliedValue);
                    }
                    break;

                case BOOL:
                    if (comp instanceof JCheckBox checkBox) {
                        checkBox.setSelected(appliedValue instanceof String ? Boolean.parseBoolean((String) appliedValue) : (boolean) appliedValue);
                    }
                    break;

                case INT:
                    if (comp instanceof JSpinner spinner) {
                        spinner.setValue(appliedValue instanceof String ? Integer.parseInt((String) appliedValue) : appliedValue);
                    }
                    break;
            }
        }
    }
}
