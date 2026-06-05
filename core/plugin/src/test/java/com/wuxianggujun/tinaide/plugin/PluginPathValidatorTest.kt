package com.wuxianggujun.tinaide.plugin

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class PluginPathValidatorTest {

    @Test
    fun `isSafePluginRelativePath should accept normalized relative asset paths`() {
        assertThat(isSafePluginRelativePath("main.lua")).isTrue()
        assertThat(isSafePluginRelativePath("scripts/main.lua")).isTrue()
        assertThat(isSafePluginRelativePath("assets/icons/file.svg")).isTrue()
    }

    @Test
    fun `isSafePluginRelativePath should reject absolute and parent traversal paths`() {
        listOf(
            "",
            " ",
            " main.lua",
            "main.lua ",
            "/tmp/main.lua",
            "\\\\server\\share\\main.lua",
            "C:/tmp/main.lua",
            "C:\\tmp\\main.lua",
            "C:main.lua",
            "../main.lua",
            "scripts/../main.lua",
            "scripts/..",
            "./main.lua",
            "scripts//main.lua",
        ).forEach { path ->
            assertWithMessage("path=$path")
                .that(isSafePluginRelativePath(path))
                .isFalse()
        }
    }
}
