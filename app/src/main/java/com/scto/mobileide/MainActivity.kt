package com.scto.mobileide

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import com.scto.mobileide.core.compile.ProcessManager
import com.scto.mobileide.core.config.Prefs
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.extensions.toastError
import com.scto.mobileide.extensions.toastInfo
import com.scto.mobileide.extensions.toastSuccess
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.file.IProjectSession
import com.scto.mobileide.output.IOutputManager
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.CompilerViewModel
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.GitViewModel
import com.scto.mobileide.ui.MainActivityActionsViewModel
import com.scto.mobileide.ui.MainActivityBottomPanelActionBridge
import com.scto.mobileide.ui.MainActivityCompileHost
import com.scto.mobileide.ui.MainActivityEditorActionBridge
import com.scto.mobileide.ui.MainActivityExternalFileLauncher
import com.scto.mobileide.ui.MainActivityFileTreeActionBridge
import com.scto.mobileide.ui.MainActivityNavigationHost
import com.scto.mobileide.ui.MainActivityShortcutDispatcher
import com.scto.mobileide.ui.MainViewModel
import com.scto.mobileide.ui.compose.screens.main.installMainActivityContent
import com.scto.mobileide.ui.createMainActivityActionsDelegate
import com.scto.mobileide.ui.createMainActivityCompileHost
import com.scto.mobileide.ui.createMainActivityNavigationHost
import com.scto.mobileide.ui.createMainActivityWorkspaceHost
import com.scto.mobileide.ui.installMainActivityCleanup
import com.scto.mobileide.ui.installMainActivityStartup
import java.io.File
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 主界面 Activity
 *
 * 纯 Compose 实现:
 * - FileTree: 纯 Compose LazyColumn 实现
 * - EditorContainer: Compose + AndroidView(CodeEditor)
 * - BottomPanel: 纯 Compose 实现
 */
class MainActivity :
    ComponentActivity(),
    MainActivityExternalFileLauncher,
    KoinComponent {

    private val projectContext: IProjectContext by inject()
    private val projectSession: IProjectSession by inject()
    private val editorManager: IEditorManager by inject()
    private val processManager: ProcessManager by inject()
    private val outputManager: IOutputManager by inject()

    private val compilerViewModel: CompilerViewModel by koinViewModel()
    private val bottomPanelViewModel: BottomPanelViewModel by koinViewModel()
    private val editorStateViewModel: EditorStateViewModel by koinViewModel()
    private val mainViewModel: MainViewModel by koinViewModel()
    private val debugViewModel: DebugViewModel by koinViewModel()
    private val gitViewModel: GitViewModel by koinViewModel()
    private val actionsViewModel: MainActivityActionsViewModel by koinViewModel()
    private val actionsDelegate by lazy(LazyThreadSafetyMode.NONE) {
        createMainActivityActionsDelegate(
            activity = this,
            actionsViewModel = actionsViewModel,
            onToastSuccess = ::toastSuccess,
            onToastError = ::toastError,
            onToastInfo = ::toastInfo,
        )
    }

    private val editorActionBridge = MainActivityEditorActionBridge()
    private val fileTreeActionBridge = MainActivityFileTreeActionBridge()
    private val bottomPanelController = MainActivityBottomPanelActionBridge()
    private val shortcutDispatcher = MainActivityShortcutDispatcher()
    private val compileHost: MainActivityCompileHost by lazy(LazyThreadSafetyMode.NONE) {
        createMainActivityCompileHost(
            activity = this,
            compilerViewModel = compilerViewModel,
            mainViewModel = mainViewModel,
            bottomPanelViewModel = bottomPanelViewModel,
            debugViewModel = debugViewModel,
            bottomPanelController = bottomPanelController,
            projectContext = projectContext,
            editorManager = editorManager,
            processManager = processManager,
            fileTreeActionBridge = fileTreeActionBridge,
            onToastSuccess = ::toastSuccess,
            onToastError = ::toastError,
            onToastInfo = ::toastInfo,
        )
    }
    private val workspaceHost by lazy(LazyThreadSafetyMode.NONE) {
        createMainActivityWorkspaceHost(
            activity = this,
            projectSession = projectSession,
            projectContext = projectContext,
            editorManager = editorManager,
            gitViewModel = gitViewModel,
            bottomPanelViewModel = bottomPanelViewModel,
            bottomPanelController = bottomPanelController,
            onToastSuccess = ::toastSuccess,
            onToastError = ::toastError,
            onToastInfo = ::toastInfo,
        )
    }
    private val navigationHost: MainActivityNavigationHost by lazy(LazyThreadSafetyMode.NONE) {
        createMainActivityNavigationHost(
            activity = this,
            projectContext = projectContext,
            editorActionBridge = editorActionBridge,
            bottomPanelController = bottomPanelController,
            onToastError = ::toastError,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Prefs.applyTheme()
        super.onCreate(savedInstanceState)
        // 进入编辑器前显式恢复上次会话（若 MainPortalActivity 走 openProject 打开则为幂等 no-op，
        // 进程死亡冷启动时会从 ConfigKeys.CurrentProject 重新建立会话）
        projectSession.restoreLastSession()
        val currentCompileHost = compileHost
        val currentWorkspaceHost = workspaceHost
        val currentNavigationHost = navigationHost
        installMainActivityStartup(
            activity = this,
            actionsDelegate = actionsDelegate,
            compileDelegate = currentCompileHost.compileDelegate,
        ) {
            installMainActivityContent(
                activity = this,
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
                compileActionsHelper = currentCompileHost.compileActionsHelper,
                actionsDelegate = actionsDelegate,
                compileDelegate = currentCompileHost.compileDelegate,
                navigationDelegate = currentNavigationHost.navigationDelegate,
                shortcutDispatcher = shortcutDispatcher,
                editorActionBridge = editorActionBridge,
                dialogCoordinator = currentWorkspaceHost.dialogCoordinator,
                workspaceActions = currentWorkspaceHost.workspaceActions,
                onOpenWithExternalApp = ::openWithExternalApp,
                onShareFileOrDirectory = ::shareFileOrDirectory,
            )
        }
    }

    override fun openWithExternalApp(file: File) {
        workspaceHost.externalFileLauncher.openWithExternalApp(file)
    }

    override fun shareFileOrDirectory(file: File) {
        workspaceHost.externalFileLauncher.shareFileOrDirectory(file)
    }

    /**
     * 硬件键盘快捷键处理
     *
     * 支持的快捷键可在设置中自定义，默认：
     * - Ctrl+S: 保存当前文件
     * - Ctrl+Shift+S: 保存全部文件
     * - Ctrl+W: 关闭当前标签页
     * - Ctrl+Shift+W: 关闭全部标签页
     * - Ctrl+Z: 撤销
     * - Ctrl+Shift+Z / Ctrl+Y: 重做
     * - Ctrl+Tab: 切换到下一个标签页
     * - Ctrl+Shift+Tab: 切换到上一个标签页
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (shortcutDispatcher.dispatch(event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        installMainActivityCleanup(
            context = this,
            bottomPanelController = bottomPanelController,
            fileTreeActionBridge = fileTreeActionBridge,
            editorActionBridge = editorActionBridge,
            shortcutDispatcher = shortcutDispatcher,
        )
        super.onDestroy()
    }
}
