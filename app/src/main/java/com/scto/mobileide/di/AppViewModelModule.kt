package com.scto.mobileide.di

import com.scto.mobileide.core.compile.CompileProjectUseCase
import com.scto.mobileide.core.compile.PluginProjectActions
import com.scto.mobileide.core.git.GitService
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.plugindev.AndroidPluginProjectActions
import com.scto.mobileide.ui.BottomPanelViewModel
import com.scto.mobileide.ui.CompilerViewModel
import com.scto.mobileide.ui.DebugViewModel
import com.scto.mobileide.ui.EditorStateViewModel
import com.scto.mobileide.ui.GitViewModel
import com.scto.mobileide.ui.GlobalSearchViewModel
import com.scto.mobileide.ui.MainActivityActionsViewModel
import com.scto.mobileide.ui.MainViewModel
import com.scto.mobileide.ui.MultiTerminalViewModel
import com.scto.mobileide.ui.ProjectManagerViewModel
import com.scto.mobileide.ui.compose.screens.main.market.MarketScreenViewModel
import com.scto.mobileide.ui.compose.screens.main.profile.DownloadHistoryViewModel
import com.scto.mobileide.ui.compose.screens.main.profile.FavoritesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appViewModelModule = module {
    single<PluginProjectActions> { AndroidPluginProjectActions(androidApplication(), get()) }

    factory {
        CompileProjectUseCase(
            appContext = get(),
            projectContext = get(),
            outputManager = get(),
            editorManagerProvider = { get<IEditorManager>() },
            linuxEnvironmentProvider = get(),
            orchestratorProvider = { get() },
            strategyRegistry = get(),
            buildContextFactory = get(),
            terminalCommandBuilder = get(),
            eventBus = get(),
            pluginProjectActions = get(),
        )
    }

    single { GitService(get()) }

    viewModel { CompilerViewModel(get(), get(), get()) }
    viewModel { BottomPanelViewModel(androidApplication(), get()) }
    viewModel { EditorStateViewModel() }
    viewModel { MainViewModel(androidApplication(), get()) }
    viewModel { DebugViewModel(androidApplication(), get(), get()) }
    viewModel { GitViewModel(get()) }
    viewModel { MainActivityActionsViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { ProjectManagerViewModel(androidApplication(), get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { DownloadHistoryViewModel(get()) }
    viewModel { MarketScreenViewModel(androidApplication(), get()) }
    viewModel { MultiTerminalViewModel(androidApplication(), get()) }
    viewModel { GlobalSearchViewModel(androidApplication()) }
}
