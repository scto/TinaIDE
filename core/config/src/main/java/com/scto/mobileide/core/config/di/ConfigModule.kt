package com.scto.mobileide.core.config.di

import com.scto.mobileide.core.config.ConfigManager
import com.scto.mobileide.core.config.IConfigManager
import org.koin.dsl.module

val configModule = module {
    single<IConfigManager> { ConfigManager(get()).also { it.onCreate() } }
}
