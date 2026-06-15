package com.scto.mobileide.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.i18n.Strings

/**
 * 关闭未保存文件的确认对话框
 *
 * @param fileName 文件名
 * @param onSaveAndClose 点击"保存并关闭"
 * @param onDiscardAndClose 点击"不保存关闭"
 * @param onCancel 点击"取消"
 */
@Composable
fun UnsavedFileDialog(
    fileName: String,
    onSaveAndClose: () -> Unit,
    onDiscardAndClose: () -> Unit,
    onCancel: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onCancel,
        title = { MobileDialogTitleText(stringResource(Strings.unsaved_changes_title)) },
        text = {
            MobileDialogContentColumn {
                MobileDialogMessageCard(
                    message = stringResource(Strings.unsaved_changes_message, fileName)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MobileDangerOutlinedButton(
                    text = stringResource(Strings.btn_dont_save),
                    onClick = onDiscardAndClose
                )
                MobilePrimaryButton(
                    text = stringResource(Strings.btn_save_and_close),
                    onClick = onSaveAndClose
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
