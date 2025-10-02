package de.uni_freiburg.informatik.ultimate.intellij.window.tab;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import de.uni_freiburg.informatik.ultimate.intellij.UltimatePlugin;
import de.uni_freiburg.informatik.ultimate.intellij.config.UltimateSettingsState;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static de.uni_freiburg.informatik.ultimate.intellij.window.WindowFactory.createToolbarPanel;

public class DebugConsoleTab extends BaseTab {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static ToolWindow window;
    private static ConsoleView consoleTab;
    private static Content content;

    public DebugConsoleTab(Project project, ToolWindow toolWindow) {
        window = toolWindow;
        consoleTab = new ConsoleViewImpl(project, false);
        JComponent consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(createToolbarPanel(consolePanel), BorderLayout.WEST);
        consolePanel.add(consoleTab.getComponent(), BorderLayout.CENTER);

        content = ContentFactory.getInstance().createContent(consolePanel, "Debug Console", false);
        Disposer.register(content, consoleTab);

        logToDebugConsole("Plugin version: " + UltimatePlugin.getPluginVersion(), ConsoleViewContentType.NORMAL_OUTPUT);
    }

    @Override
    public Content getContent() {
        return content;
    }

    @Override
    public void update() {
        boolean isConsoleShown = window.getContentManager().getIndexOfContent(content) != -1;

        if (UltimateSettingsState.getInstance().isShowDebug()) {
            if (!isConsoleShown) window.getContentManager().addContent(content);
        } else {
            if (isConsoleShown) window.getContentManager().removeContent(content, false);
        }
    }

    public static void logToDebugConsole(String message, ConsoleViewContentType type) {
        if (consoleTab != null) {
            consoleTab.print(String.format("[%s] %s \n", LocalDateTime.now().format(formatter), message), type);
        }
    }

    public static void clearConsole() {
        if (consoleTab != null) {
            consoleTab.clear();
        }
    }
}
