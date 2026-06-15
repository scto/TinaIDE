package com.scto.mobileide.output.di

import com.scto.mobileide.output.IOutputManager
import com.scto.mobileide.output.OutputManager
import org.koin.dsl.module

val outputModule = module {
    single<IOutputManager> { OutputManager(get()) }
}
