package com.scto.mobileide.ui.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.scto.mobileide.core.i18n.Strings

private const val MOBILE_DIALOG_WIDTH_FRACTION = 0.94f
private val MobileDialogSectionSpacing = 12.dp

/**
 * MobileIDE 对话框组件库
 *
 * ## 设计规范
 *
 * ### 按钮类型
 * - 确认按钮（需要用户主动操作）：使用 MobilePrimaryButton
 * - 危险操作确认按钮：使用 MobileDangerButton
 * - 取消按钮：使用 MobileTextButton
 * - 仅关闭按钮（信息展示类）：使用 MobileTextButton
 *
 * ### 按钮文本
 * - 确认操作：btn_confirm
 * - 取消操作：btn_cancel
 * - 仅关闭/知道了：btn_ok
 *
 * ## 对话框类型
 * - MobileAlertDialog: 基础对话框
 * - MobileConfirmDialog: 确认对话框
 * - MobileInfoDialog: 信息对话框
 * - MobileErrorDialog: 错误对话框
 * - MobileInputDialog: 输入对话框（受控）
 * - MobileValidatedInputDialog: 带验证的文本输入对话框
 * - MobileLoadingDialog: 加载/进度对话框
 * - MobileSingleChoiceDialog: 单选对话框
 * - MobileSliderDialog: 滑块对话框
 * - MobileActionChoiceDialog: 操作选择对话框
 */

// ==================== 基础对话框 ====================

/**
 * 统一的对话框组件
 */
@Composable
fun MobileAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    modifier: Modifier = Modifier.fillMaxWidth(MOBILE_DIALOG_WIDTH_FRACTION)
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        icon = icon,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        properties = properties,
        modifier = modifier,
        shape = RoundedCornerShape(MobileShapes.DialogCorner),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
@Composable
fun MobileCustomDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(MOBILE_DIALOG_WIDTH_FRACTION),
    properties: DialogProperties = DialogProperties(),
    shape: Shape = RoundedCornerShape(MobileShapes.DialogCorner),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun MobileOverlayPanelSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MobileShapes.DialogCorner),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 8.dp,
    shadowElevation: Dp = 8.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(shadowElevation, shape),
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
        border = border,
        content = content
    )
}
@Composable
fun MobileDialogCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f),
    border: BorderStroke? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(MobileDialogSectionSpacing),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MobileShapes.CardCorner),
        color = color,
        border = border
    ) {
        Column(
            modifier = contentModifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

@Composable
fun MobileDialogContentColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(MobileDialogSectionSpacing),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun MobileDialogTitleText(
    title: String,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun MobileCustomDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingContent: (@Composable (() -> Unit))? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.let { leading ->
            leading()
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MobileDialogTitleText(title)
            subtitle?.takeIf(String::isNotBlank)?.let { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailingContent?.invoke(this)
    }
}

@Composable
fun MobileCustomDialogScaffold(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MobileDialogContentColumn(content = header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            content = content
        )
        footer?.let {
            Spacer(modifier = Modifier.height(MobileDialogSectionSpacing))
            MobileDialogContentColumn(content = it)
        }
    }
}

@Composable
fun MobileDialogActionRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
fun MobilePanelSegmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 32.dp,
    shape: Shape = RoundedCornerShape(MobileShapes.SmallCorner),
    color: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = shape,
        color = color
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = minHeight)
                .padding(contentPadding),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Composable
private fun MobileDialogMessageText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun MobileDialogMessageCard(
    message: String,
    color: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    border: BorderStroke? = null
) {
    MobileDialogCard(
        color = color,
        border = border,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
fun MobileDialogSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MobileShapes.SmallCorner),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
    selectedBorder: BorderStroke? = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    ),
    unselectedBorder: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = if (selected) {
            selectedColor
        } else {
            unselectedColor
        },
        shape = shape,
        border = if (selected) {
            selectedBorder
        } else {
            unselectedBorder
        }
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

// ==================== 确认/信息对话框 ====================

/**
 * 确认对话框
 *
 * 用于需要用户确认的操作
 *
 * @param title 标题
 * @param message 消息内容
 * @param confirmText 确认按钮文本（默认"确认"）
 * @param dismissText 取消按钮文本（默认"取消"）
 * @param onConfirm 确认回调
 * @param onDismiss 取消回调
 * @param isDanger 是否为危险操作（使用红色按钮）
 * @param icon 图标（可选）
 */
@Composable
fun MobileConfirmDialog(
    title: String,
    message: String,
    confirmText: String? = null,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDanger: Boolean = false,
    icon: @Composable (() -> Unit)? = null
) {
    val actualConfirmText = confirmText ?: stringResource(Strings.btn_confirm)
    val actualDismissText = dismissText ?: stringResource(Strings.btn_cancel)

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileDialogMessageCard(
                message = message,
                color = if (isDanger) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.36f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
                },
                textColor = if (isDanger) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        icon = icon,
        confirmButton = {
            if (isDanger) {
                MobileDangerButton(
                    text = actualConfirmText,
                    onClick = onConfirm
                )
            } else {
                MobilePrimaryButton(
                    text = actualConfirmText,
                    onClick = onConfirm
                )
            }
        },
        dismissButton = {
            MobileTextButton(
                text = actualDismissText,
                onClick = onDismiss
            )
        }
    )
}

