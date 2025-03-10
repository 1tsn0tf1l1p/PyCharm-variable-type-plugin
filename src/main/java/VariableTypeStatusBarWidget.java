import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VariableTypeStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation, Disposable {
    private final Project project;
    private StatusBar statusBar;
    private String currentText = "";

    public VariableTypeStatusBarWidget(Project project) {
        this.project = project;

        EditorFactoryListener editorFactoryListener = new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                if (project == editor.getProject()) {
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

    private void updateType(Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (!(psiFile instanceof PyFile)) {
            currentText = "";
            if (statusBar != null) {
                statusBar.updateWidget(ID());
                return;
            }
        }

        PyElement element = (PyElement) psiFile.findElementAt(offset);
        if (element == null) {
            currentText = "";
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
            return;
        }
        PyTargetExpression targetExpression = findParentTargetExpression(element);
        if (targetExpression != null) {
            TypeEvalContext context = TypeEvalContext.userInitiated(project, psiFile);
            PyType pyType = context.getType(targetExpression);
            currentText = (pyType != null) ? pyType.getName() : "";
        } else {
            currentText = "";
        }

        if (statusBar != null) {
            statusBar.updateWidget(ID());
        }
    }

    private PyTargetExpression findParentTargetExpression(PyElement element) {
        PyElement current = element;
        while (current != null) {
            if (current instanceof PyTargetExpression) {
                return (PyTargetExpression) current;
            }
            current = (PyElement) current.getParent();
        }
        return null;
    }


    @Override
    public @NotNull String ID() {
        return "VariableTypeStatusBarWidget";
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
        return currentText.isEmpty() ? null : "Variable type.";
    }



    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }
}
