package com.scto.mobileide

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import com.itsaky.androidide.treesitter.TreeSitter
import com.scto.mobileide.ai.di.aiModule
import com.scto.mobileide.core.compile.di.compileModule
import com.scto.mobileide.core.config.IConfigManager
import com.scto.mobileide.core.config.di.configModule
import com.scto.mobileide.core.crash.CrashLogAutoUploader
import com.scto.mobileide.core.crash.NativeCrashHandler
import com.scto.mobileide.core.debug.di.debugModule
import com.scto.mobileide.core.i18n.AppStrings
import com.scto.mobileide.core.logging.MobileTimber
import com.scto.mobileide.core.proot.PRootBootstrap
import com.scto.mobileide.core.proot.di.prootModule
import com.scto.mobileide.core.util.CrashLogPrivacyClassifier
import com.scto.mobileide.database.di.databaseModule
import com.scto.mobileide.database.worker.FavoriteSyncWorker
import com.scto.mobileide.di.appModule
import com.scto.mobileide.di.appViewModelModule
import com.scto.mobileide.editor.di.editorModule
import com.scto.mobileide.extensions.applyMobileSystemBars
import com.scto.mobileide.output.di.outputModule
import com.scto.mobileide.plugin.di.pluginModule
import com.scto.mobileide.startup.BundledPackagesInstallTask
import com.scto.mobileide.startup.CoreServiceRegistrar
import com.scto.mobileide.startup.ProjectMetadataInitializer
import com.scto.mobileide.startup.ServerConfigSyncTask
import com.scto.mobileide.startup.StartupFlowManager
import com.scto.mobileide.startup.ThemeInitializer
import com.scto.mobileide.storage.di.storageModule
import com.scto.mobileide.terminal.di.terminalModule
import com.scto.mobileide.ui.activity.CrashActivity
import com.scto.mobileide.ui.compose.screens.help.di.helpModule
import com.scto.mobileide.ui.compose.screens.packages.di.packagesModule
import com.scto.mobileide.ui.compose.screens.settings.di.settingsModule
import com.scto.mobileide.ui.workspace.di.workspaceModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get as koinGet
import timber.log.Timber

class MobileApplication : Application() {

    companion object {
        private const val TAG = "MobileApplication"
        private const val TOOLCHAIN_PROCESS_SUFFIX = ":toolchain"
    }

    // 应用级协程作用域（用于后台任务）
    // 使用 Dispatchers.Default 而非 Dispatchers.Main：后台任务（ServerConfigSync、Auth、BundledPackages 等）
    // 不需要在主线程排队，子任务按需通过 withContext(Dispatchers.IO) 切换到 IO 线程。
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * attachBaseContext 中初始化 xCrash
     *
     * xCrash 必须尽早初始化以捕获 Native 崩溃，
     * 在 attachBaseContext 中初始化可确保在任何其他代码执行前就绑定信号处理器。
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        // 初始化 Timber 日志框架（尽早初始化以捕获所有日志）
        MobileTimber.initialize(
            context = this,
            isDebug = BuildConfig.DEBUG,
            logDir = com.scto.mobileide.storage.ProjectPaths.ensureDir(
                com.scto.mobileide.storage.ProjectPaths.getLogsRoot(this)
            ),
        )

        // 初始化 xCrash（Native 崩溃捕获）
        // 必须在 attachBaseContext 中尽早调用
        val processName = Application.getProcessName()
        // 主进程和原生运行容器进程弹出崩溃界面。
        // SDL 用户 native 程序运行在隔离进程，崩溃时只结束运行容器，并由 :crash 进程展示日志。
        // 用户项目/插件运行日志属于用户隐私，只本地保存与展示，不上传到 MobileIDE 服务器。
        val crashSource = when {
            CrashLogPrivacyClassifier.isHostAppProcess(packageName, processName) -> CrashActivity.SOURCE_APP
            CrashLogPrivacyClassifier.isUserRuntimeProcess(
                packageName,
                processName
            ) -> CrashActivity.SOURCE_USER_NATIVE
            else -> null
        }
        NativeCrashHandler.setCrashUploadEnabled(
            CrashLogPrivacyClassifier.shouldUploadCrashForProcess(packageName, processName)
        )
        NativeCrashHandler.setCrashDisplayer(
            if (crashSource != null) {
                NativeCrashHandler.CrashDisplayer { context, crashReport ->
                    CrashActivity.start(context, crashReport, crashSource)
                }
            } else {
                null
            }
        )
        NativeCrashHandler.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        val processName = Application.getProcessName()
        val isHostAppProcess = CrashLogPrivacyClassifier.isHostAppProcess(packageName, processName)
        if (processName == packageName + TOOLCHAIN_PROCESS_SUFFIX) {
            Timber.tag(TAG).i("Toolchain process detected, skipping heavy app initialization")
            return
        }

