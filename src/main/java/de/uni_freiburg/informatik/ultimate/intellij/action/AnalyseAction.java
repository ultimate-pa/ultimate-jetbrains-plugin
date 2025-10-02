package de.uni_freiburg.informatik.ultimate.intellij.action;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.uni_freiburg.informatik.ultimate.intellij.UltimatePlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class AnalyseAction extends AnAction {

    private final NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Ultimate");

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        if (project == null) return;

        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        VirtualFile viewedFile = (selectedEditor != null) ? selectedEditor.getFile() : null;

        if (viewedFile == null) {
            notificationGroup.createNotification("No open file", "You must open the file in the editor to submit it to Ultimate Automizer", NotificationType.ERROR).notify(project);
            return;
        }

        UltimatePlugin plugin = ApplicationManager.getApplication().getService(UltimatePlugin.class);
        plugin.handleAnalysis(project, Collections.singletonList(viewedFile));
    }
}
