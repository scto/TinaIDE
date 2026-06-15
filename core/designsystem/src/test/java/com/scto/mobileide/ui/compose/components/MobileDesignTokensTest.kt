package com.scto.mobileide.ui.compose.components

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MobileDesignTokensTest {

    @Test
    fun spacingAliases_shouldPointToStableBaseTokens() {
        assertThat(MobileSpacing.iconText).isEqualTo(MobileSpacing.sm)
        assertThat(MobileSpacing.cardPadding).isEqualTo(MobileSpacing.lg)
        assertThat(MobileSpacing.buttonGap).isEqualTo(MobileSpacing.md)
        assertThat(MobileSpacing.pageHorizontal).isEqualTo(MobileSpacing.xl)
    }

    @Test
    fun shapeTokens_shouldKeepExpectedRelativeSizes() {
        assertThat(MobileShapes.ExtraSmallCorner.value).isLessThan(MobileShapes.SmallCorner.value)
        assertThat(MobileShapes.SmallCorner.value).isLessThan(MobileShapes.ButtonCorner.value)
        assertThat(MobileShapes.ButtonCorner).isEqualTo(MobileShapes.TextFieldCorner)
        assertThat(MobileShapes.DialogCorner.value).isGreaterThan(MobileShapes.CardCorner.value)
    }
}
