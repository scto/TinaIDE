package com.scto.mobileide.ui.compose.screens.packages

import com.scto.mobileide.core.packages.model.GUIPackage
import com.scto.mobileide.core.packages.model.InstallProgressEvent
import com.scto.mobileide.core.packages.model.Platform

object PackageInstallUiStateSupport {
    fun resolvePreferredInstallPlatform(pkg: GUIPackage): Platform? = when {
        pkg.android != null -> Platform.ANDROID
        pkg.linux != null -> Platform.LINUX
        else -> null
    }

    fun resolveAvailableInstallPlatform(pkg: GUIPackage, preferred: Platform? = null): Platform? {
        if (preferred == Platform.ANDROID && pkg.android != null) return Platform.ANDROID
        if (preferred == Platform.LINUX && pkg.linux != null) return Platform.LINUX
        return resolvePreferredInstallPlatform(pkg)
    }

    fun progressFromEvent(event: InstallProgressEvent): Float? = when (event) {
        is InstallProgressEvent.Downloading -> event.progress
        is InstallProgressEvent.Extracting -> event.progress
        is InstallProgressEvent.Completed -> 1f
        else -> null
    }
}