/**
 * 信息对话框（只有一个关闭按钮）
 *
 * 用于展示信息，用户只需知晓
 *
 * @param title 标题
 * @param message 消息内容
 * @param confirmText 关闭按钮文本（默认"知道了"）
 * @param onDismiss 关闭回调
 * @param icon 图标（可选）
 */
@Composable
fun MobileInfoDialog(
    title: String,
    message: String,
    confirmText: String? = null,
    onDismiss: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    val actualConfirmText = confirmText ?: stringResource(Strings.btn_ok)

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = { MobileDialogMessageCard(message = message) },
        icon = icon,
        confirmButton = {
            MobileTextButton(
                text = actualConfirmText,
                onClick = onDismiss
            )
        }
    )
}

/**
 * 错误对话框
 *
 * 用于展示错误信息，可选重试功能
 *
 * @param title 标题
 * @param message 错误消息
 * @param onRetry 重试回调（可选，提供时显示重试按钮）
 * @param onDismiss 关闭回调
 */
@Composable
fun MobileErrorDialog(
    title: String,
    message: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileDialogMessageCard(
                message = message,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.36f),
                textColor = MaterialTheme.colorScheme.onErrorContainer
            )
        },
        confirmButton = {
            if (onRetry != null) {
                MobilePrimaryButton(
                    text = stringResource(Strings.btn_retry),
                    onClick = onRetry
                )
            } else {
                MobileTextButton(
                    text = stringResource(Strings.btn_ok),
                    onClick = onDismiss
                )
            }
        },
        dismissButton = if (onRetry != null) {
            {
                MobileTextButton(
                    text = stringResource(Strings.btn_cancel),
                    onClick = onDismiss
                )
            }
        } else null
    )
}

// ==================== 输入对话框 ====================

/**
 * 输入对话框（受控模式）
 *
 * 状态由外部管理，适合需要实时验证的场景
 *
 * @param title 标题
 * @param value 当前输入值
 * @param onValueChange 值变化回调
 * @param confirmText 确认按钮文本（默认"确认"）
 * @param dismissText 取消按钮文本（默认"取消"）
 * @param onConfirm 确认回调
 * @param onDismiss 取消回调
 * @param label 输入框标签
 * @param placeholder 占位符
 * @param isError 是否显示错误状态
 * @param errorText 错误文本
 * @param singleLine 是否单行
 */
@Composable
fun MobileInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    confirmText: String? = null,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = true
) {
    val actualConfirmText = confirmText ?: stringResource(Strings.btn_confirm)
    val actualDismissText = dismissText ?: stringResource(Strings.btn_cancel)

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileTextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                placeholder = placeholder,
                isError = isError,
                errorText = errorText,
                singleLine = singleLine
            )
        },
        confirmButton = {
            MobilePrimaryButton(
                text = actualConfirmText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            MobileTextButton(
                text = actualDismissText,
                onClick = onDismiss
            )
        }
    )
}

/**
 * 带验证的文本输入对话框
 *
 * 支持动态验证和提示
 *
 * @param title 对话框标题
 * @param label 输入框标签
 * @param initialValue 初始值
 * @param placeholder 占位符文本
 * @param validator 验证函数（@Composable），返回 null 表示验证通过，返回错误消息表示验证失败
 * @param hint 动态提示函数（@Composable，可选），根据当前输入值返回提示文本，在没有错误时显示
 * @param keyboardOptions 键盘选项（可选）
 * @param allowEmpty 是否允许空值（默认 false）
 * @param onConfirm 确认回调
 * @param onDismiss 取消回调
 */
