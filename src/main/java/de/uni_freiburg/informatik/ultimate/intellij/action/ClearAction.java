package de.uni_freiburg.informatik.ultimate.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;
import de.uni_freiburg.informatik.ultimate.intellij.ResultService;
import de.uni_freiburg.informatik.ultimate.intellij.window.WindowFactory;
import de.uni_freiburg.informatik.ultimate.intellij.window.tab.DebugConsoleTab;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ClearAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ResultService service = ApplicationManager.getApplication().getService(ResultService.class);
            Set<VirtualFile> files = new HashSet<>(service.getAllResults().keySet());

            service.clearResults();
            DebugConsoleTab.clearConsole();

            FileContentUtil.reparseFiles(Objects.requireNonNull(e.getProject()), files, false);
            WindowFactory.update();
        });
    }

}