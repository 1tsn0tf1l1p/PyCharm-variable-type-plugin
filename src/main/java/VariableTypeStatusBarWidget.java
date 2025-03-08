import com.intellij.openapi.Disposable;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VariableTypeStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation, Disposable {
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
