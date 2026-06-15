package com.scto.mobileide.ui.compose.screens.settings.sections

internal object SettingsRootSectionSupport {
    fun shouldShowDeveloperEntry(
        developerOptionsEnabled: Boolean,
        serverDeveloperOptionsEnabled: Boolean
    ): Boolean = developerOptionsEnabled && serverDeveloperOptionsEnabled
}
