package de.uni_freiburg.informatik.ultimate.intellij.window.result.tree;

import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class TypeNode extends UltimateTreeNode {

    private final UltimateResultType type;

    public TypeNode(UltimateResultType type) {
        super(type);
        this.type = type;
    }

    @Override
    public Icon getIcon() {
        return type.getIcon();
    }

    @Override
    public String getText() {
        return type.getDisplayName();
    }

    @Override
    public String getExtraText() {
        return String.format("%s", countResultNodes(this));
    }


    private int countResultNodes(DefaultMutableTreeNode parent) {
        int count = 0;

        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeNode child = parent.getChildAt(i);

            if (child instanceof ResultNode) {
                count++;
            }

            if (child instanceof DefaultMutableTreeNode node) {
                count += countResultNodes(node);
            }
        }

        return count;
    }
}