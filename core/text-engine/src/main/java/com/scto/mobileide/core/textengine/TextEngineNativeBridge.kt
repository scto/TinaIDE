package com.scto.mobileide.core.textengine

internal object TextEngineNativeBridge {
    private const val LIB_NAME = "mobile_text_engine"

    private val available: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        runCatching {
            System.loadLibrary(LIB_NAME)
            true
        }.getOrDefault(false)
    }

    fun isAvailable(): Boolean = available
}
