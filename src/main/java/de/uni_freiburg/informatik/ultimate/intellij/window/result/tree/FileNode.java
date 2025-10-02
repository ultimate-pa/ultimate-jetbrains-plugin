package de.uni_freiburg.informatik.ultimate.intellij.window.result.tree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

public class FileNode extends UltimateTreeNode {

    private final VirtualFile file;

    public FileNode(VirtualFile file) {
        super(file);
        this.file = file;
    }

    @Override
    public Icon getIcon() {
        Icon icon = file.getFileType().getIcon();
        return icon != null ? icon : AllIcons.FileTypes.Unknown;
    }

    @Override
    public String getText() {
        return file.getName();
    }

    @Override
    public String getExtraText() {
        return file.getPresentableUrl();
    }

}