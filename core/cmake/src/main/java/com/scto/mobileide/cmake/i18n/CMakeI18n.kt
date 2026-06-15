package com.scto.mobileide.cmake.i18n

import com.scto.mobileide.core.i18n.str

internal object CMakeI18n {
    fun strOrFallback(resId: Int, fallback: String, vararg formatArgs: Any?): String {
        return runCatching { resId.str(*formatArgs) }.getOrDefault(fallback)
    }
}