@Composable
fun MobileValidatedInputDialog(
    title: String,
    label: String,
    initialValue: String,
    placeholder: String = "",
    validator: @Composable (String) -> String? = { null },
    hint: (@Composable (String) -> String?)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    allowEmpty: Boolean = false,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    // 在 Composable 上下文中计算验证结果
    val errorMessage = validator(text)

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileTextField(
                value = text,
                onValueChange = { text = it },
                label = label,
                placeholder = placeholder,
                dynamicHint = if (errorMessage == null) hint else null,
                isError = errorMessage != null,
                errorText = errorMessage,
                keyboardOptions = keyboardOptions,
                singleLine = true
            )
        },
        confirmButton = {
            MobilePrimaryButton(
                text = stringResource(Strings.btn_confirm),
                onClick = { onConfirm(text) },
                enabled = errorMessage == null && (allowEmpty || text.isNotBlank())
            )
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}

// ==================== 进度/加载对话框 ====================

/**
 * 加载/进度对话框（不可取消）
 *
 * 用于显示加载状态或进度信息
 *
 * @param title 标题（可选）
 * @param message 消息（可选，默认"请稍候..."）
 * @param progress 可选进度，传入后显示确定型进度条
 */
@Composable
fun MobileLoadingDialog(
    title: String? = null,
    message: String? = null,
    progress: Float? = null
) {
    val actualMessage = message ?: stringResource(Strings.progress_please_wait)
    val actualProgress = progress?.coerceIn(0f, 1f)

    MobileAlertDialog(
        onDismissRequest = { /* 不允许关闭 */ },
        title = title?.let { { MobileDialogTitleText(it) } },
        text = {
            MobileDialogCard {
                if (actualProgress == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        MobileDialogMessageText(actualMessage)
                    }
                } else {
                    MobileDialogContentColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MobileDialogMessageText(actualMessage)
                        LinearProgressIndicator(
                            progress = { actualProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = { /* 无按钮 */ }
    )
}

// ==================== 选择对话框 ====================

/**
 * 单选对话框
 *
 * 点击选项后立即选中并关闭
 *
 * @param title 标题
 * @param options 选项列表，每项为 (value, label) 对
 * @param selectedValue 当前选中的值
 * @param onSelected 选中回调
 * @param onDismiss 取消回调
 */
@Composable
fun MobileSingleChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileDialogContentColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { (value, label) ->
                    MobileDialogSelectableCard(
                        selected = selectedValue == value,
                        onClick = { onSelected(value) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = selectedValue == value,
                                onClick = { onSelected(value) }
                            )
                            Text(text = label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}

/**
 * 滑块对话框
 *
 * 用于选择数值范围内的值
 *
 * @param title 标题
 * @param value 当前值
 * @param valueRange 值范围
 * @param steps 步数
 * @param valueLabel 值标签格式化函数
 * @param onValueSelected 确认回调
 * @param onDismiss 取消回调
 */
@Composable
fun MobileSliderDialog(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: (Float) -> String,
    onValueSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var currentValue by remember { mutableFloatStateOf(value) }

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileDialogCard {
                MobileDialogContentColumn {
                    Text(
                        text = valueLabel(currentValue),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Slider(
                        value = currentValue,
                        onValueChange = { currentValue = it },
                        valueRange = valueRange,
                        steps = steps
                    )
                }
            }
        },
        confirmButton = {
            MobilePrimaryButton(
                text = stringResource(Strings.btn_confirm),
                onClick = { onValueSelected(currentValue) }
            )
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}

/**
 * 操作选择对话框
 *
 * 用于显示多个操作选项供用户选择
 *
 * @param title 对话框标题
 * @param message 说明文字（可选）
 * @param actions 操作列表，每个操作包含文本和回调
 * @param onDismiss 关闭回调
 */
@Composable
fun MobileActionChoiceDialog(
    title: String,
    message: String? = null,
    actions: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(title) },
        text = {
            MobileDialogContentColumn {
                if (message != null) {
                    MobileDialogMessageCard(message = message)
                }
                MobileDialogCard(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions.forEach { (text, action) ->
                        MobileTextButton(
                            text = text,
                            onClick = action,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}
