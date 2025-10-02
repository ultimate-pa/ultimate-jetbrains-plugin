package de.uni_freiburg.informatik.ultimate.intellij.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import de.uni_freiburg.informatik.ultimate.intellij.ResultService;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResult;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultLevel;
import de.uni_freiburg.informatik.ultimate.intellij.api.model.response.result.UltimateResultType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class UltimateAnnotator extends ExternalAnnotator<PsiFile, List<UltimateResult>> {

    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Override
    public List<UltimateResult> doAnnotate(PsiFile file) {
        ResultService service = ApplicationManager.getApplication().getService(ResultService.class);
        return service.getResults(file.getVirtualFile());
    }

    @Override
    public void apply(@NotNull PsiFile file, List<UltimateResult> results, @NotNull AnnotationHolder holder) {
        Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (doc == null) return;

        MarkupModel markupModel = FileEditorManager.getInstance(file.getProject()).getSelectedTextEditor().getMarkupModel();
        markupModel.removeAllHighlighters();

        for (UltimateResult result : results) {
            int startOffset = getOffset(doc, result.getStartLine(), result.getStartColumn());
            int endOffset = getOffset(doc, result.getEndLine(), result.getEndColumn());

            TextRange range = new TextRange(startOffset, endOffset);

            TextAttributes attributes = new TextAttributes();

            attributes.setBackgroundColor(mapColor(result.getType()));
            attributes.setFontType(Font.BOLD);

            markupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.ADDITIONAL_SYNTAX, attributes, HighlighterTargetArea.EXACT_RANGE);

            holder.newAnnotation(mapSeverity(result.getLevel()), "[" + result.getType().name().toLowerCase() + "] " + result.getShortDescription())
                    .range(range)
                    .tooltip(result.getLongDescription())
                    .create();
        }
    }

    private HighlightSeverity mapSeverity(UltimateResultLevel level) {
        return switch (level) {
            case INFO -> HighlightSeverity.INFORMATION;
            case WARNING -> HighlightSeverity.WARNING;
            case ERROR -> HighlightSeverity.ERROR;
        };
    }

    private Color mapColor(UltimateResultType type) {
        return switch (type) {
            case COUNTER, ERROR, EXCEPTION_OR_ERROR, SYNTAX_ERROR, SYNTAX_UNSUPPORTED, TYPE_ERROR -> new Color(255, 0, 0, 50);
            case WARNING, UNPROVABLE -> new Color(255, 255, 0, 50);
            case INVARIANT, POSITIVE -> new Color(0, 255, 0, 50);
            case INFO -> new Color(0, 128, 255, 50);
            case UNKNOWN, TIMEOUT, UNDEF, NO_RESULT, BENCHMARK -> new Color(0, 128, 255, 50);
        };
    }

    private int getOffset(Document doc, int line, int col) {
        int lineStart = doc.getLineStartOffset(Math.max(0, line - 1));
        return Math.min(doc.getTextLength(), lineStart + Math.max(0, col - 1));
    }
}