package de.uni_freiburg.informatik.ultimate.intellij.window.tab;

import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.content.Content;

public abstract class BaseTab implements DumbAware {

    public abstract Content getContent();

    public abstract void update();

}
