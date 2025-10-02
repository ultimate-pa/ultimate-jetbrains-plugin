package de.uni_freiburg.informatik.ultimate.intellij.window.tab;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
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
import de.uni_freiburg.informatik.ultimate.intellij.window.result.ResultInfoPanel;
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
import java.util.stream.Collectors;

public class FileInfoTab extends BaseTab {

    private final JPanel contentPanel;

    private final SimpleTree tree;
    private final Project project;

    private VirtualFile viewedFile;

    public FileInfoTab(Project project) {
        this.project = project;
        this.contentPanel = new JPanel(new BorderLayout());

        JPanel toolbarPanel = WindowFactory.createToolbarPanel(this.contentPanel);
        this.contentPanel.add(toolbarPanel, BorderLayout.WEST);

        this.tree = new SimpleTree();
        this.tree.setCellRenderer(new UltimateTreeCellRenderer());

        this.tree.getEmptyText()
                .clear()
                .appendLine("File not analysed.", SimpleTextAttributes.GRAYED_ATTRIBUTES, null)
                .appendLine("Analyse this file with Ultimate Automizer", SimpleTextAttributes.LINK_ATTRIBUTES, event -> {
                    ActionManager am = ActionManager.getInstance();
                    AnAction action = am.getAction("de.uni_freiburg.informatik.ultimate.intellij.action.AnalyseAction");

                    AnActionEvent actionEvent = new AnActionEvent(
                            DataManager.getInstance().getDataContext(),
                            new Presentation(),
                            ActionPlaces.UNKNOWN,
                            ActionUiKind.NONE,
                            null, 0, am
                    );

                    ActionUtil.invokeAction(action, actionEvent, null);
                });

        ResultInfoPanel infoPanel = new ResultInfoPanel();

        JBScrollPane scrollPane = new JBScrollPane(tree);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setViewportBorder(JBUI.Borders.empty());

        // Split view: Tree (left) | Info (right)
        JBSplitter fileInfoSplitter = new JBSplitter();
        fileInfoSplitter.setFirstComponent(scrollPane);
        fileInfoSplitter.setSecondComponent(infoPanel);
        fileInfoSplitter.setProportion(0.7f);

        this.contentPanel.add(fileInfoSplitter, BorderLayout.CENTER);

        this.tree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path == null) return;

            Object last = path.getLastPathComponent();
            if (last instanceof ResultNode rn && rn.getUserObject() instanceof ResultNode.ResultInfo result) {
                infoPanel.setVisible(true);
                infoPanel.updateInfo(result);
                navigateTo(result);
            } else {
                infoPanel.setVisible(false);
            }
        });

        update();
    }

    @Override
    public Content getContent() {
        return ContentFactory.getInstance().createContent(contentPanel, "File Info", false);
    }

    @Override
    public void update() {
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        viewedFile = (selectedEditor != null) ? selectedEditor.getFile() : null;

        updateTree();
    }

    private void updateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("File Info");
        tree.setRootVisible(false);

        if (ApplicationManager.getApplication().getService(ResultService.class).hasResults(viewedFile)) {
            updateFileTree(root, viewedFile);
        }

        tree.setModel(new DefaultTreeModel(root));
        tree.expandRow(0); // expand the FileNode by default.
    }

    public void updateFileTree(DefaultMutableTreeNode root, VirtualFile file) {
        ResultService service = ApplicationManager.getApplication().getService(ResultService.class);
        List<UltimateResult> results = service.getResults(file);

        FileNode fileNode = new FileNode(file);

        Map<UltimateResultType, List<UltimateResult>> byType = results.stream().collect(Collectors.groupingBy(UltimateResult::getType));

        for (Map.Entry<UltimateResultType, List<UltimateResult>> entry : byType.entrySet()) {
            TypeNode typeNode = new TypeNode(entry.getKey());
            for (UltimateResult result : entry.getValue()) {
                ResultNode resultNode = new ResultNode(new ResultNode.ResultInfo(result, file));
                typeNode.add(resultNode);
            }
            fileNode.add(typeNode);
        }

        root.add(fileNode);
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
