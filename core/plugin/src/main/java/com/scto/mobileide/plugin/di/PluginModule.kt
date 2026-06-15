package com.scto.mobileide.plugin.di

import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.plugin.PluginLinuxEnvironmentProvider
import com.scto.mobileide.plugin.PluginManager
import com.scto.mobileide.plugin.PluginSnippetManager
import com.scto.mobileide.plugin.lsp.LspPluginManager
import com.scto.mobileide.plugin.script.ScriptPluginManager
import org.koin.dsl.module

val pluginModule = module {
    // 运行时必须复用 PluginManager 的全局单例，否则设置页和主界面会各持一份状态。
    single { PluginManager.getInstance(get()) }
    single<LinuxEnvironmentProvider> { PluginLinuxEnvironmentProvider(get(), get()) }
    single { PluginSnippetManager(get()).also { it.onCreate() } }
    single { LspPluginManager(get(), get(), get()).also { it.onCreate() } }
    single(createdAtStart = true) {
        ScriptPluginManager.getInstance(get()).also { manager ->
            manager.setProjectRootProvider {
                get<IProjectContext>().getCurrentProject()?.rootPath
            }
        }
    }
}
