package com.scto.mobileide.ui.compose.screens.packages.di

import com.scto.mobileide.core.packages.PackageManager
import com.scto.mobileide.core.packages.PackageManagerImpl
import com.scto.mobileide.core.packages.api.PackageApiClient
import com.scto.mobileide.core.packages.store.LocalInstallStateStore
import com.scto.mobileide.core.proot.PRootEnvironment
import com.scto.mobileide.ui.compose.screens.packages.PackageManagerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val packagesModule = module {
    factory<PackageManager> {
        val apiClient = PackageApiClient.getInstance(get())
        val installStateStore = LocalInstallStateStore(get())
        val prootEnv = PRootEnvironment(get())
        PackageManagerImpl(get(), apiClient, installStateStore, prootEnv = prootEnv)
    }
    viewModel { PackageManagerViewModel(get(), get()) }
}
