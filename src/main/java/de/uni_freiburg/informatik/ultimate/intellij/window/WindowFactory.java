package de.uni_freiburg.informatik.ultimate.intellij.window;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import de.uni_freiburg.informatik.ultimate.intellij.config.UltimateSettingsState;
import de.uni_freiburg.informatik.ultimate.intellij.window.tab.DebugConsoleTab;
import de.uni_freiburg.informatik.ultimate.intellij.window.tab.FileInfoTab;
import de.uni_freiburg.informatik.ultimate.intellij.window.tab.ResultsTab;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class WindowFactory implements ToolWindowFactory, DumbAware, FileEditorManagerListener {

    private static final String ACTION_GROUP = "UltimateActions";
    private static final String ACTION_GROUP_SECONDARY = "UltimateActionsSecondary";

    private static ResultsTab resultsTab;
    private static FileInfoTab fileInfoTab;
    private static DebugConsoleTab debugConsoleTab;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        resultsTab = new ResultsTab(project);
        fileInfoTab = new FileInfoTab(project);
        debugConsoleTab = new DebugConsoleTab(project, toolWindow);

        Content content = resultsTab.getContent();
        Content fileInfo = fileInfoTab.getContent();
        Content consoleContent = debugConsoleTab.getContent();

        toolWindow.getContentManager().addContent(content);
        toolWindow.getContentManager().addContent(fileInfo);

        if(UltimateSettingsState.getInstance().isShowDebug()) {
            toolWindow.getContentManager().addContent(consoleContent);
        }
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        if(fileInfoTab != null) fileInfoTab.update();
    }

    public static JPanel createToolbarPanel(JComponent targetComponent) {
        JPanel toolbarPanel = new JPanel(new BorderLayout());

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(ACTION_GROUP);
        ActionGroup actionGroupSecondary = (ActionGroup) ActionManager.getInstance().getAction(ACTION_GROUP_SECONDARY);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("Ultimate", actionGroup, false);
        ActionToolbar toolbarSecondary = ActionManager.getInstance().createActionToolbar("Ultimate", actionGroupSecondary, false);

        toolbar.setTargetComponent(targetComponent);
        toolbarSecondary.setTargetComponent(targetComponent);

        Box toolbarBox = Box.createHorizontalBox();
        toolbarBox.add(toolbar.getComponent());

        Box secondaryBox = Box.createHorizontalBox();
        secondaryBox.add(toolbarSecondary.getComponent());

        toolbarPanel.add(toolbarBox, BorderLayout.NORTH);
        toolbarPanel.add(secondaryBox, BorderLayout.SOUTH);

        return toolbarPanel;
    }

    public static void update() {
        if(resultsTab != null) resultsTab.update();
        if(fileInfoTab != null) fileInfoTab.update();
        if(debugConsoleTab != null) debugConsoleTab.update();
    }

}
