package de.uni_freiburg.informatik.ultimate.intellij.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import de.uni_freiburg.informatik.ultimate.intellij.UltimatePlugin;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnalyseManyAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        FileSelectionDialog dialog = new FileSelectionDialog(project);
        if (dialog.showAndGet()) {
            Collection<VirtualFile> selectedFiles = dialog.getSelectedFiles();
            if (!selectedFiles.isEmpty()) {
                UltimatePlugin plugin = ApplicationManager.getApplication().getService(UltimatePlugin.class);
                plugin.handleAnalysis(project, selectedFiles);
            }
        }
    }

    private static class FileSelectionDialog extends DialogWrapper {

        private final Project project;
        private CheckboxTree tree;
        private CheckedTreeNode rootNode;
        private JBCheckBox showAllCheckBox;
        private boolean showAll = false;

        public FileSelectionDialog(Project project) {
            super(project);
            this.project = project;
            setTitle("Select Files/Folders to Analyse");
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JBPanel<>();
            panel.setLayout(new BorderLayout());

            rootNode = new CheckedTreeNode("Project");
            buildTree(rootNode);

            // File tree with icons
            tree = new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer() {
                @Override
                public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    if (value instanceof CheckedTreeNode node) {
                        Object userObj = node.getUserObject();
                        if (userObj instanceof VirtualFile vf) {
                            ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
                            SimpleTextAttributes attributes;

                            // Render excluded files with a gray name when show all files is enabled
                            if (fileIndex.isExcluded(vf)) {
                                attributes = SimpleTextAttributes.GRAYED_ATTRIBUTES;
                            } else {
                                attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
                            }

                            getTextRenderer().append(vf.getName(), attributes);
                            getTextRenderer().setIcon(getFileIcon(vf));

                        } else {
                            getTextRenderer().append(value.toString());
                        }
                    }
                }
            }, rootNode);

            JBScrollPane scrollPane = new JBScrollPane(tree);
            scrollPane.setPreferredSize(new Dimension(450, 500));
            panel.add(scrollPane, BorderLayout.CENTER);

            showAllCheckBox = new JBCheckBox("Show all files", false);
            showAllCheckBox.addActionListener(e -> {
                showAll = showAllCheckBox.isSelected();
                rebuildTree();
            });

            panel.add(showAllCheckBox, BorderLayout.SOUTH);

            return panel;
        }

        private void rebuildTree() {
            rootNode.removeAllChildren();
            buildTree(rootNode);
            ((DefaultTreeModel) tree.getModel()).reload();
        }

        private void buildTree(CheckedTreeNode node) {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
                FileType cFileType = FileTypeManager.getInstance().getFileTypeByExtension("c"); // only show .c files
                addFilesRecursive(node, baseDir, fileIndex, cFileType);
            }
        }

        private boolean addFilesRecursive(CheckedTreeNode node, VirtualFile file, ProjectFileIndex fileIndex, FileType cFileType) {
            if (file == null || !file.isValid()) return false;

            boolean isCFile = file.getFileType() == cFileType;
            boolean isExcluded = fileIndex.isExcluded(file);

            if (file.isDirectory()) {
                CheckedTreeNode dirNode = new CheckedTreeNode(file);
                boolean hasVisibleChild = false;

                for (VirtualFile child : file.getChildren()) {
                    hasVisibleChild |= addFilesRecursive(dirNode, child, fileIndex, cFileType);
                }

                if (hasVisibleChild) {
                    node.add(dirNode);
                    return true;
                } else {
                    return false;
                }
            } else {
                if (showAll || (!isExcluded && isCFile)) {
                    node.add(new CheckedTreeNode(file));
                    return true;
                } else {
                    return false;
                }
            }
        }

        public Collection<VirtualFile> getSelectedFiles() {
            List<VirtualFile> selected = new ArrayList<>();
            collectCheckedNodes(rootNode, selected);
            return selected;
        }

        private void collectCheckedNodes(CheckedTreeNode node, List<VirtualFile> list) {
            if (node.isChecked() && node.getUserObject() instanceof VirtualFile vf && !vf.isDirectory()) {
                list.add(vf);
            }

            for (int i = 0; i < node.getChildCount(); i++) {
                collectCheckedNodes((CheckedTreeNode) node.getChildAt(i), list);
            }
        }

        private Icon getFileIcon(VirtualFile file) {
            if (file.isDirectory()) {
                return AllIcons.Nodes.Folder;
            } else {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

                Icon icon;
                if (psiFile != null) {
                    icon = psiFile.getIcon(Iconable.ICON_FLAG_READ_STATUS);
                } else {
                    icon = file.getFileType().getIcon();
                }
                return icon;
            }
        }
    }
}
