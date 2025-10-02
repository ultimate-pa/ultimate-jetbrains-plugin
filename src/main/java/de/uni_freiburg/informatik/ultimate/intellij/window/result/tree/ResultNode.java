package de.uni_freiburg.informatik.ultimate.intellij.window.result.tree;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;

import javax.swing.*;

public class ResultNode extends UltimateTreeNode {

    private final ResultInfo result;

    public record ResultInfo(UltimateResult ultimateResult, VirtualFile file) {}

    public ResultNode(ResultInfo result) {
        super(result);
        this.result = result;
    }

    @Override
    public Icon getIcon() {
        return switch (result.ultimateResult.getLevel()) {
            case INFO -> AllIcons.General.Information;
            case WARNING -> AllIcons.General.Warning;
            case ERROR -> AllIcons.General.Error;
        };
    }

    @Override
    public String getText() {
        return result.ultimateResult.getShortDescription();
    }

    public UltimateResult getResult() {
        return result.ultimateResult;
    }

    @Override
    public String getExtraText() {
        if(result.ultimateResult.getStartLine() == result.ultimateResult.getEndLine()) return String.format("[:%s]", result.ultimateResult.getStartLine());
        else return String.format("[%s:%s]", result.ultimateResult.getStartLine(), result.ultimateResult.getEndLine());
    }
}