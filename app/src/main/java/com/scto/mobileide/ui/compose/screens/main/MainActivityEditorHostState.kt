package com.scto.mobileide.ui.compose.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.scto.mobileide.core.lsp.Diagnostic
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.editor.symbol.ProjectSymbolIndexService
import com.scto.mobileide.editor.theme.PluginEditorThemeRegistry
import com.scto.mobileide.plugin.PluginSnippetManager
import com.scto.mobileide.ui.compose.state.editor.EditorContainerState
import com.scto.mobileide.ui.compose.state.editor.rememberEditorContainerState
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext

@Stable
internal data class MainActivityEditorHostState(
    val editorContainerState: EditorContainerState,
    val projectSymbolIndexService: ProjectSymbolIndexService?,
)

@Composable
internal fun rememberMainActivityEditorHostState(
    editorManager: IEditorManager,
    projectRootPathProvider: () -> String?,
    onLspDiagnosticsChanged: (String, List<Diagnostic>) -> Unit,
): MainActivityEditorHostState {
    val projectSymbolIndexServiceProvider = remember {
        { GlobalContext.getOrNull()?.getOrNull<ProjectSymbolIndexService>() }
    }
    val pluginSnippetManager: PluginSnippetManager = koinInject()
    val pluginEditorThemeRegistry: PluginEditorThemeRegistry = koinInject()
    val editorContainerState = rememberEditorContainerState(
        editorManager = editorManager,
        snippetManager = pluginSnippetManager,
        pluginThemeRegistry = pluginEditorThemeRegistry,
        projectSymbolIndexServiceProvider = projectSymbolIndexServiceProvider,
        projectRootPathProvider = projectRootPathProvider,
        onLspDiagnosticsChanged = onLspDiagnosticsChanged,
    )
    val projectSymbolIndexService = projectSymbolIndexServiceProvider()

    return remember(editorContainerState, projectSymbolIndexService) {
        MainActivityEditorHostState(
            editorContainerState = editorContainerState,
            projectSymbolIndexService = projectSymbolIndexService,
        )
    }
}