        AppStrings.initialize(this)
        if (CrashLogPrivacyClassifier.isUserRuntimeProcess(packageName, processName)) {
            // 原生运行容器进程只保留 Activity 必需的主题/字符串初始化。
            // 用户 native 崩溃会结束对应隔离进程，不应触发宿主侧数据库、Koin、后台任务等初始化。
            val themeInitializer = ThemeInitializer(this)
            themeInitializer.initialize()
            themeInitializer.applyNightMode()
            Timber.tag(TAG).i("Native runtime process detected, skipping host app initialization: %s", processName)
            return
        }

        // 初始化项目元数据存储的 IDE 版本信息
        ProjectMetadataInitializer(this).execute()

        // Tree-sitter runtime (android-tree-sitter) 仅在主进程加载，避免 :crash 等进程不必要的 native 初始化
        if (isHostAppProcess) {
            runCatching {
                TreeSitter.loadLibrary()
                Timber.i("android-tree-sitter native library loaded successfully")
            }.onFailure { t ->
                Timber.e(t, "CRITICAL: Failed to load android-tree-sitter native library — tree-sitter features will crash")
            }
        }

        // 清理旧的崩溃日志（保留最近 10 个）
        NativeCrashHandler.cleanupOldTombstones(10)

        // 初始化 Koin DI 框架
        startKoin {
            androidContext(this@MobileApplication)
            modules(
                configModule,
                storageModule,
                databaseModule,
                pluginModule,
                editorModule,
                outputModule,
                packagesModule,
                settingsModule,
                terminalModule,
                helpModule,
                workspaceModule,
                debugModule,
                prootModule,
                compileModule,
                aiModule,
                appModule,
                appViewModelModule,
            )
        }

        // 执行副作用初始化（Prefs、设备信息、PRoot 等）
        CoreServiceRegistrar(this).execute()

        // 启动时立即尝试上传主进程崩溃日志，并注册 JobScheduler 兜底重试（仅主进程）。
        if (isHostAppProcess) {
            CrashLogAutoUploader.uploadOnStartup(this, applicationScope)
        }

        val startupFlowManager = if (isHostAppProcess) {
            StartupFlowManager(this, koinGet(IConfigManager::class.java))
        } else {
            null
        }
        val needsDependencyInstall = startupFlowManager?.requiresDependencyInstallation() == true

        // 后台同步服务器配置 + 认证状态维护（仅主进程）
        if (isHostAppProcess) {
            if (needsDependencyInstall) {
                Timber.tag(TAG).i("Skip immediate server config sync: dependency installation required")
            } else {
                ServerConfigSyncTask(this, applicationScope).execute()
            }

            // 调度收藏后台同步任务
            FavoriteSyncWorker.schedule(this)

            // 安装内置包（SDL3 等预编译库）
            BundledPackagesInstallTask(this, applicationScope).execute()
        }

        // 初始化主题
        val themeInitializer = ThemeInitializer(this)
        themeInitializer.initialize()
        themeInitializer.applyNightMode()

        // 统一修复系统栏图标颜色：enableEdgeToEdge() 可能在部分 ROM/机型上覆写为错误模式
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                if (activity is CrashActivity) return
                activity.applyMobileSystemBars()
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity is CrashActivity) return
                activity.applyMobileSystemBars()
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })

        // 启动时后台初始化 PRoot 环境（仅在工作空间配置完成后）
        // 首次启动时，PRoot 环境会在 DependencyInstallActivity 中安装
        // 非首次启动时，在后台检查并更新 PRoot 环境
        // 注意：CrashActivity 运行在 :crash 进程，避免多进程同时解包导致环境损坏
        if (isHostAppProcess) {
            val prootEnvReady = startupFlowManager?.isPRootEnvironmentReady() == true

            // 注意：PRoot Linux 环境是可选功能，不应因为"启动流程完成"而自动触发下载/安装。
            // 仅当检测到 PRoot 环境已就绪（rootfs 已存在）时，才在后台做更新/修复。
            if (prootEnvReady) {
                PRootBootstrap.start(this)
            } else {
                Timber.tag(TAG).i("Skip PRoot bootstrap: environment not ready")
            }
        } else {
            Timber.tag(TAG).i("Skip PRoot bootstrap in process: %s", processName)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        // 关闭 Timber 日志系统
        MobileTimber.shutdown()
    }
}
