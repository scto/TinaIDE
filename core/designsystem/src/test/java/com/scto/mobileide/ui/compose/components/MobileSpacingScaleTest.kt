package com.scto.mobileide.ui.compose.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MobileSpacingScaleTest {

    @Test
    fun baseSpacingTokens_shouldStayInAscendingOrder() {
        val values = listOf(
            MobileSpacing.xxs.value,
            MobileSpacing.xs.value,
            MobileSpacing.sm.value,
            MobileSpacing.md.value,
            MobileSpacing.mdLg.value,
            MobileSpacing.lg.value,
            MobileSpacing.xl.value,
            MobileSpacing.xxl.value,
            MobileSpacing.xxxl.value,
            MobileSpacing.huge.value
        )

        assertThat(values).isEqualTo(values.sorted())
        assertThat(values.toSet()).hasSize(values.size)
    }

    @Test
    fun semanticSpacingTokens_shouldMapToExpectedLayoutDensity() {
        assertThat(MobileSpacing.listItemVertical).isEqualTo(MobileSpacing.xxs)
        assertThat(MobileSpacing.listItemHorizontal).isEqualTo(MobileSpacing.xs)
        assertThat(MobileSpacing.toolbarPadding).isEqualTo(MobileSpacing.md)
        assertThat(MobileSpacing.statusBarPadding).isEqualTo(MobileSpacing.lg)
        assertThat(MobileSpacing.dialogPadding).isEqualTo(MobileSpacing.xxxl)
    }
}
