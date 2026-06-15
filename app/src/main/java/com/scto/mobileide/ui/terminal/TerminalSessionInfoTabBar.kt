package com.scto.mobileide.ui.terminal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.scto.mobileide.core.terminal.TerminalSessionInfo
import com.scto.mobileide.terminal.session.SessionStatus
import com.scto.mobileide.terminal.session.TerminalSessionState
import com.scto.mobileide.terminal.shell.TerminalBackend
import com.scto.mobileide.terminal.ui.TerminalTabBar as FeatureTerminalTabBar

/**
 * TerminalSessionInfoTabBar 适配器
 *
 * 将 app 层的 TerminalSessionInfo 转换为 feature 层的 TerminalSessionState
 */
@Composable
fun TerminalSessionInfoTabBar(
    sessions: List<TerminalSessionInfo>,
    activeSessionId: String?,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier,
    onTabRename: ((String, String) -> Unit)? = null,
    onNewTabLongClick: (() -> Unit)? = null,
) {
    // 转换 TerminalSessionInfo 到 TerminalSessionState
    val sessionStates = sessions.map { info ->
        TerminalSessionState(
            id = info.id,
            title = info.title,
            backend = when (info.backend) {
                com.scto.mobileide.core.terminal.TerminalBackend.HOST -> TerminalBackend.HOST
                com.scto.mobileide.core.terminal.TerminalBackend.PROOT -> TerminalBackend.PROOT
            },
            session = null, // TabBar 不需要实际的 session 对象
            status = when (info.status) {
                com.scto.mobileide.core.terminal.SessionStatus.STARTING -> SessionStatus.STARTING
                com.scto.mobileide.core.terminal.SessionStatus.RUNNING -> SessionStatus.RUNNING
                com.scto.mobileide.core.terminal.SessionStatus.EXITED -> SessionStatus.EXITED
                com.scto.mobileide.core.terminal.SessionStatus.ERROR -> SessionStatus.ERROR
            },
            createdAt = info.createdAt,
            exitCode = info.exitCode,
            errorMessage = info.errorMessage,
            shellPid = info.shellPid
        )
    }

    FeatureTerminalTabBar(
        sessions = sessionStates,
        activeSessionId = activeSessionId,
        onTabClick = onTabClick,
        onTabClose = onTabClose,
        onTabRename = onTabRename,
        onNewTab = onNewTab,
        onNewTabLongClick = onNewTabLongClick,
        modifier = modifier
    )
}
