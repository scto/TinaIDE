package com.scto.mobileide.ui.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * 骨架屏基础组件 - 带闪烁动画的占位块
 */
@Composable
private fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(MobileShapes.CardCorner)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor.copy(alpha = shimmerAlpha),
                        highlightColor.copy(alpha = shimmerAlpha * 0.5f),
                        baseColor.copy(alpha = shimmerAlpha)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    )
}

/**
 * 项目卡片骨架屏
 */
@Composable
fun ProjectCardSkeleton(
    modifier: Modifier = Modifier
) {
    MobileCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MobileSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 项目图标
            SkeletonBox(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(MobileShapes.ButtonCorner)
            )

            Spacer(modifier = Modifier.width(MobileSpacing.lg))

            // 项目信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MobileSpacing.sm)
            ) {
                // 项目名称
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp),
                    shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                )

                // 项目路径
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp),
                    shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                )

                // 标签行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MobileSpacing.sm)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp),
                        shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(50.dp)
                            .height(24.dp),
                        shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                    )
                }
            }
        }
    }
}

/**
 * 插件/包卡片骨架屏
 */
@Composable
fun PluginCardSkeleton(
    modifier: Modifier = Modifier
) {
    MobileCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MobileSpacing.lg),
            verticalAlignment = Alignment.Top
        ) {
            // 插件图标
            SkeletonBox(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(MobileShapes.ButtonCorner)
            )

            Spacer(modifier = Modifier.width(MobileSpacing.lg))

            // 插件信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MobileSpacing.sm)
            ) {
                // 插件名称
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp),
                    shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                )

                // 插件描述（两行）
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                )
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp),
                    shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                )

                Spacer(modifier = Modifier.height(MobileSpacing.xs))

                // 底部信息行（作者、版本、大小）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MobileSpacing.md)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(MobileShapes.ButtonCorner)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MobileSpacing.md))

            // 安装按钮
            SkeletonBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(36.dp),
                shape = RoundedCornerShape(MobileShapes.ButtonCorner)
            )
        }
    }
}
