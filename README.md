# PyCharm Variable Type Status Bar Widget

A simple IntelliJ Platform plugin for PyCharm that displays the inferred type of the Python variable under your caret in the IDE status bar.

This was created as a learning project during a Flask support internship to explore IntelliJ Platform plugin development and the Python plugin’s type inference APIs.


## Overview
- Shows the inferred type (e.g., int, str, list[str], etc.) for a Python variable at the caret.
- Appears as a status bar widget that updates when you move the caret.
- Works in PyCharm (or IntelliJ IDEA Ultimate with the Python plugin) for builds 2024.1.x–2024.3.x.


## What I used
- IntelliJ Platform SDK and Gradle IntelliJ Plugin (org.jetbrains.intellij 1.17.4)
- Target IDE: PyCharm (type = PY), version 2024.1.7
- Python plugin APIs (com.jetbrains.python)
  - PSI: com.jetbrains.python.psi.* (e.g., PyFile, PyTargetExpression)
  - Type inference: com.jetbrains.python.psi.types.* and TypeEvalContext
- Java 17 (source/target)
- Gradle (wrapper included)


## How it works (under the hood)
The core of the plugin is a status bar widget that listens to caret movement and queries the PSI to find the variable under the caret, then asks the Python type evaluator for its type.

Key pieces:
- VariableTypeStatusBarWidget (src/main/java/com/expample/VariableTypeStatusBarWidget.java)
  - Registers an EditorFactoryListener to hook into newly created editors.
  - Adds a CaretListener so every caret move triggers an update.
  - On update:
    - Gets the PsiFile from the editor’s document and checks it’s a PyFile.
    - Locates the PSI element at the caret and walks up to the nearest PyTargetExpression (a variable).
    - Uses TypeEvalContext.userInitiated(project, psiFile) to retrieve a PyType for that variable.
    - Displays the resulting type (or a fallback like Unknown) in the status bar.
- VariableTypeStatusBarWidgetFactory (src/main/java/com/expample/VariableTypeStatusBarWidgetFactory.java)
  - Provides the widget to the IDE and names it “Variable Type Status”.
- plugin.xml (src/main/resources/META-INF/plugin.xml)
  - Registers the statusBarWidgetFactory extension and depends on com.intellij.modules.python.


## How I did it
1. Project setup
   - Used the Gradle IntelliJ Plugin to target PyCharm (type = PY) and add the Python plugin dependency.
   - Set since/until builds (241–243.*) and Java/Kotlin JVM target 17.
2. Extension registration
   - Declared the status bar widget factory in plugin.xml with the same ID used by the widget.
3. Widget implementation
   - Implemented StatusBarWidget and StatusBarWidget.TextPresentation to render text in the status bar.
   - Managed lifecycle via install(...) and Disposable.
4. Caret and editor plumbing
   - Subscribed to editor creation with EditorFactoryListener.
   - Attached a CaretListener for each editor to refresh the type on caret movement.
5. PSI and type evaluation
   - Converted editor offset to PSI element.
   - Walked up to PyTargetExpression and evaluated type using TypeEvalContext.
6. Testing in a sandbox IDE
   - Used the Gradle runIde task to launch a PyCharm sandbox and verify behavior on sample Python files.


## What I learned
- IntelliJ PSI basics for Python
  - Navigating from caret to PSI elements and recognizing Python-specific PSI nodes like PyFile and PyTargetExpression.
- Type evaluation in the Python plugin
  - Using TypeEvalContext to request inferred types and understanding that some cases may return null/Unknown.
- Building status bar widgets
  - Implementing StatusBarWidget/TextPresentation and wiring a StatusBarWidgetFactory.
  - Updating widget text efficiently and being mindful of IDE performance.
- Plugin mechanics and compatibility
  - Registering extensions in plugin.xml and aligning IDs across factory and widget.
  - Setting target IDE type/version and python plugin dependency for compatibility.


## Requirements
- IDE: PyCharm 2024.1.7–2024.3.* (or IntelliJ IDEA Ultimate in the same range)
- Python plugin (com.intellij.modules.python)
- JDK 17 for building/running from source


## Build and run from source
- Clone the repo and ensure JDK 17 is available.
- Useful Gradle tasks:
  - ./gradlew runIde — launches a PyCharm sandbox with the plugin.
  - ./gradlew build — builds the project.
  - ./gradlew buildPlugin — produces a distributable ZIP under build/distributions.


## Install into your IDE
- From source: run ./gradlew runIde and test in the sandbox.
- From ZIP: build with ./gradlew buildPlugin, then in PyCharm go to Settings → Plugins → Gear Icon → Install Plugin from Disk and choose the ZIP from build/distributions.
- Enable the widget (if not visible): right‑click the IDE status bar → choose Variable Type Status.


## Usage
- Open a Python file, place the caret over or inside a variable.
- Look at the status bar for “Type: <inferred type>”.
- Move the caret—types update automatically.


## Project structure
- build.gradle.kts — Gradle + IntelliJ Platform configuration (PY 2024.1.7, python plugin, Java 17)
- src/main/java/com/expample/VariableTypeStatusBarWidget.java — main widget logic
- src/main/java/com/expample/VariableTypeStatusBarWidgetFactory.java — factory for the widget
- src/main/resources/META-INF/plugin.xml — plugin metadata and extension registration
- src/main/resources/META-INF/pluginIcon.svg — plugin icon


## Limitations and future ideas
- Only shows types for simple variable targets (PyTargetExpression). It doesn’t resolve complex expressions, attributes, or call results.
- The displayed type name may be simplified and not always fully qualified.
- Could add richer UI/tooltip details (e.g., full type, doc link) and better handling for multiple carets.
- Could cache type results per file/revision for performance and add a setting to disable in large files.


## Acknowledgements
Thanks to the IntelliJ Platform SDK docs and the Python plugin APIs for making this exploration possible.
