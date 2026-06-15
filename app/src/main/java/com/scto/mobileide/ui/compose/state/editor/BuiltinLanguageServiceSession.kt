package com.scto.mobileide.ui.compose.state.editor

import com.scto.mobileide.core.editorlsp.CompletionFetchResult
import com.scto.mobileide.core.editorlsp.SemanticToken
import com.scto.mobileide.core.editorlsp.SignatureHelpResult
import com.scto.mobileide.core.lsp.Diagnostic
import com.scto.mobileide.core.lsp.DocumentSymbolItem
import com.scto.mobileide.core.lsp.LocationItem
import com.scto.mobileide.core.textengine.Position
import com.scto.mobileide.core.textengine.TextChange

/**
 * 内建语言服务统一抽象，避免不同语言在 LSP 管理器里继续复制分支逻辑。
 */
internal interface BuiltinLanguageServiceSession {
    val isConnected: Boolean

    fun didChange(change: TextChange)

    fun didSave(fullText: String?)

    suspend fun requestCompletion(
        position: Position,
        triggerChar: Char?
    ): CompletionFetchResult

    suspend fun requestSemanticTokens(): List<SemanticToken>

    fun currentDiagnostics(): List<Diagnostic>

    fun documentSymbols(): List<DocumentSymbolItem>

    fun gotoDefinition(position: Position): List<LocationItem>

    fun findReferences(position: Position): List<LocationItem>

    fun hover(position: Position): String?

    suspend fun requestSignatureHelp(position: Position): SignatureHelpResult? = null

    fun close()
}
