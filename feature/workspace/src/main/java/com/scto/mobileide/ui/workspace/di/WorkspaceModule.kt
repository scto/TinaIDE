package com.scto.mobileide.ui.workspace.di

import com.scto.mobileide.core.proot.ToolchainConfig
import com.scto.mobileide.ui.workspace.DependencyInstallViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workspaceModule = module {
    viewModel { (tc: ToolchainConfig, llvm: Int?, installLinuxEnv: Boolean) ->
        DependencyInstallViewModel(get(), get(), get(), tc, llvm, installLinuxEnv)
    }
}
