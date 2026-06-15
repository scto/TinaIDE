package com.scto.mobileide.storage.di

import com.scto.mobileide.storage.ProjectLocationManager
import com.scto.mobileide.storage.StorageCleanupManager
import com.scto.mobileide.storage.StorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val storageModule = module {
    single { StorageManager(get()).also { it.onCreate() } }
    single {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        ProjectLocationManager(get(), scope).also { it.onCreate() }
    }
    single { StorageCleanupManager(get()) }
}
