package com.scto.mobileide.ui.compose.screens.main

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.scto.mobileide.core.compile.ProcessManager
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.i18n.strOr
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.output.IOutputManager
import com.scto.mobileide.ui.BindMainActivityEditorHost
import com.scto.mobileide.ui.BindPluginDiagnosticsProvider
import com.scto.mobileide.ui.BindPluginHostCommandExecutor
import com.scto.mobileide.ui.BindPluginHostEvents
import com.scto.mobileide.ui.BindPluginKeyBindings
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.CompileActionsHelper
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.GitViewModel
import com.scto.mobileide.ui.MainActivityActionsDelegate
import com.scto.mobileide.ui.MainActivityActionsViewModel
import com.scto.mobileide.ui.MainActivityBottomPanelActionBridge
import com.scto.mobileide.ui.MainActivityCompileDelegate
import com.scto.mobileide.ui.MainActivityDialogCoordinator
import com.scto.mobileide.ui.MainActivityEditorActionBridge
import com.scto.mobileide.ui.MainActivityNavigationDelegate
import com.scto.mobileide.ui.MainActivityShortcutDispatcher
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun MainActivityWorkspaceSection(
    activity: Activity,
    lifecycleScope: CoroutineScope,
    projectContext: IProjectContext,
    processManager: ProcessManager,
    outputManager: IOutputManager,
    editorManager: IEditorManager,
    gitViewModel: GitViewModel,
    bottomPanelViewModel: BottomPanelViewModel,
    bottomPanelController: MainActivityBottomPanelActionBridge,
    actionsViewModel: MainActivityActionsViewModel,
    compileActionsHelper: CompileActionsHelper,
    editorStateViewModel: EditorStateViewModel,
    debugViewModel: DebugViewModel,
    actionsDelegate: MainActivityActionsDelegate,
    compileDelegate: MainActivityCompileDelegate,
    navigationDelegate: MainActivityNavigationDelegate,
    shortcutDispatcher: MainActivityShortcutDispatcher,
    editorActionBridge: MainActivityEditorActionBridge,
    dialogCoordinator: MainActivityDialogCoordinator,
    mainScreenState: MainActivityMainScreenState,
    editorHostState: MainActivityEditorHostState,
    callbacks: MainActivityWorkspaceCallbacks,
) {
    val buildUiState = mainScreenState.buildUiState
    val dialogState = mainScreenState.dialogState
    val drawerState = mainScreenState.drawerState
    val editorActionsState = mainScreenState.editorActionsState
    val fileTreeState = mainScreenState.fileTreeState
    val gitDialogState = mainScreenState.gitDialogState
    val locationDialogState = mainScreenState.locationDialogState
    val projectSnapshot = mainScreenState.projectSnapshot
    val screenUiState = mainScreenState.screenUiState
    val uiScope = mainScreenState.scope
    val editorContainerState = editorHostState.editorContainerState
    var showCommandPalette by remember { mutableStateOf(false) }
    val openCommandPalette = { showCommandPalette = true }
    val dismissCommandPalette = { showCommandPalette = false }

    val currentAiChatViewModel = mainActivityHostEffects(
        context = activity,
        lifecycleScope = lifecycleScope,
        projectContext = projectContext,
        fileTreeState = fileTreeState,
        gitViewModel = gitViewModel,
        uiScope = uiScope,
        editorContainerState = editorContainerState,
        bottomPanelViewModel = bottomPanelViewModel,
        processManager = processManager,
        buildUiState = buildUiState,
        editorManager = editorManager,
        outputManager = outputManager,
        bottomPanelController = bottomPanelController,
    )

    val workspaceUi = rememberMainActivityWorkspaceUi(
        activity = activity,
        editorContainerState = editorContainerState,
        fileTreeState = fileTreeState,
        projectContext = projectContext,
        drawerState = drawerState,
        dialogState = dialogState,
        bottomPanelViewModel = bottomPanelViewModel,
        bottomPanelController = bottomPanelController,
        actionsViewModel = actionsViewModel,
        compileActionsHelper = compileActionsHelper,
        hostScope = lifecycleScope,
        onOpenRunConfig = { buildUiState.openRunConfigDialog() },
        onOpenCommandPalette = openCommandPalette,
        callbacks = callbacks,
    )

    BindMainActivityEditorHost(
        editorContainerState = editorContainerState,
        editorActionBridge = editorActionBridge,
        shortcutDispatcher = shortcutDispatcher,
        hostCommandExecutor = workspaceUi.hostCommandExecutor,
        editorActionsState = editorActionsState,
        locationDialogState = locationDialogState,
        scope = uiScope,
        onFileNotExist = {
            callbacks.toastError(Strings.toast_file_not_exist.strOr(activity))
        },
        onToastInfo = callbacks.toastInfo,
        onToastError = callbacks.toastError,
    )

    BindPluginHostCommandExecutor(workspaceUi.hostCommandExecutor)
    BindPluginKeyBindings(
        shortcutDispatcher = shortcutDispatcher,
        editorContainerState = editorContainerState,
        hostCommandExecutor = workspaceUi.hostCommandExecutor,
    )
    BindPluginDiagnosticsProvider(
        bottomPanelViewModel = bottomPanelViewModel,
        projectRootProvider = { projectSnapshot.rootPath },
    )
    BindPluginHostEvents(
        projectContext = projectContext,
        isCompiling = screenUiState.isCompiling,
    )

    MainActivityDrawerSection(
        uiState = screenUiState,
        fileTreeState = fileTreeState,
        editorContainerState = editorContainerState,
        dialogState = dialogState,
        gitDialogState = gitDialogState,
        currentAiChatViewModel = currentAiChatViewModel,
        drawerState = drawerState,
        buildUiState = buildUiState,
        hostCommandExecutor = workspaceUi.hostCommandExecutor,
        uiScope = uiScope,
        projectContext = projectContext,
        editorManager = editorManager,
        projectSymbolIndexService = editorHostState.projectSymbolIndexService,
        gitViewModel = gitViewModel,
        bottomPanelController = bottomPanelController,
        bottomPanelViewModel = bottomPanelViewModel,
        editorStateViewModel = editorStateViewModel,
        debugViewModel = debugViewModel,
        actionsDelegate = actionsDelegate,
        compileDelegate = compileDelegate,
        navigationDelegate = navigationDelegate,
        showCommandPalette = showCommandPalette,
        onOpenCommandPalette = openCommandPalette,
        onDismissCommandPalette = dismissCommandPalette,
        callbacks = workspaceUi.screenCallbacks,
    )

    MainActivityDialogsSection(
        editorActionsState = editorActionsState,
        locationDialogState = locationDialogState,
        dialogState = dialogState,
        buildUiState = buildUiState,
        gitUiState = screenUiState.gitUiState,
        gitDialogState = gitDialogState,
        projectName = screenUiState.projectName,
        projectRoot = projectSnapshot.projectRoot,
        buildDir = projectSnapshot.buildDir,
        showUnsavedExitDialog = dialogCoordinator.showUnsavedExitDialog,
        uiScope = uiScope,
        editorContainerState = editorContainerState,
        actionsViewModel = actionsViewModel,
        fileTreeState = fileTreeState,
        gitViewModel = gitViewModel,
        editorManager = editorManager,
        saveScope = lifecycleScope,
        onCloseProject = dialogCoordinator::closeProject,
        onPersistRunConfigManager = callbacks.screenCallbacks.onPersistRunConfigManager,
        onShowUnsavedExitDialogChange = dialogCoordinator::onShowUnsavedExitDialogChange,
        onFinish = activity::finish,
    )
}
