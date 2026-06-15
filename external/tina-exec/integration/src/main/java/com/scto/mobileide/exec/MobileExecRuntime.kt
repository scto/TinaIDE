package com.scto.mobileide.exec

import android.content.Context
import java.io.File

enum class MobileExecPreloadMode {
    DIRECT,
    LINKER,
}

enum class MobileExecSystemLinkerMode(val envValue: String) {
    DISABLE("disable"),
    ENABLE("enable"),
    FORCE("force"),
}

/**
 * MobileIDE 内部封装的 exec preload 运行时。
 *
 * 这里只负责：
 * 1. 解析打包进 APK 的 preload so 路径
 * 2. 注入运行时环境变量
 * 3. 为现有执行链路提供按需接入点
 */
object MobileExecRuntime {

    private const val DIRECT_LIBRARY_FILE_NAME = "libmobile_exec_direct_ld_preload.so"
    private const val LINKER_LIBRARY_FILE_NAME = "libmobile_exec_linker_ld_preload.so"
    const val ENV_APP_DATA_DIR = "MOBILE_APP__DATA_DIR"
    const val ENV_APP_LEGACY_DATA_DIR = "MOBILE_APP__LEGACY_DATA_DIR"
    const val ENV_ROOTFS = "MOBILE_ROOTFS"
    const val ENV_PREFIX = "MOBILE_PREFIX"
    const val ENV_LOG_LEVEL = "MOBILE_EXEC__LOG_LEVEL"
    const val ENV_SYSTEM_LINKER_EXEC_MODE = "MOBILE_EXEC__SYSTEM_LINKER_EXEC__MODE"
    const val ENV_PROC_SELF_EXE = "MOBILE_EXEC__PROC_SELF_EXE"

    fun resolveLibraryPath(
        context: Context,
        mode: MobileExecPreloadMode,
    ): String? {
        val fileName = when (mode) {
            MobileExecPreloadMode.DIRECT -> DIRECT_LIBRARY_FILE_NAME
            MobileExecPreloadMode.LINKER -> LINKER_LIBRARY_FILE_NAME
        }
        return resolveBinaryPath(context, fileName)
    }

    fun applyLdPreload(
        environment: MutableMap<String, String>,
        context: Context,
        mode: MobileExecPreloadMode,
        systemLinkerMode: MobileExecSystemLinkerMode? = null,
        logLevel: Int? = null,
    ): Boolean {
        val libraryPath = resolveLibraryPath(context, mode) ?: return false

        populateBaseEnvironment(environment, context)
        environment["LD_PRELOAD"] = mergeLdPreload(libraryPath, environment["LD_PRELOAD"])

        if (systemLinkerMode != null) {
            environment[ENV_SYSTEM_LINKER_EXEC_MODE] = systemLinkerMode.envValue
        }
        if (logLevel != null) {
            environment[ENV_LOG_LEVEL] = logLevel.toString()
        }
        return true
    }

    fun recommendedMode(preferLinker64: Boolean): MobileExecPreloadMode {
        return if (preferLinker64) {
            MobileExecPreloadMode.LINKER
        } else {
            MobileExecPreloadMode.DIRECT
        }
    }

    private fun populateBaseEnvironment(
        environment: MutableMap<String, String>,
        context: Context,
    ) {
        val dataDir = context.dataDir.absolutePath
        val legacyDataDir = "/data/data/${context.packageName}"
        val rootfsDir = context.filesDir.absolutePath
        val prefixDir = File(context.filesDir, "usr").absolutePath

        environment.putIfAbsent(ENV_APP_DATA_DIR, dataDir)
        environment.putIfAbsent(ENV_APP_LEGACY_DATA_DIR, legacyDataDir)
        environment.putIfAbsent(ENV_ROOTFS, rootfsDir)
        environment.putIfAbsent(ENV_PREFIX, prefixDir)
    }

    private fun resolveBinaryPath(
        context: Context,
        fileName: String,
    ): String? {
        val candidate = File(context.applicationInfo.nativeLibraryDir, fileName)
        return candidate.takeIf { it.isFile }?.absolutePath
    }

    private fun mergeLdPreload(
        libraryPath: String,
        existingValue: String?,
    ): String {
        val current = existingValue?.trim().orEmpty()
        if (current.isEmpty()) return libraryPath

        val entries = current.split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (entries.contains(libraryPath)) {
            current
        } else {
            "$libraryPath $current"
        }
    }
}
