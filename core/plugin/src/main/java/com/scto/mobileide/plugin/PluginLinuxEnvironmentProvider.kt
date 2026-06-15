package com.scto.mobileide.plugin

import android.content.Context
import com.scto.mobileide.core.linux.LinuxEnvironment
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.core.linux.UnavailableLinuxEnvironment
import com.scto.mobileide.core.proot.PRootEnvironment

/**
 * 由插件 capability 驱动的 Linux 环境提供器。
 */
class PluginLinuxEnvironmentProvider(
    private val context: Context,
    private val pluginManager: PluginManager,
) : LinuxEnvironmentProvider {

    private val prootEnvironment: PRootEnvironment by lazy {
        PRootEnvironment(context.applicationContext)
    }

    override fun get(): LinuxEnvironment {
        return if (pluginManager.hasEnabledCapability(PluginCapabilities.LINUX_ENVIRONMENT)) {
            prootEnvironment
        } else {
            UnavailableLinuxEnvironment
        }
    }
}
