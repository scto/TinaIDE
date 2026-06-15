package com.scto.mobileide.core.compile.strategy

import android.content.Context
import com.scto.mobileide.core.compile.BuildSystem
import com.scto.mobileide.core.compile.TargetInfo
import com.scto.mobileide.core.compile.artifact.Artifact
import com.scto.mobileide.core.compile.artifact.ArtifactId
import com.scto.mobileide.core.compile.artifact.ArtifactKind
import com.scto.mobileide.core.compile.artifact.ArtifactSpec
import com.scto.mobileide.core.compile.artifact.BuildFingerprint
import com.scto.mobileide.core.compile.artifact.SourceRef
import com.scto.mobileide.core.compile.event.BuildEvent
import com.scto.mobileide.core.compile.event.BuildEventEmitter
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.core.linux.UnavailableLinuxEnvironmentProvider
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

/**
 * Gradle-Build-Strategie.
 *
 * Führt `./gradlew <task>` (Standard: `assembleDebug`) in der PRoot-Linux-Umgebung aus.
 * Setzt eine aktive [LinuxEnvironmentProvider]-Implementierung voraus.
 *
 * Design-Entscheidungen (KISS/DIP):
 * - Abhängigkeit von [LinuxEnvironmentProvider] (nicht konkret von PRoot) → DIP eingehalten.
 * - Kein eigener Gradle-Daemon-Management; das übernimmt Gradle selbst im Container.
 * - `describeOutput` gibt immer das Standard-Debug-APK zurück; Gradle-Varianten (Release etc.)
 *   können später über [BuildContext.options] gesteuert werden.
 */
