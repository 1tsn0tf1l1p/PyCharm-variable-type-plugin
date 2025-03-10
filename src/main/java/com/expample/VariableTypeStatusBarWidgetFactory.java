package com.expample;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class VariableTypeStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @Override
    public @NotNull @NonNls String getId() {
        return "com.expample.VariableTypeStatusBarWidget";
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Variable Type Status";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new VariableTypeStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }
}
