package com.scto.mobileide.ui.compose.screens.settings.di

import com.scto.mobileide.ui.compose.screens.settings.SettingsViewModel
import com.scto.mobileide.ui.compose.screens.settings.StorageCleanupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { StorageCleanupViewModel(get()) }
}
