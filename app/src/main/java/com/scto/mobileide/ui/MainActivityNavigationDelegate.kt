package com.scto.mobileide.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.i18n.strOr
import com.scto.mobileide.core.lsp.Diagnostic
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.ui.compose.state.editor.EditorContainerState

/**
 * MainActivity 的导航宿主委托。
 *
 * 负责全局搜索入口、搜索结果回填以及诊断跳转的宿主编排。
 */
class MainActivityNavigationDelegate(
    private val context: Context,
    private val projectContext: IProjectContext,
    private val launchIntent: (Intent) -> Unit,
    private val onNavigateToSearchResult: (String, Int) -> Unit,
    private val onNavigateToDiagnostic: (Diagnostic, EditorContainerState) -> Unit,
    private val onToastError: (String) -> Unit,
) {
    fun openGlobalSearch() {
        val project = projectContext.getCurrentProject()
        if (project == null) {
            onToastError(Strings.toast_please_open_project.strOr(context))
            return
        }
        launchIntent(GlobalSearchActivity.createIntent(context, project.rootPath))
    }

    fun handleGlobalSearchResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        val filePath = data?.getStringExtra(GlobalSearchActivity.RESULT_FILE_PATH) ?: return
        val lineNumber = data.getIntExtra(GlobalSearchActivity.RESULT_LINE_NUMBER, 1)
        onNavigateToSearchResult(filePath, lineNumber)
    }

    fun navigateToDiagnostic(editorContainerState: EditorContainerState, diagnostic: Diagnostic) {
        onNavigateToDiagnostic(diagnostic, editorContainerState)
    }
}
