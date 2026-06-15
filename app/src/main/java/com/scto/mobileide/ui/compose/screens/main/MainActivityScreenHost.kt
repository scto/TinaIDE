package com.scto.mobileide.ui.compose.screens.main

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.compile.ProcessManager
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.extensions.toastError
import com.scto.mobileide.extensions.toastInfo
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.output.IOutputManager
import com.scto.mobileide.ui.BindMainActivityFileTreeState
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
import java.io.File
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun MainActivityScreenHost(
    activity: Activity,
    lifecycleScope: CoroutineScope,
    projectContext: IProjectContext,
    compilerViewModel: CompilerViewModel,
    mainViewModel: MainViewModel,
    editorStateViewModel: EditorStateViewModel,
    debugViewModel: DebugViewModel,
    gitViewModel: GitViewModel,
    editorManager: IEditorManager,
    fileTreeActionBridge: MainActivityFileTreeActionBridge,
    processManager: ProcessManager,
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
    val drawerWidth = 300.dp

    val mainScreenState = rememberMainActivityMainScreenState(
        drawerWidth = drawerWidth,
        projectContext = projectContext,
        initialRunConfigManager = compilerViewModel.getRunConfigurationManager(),
        detectBuildSystem = { compilerViewModel.detectBuildSystem() },
        loadAvailableTargets = { compilerViewModel.getAvailableTargets() },
        mainViewModel = mainViewModel,
        editorStateViewModel = editorStateViewModel,
        debugViewModel = debugViewModel,
        gitViewModel = gitViewModel,
    )
    val projectSnapshot = mainScreenState.projectSnapshot
    val fileTreeState = mainScreenState.fileTreeState

    BindMainActivityFileTreeState(
        fileTreeState = fileTreeState,
        fileTreeActionBridge = fileTreeActionBridge,
    )

    val editorHostState = rememberMainActivityEditorHostState(
        editorManager = editorManager,
        projectRootPathProvider = { projectSnapshot.rootPath },
        onLspDiagnosticsChanged = bottomPanelViewModel::replaceDiagnosticsForFile,
    )
    val workspaceCallbacks = rememberMainActivityWorkspaceCallbacksHost(
        mainScreenState = mainScreenState,
        workspaceActions = workspaceActions,
        dialogCoordinator = dialogCoordinator,
        toastInfo = activity::toastInfo,
        toastError = activity::toastError,
        onOpenWithExternalApp = onOpenWithExternalApp,
        onShareFileOrDirectory = onShareFileOrDirectory,
        onPersistRunConfigManager = compilerViewModel::saveRunConfigurationManager,
        onGitRefresh = gitViewModel::loadCommitHistory,
    )

    MainActivityWorkspaceSection(
        activity = activity,
        lifecycleScope = lifecycleScope,
        projectContext = projectContext,
        processManager = processManager,
        outputManager = outputManager,
        editorManager = editorManager,
        gitViewModel = gitViewModel,
        bottomPanelViewModel = bottomPanelViewModel,
        bottomPanelController = bottomPanelController,
        actionsViewModel = actionsViewModel,
        compileActionsHelper = compileActionsHelper,
        editorStateViewModel = editorStateViewModel,
        debugViewModel = debugViewModel,
        actionsDelegate = actionsDelegate,
        compileDelegate = compileDelegate,
        navigationDelegate = navigationDelegate,
        shortcutDispatcher = shortcutDispatcher,
        editorActionBridge = editorActionBridge,
        dialogCoordinator = dialogCoordinator,
        mainScreenState = mainScreenState,
        editorHostState = editorHostState,
        callbacks = workspaceCallbacks,
    )
}
