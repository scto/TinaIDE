package com.scto.mobileide.ai.integration

import android.content.Context
import com.scto.mobileide.ai.viewmodel.AiChatViewModel
import com.scto.mobileide.core.compile.ProcessManager
import com.scto.mobileide.core.compile.RunConfigurationManager
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.editor.symbol.ProjectSymbolIndexService
import com.scto.mobileide.ui.BottomPanelController
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.compose.state.editor.EditorContainerState
import kotlinx.coroutines.CoroutineScope

/**
 * AI 工具集成管理器
 *
 * 负责初始化和管理所有 AI 工具回调
 * 所有回调实现都在 app 模块中，直接集成当前编辑器状态能力
 */
class AiToolsIntegrationManager(
    private val context: Context,
    private val viewModel: AiChatViewModel,
    private val scope: CoroutineScope,
    private val linuxEnvironmentProvider: LinuxEnvironmentProvider
) {
    /**
     * 初始化项目上下文
     */
    fun initializeProject(
        projectRoot: String,
        editorState: EditorContainerState
    ) {
        viewModel.initializeProjectContext(
            projectRoot = projectRoot,
            getCurrentFile = {
                editorState.getActiveFileAbsolutePathOrNull()
            },
            getCurrentFileContent = {
                editorState.readActiveTabText()
            }
        )
    }

    /**
     * 注册所有回调
     */
    fun registerCallbacks(
        projectRoot: String,
        editorState: EditorContainerState,
        symbolIndexService: ProjectSymbolIndexService?,
        processManager: ProcessManager,
        runConfigManager: RunConfigurationManager,
        bottomPanelViewModel: BottomPanelViewModel,
        editorManager: com.scto.mobileide.editor.IEditorManager,
        outputManager: com.scto.mobileide.output.IOutputManager,
        compileProjectUseCase: com.scto.mobileide.core.compile.CompileProjectUseCase,
        bottomPanelController: BottomPanelController
    ) {
        val editorCallbacks = EditorToolCallbacksImpl(
            context = context,
            editorState = editorState,
            projectRoot = projectRoot,
            linuxEnvironmentProvider = linuxEnvironmentProvider
        )
        viewModel.setEditorCallbacks(editorCallbacks)

        val fileSystemCallbacks = FileSystemCallbacksImpl(context, projectRoot, editorState)
        viewModel.setFileSystemCallbacks(fileSystemCallbacks)

        val codeAnalysisCallbacks = CodeAnalysisCallbacksImpl(projectRoot, symbolIndexService)
        viewModel.setCodeAnalysisCallbacks(codeAnalysisCallbacks)

        val diagnosticsCallbacks = DiagnosticsCallbacksImpl(bottomPanelViewModel, projectRoot)
        viewModel.setDiagnosticsCallbacks(diagnosticsCallbacks)

        val executionCallbacks = ExecutionCallbacksImpl(
            projectRoot = projectRoot,
            processManager = processManager,
            runConfigManager = runConfigManager,
            editorManager = editorManager,
            outputManager = outputManager,
            compileProjectUseCase = compileProjectUseCase,
            scope = scope,
            bottomPanelViewModel = bottomPanelViewModel,
            bottomPanelController = bottomPanelController
        )
        viewModel.setExecutionCallbacks(executionCallbacks)
    }

    /**
     * 完整初始化
     */
    fun initializeAll(
        projectRoot: String,
        editorState: EditorContainerState,
        symbolIndexService: ProjectSymbolIndexService?,
        processManager: ProcessManager,
        runConfigManager: RunConfigurationManager,
        bottomPanelViewModel: BottomPanelViewModel,
        editorManager: com.scto.mobileide.editor.IEditorManager,
        outputManager: com.scto.mobileide.output.IOutputManager,
        compileProjectUseCase: com.scto.mobileide.core.compile.CompileProjectUseCase,
        bottomPanelController: BottomPanelController
    ) {
        initializeProject(projectRoot, editorState)
        registerCallbacks(
            projectRoot = projectRoot,
            editorState = editorState,
            symbolIndexService = symbolIndexService,
            processManager = processManager,
            runConfigManager = runConfigManager,
            bottomPanelViewModel = bottomPanelViewModel,
            editorManager = editorManager,
            outputManager = outputManager,
            compileProjectUseCase = compileProjectUseCase,
            bottomPanelController = bottomPanelController
        )
    }
}
