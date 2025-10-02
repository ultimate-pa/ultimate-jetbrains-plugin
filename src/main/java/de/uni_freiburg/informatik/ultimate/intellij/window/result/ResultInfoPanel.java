package de.uni_freiburg.informatik.ultimate.intellij.window.result;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultLevel;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultType;
import de.uni_freiburg.informatik.ultimate.intellij.window.result.tree.ResultNode;

import javax.swing.*;
import java.awt.*;

public class ResultInfoPanel extends JPanel {

    private final JBLabel shortDescLabel;

    private final JBLabel typeLabel;
    private final JBLabel levelLabel;

    private final JBTextArea descriptionText;

    public ResultInfoPanel() {
        super(new BorderLayout());
        this.setBorder(JBUI.Borders.empty(8));

        // Title
        shortDescLabel = new JBLabel("Select a result");
        shortDescLabel.setFont(shortDescLabel.getFont().deriveFont(Font.BOLD, 14f));
        shortDescLabel.setAlignmentX(LEFT_ALIGNMENT);

        // type and level line
        JPanel typeLinePanel = new JBPanel<>();
        typeLinePanel.setLayout(new BoxLayout(typeLinePanel, BoxLayout.X_AXIS));
        typeLinePanel.setAlignmentX(LEFT_ALIGNMENT);

        // level icon label
        levelLabel = new JBLabel();
        levelLabel.setFont(levelLabel.getFont().deriveFont(Font.PLAIN, 12f));
        levelLabel.setIconTextGap(4);

        // Type label
        typeLabel = new JBLabel();
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.PLAIN, 12f));
        typeLabel.setIconTextGap(4);

        typeLinePanel.add(levelLabel);
        typeLinePanel.add(Box.createHorizontalStrut(4));

        JSeparator vertSep = new JSeparator(SwingConstants.VERTICAL);
        vertSep.setMaximumSize(new Dimension(4, 16)); // fixed width and height, otherwise expands to full size
        typeLinePanel.add(vertSep);

        typeLinePanel.add(Box.createHorizontalStrut(4));
        typeLinePanel.add(typeLabel);

        // Panel for title, level, and type
        JPanel titlePanel = new JBPanel<>();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(shortDescLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(typeLinePanel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(new JSeparator());
        titlePanel.add(Box.createVerticalStrut(4));

        // Long description
        descriptionText = new JBTextArea();
        descriptionText.setEditable(false);
        descriptionText.setFocusable(false);
        descriptionText.setHighlighter(null);
        descriptionText.setCursor(Cursor.getDefaultCursor());
        descriptionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        descriptionText.setLineWrap(false);
        descriptionText.setWrapStyleWord(false);

        JBScrollPane scrollPane = new JBScrollPane(descriptionText);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(titlePanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        // hidden until a result is selected
        this.setVisible(false);
    }

    public void updateInfo(ResultNode.ResultInfo resultInfo) {
        if (resultInfo == null || resultInfo.ultimateResult() == null) {
            this.setVisible(false);
            return;
        }

        UltimateResult result = resultInfo.ultimateResult();
        UltimateResultType type = result.getType();
        UltimateResultLevel level = result.getLevel();

        // short description as title
        String shortDesc = result.getShortDescription();
        if (shortDesc == null || shortDesc.isEmpty()) {
            shortDesc = "(No short description)";
        }

        shortDescLabel.setText("<html>" + escapeHtml(shortDesc) + "</html>");

        // Level type with icon
        levelLabel.setText(level.getDisplayName());
        levelLabel.setIcon(level.getIcon());

        // Result type with icon
        typeLabel.setText(type.getDisplayName());
        typeLabel.setIcon(type.getIcon());

        // Long description
        String longDesc = result.getLongDescription();
        if (longDesc == null || longDesc.isEmpty()) {
            longDesc = "(No long description)";
        }

        descriptionText.setText(longDesc);
        descriptionText.setCaretPosition(0);

        this.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
