package com.scto.mobileide.core.compile.strategy

import com.scto.mobileide.core.compile.BuildDiagnostic
import com.scto.mobileide.core.compile.artifact.Artifact

/**
 * Strategy.execute(...) 的结果。
 *
 * 替代旧 `BuildResult.Success/Error`:
 * - Success 直接携带已填充 hash + sources 的 [Artifact],让 Orchestrator 可立即 register
 * - Failure 保留 diagnostics 结构,便于上层显示
 */
sealed interface ExecutionOutcome {
    data class Success(
        val artifact: Artifact,
        val rawOutput: String = "",
    ) : ExecutionOutcome

    data class Failure(
        val reason: String,
        val diagnostics: List<BuildDiagnostic> = emptyList(),
        val rawOutput: String = "",
    ) : ExecutionOutcome
}
