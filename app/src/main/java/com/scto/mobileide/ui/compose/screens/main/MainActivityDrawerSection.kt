package com.scto.mobileide.ui.compose.screens.main

import androidx.compose.runtime.Composable
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

@Composable
internal fun MainActivityDrawerSection(
    uiState: MainActivityScreenUiState,
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
    showCommandPalette: Boolean,
    onOpenCommandPalette: () -> Unit,
    onDismissCommandPalette: () -> Unit,
    callbacks: MainActivityScreenCallbacks,
) {
    val dependencies = rememberMainActivityDrawerDependencies(
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

    MainActivityDrawerHost(
        uiState = uiState,
        dependencies = dependencies,
        showCommandPalette = showCommandPalette,
        onOpenCommandPalette = onOpenCommandPalette,
        onDismissCommandPalette = onDismissCommandPalette,
        callbacks = callbacks,
    )
}
