package com.scto.mobileide.core.proot.di

import com.scto.mobileide.core.proot.InstallLogManager
import org.koin.dsl.module

val prootModule = module {
    single { InstallLogManager(get()) }
}
