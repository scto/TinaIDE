package com.scto.mobileide.plugin

internal fun isSafePluginRelativePath(path: String): Boolean {
    if (path.isBlank() || path != path.trim()) return false
    val normalized = path.replace('\\', '/')
    if (normalized.startsWith("/")) return false
    if (WINDOWS_DRIVE_PATH.matches(normalized)) return false
    if (normalized.split('/').any { segment -> segment.isBlank() || segment == "." || segment == ".." }) {
        return false
    }
    return true
}

private val WINDOWS_DRIVE_PATH = Regex("^[A-Za-z]:.*")
