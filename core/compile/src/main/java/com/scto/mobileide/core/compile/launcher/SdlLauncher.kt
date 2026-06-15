package com.scto.mobileide.core.compile.launcher

import com.scto.mobileide.core.compile.artifact.Artifact
import com.scto.mobileide.core.compile.artifact.ArtifactKind
import com.scto.mobileide.core.compile.event.BuildEvent
import com.scto.mobileide.core.compile.event.BuildEventEmitter
import com.scto.mobileide.core.compile.strategy.BuildContext
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.i18n.strOr
import java.io.File
import timber.log.Timber

/**
 * Validates a `.so` artifact before the UI starts the SDL graphical runtime.
 */
class SdlLauncher : Launcher {

    companion object {
        private const val TAG = "SdlLauncher"
    }

    override suspend fun launch(
        artifact: Artifact,
        ctx: BuildContext,
        emitter: BuildEventEmitter,
    ): LaunchOutcome {
        val file = File(artifact.absolutePath)
        if (!file.isFile) {
            val reason = Strings.sdl_runtime_error_main_library_invalid.strOr(ctx.appContext, artifact.absolutePath)
            emitter.emit(BuildEvent.Launch.Failed(reason, wasArtifactCached = false))
            return LaunchOutcome.Failed(reason)
        }
        if (artifact.kind != ArtifactKind.SHARED_LIBRARY || !file.name.endsWith(".so", ignoreCase = true)) {
            val reason = Strings.sdl_runtime_invalid_shared_library.strOr(ctx.appContext, artifact.absolutePath)
            emitter.emit(BuildEvent.Launch.Failed(reason, wasArtifactCached = false))
            return LaunchOutcome.Failed(reason)
        }
        Timber.tag(TAG).d("SDL graphical launch prepared: %s", file.absolutePath)
        emitter.emit(BuildEvent.Launch.Completed)
        return LaunchOutcome.Prepared(LaunchDescriptor.Sdl(artifact = artifact, libraryPath = artifact.absolutePath))
    }
}