class GradleStrategy(
    private val context: Context,
    private val linuxEnvironmentProvider: LinuxEnvironmentProvider = UnavailableLinuxEnvironmentProvider,
) : BuildStrategy {

    companion object {
        private const val TAG = "GradleStrategy"

        /** Standard-APK-Ausgabepfad relativ zum App-Modul-Root. */
        private const val DEFAULT_APK_RELATIVE = "app/build/outputs/apk/debug/app-debug.apk"

        /** Timeout für einen vollständigen Gradle-Build in Millisekunden (10 Minuten). */
        private const val GRADLE_BUILD_TIMEOUT_MS = 10 * 60 * 1_000L

        /** JAVA_HOME im PRoot-Container (Standard unter Debian/Ubuntu). */
        private const val PROOT_JAVA_HOME = "/usr/lib/jvm/default-java"

        private val TRACKED_GRADLE_FILES = setOf(
            "build.gradle", "build.gradle.kts",
            "settings.gradle", "settings.gradle.kts",
            "gradle.properties", "local.properties",
            "gradlew", "gradlew.bat",
        )
        private val TRACKED_SOURCE_EXTENSIONS = setOf(
            "kt", "kts", "java", "xml",
        )
    }

    override val buildSystem: BuildSystem = BuildSystem.GRADLE

    /**
     * Ein Verzeichnis gilt als Gradle-Projekt, wenn ein Build-Skript vorhanden ist.
     */
    override suspend fun canHandle(projectRoot: File): Boolean {
        return File(projectRoot, "build.gradle").exists() ||
                File(projectRoot, "build.gradle.kts").exists() ||
                File(projectRoot, "settings.gradle").exists() ||
                File(projectRoot, "settings.gradle.kts").exists()
    }

    /**
     * Beschreibt das erwartete Ausgabe-APK.
     *
     * Gradle-Projekte produzieren standardmäßig ein Debug-APK im `app`-Untermodul.
     * Falls das Projekt nur ein einziges Root-Modul besitzt (kein `app/`-Unterverzeichnis),
     * wird das APK direkt unter `build/outputs/apk/debug/` erwartet.
     */
    override suspend fun describeOutput(ctx: BuildContext): ArtifactSpec {
        val apkFile = resolveExpectedApk(ctx.projectRoot)
        return ArtifactSpec(
            id = ArtifactId(
                projectId = ctx.projectId,
                targetName = resolveGradleTask(ctx),
                variant = "debug",
            ),
            expectedPath = apkFile,
            kind = ArtifactKind.APK,
            sources = collectGradleSourceFiles(ctx.projectRoot),
        )
    }

    /**
     * Führt den Gradle-Build im PRoot-Container aus.
     *
     * Ablauf:
     * 1. Prüft, ob die Linux-Umgebung verfügbar ist.
     * 2. Ermittelt den Gradle-Befehl (Wrapper bevorzugt).
     * 3. Führt `./gradlew <task>` aus und leitet stdout/stderr als [BuildEvent.Build.CompileProgress] weiter.
     * 4. Prüft, ob das APK tatsächlich erzeugt wurde.
     * 5. Berechnet den Content-Hash und gibt [ExecutionOutcome.Success] zurück.
     */
    override suspend fun execute(
        ctx: BuildContext,
        spec: ArtifactSpec,
        fingerprint: BuildFingerprint,
        emitter: BuildEventEmitter,
    ): ExecutionOutcome {
        val linuxEnv = linuxEnvironmentProvider.get()

        if (!linuxEnv.isAvailable()) {
            val reason = "PRoot-Linux-Umgebung ist nicht installiert oder deaktiviert. " +
                    "Bitte installieren Sie die PRoot-Umgebung, um Gradle-Projekte zu erstellen."
            emitter.emit(BuildEvent.Build.CompileFailed(reason, emptyList()))
            return ExecutionOutcome.Failure(reason)
        }

        val task = resolveGradleTask(ctx)
        emitter.emit(BuildEvent.Build.CompileStarted(task))
        Timber.tag(TAG).d("Starting Gradle build: task=%s, project=%s", task, ctx.projectRoot)

        val startTime = System.currentTimeMillis()
        val guestProjectRoot = linuxEnv.toGuestPath(ctx.projectRoot.absolutePath)
        val hasWrapper = File(ctx.projectRoot, "gradlew").exists()
        val gradleCmd = if (hasWrapper) "./gradlew" else "gradle"

        if (!hasWrapper) {
            Timber.tag(TAG).w("No gradlew found in %s, falling back to global 'gradle'", ctx.projectRoot)
            emitter.tryEmit(
                BuildEvent.Build.CompileProgress(
                    "⚠ Kein Gradle-Wrapper (gradlew) gefunden. Verwende globales 'gradle'."
                )
            )
        }

        val buildResult = linuxEnv.execute(
            command = listOf(
                "/bin/sh", "-lc",
                "cd \"$guestProjectRoot\" && chmod +x gradlew 2>/dev/null; $gradleCmd $task --no-daemon"
            ),
            workDir = guestProjectRoot,
            env = mapOf("JAVA_HOME" to PROOT_JAVA_HOME),
            timeout = GRADLE_BUILD_TIMEOUT_MS,
        )

        val elapsed = System.currentTimeMillis() - startTime
        val combinedOutput = buildResult.combinedOutput

        // Live-Ausgabe zeilenweise weiterleiten
        combinedOutput.lineSequence().forEach { line ->
            emitter.tryEmit(BuildEvent.Build.CompileProgress(line))
        }

        if (!buildResult.isSuccess) {
            val reason = if (buildResult.timedOut) {
                "Gradle-Build Timeout nach ${GRADLE_BUILD_TIMEOUT_MS / 1000}s. Verwenden Sie '--no-daemon' und erhöhen Sie ggf. die Speicherlimits."
            } else {
                "Gradle-Build fehlgeschlagen (Exit-Code ${buildResult.exitCode}). " +
                        "Prüfen Sie die Ausgabe oben auf Fehlerdetails."
            }
            Timber.tag(TAG).e("Gradle build failed: exitCode=%d, timedOut=%b", buildResult.exitCode, buildResult.timedOut)
            emitter.emit(BuildEvent.Build.CompileFailed(reason, emptyList()))
            return ExecutionOutcome.Failure(reason = reason, rawOutput = combinedOutput)
        }

        // APK lokalisieren
        val apkFile = resolveExpectedApk(ctx.projectRoot)
        if (!apkFile.isFile) {
            // Fallback: suche im gesamten build/outputs/apk/-Verzeichnis
            val fallbackApk = findFirstApk(ctx.projectRoot)
            if (fallbackApk == null) {
                val reason = "Gradle-Build erfolgreich, aber kein APK gefunden unter: ${apkFile.absolutePath}"
                Timber.tag(TAG).e(reason)
                emitter.emit(BuildEvent.Build.CompileFailed(reason, emptyList()))
                return ExecutionOutcome.Failure(reason = reason, rawOutput = combinedOutput)
            }
            Timber.tag(TAG).d("APK found at fallback path: %s", fallbackApk.absolutePath)
            return buildSuccess(ctx, spec, fingerprint, fallbackApk, elapsed, combinedOutput, emitter)
        }

        return buildSuccess(ctx, spec, fingerprint, apkFile, elapsed, combinedOutput, emitter)
    }

    override suspend fun clean(ctx: BuildContext, reconfigure: Boolean) {
        val linuxEnv = linuxEnvironmentProvider.get()
        if (!linuxEnv.isAvailable()) {
            Timber.tag(TAG).w("clean() called but Linux environment is unavailable; skipping")
            return
        }
        val guestProjectRoot = linuxEnv.toGuestPath(ctx.projectRoot.absolutePath)
        val hasWrapper = File(ctx.projectRoot, "gradlew").exists()
        val gradleCmd = if (hasWrapper) "./gradlew" else "gradle"
        Timber.tag(TAG).d("Running Gradle clean in %s", ctx.projectRoot)
        linuxEnv.execute(
            command = listOf("/bin/sh", "-lc", "cd \"$guestProjectRoot\" && $gradleCmd clean --no-daemon"),
            workDir = guestProjectRoot,
            env = mapOf("JAVA_HOME" to PROOT_JAVA_HOME),
            timeout = GRADLE_BUILD_TIMEOUT_MS,
        )
    }

    /**
     * Gradle-Projekte haben keine statischen Targets im IDE-Sinne.
     * Wir liefern einen einzelnen Pseudo-Target zurück, der den ausgewählten Task beschreibt.
     */
    override suspend fun getTargets(ctx: BuildContext): List<TargetInfo> {
        return listOf(
            TargetInfo(
                name = resolveGradleTask(ctx),
                type = TargetInfo.Type.EXECUTABLE,
                sources = emptyList(),
                outputName = "app-debug.apk",
            )
        )
    }

    // ---------- Private Hilfsmethoden ----------

    /**
     * Bestimmt den auszuführenden Gradle-Task.
     * Standard: `assembleDebug`; benutzerdefinierter Target-Name aus [BuildContext.target] wird bevorzugt.
     */
    private fun resolveGradleTask(ctx: BuildContext): String {
        return ctx.target.takeIf { !it.isNullOrBlank() } ?: "assembleDebug"
    }

    /**
     * Ermittelt den erwarteten APK-Pfad.
     * Bevorzugt die klassische Zwei-Modul-Struktur (`app/build/…`),
     * fällt auf Root-Build-Ausgabe zurück, falls kein `app/`-Unterverzeichnis vorhanden.
     */
    private fun resolveExpectedApk(projectRoot: File): File {
        val appModuleApk = File(projectRoot, DEFAULT_APK_RELATIVE)
        if (appModuleApk.isFile) return appModuleApk

        // Fallback: Root-Modul ohne app/-Unterverzeichnis
        val rootModuleApk = File(projectRoot, "build/outputs/apk/debug")
            .listFiles { f -> f.extension.equals("apk", ignoreCase = true) }
            ?.firstOrNull()
        return rootModuleApk ?: appModuleApk // Gibt erwarteten Pfad zurück (auch wenn noch nicht vorhanden)
    }

    /**
     * Sucht rekursiv das erste APK im gesamten Projektverzeichnis
     * (Fallback, wenn das APK nicht am Standardort liegt).
     */
    private fun findFirstApk(projectRoot: File): File? {
        return projectRoot.walkTopDown()
            .maxDepth(8)
            .firstOrNull { it.isFile && it.extension.equals("apk", ignoreCase = true) }
    }

    /**
     * Sammelt Gradle-spezifische Eingabedateien für die Fingerprint-Berechnung.
     * Beinhaltet alle Build-Skripte, Kotlin/Java-Quellen und AndroidManifest.xml.
     */
    private fun collectGradleSourceFiles(projectRoot: File): List<File> {
        val gradleScripts = projectRoot.walkTopDown()
            .maxDepth(3)
            .filter { it.isFile && it.name in TRACKED_GRADLE_FILES }
            .toList()
        val sourceDirs = listOf(
            File(projectRoot, "src"),
            File(projectRoot, "app/src"),
        )
        val sourceFiles = sourceDirs
            .filter { it.isDirectory }
            .flatMap { dir ->
                dir.walkTopDown()
                    .filter { it.isFile && it.extension.lowercase() in TRACKED_SOURCE_EXTENSIONS }
                    .toList()
            }
        return (gradleScripts + sourceFiles).distinct()
    }

    private suspend fun buildSuccess(
        ctx: BuildContext,
        spec: ArtifactSpec,
        fingerprint: BuildFingerprint,
        apkFile: File,
        elapsedMs: Long,
        rawOutput: String,
        emitter: BuildEventEmitter,
    ): ExecutionOutcome {
        val artifact = Artifact(
            id = spec.id,
            absolutePath = apkFile.absolutePath,
            kind = ArtifactKind.APK,
            contentHash = computeContentHash(apkFile),
            fingerprint = fingerprint,
            sources = spec.sources.map { captureSourceRef(it, ctx.projectRoot) },
            compiledAt = System.currentTimeMillis(),
            buildTimeMs = elapsedMs,
        )
        emitter.emit(BuildEvent.Build.CompileCompleted(artifact))
        Timber.tag(TAG).d("Gradle build succeeded in %dms, APK: %s", elapsedMs, apkFile.absolutePath)
        return ExecutionOutcome.Success(artifact, rawOutput)
    }

    private fun captureSourceRef(file: File, projectRoot: File): SourceRef = SourceRef(
        relativePath = file.toRelativeString(projectRoot),
        mtime = file.lastModified(),
        size = file.length(),
    )

    private fun computeContentHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n <= 0) break
                digest.update(buffer, 0, n)
            }
        }
        val bytes = digest.digest()
        return buildString(32) {
            for (i in 0 until 16) {
                append((bytes[i].toInt() and 0xFF).toString(16).padStart(2, '0'))
            }
        }
    }

}
