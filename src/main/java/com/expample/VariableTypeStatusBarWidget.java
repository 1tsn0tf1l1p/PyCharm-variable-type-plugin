package com.expample;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A status bar widget that detects a Python variable under the caret in a Python file
 * and displays its inferred type (e.g. "str", "int", etc.) in the status bar.
 *
 * <p>This widget attaches a caret listener to every editor in the project so that whenever the caret moves,
 * it tries to locate a Python variable (a {@link PyTargetExpression}) at or near the caret.
 * If one is found, it uses the Python type evaluation context to determine its type and updates the status bar.</p>
 */
public class VariableTypeStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation, Disposable {
    private final Project project;
    private StatusBar statusBar;
    private String currentText = "empty";

    /**
     * Constructs a new {@code VariableTypeStatusBarWidget} for the given project.
     *
     * <p>This constructor registers an {@link EditorFactoryListener} so that when a new editor is created,
     * a {@link CaretListener} is attached. The caret listener triggers type updates when the caret moves.</p>
     *
     * @param project the current project; must not be null.
     */
    public VariableTypeStatusBarWidget(Project project) {
        this.project = project;

        EditorFactoryListener editorFactoryListener = new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                if (project.equals(editor.getProject())) {
                    editor.getCaretModel().addCaretListener(new CaretListener() {
                        @Override
                        public void caretPositionChanged(@NotNull CaretEvent event) {
                            updateType(editor);
                        }
                    }, VariableTypeStatusBarWidget.this);
                }
            }
        };
        EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener, this);
    }

    /**
     * Updates the widget's displayed type by inspecting the PSI element at the current caret position.
     *
     * <p>This method retrieves the PSI file for the current editor document, checks if it's a Python file,
     * finds the PSI element at the caret offset, and then attempts to locate a variable (PyTargetExpression)
     * in the PSI tree. If found, it uses the {@link TypeEvalContext} to determine the type of the variable,
     * updating {@code currentText} accordingly. Finally, it updates the status bar widget.</p>
     *
     * @param editor the current editor.
     */
    private void updateType(Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        if (!(psiFile instanceof PyFile)) {
            currentText = "empty";
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
            return;
        }

        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            currentText = "No element";
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
            return;
        }

        PyTargetExpression targetExpression = findParentTargetExpression(element);
        if (targetExpression != null) {
            TypeEvalContext context = TypeEvalContext.userInitiated(project, psiFile);
            PyType pyType = context.getType(targetExpression);
            currentText = (pyType != null) ? pyType.getName() : "Unknown";
        } else {
            currentText = "Not a variable";
        }

        if (statusBar != null) {
            statusBar.updateWidget(ID());
        }
    }

    /**
     * Traverses up the PSI tree starting from the given element to locate a {@link PyTargetExpression}.
     *
     * @param element the PSI element at the caret position.
     * @return the closest {@link PyTargetExpression} (variable) found in the parent chain, or {@code null} if none is found.
     */
    private PyTargetExpression findParentTargetExpression(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof PyTargetExpression) {
                return (PyTargetExpression) current;
            }
            current = current.getParent();
        }
        return null;
    }

    @Override
    public @NotNull String ID() {
        return "com.expample.VariableTypeStatusBarWidget";
    }

    @Override
    public float getAlignment() {
        return 0.5f;
    }

    @Override
    public @NotNull String getText() {
        return currentText.isEmpty() ? "" : "Type: " + currentText;
    }

    @Override
    public @Nullable String getTooltipText() {
        return currentText.isEmpty() ? null : "Detected variable type";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {
    }
}
