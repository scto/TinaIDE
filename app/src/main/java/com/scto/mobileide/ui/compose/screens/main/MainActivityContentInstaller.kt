package com.scto.mobileide.ui.compose.screens.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.output.IOutputManager
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.CompileActionsHelper
import com.scto.mobileide.ui.CompilerViewModel
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.GitViewModel
import com.scto.mobileide.ui.MainActivityActionsDelegate
import com.scto.mobileide.ui.MainActivityActionsViewModel
import com.scto.mobileide.ui.MainActivityBottomPanelActionBridge
import com.scto.mobileide.ui.MainActivityCompileDelegate
import com.scto.mobileide.ui.MainActivityDialogCoordinator
import com.scto.mobileide.ui.MainActivityEditorActionBridge
import com.scto.mobileide.ui.MainActivityFileTreeActionBridge
import com.scto.mobileide.ui.MainActivityNavigationDelegate
import com.scto.mobileide.ui.MainActivityShortcutDispatcher
import com.scto.mobileide.ui.MainActivityWorkspaceActionsDelegate
import com.scto.mobileide.ui.MainViewModel
import com.scto.mobileide.ui.theme.MobileIDETheme
import java.io.File

internal fun installMainActivityContent(
    activity: ComponentActivity,
    projectContext: IProjectContext,
    compilerViewModel: CompilerViewModel,
    mainViewModel: MainViewModel,
    editorStateViewModel: EditorStateViewModel,
    debugViewModel: DebugViewModel,
    gitViewModel: GitViewModel,
    editorManager: IEditorManager,
    fileTreeActionBridge: MainActivityFileTreeActionBridge,
    processManager: com.scto.mobileide.core.compile.ProcessManager,
    outputManager: IOutputManager,
    bottomPanelViewModel: BottomPanelViewModel,
    bottomPanelController: MainActivityBottomPanelActionBridge,
    actionsViewModel: MainActivityActionsViewModel,
    compileActionsHelper: CompileActionsHelper,
    actionsDelegate: MainActivityActionsDelegate,
    compileDelegate: MainActivityCompileDelegate,
    navigationDelegate: MainActivityNavigationDelegate,
    shortcutDispatcher: MainActivityShortcutDispatcher,
    editorActionBridge: MainActivityEditorActionBridge,
    dialogCoordinator: MainActivityDialogCoordinator,
    workspaceActions: MainActivityWorkspaceActionsDelegate,
    onOpenWithExternalApp: (File) -> Unit,
    onShareFileOrDirectory: (File) -> Unit,
) {
    activity.setContent {
        MobileIDETheme {
            MainActivityScreenHost(
                activity = activity,
                lifecycleScope = activity.lifecycleScope,
                projectContext = projectContext,
                compilerViewModel = compilerViewModel,
                mainViewModel = mainViewModel,
                editorStateViewModel = editorStateViewModel,
                debugViewModel = debugViewModel,
                gitViewModel = gitViewModel,
                editorManager = editorManager,
                fileTreeActionBridge = fileTreeActionBridge,
                processManager = processManager,
                outputManager = outputManager,
                bottomPanelViewModel = bottomPanelViewModel,
                bottomPanelController = bottomPanelController,
                actionsViewModel = actionsViewModel,
                compileActionsHelper = compileActionsHelper,
                actionsDelegate = actionsDelegate,
                compileDelegate = compileDelegate,
                navigationDelegate = navigationDelegate,
                shortcutDispatcher = shortcutDispatcher,
                editorActionBridge = editorActionBridge,
                dialogCoordinator = dialogCoordinator,
                workspaceActions = workspaceActions,
                onOpenWithExternalApp = onOpenWithExternalApp,
                onShareFileOrDirectory = onShareFileOrDirectory,
            )
        }
    }
}
