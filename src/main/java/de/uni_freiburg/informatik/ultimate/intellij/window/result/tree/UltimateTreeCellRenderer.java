package de.uni_freiburg.informatik.ultimate.intellij.window.result.tree;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UltimateTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof UltimateTreeNode node)) return;

        setIcon(node.getIcon());
        append(node.getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        if(!node.getExtraText().isBlank()) {
            append(" " + node.getExtraText(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
    }

}