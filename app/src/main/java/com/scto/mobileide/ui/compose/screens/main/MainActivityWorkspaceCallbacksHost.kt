package com.scto.mobileide.ui.compose.screens.main

import androidx.compose.runtime.Composable
import com.scto.mobileide.core.compile.RunConfigurationManager
import com.scto.mobileide.ui.MainActivityDialogCoordinator
import com.scto.mobileide.ui.MainActivityWorkspaceActionsDelegate
import java.io.File

@Composable
internal fun rememberMainActivityWorkspaceCallbacksHost(
    mainScreenState: MainActivityMainScreenState,
    workspaceActions: MainActivityWorkspaceActionsDelegate,
    dialogCoordinator: MainActivityDialogCoordinator,
    toastInfo: (String) -> Unit,
    toastError: (String) -> Unit,
    onOpenWithExternalApp: (File) -> Unit,
    onShareFileOrDirectory: (File) -> Unit,
    onPersistRunConfigManager: (RunConfigurationManager) -> Unit,
    onGitRefresh: () -> Unit,
): MainActivityWorkspaceCallbacks = rememberMainActivityWorkspaceCallbacks(
    onOpenSettings = workspaceActions::openSettings,
    toastInfo = toastInfo,
    toastError = toastError,
    onDuplicateFileOrDirectory = workspaceActions::duplicateFileOrDirectory,
    onDuplicateSuccess = workspaceActions::onDuplicateSuccess,
    onDuplicateFailure = workspaceActions::onDuplicateFailure,
    onOpenWithExternalApp = onOpenWithExternalApp,
    onShareFileOrDirectory = onShareFileOrDirectory,
    onProjectNotOpen = workspaceActions::onProjectNotOpen,
    onGitCommitSuccess = workspaceActions::onGitCommitSuccess,
    onGitInitSuccess = workspaceActions::onGitInitSuccess,
    onOpenAiSettings = workspaceActions::openAiSettings,
    onPersistRunConfigManager = onPersistRunConfigManager,
    onNoOpenFile = workspaceActions::onNoOpenFile,
    onUnsupportedEditor = workspaceActions::onUnsupportedEditor,
    onOpenBookmarks = workspaceActions::openBookmarksPanel,
    onOpenTerminal = workspaceActions::openProjectTerminal,
    onSaveFile = workspaceActions::saveFileForClose,
    onGitRefresh = onGitRefresh,
    onGitBranchSelect = workspaceActions::checkoutGitBranch,
    onGitCommitClick = mainScreenState.gitDialogState::openCommitDetail,
    onRequestUnsavedExitConfirm = dialogCoordinator::requestUnsavedExitConfirm,
)
