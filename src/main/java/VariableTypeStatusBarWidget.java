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

    }


    @Override
    public @NotNull String ID() {
        return "";
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @NotNull String getText() {
        return "";
    }

    @Override
    public @Nullable String getTooltipText() {
        return "";
    }
}
