package com.scto.mobileide.startup

import android.content.Context
import com.scto.mobileide.BuildConfig
import com.scto.mobileide.core.config.AppPreferences
import com.scto.mobileide.core.config.IConfigManager
import com.scto.mobileide.core.config.KeyboardShortcutManager
import com.scto.mobileide.core.config.MTFileProviderManager
import com.scto.mobileide.core.config.Prefs
import com.scto.mobileide.core.lsp.RemoteLspConfigManager
import com.scto.mobileide.core.network.server.MobileServerEnvironment
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import timber.log.Timber

/**
 * 核心初始化器
 *
 * 在 Application.onCreate() 中执行副作用初始化（Prefs、设备信息、PRoot 等）。
 * 服务实例的创建和注册已由 Koin DI 模块负责。
 */
class CoreServiceRegistrar(private val context: Context) : KoinComponent {

    companion object {
        private const val TAG = "CoreServiceRegistrar"
    }

    fun execute() {
        val appContext = context.applicationContext
        val configManager: IConfigManager = get()

        // === 副作用初始化（不创建服务实例，只做配置/注册回调） ===

        Prefs.initialize(appContext, configManager)
        com.scto.mobileide.core.device.DeviceInfoProvider.initialize(BuildConfig.VERSION_NAME)
        MobileServerEnvironment.initialize(BuildConfig.SERVER_CONFIG_HMAC_SECRET)
        KeyboardShortcutManager.initialize(AppPreferences.get(appContext))
        RemoteLspConfigManager.install(configManager)

        // 注入 GestureTrace 启用判断逻辑
        com.scto.mobileide.core.logging.GestureTrace.enabledProvider = {
            Prefs.developerOptionsEnabled && Prefs.devDiagnosticsEnabled && Prefs.devGestureTraceEnabled
        }

        // 初始化 MT 文件提供器（根据配置启用/禁用）
        try {
            MTFileProviderManager.initialize(appContext, configManager)
            Timber.tag(TAG).i("MT File Provider initialized")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize MT File Provider")
        }
    }
}
