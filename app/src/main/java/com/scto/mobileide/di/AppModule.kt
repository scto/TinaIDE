package com.scto.mobileide.di

import android.content.Context
import android.content.Intent
import com.scto.mobileide.core.IAppNavigator
import com.scto.mobileide.core.symbol.IProjectSymbolIndexService
import com.scto.mobileide.file.FileManager
import com.scto.mobileide.file.IFileOperations
import com.scto.mobileide.file.IFileWatchService
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.file.IProjectSession
import com.scto.mobileide.file.IRecentFilesProvider
import org.koin.dsl.module

val appModule = module {
    // 应用级导航器（供 feature 模块跳转 app 内 Activity）
    single<IAppNavigator> {
        object : IAppNavigator {
            override fun navigateToProjectManager(context: Context) {
                val intent = Intent(context, com.scto.mobileide.ui.MainPortalActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }

            override fun navigateToTerminal(context: Context, workDir: String) {
                val intent = Intent(context, com.scto.mobileide.ui.TerminalActivity::class.java).apply {
                    putExtra(com.scto.mobileide.ui.TerminalActivity.EXTRA_WORK_DIR, workDir)
                    putExtra(com.scto.mobileide.ui.TerminalActivity.EXTRA_PROJECT_PATH, workDir)
                }
                context.startActivity(intent)
            }
        }
    }

    // 文件管理器（app 模块实现）
    single {
        FileManager(
            context = get(),
            configManager = get(),
            projectLocationManager = get(),
            storageManager = get(),
            projectSymbolIndexServiceProvider = { getKoin().getOrNull<IProjectSymbolIndexService>() },
        ).also { it.onCreate() }
    }
    single<IFileOperations> { get<FileManager>() }
    single<IRecentFilesProvider> { get<FileManager>() }
    single<IFileWatchService> { get<FileManager>() }
    single<IProjectContext> { get<FileManager>() }
    single<IProjectSession> { get<FileManager>() }
}
