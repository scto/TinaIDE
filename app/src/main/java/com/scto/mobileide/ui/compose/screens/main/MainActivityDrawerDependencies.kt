package com.scto.mobileide.ui.compose.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.scto.mobileide.ai.viewmodel.AiChatViewModel
import com.scto.mobileide.core.commands.HostCommandExecutor
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.editor.symbol.ProjectSymbolIndexService
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.GitViewModel
import com.scto.mobileide.ui.MainActivityActionsDelegate
import com.scto.mobileide.ui.MainActivityBottomPanelActionBridge
import com.scto.mobileide.ui.MainActivityCompileDelegate
import com.scto.mobileide.ui.MainActivityNavigationDelegate
import com.scto.mobileide.ui.compose.components.FileTreeState
import com.scto.mobileide.ui.compose.components.SwipeableDrawerState
import com.scto.mobileide.ui.compose.state.DialogState
import com.scto.mobileide.ui.compose.state.editor.EditorContainerState
import com.scto.mobileide.ui.compose.state.git.GitDialogState
import kotlinx.coroutines.CoroutineScope

@Stable
internal class MainActivityDrawerDependencies(
    val fileTreeState: FileTreeState,
    val editorContainerState: EditorContainerState,
    val dialogState: DialogState,
    val gitDialogState: GitDialogState,
    val currentAiChatViewModel: AiChatViewModel,
    val drawerState: SwipeableDrawerState,
    val buildUiState: MainActivityBuildUiState,
    val hostCommandExecutor: HostCommandExecutor?,
    val uiScope: CoroutineScope,
    val projectContext: IProjectContext,
    val editorManager: IEditorManager,
    val projectSymbolIndexService: ProjectSymbolIndexService?,
    val gitViewModel: GitViewModel,
    val bottomPanelController: MainActivityBottomPanelActionBridge,
    val bottomPanelViewModel: BottomPanelViewModel,
    val editorStateViewModel: EditorStateViewModel,
    val debugViewModel: DebugViewModel,
    val actionsDelegate: MainActivityActionsDelegate,
    val compileDelegate: MainActivityCompileDelegate,
    val navigationDelegate: MainActivityNavigationDelegate,
)

@Composable
internal fun rememberMainActivityDrawerDependencies(
    fileTreeState: FileTreeState,
    editorContainerState: EditorContainerState,
    dialogState: DialogState,
    gitDialogState: GitDialogState,
    currentAiChatViewModel: AiChatViewModel,
    drawerState: SwipeableDrawerState,
    buildUiState: MainActivityBuildUiState,
    hostCommandExecutor: HostCommandExecutor?,
    uiScope: CoroutineScope,
    projectContext: IProjectContext,
    editorManager: IEditorManager,
    projectSymbolIndexService: ProjectSymbolIndexService?,
    gitViewModel: GitViewModel,
    bottomPanelController: MainActivityBottomPanelActionBridge,
    bottomPanelViewModel: BottomPanelViewModel,
    editorStateViewModel: EditorStateViewModel,
    debugViewModel: DebugViewModel,
    actionsDelegate: MainActivityActionsDelegate,
    compileDelegate: MainActivityCompileDelegate,
    navigationDelegate: MainActivityNavigationDelegate,
): MainActivityDrawerDependencies = remember(
    fileTreeState,
    editorContainerState,
    dialogState,
    gitDialogState,
    currentAiChatViewModel,
    drawerState,
    buildUiState,
    hostCommandExecutor,
    uiScope,
    projectContext,
    editorManager,
    projectSymbolIndexService,
    gitViewModel,
    bottomPanelController,
    bottomPanelViewModel,
    editorStateViewModel,
    debugViewModel,
    actionsDelegate,
    compileDelegate,
    navigationDelegate,
) {
    MainActivityDrawerDependencies(
        fileTreeState = fileTreeState,
        editorContainerState = editorContainerState,
        dialogState = dialogState,
        gitDialogState = gitDialogState,
        currentAiChatViewModel = currentAiChatViewModel,
        drawerState = drawerState,
        buildUiState = buildUiState,
        hostCommandExecutor = hostCommandExecutor,
        uiScope = uiScope,
        projectContext = projectContext,
        editorManager = editorManager,
        projectSymbolIndexService = projectSymbolIndexService,
        gitViewModel = gitViewModel,
        bottomPanelController = bottomPanelController,
        bottomPanelViewModel = bottomPanelViewModel,
        editorStateViewModel = editorStateViewModel,
        debugViewModel = debugViewModel,
        actionsDelegate = actionsDelegate,
        compileDelegate = compileDelegate,
        navigationDelegate = navigationDelegate,
    )
}
