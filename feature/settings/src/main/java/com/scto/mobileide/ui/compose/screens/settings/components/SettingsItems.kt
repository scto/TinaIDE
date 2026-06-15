package com.scto.mobileide.ui.compose.screens.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scto.mobileide.ui.compose.components.MobileCard
import com.scto.mobileide.ui.compose.components.MobileDivider
import com.scto.mobileide.ui.compose.components.MobileSpacing
import com.scto.mobileide.ui.compose.icons.rememberMobilePainter

/**
 * 设置项组件中的尺寸常量
 */
private object SettingsSizes {
    /** 菜单项图标背景尺寸 */
    val iconBackgroundSize = 44.dp

    /** 菜单项图标尺寸 */
    val iconSize = 24.dp

    /** 箭头图标尺寸 */
    val arrowSize = 24.dp

    /** 分隔线左侧缩进（图标背景 + 间距） */
    val dividerStartPadding = 76.dp

    /** 分隔线透明度 */
    const val DIVIDER_ALPHA = 0.5f

    /** 箭头透明度 */
    const val ARROW_ALPHA = 0.5f

    /** 值文本最大宽度 */
    val valueMaxWidth = 120.dp

    /** 展示项值文本最大宽度 */
    val displayValueMaxWidth = 160.dp
}
/**
 * 设置分组卡片容器
 *
 * 使用 MobileCard 作为底层实现，保持设计系统一致性
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MobileCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.sm),
        elevation = 0.dp
    ) {
        content()
    }
}
/**
 * 带图标的设置菜单项（用于主设置页面）
 */
@Composable
fun SettingsMenuItemWithIcon(
    @DrawableRes iconRes: Int? = null,
    imageVector: ImageVector? = null,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    showArrow: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.mdLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圆形图标背景
            Box(
                modifier = Modifier
                    .size(SettingsSizes.iconBackgroundSize)
                    .clip(CircleShape)
                    .background(iconBackgroundColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageVector != null -> {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier.size(SettingsSizes.iconSize),
                            tint = iconBackgroundColor
                        )
                    }
                    iconRes != null -> {
                        Image(
                            painter = rememberMobilePainter(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(SettingsSizes.iconSize)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(MobileSpacing.xl))

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(MobileSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // 箭头（可选）
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SettingsSizes.ARROW_ALPHA),
                    modifier = Modifier.size(SettingsSizes.arrowSize)
                )
            }
        }

        if (showDivider) {
            MobileDivider(
                modifier = Modifier.padding(start = SettingsSizes.dividerStartPadding),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = SettingsSizes.DIVIDER_ALPHA)
            )
        }
    }
}
/**
 * 可点击的设置项
 */
@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.mdLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(MobileSpacing.xxs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = SettingsSizes.valueMaxWidth)
                )
                Spacer(modifier = Modifier.width(MobileSpacing.md))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SettingsSizes.ARROW_ALPHA),
                modifier = Modifier.size(SettingsSizes.arrowSize)
            )
        }

        if (showDivider) {
            MobileDivider(
                modifier = Modifier.padding(start = MobileSpacing.xl),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = SettingsSizes.DIVIDER_ALPHA)
            )
        }
    }
}
/**
 * 纯展示设置项（不可点击，无箭头）
 */
@Composable
fun SettingsDisplayItem(
    title: String,
    value: String,
    showDivider: Boolean = true,
    valueMaxLines: Int = 1,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.mdLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = valueMaxLines.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = SettingsSizes.displayValueMaxWidth)
            )
        }

        if (showDivider) {
            MobileDivider(
                modifier = Modifier.padding(start = MobileSpacing.xl),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = SettingsSizes.DIVIDER_ALPHA)
            )
        }
    }
}
/**
 * 开关设置项
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.mdLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = SettingsSizes.ARROW_ALPHA)
                    }
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(MobileSpacing.xxs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SettingsSizes.ARROW_ALPHA)
                        }
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }

        if (showDivider) {
            MobileDivider(
                modifier = Modifier.padding(start = MobileSpacing.xl),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = SettingsSizes.DIVIDER_ALPHA)
            )
        }
    }
}

/**
 * 分类标题
 */
@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = MobileSpacing.xl,
            top = MobileSpacing.xxxl,
            bottom = MobileSpacing.md,
            end = MobileSpacing.xl
        )
    )
}
