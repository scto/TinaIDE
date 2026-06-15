package com.scto.mobileide.terminal.di

import com.scto.mobileide.core.terminal.ILocaleInstaller
import com.scto.mobileide.core.terminal.IGuestDevPackagesInstaller
import com.scto.mobileide.core.terminal.IShellInstaller
import com.scto.mobileide.core.terminal.IShellResolver
import com.scto.mobileide.core.terminal.ITerminalPreferences
import com.scto.mobileide.core.terminal.ITerminalSessionManager
import com.scto.mobileide.core.terminal.ITerminalThemeProvider
import com.scto.mobileide.terminal.install.GuestDevelopmentPackagesInstaller
import com.scto.mobileide.terminal.locale.LocaleInstaller
import com.scto.mobileide.terminal.preferences.TerminalPreferences
import com.scto.mobileide.terminal.session.TerminalSessionManager
import com.scto.mobileide.terminal.session.TerminalSessionManagerAdapter
import com.scto.mobileide.terminal.shell.ShellResolverAdapter
import com.scto.mobileide.terminal.shell.ZshInstaller
import com.scto.mobileide.terminal.theme.TerminalThemeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val terminalModule = module {
    // 终端配置
    single<ITerminalPreferences> { TerminalPreferences.get(get()) }

    // 终端主题
    single<ITerminalThemeProvider> { TerminalThemeProvider() }

    // Locale 安装器
    factory<ILocaleInstaller> { LocaleInstaller(get()) }

    // Guest 开发基础包安装器
    factory<IGuestDevPackagesInstaller> { GuestDevelopmentPackagesInstaller(get()) }

    // Shell 安装器（Zsh）
    factory<IShellInstaller> { ZshInstaller(get()) }

    // Shell 解析器
    factory<IShellResolver> { ShellResolverAdapter(get()) }

    // TerminalSessionManager（feature:terminal 层具体类）
    single {
        TerminalSessionManager(
            application = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    // ITerminalSessionManager（core:common 层接口）
    single<ITerminalSessionManager> { TerminalSessionManagerAdapter(get()) }
}
