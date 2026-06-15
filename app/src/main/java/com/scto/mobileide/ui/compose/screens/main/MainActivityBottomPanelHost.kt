package com.scto.mobileide.ui.compose.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.commands.HostCommandExecutor
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.editor.symbol.ProjectSymbolIndexService
import com.scto.mobileide.settings.SettingsActivity
import com.scto.mobileide.ui.BindMainActivityBottomPanelState
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.MainActivityActionsDelegate
import com.scto.mobileide.ui.MainActivityBottomPanelActionBridge
import com.scto.mobileide.ui.MainActivityNavigationDelegate
import com.scto.mobileide.ui.compose.components.BottomPanel
import com.scto.mobileide.ui.compose.components.BottomPanelSecondaryBarHeight
import com.scto.mobileide.ui.compose.components.EditorContainer
import com.scto.mobileide.ui.compose.components.EditorStatusBarHeight
import com.scto.mobileide.ui.compose.components.SwipeableDrawerState
import com.scto.mobileide.ui.compose.components.rememberBottomPanelDragState
import com.scto.mobileide.ui.compose.state.DialogState
import com.scto.mobileide.ui.compose.state.editor.EditorContainerState
import com.scto.mobileide.ui.compose.state.git.GitUiState
import org.koin.compose.koinInject

@Composable
internal fun MainActivityBottomPanelHost(
    paddingValues: PaddingValues,
    editorContainerState: EditorContainerState,
    dialogState: DialogState,
    hostCommandExecutor: HostCommandExecutor?,
    drawerState: SwipeableDrawerState,
    gitUiState: GitUiState,
    cursorLine: Int,
    cursorColumn: Int,
    fileEncoding: String,
    editorManager: IEditorManager,
    projectSymbolIndexService: ProjectSymbolIndexService?,
    bottomPanelController: MainActivityBottomPanelActionBridge,
    bottomPanelViewModel: BottomPanelViewModel,
    editorStateViewModel: EditorStateViewModel,
    debugViewModel: DebugViewModel,
    actionsDelegate: MainActivityActionsDelegate,
    navigationDelegate: MainActivityNavigationDelegate,
    callbacks: MainActivityScreenCallbacks,
) {
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .imePadding()
    ) {
        val maxPanelHeight = (maxHeight - EditorStatusBarHeight - BottomPanelSecondaryBarHeight)
            .coerceAtLeast(0.dp)

        val bottomPanelState = rememberBottomPanelDragState(
            initialExpanded = false,
            minHeight = 0.dp,
            maxHeight = maxPanelHeight
        )
        BindMainActivityBottomPanelState(
            bottomPanelState = bottomPanelState,
            bottomPanelController = bottomPanelController,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                EditorContainer(
                    state = editorContainerState,
                    editorManager = editorManager,
                    pluginManager = koinInject(),
                    hostCommandExecutor = hostCommandExecutor,
                    onOpenFileTree = { drawerState.open() },
                    onEditorStateChanged = { hasFiles, undo, redo, dirty ->
                        editorStateViewModel.updateEditorState(hasFiles, undo, redo, dirty)
                    },
                    onCursorPositionChanged = { line, column ->
                        editorStateViewModel.updateCursorPosition(line, column)
                    },
                    onFileEncodingChanged = { encoding ->
                        editorStateViewModel.updateFileEncoding(encoding)
                    },
                    onSaveFile = callbacks.onSaveFile,
                    onOpenPluginLspDependencySettings = { pluginId ->
                        SettingsActivity.startPluginDetail(context, pluginId)
                    },
                )
            }

            BottomPanel(
                editorContainerState = editorContainerState,
                bottomPanelState = bottomPanelState,
                bottomPanelViewModel = bottomPanelViewModel,
                editorStateViewModel = editorStateViewModel,
                debugViewModel = debugViewModel,
                projectSymbolIndexService = projectSymbolIndexService,
                modifier = Modifier.fillMaxWidth(),
                onUndoClick = { actionsDelegate.performUndo(editorContainerState) },
                onRedoClick = { actionsDelegate.performRedo(editorContainerState) },
                onSymbolClick = { symbol ->
                    editorContainerState.insertTextAtCursor(symbol)
                },
                onBookmarkNavigate = { filePath, line ->
                    actionsDelegate.navigateToBookmark(editorContainerState, filePath, line)
                },
                onDiagnosticClick = { diagnostic ->
                    navigationDelegate.navigateToDiagnostic(editorContainerState, diagnostic)
                },
                gitCurrentBranch = gitUiState.status.branch,
                gitBranches = gitUiState.branches,
                gitCommits = gitUiState.commitHistory,
                gitIsLoadingHistory = gitUiState.isLoadingHistory,
                onGitRefresh = callbacks.onGitRefresh,
                onGitBranchSelect = callbacks.onGitBranchSelect,
                onGitCommitClick = callbacks.onGitCommitClick,
                cursorLine = cursorLine,
                cursorColumn = cursorColumn,
                fileEncoding = fileEncoding,
                onCursorPositionClick = {
                    when (editorContainerState.getActiveEditableEditorCommandAvailability()) {
                        EditorContainerState.ActiveEditorCommandResult.SUCCESS -> {
                            dialogState.openGotoLineDialog()
                        }

                        EditorContainerState.ActiveEditorCommandResult.NO_OPEN_FILE -> {
                            callbacks.onNoOpenFile()
                        }

                        EditorContainerState.ActiveEditorCommandResult.UNSUPPORTED_EDITOR -> {
                            callbacks.onUnsupportedEditor()
                        }
                    }
                }
            )
        }
    }
}
