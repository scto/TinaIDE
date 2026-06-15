package com.scto.mobileide.ui.compose.screens.help.di

import com.scto.mobileide.data.repository.FeedbackRepository
import com.scto.mobileide.ui.compose.viewmodel.FeedbackViewModel
import com.scto.mobileide.ui.compose.screens.help.HelpViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val helpModule = module {
    factory { FeedbackRepository(get()) }
    viewModel { FeedbackViewModel(get(), get()) }
    viewModel { HelpViewModel(androidApplication()) }
}
