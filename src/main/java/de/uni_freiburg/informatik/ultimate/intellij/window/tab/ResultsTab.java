package de.uni_freiburg.informatik.ultimate.intellij.window.tab;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ui.JBUI;
import de.uni_freiburg.informatik.ultimate.intellij.ResultService;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultType;
import de.uni_freiburg.informatik.ultimate.intellij.window.WindowFactory;
import de.uni_freiburg.informatik.ultimate.intellij.window.result.tree.FileNode;
import de.uni_freiburg.informatik.ultimate.intellij.window.result.tree.ResultNode;
import de.uni_freiburg.informatik.ultimate.intellij.window.result.tree.TypeNode;
import de.uni_freiburg.informatik.ultimate.intellij.window.result.tree.UltimateTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResultsTab extends BaseTab {

    private final JPanel contentPanel;
    private final SimpleTree tree;
    private final Project project;

    public ResultsTab(Project project) {
        this.project = project;
        this.contentPanel = new JPanel(new BorderLayout());

        JPanel toolbarPanel = WindowFactory.createToolbarPanel(this.contentPanel);
        this.contentPanel.add(toolbarPanel, BorderLayout.WEST);

        this.tree = new SimpleTree();
        this.tree.setCellRenderer(new UltimateTreeCellRenderer());

        this.tree.getEmptyText()
                .clear()
                .appendLine("No results yet.", SimpleTextAttributes.GRAYED_ATTRIBUTES, null)
                .appendLine("Select files to analyse with Ultimate Automizer...", SimpleTextAttributes.LINK_ATTRIBUTES, event -> {
                    ActionManager am = ActionManager.getInstance();
                    AnAction action = am.getAction("de.uni_freiburg.informatik.ultimate.intellij.action.AnalyseManyAction");

                    AnActionEvent actionEvent = new AnActionEvent(
                            DataManager.getInstance().getDataContext(),
                            new Presentation(),
                            ActionPlaces.UNKNOWN,
                            ActionUiKind.NONE,
                            null, 0, am
                    );

                    ActionUtil.invokeAction(action, actionEvent, null);
                });

        this.tree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path == null) return;

            Object last = path.getLastPathComponent();
            if (last instanceof ResultNode rn && rn.getUserObject() instanceof ResultNode.ResultInfo result) {
                navigateTo(result);
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(tree);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setViewportBorder(JBUI.Borders.empty());

        this.contentPanel.add(scrollPane, BorderLayout.CENTER);

        update();
    }

    @Override
    public Content getContent() {
        return ContentFactory.getInstance().createContent(contentPanel, "Results", false);
    }

    @Override
    public void update() {
        updateTree();
    }

    private void updateTree() {
        Set<TreePath> expanded = tree.getExpandedPaths();
        TreePath selection = tree.getSelectionPath();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(""); // hide actual root of all type nodes
        tree.setRootVisible(false);

        ResultService service = ApplicationManager.getApplication().getService(ResultService.class);
        Map<VirtualFile, List<UltimateResult>> files = service.getAllResults();

        // Build index: type -> file -> results
        Map<UltimateResultType, Map<VirtualFile, List<UltimateResult>>> byType = files.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(r -> Map.entry(e.getKey(), r)))
                .collect(Collectors.groupingBy(entry -> entry.getValue().getType(),
                        Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList()))
                ));

        for (Map.Entry<UltimateResultType, Map<VirtualFile, List<UltimateResult>>> typeEntry : byType.entrySet()) {
            TypeNode typeNode = new TypeNode(typeEntry.getKey());
            root.add(typeNode);

            for (Map.Entry<VirtualFile, List<UltimateResult>> fileEntry : typeEntry.getValue().entrySet()) {
                FileNode fileNode = new FileNode(fileEntry.getKey());
                typeNode.add(fileNode);

                for (UltimateResult result : fileEntry.getValue()) {
                    ResultNode resultNode = new ResultNode(new ResultNode.ResultInfo(result, fileEntry.getKey()));
                    fileNode.add(resultNode);
                }
            }
        }

        tree.setModel(new DefaultTreeModel(root));

        for (TreePath p : expanded) {
            tree.expandPath(p);
        }

        if (selection != null) {
            tree.setSelectionPath(selection);
        }
    }

    private void navigateTo(ResultNode.ResultInfo result) {
        VirtualFile file = result.file();
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return;

        int offset = getOffset(document, result.ultimateResult().getStartLine(), result.ultimateResult().getStartColumn());
        FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, file, offset), true);
    }

    private int getOffset(Document doc, int line, int col) {
        int lineStart = doc.getLineStartOffset(Math.max(0, line - 1));
        return Math.min(doc.getTextLength(), lineStart + Math.max(0, col - 1));
    }
}
