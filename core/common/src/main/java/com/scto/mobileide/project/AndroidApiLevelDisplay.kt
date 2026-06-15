package com.scto.mobileide.project

import android.content.Context
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.i18n.strOr

/**
 * Android API Level 的本地化显示扩展。
 */
fun AndroidApiLevel.getDisplayName(context: Context): String {
    return Strings.ndk_api_level_display_name.strOr(context, level, codename)
}
