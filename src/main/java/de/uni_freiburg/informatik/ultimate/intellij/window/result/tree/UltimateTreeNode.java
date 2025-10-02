package de.uni_freiburg.informatik.ultimate.intellij.window.result.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class UltimateTreeNode extends DefaultMutableTreeNode {

    public UltimateTreeNode(Object userObject) {
        super(userObject);
    }

    public abstract Icon getIcon();

    public abstract String getText();

    public abstract String getExtraText();
}