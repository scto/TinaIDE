package com.scto.mobileide.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.i18n.Strings

/**
 * 退出时未保存更改确认对话框
 *
 * 当用户使用返回手势且有未保存的文件时显示此对话框
 *
 * @param unsavedCount 未保存文件的数量
 * @param onSaveAllAndExit 点击"全部保存并退出"
 * @param onDiscardAndExit 点击"不保存退出"
 * @param onCancel 点击"取消"
 */
@Composable
fun UnsavedChangesOnExitDialog(
    unsavedCount: Int,
    onSaveAllAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
    onCancel: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onCancel,
        title = { MobileDialogTitleText(stringResource(Strings.unsaved_changes_on_exit_title)) },
        text = {
            MobileDialogContentColumn {
                MobileDialogMessageCard(
                    message = stringResource(Strings.unsaved_changes_on_exit_message, unsavedCount)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MobileDangerOutlinedButton(
                    text = stringResource(Strings.btn_discard_and_exit),
                    onClick = onDiscardAndExit
                )
                MobilePrimaryButton(
                    text = stringResource(Strings.btn_save_all_and_exit),
                    onClick = onSaveAllAndExit
                )
            }
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onCancel
            )
        }
    )
}
