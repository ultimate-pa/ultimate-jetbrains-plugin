package de.uni_freiburg.informatik.ultimate.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import de.uni_freiburg.informatik.ultimate.intellij.config.UltimateConfigurable;
import org.jetbrains.annotations.NotNull;

public class SettingsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), UltimateConfigurable.class);
    }

}