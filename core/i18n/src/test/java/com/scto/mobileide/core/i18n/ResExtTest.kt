package com.scto.mobileide.core.i18n

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResExtTest {

    @Test
    fun strOr_shouldResolveStringWithProvidedContext() {
        val context = fakeStringContext()

        val appName = Strings.app_name.strOr(context)

        assertThat(appName).isEqualTo("MobileIDE")
    }

    @Test
    fun strOr_shouldReplaceNullFormatArgsWithBlankStrings() {
        val context = fakeStringContext()

        val text = Strings.log_export_app_name.strOr(context, null)

        assertThat(text).startsWith("应用名称:")
        assertThat(text).doesNotContain("null")
    }

    private fun fakeStringContext(): Context {
        return mockk {
            every { getString(Strings.app_name) } returns "MobileIDE"
            every { getString(Strings.log_export_app_name, *anyVararg()) } answers {
                "应用名称: %1\$s".format(*args.drop(1).toTypedArray())
            }
        }
    }
}
