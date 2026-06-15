package com.scto.mobileide.core.network.server

import android.content.Context
import android.os.Build
import com.scto.mobileide.core.device.DeviceInfo
import com.scto.mobileide.core.device.DeviceInfoProvider
import com.scto.mobileide.core.network.OkHttpClientProvider
import java.util.UUID
import okhttp3.OkHttpClient

/**
 * MobileServer 匿名配置管理。
 *
 * 开源版客户端不再维护账号登录态，这里只保存服务端地址、设备标识和匿名 API 客户端。
 */
class MobileServerConfig private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "mobile_server_config"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_DEVICE_ID = "device_id"

        private const val DEFAULT_SERVER_URL = "https://mobileide.Thomas Schmid.com"

        const val URL_SERVICE_AGREEMENT = "https://mobileide.Thomas Schmid.com/legal/terms"
        const val URL_PRIVACY_POLICY = "https://mobileide.Thomas Schmid.com/legal/privacy"
        const val URL_HELP = "https://mobileide.Thomas Schmid.com/help"

        @Volatile
        private var instance: MobileServerConfig? = null

        fun getInstance(context: Context): MobileServerConfig {
            return instance ?: synchronized(this) {
                instance ?: MobileServerConfig(context.applicationContext).also { instance = it }
            }
        }

        fun getBaseUrl(): String = DEFAULT_SERVER_URL
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val apiClient: OkHttpClient by lazy {
        MobileServerHttpClientFactory.anonymous(OkHttpClientProvider.default)
    }

    suspend fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: getDefaultServerUrl()
    }

    suspend fun setServerUrl(url: String?) {
        prefs.edit().apply {
            if (url.isNullOrBlank()) {
                remove(KEY_SERVER_URL)
            } else {
                putString(KEY_SERVER_URL, url.trimEnd('/'))
            }
        }.apply()
        MobileServerApi.resetInstance()
    }

    fun getDefaultServerUrl(): String = DEFAULT_SERVER_URL

    suspend fun getDeviceId(): String {
        prefs.getString(KEY_DEVICE_ID, null)?.takeIf { it.isNotBlank() }?.let { return it }
        val newId = generateDeviceId()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }

    private fun generateDeviceId(): String {
        val deviceInfo = buildString {
            append(Build.BRAND)
            append(Build.MODEL)
            append(Build.DEVICE)
            append(Build.BOARD)
            append(Build.HARDWARE)
            append(System.currentTimeMillis())
        }
        return UUID.nameUUIDFromBytes(deviceInfo.toByteArray()).toString()
    }

    suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfoProvider.getDeviceInfo(context)
    }

    suspend fun getApi(): MobileServerApi {
        return MobileServerApi.getInstance(getServerUrl(), apiClient)
    }

    suspend fun checkServerConnection(): Boolean {
        return try {
            getApi().healthCheck().isSuccess
        } catch (_: Exception) {
            false
        }
    }
}
