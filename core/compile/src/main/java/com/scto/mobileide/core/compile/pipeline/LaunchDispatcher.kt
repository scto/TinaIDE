package com.scto.mobileide.core.compile.pipeline

import com.scto.mobileide.core.compile.OutputMode
import com.scto.mobileide.core.compile.action.LaunchIntent
import com.scto.mobileide.core.compile.artifact.Artifact
import com.scto.mobileide.core.compile.event.BuildEvent
import com.scto.mobileide.core.compile.event.BuildEventEmitter
import com.scto.mobileide.core.compile.event.BuildReport
import com.scto.mobileide.core.compile.launcher.DebugLauncher
import com.scto.mobileide.core.compile.launcher.LaunchOutcome
import com.scto.mobileide.core.compile.launcher.SdlLauncher
import com.scto.mobileide.core.compile.launcher.TerminalLauncher
import com.scto.mobileide.core.compile.strategy.BuildContext

/**
 * Launch 分派器:按 [LaunchIntent] 路由到具体 [com.scto.mobileide.core.compile.launcher.Launcher]。
 *
 * 统一:
 * - 发射 `Launch.Started`(避免各 Launcher 重复发)
 * - 把 [LaunchOutcome] 封装为 [BuildReport.Success] / [BuildReport.LaunchFailed]
 * - 透传 `artifactWasCached`(Auto-Fallback 决策必需)
*/
class LaunchDispatcher(
    private val sdlLauncher: SdlLauncher,
    private val debugLauncher: DebugLauncher,
    private val terminalLauncher: TerminalLauncher,
) {

    suspend fun dispatch(
        intent: LaunchIntent,
        artifact: Artifact,
        artifactWasCached: Boolean,
        ctx: BuildContext,
        emitter: BuildEventEmitter,
    ): BuildReport {
        require(intent !is LaunchIntent.None) {
            "LaunchDispatcher.dispatch should not be called with LaunchIntent.None"
        }
        emitter.emit(BuildEvent.Launch.Started(artifact))

        if (artifact.kind == com.scto.mobileide.core.compile.artifact.ArtifactKind.APK) {
            return BuildReport.Success(
                artifact = artifact,
                descriptor = com.scto.mobileide.core.compile.launcher.LaunchDescriptor.Apk(artifact, artifact.absolutePath),
                summary = if (artifactWasCached) "launched from cached APK" else "launched freshly built APK",
            )
        }

        val outcome: LaunchOutcome = when (intent) {
            is LaunchIntent.Run -> when (intent.outputMode) {
                OutputMode.TERMINAL -> terminalLauncher.launch(artifact, ctx, emitter)
                OutputMode.SDL -> sdlLauncher.launch(artifact, ctx, emitter)
            }
            LaunchIntent.Debug -> debugLauncher.launch(artifact, ctx, emitter)
            is LaunchIntent.Terminal -> terminalLauncher.launchWithWorkingDir(
                artifact = artifact,
                workingDir = intent.workingDir,
                ctx = ctx,
                emitter = emitter,
            )
            LaunchIntent.None -> error("unreachable (pre-checked above)")
        }

        return when (outcome) {
            is LaunchOutcome.Prepared -> BuildReport.Success(
                artifact = artifact,
                descriptor = outcome.descriptor,
                summary = if (artifactWasCached) "launched from cached artifact" else "launched freshly built artifact",
            )
            is LaunchOutcome.Failed -> BuildReport.LaunchFailed(
                reason = outcome.reason,
                artifact = artifact,
                artifactWasCached = artifactWasCached,
            )
        }
    }
}
